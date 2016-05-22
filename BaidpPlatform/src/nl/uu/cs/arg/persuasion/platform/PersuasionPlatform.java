package nl.uu.cs.arg.persuasion.platform;

import java.util.*;

import nl.uu.cs.arg.persuasion.model.PersuasionAgent;
import nl.uu.cs.arg.persuasion.model.PersuasionParticipant;
import nl.uu.cs.arg.persuasion.model.dialogue.*;
import nl.uu.cs.arg.persuasion.model.dialogue.locutions.ClaimLocution;
import nl.uu.cs.arg.persuasion.model.dialogue.protocol.PersuasionProtocolException;
import nl.uu.cs.arg.persuasion.model.dialogue.protocol.PersuasionRule;
import nl.uu.cs.arg.persuasion.model.dialogue.protocol.PersuasionTerminationMessage;
import nl.uu.cs.arg.persuasion.model.dialogue.protocol.PersuasionTerminationRule;

import nl.uu.cs.arg.platform.ParticipatingAgent;
import nl.uu.cs.arg.shared.dialogue.Move;
import nl.uu.cs.arg.shared.dialogue.locutions.Locution;
import org.aspic.inference.Constant;
import org.aspic.inference.ReasonerException;
import org.aspic.inference.parser.ParseException;

public class PersuasionPlatform implements Runnable {

    private boolean initialized = false;

    private boolean paused;

    private boolean stopAfterEachStep = false;

    private List<PersuasionPlatformListener> listeners;

    private final PersuasionSettings settings;

    private PersuasionDialogue dialogue;

    private List<PersuasionMove<? extends Locution>> allMoves;

    private List<PersuasionParticipatingAgent> agents;

    private List<PersuasionParticipatingAgent> opponents;

    private List<PersuasionParticipatingAgent> proponents;

    private Iterator<PersuasionParticipatingAgent> opponentIterator;

    private Iterator<PersuasionParticipatingAgent> proponentIterator;

    private PersuasionParticipatingAgent currentToMove;

    private boolean startPaused = true;

    private int skipsInRound;

    public PersuasionPlatform(PersuasionSettings settings) {
        // Define settings
        this.settings = settings;

        // Create list of listeners
        this.listeners = new ArrayList<PersuasionPlatformListener>();

        // Create list of agents
        this.agents = new ArrayList<PersuasionParticipatingAgent>();

        // Create lists of proponents and opponents
        this.opponents = new ArrayList<PersuasionParticipatingAgent>();
        this.proponents = new ArrayList<PersuasionParticipatingAgent>();
    }

    public void init(Constant topic, List<PersuasionAgent> agents) {
        // Create the persuasion dialogue, set the topic
        this.dialogue = new PersuasionDialogue(topic);

        // Create list for every move
        this.allMoves = new ArrayList<PersuasionMove<? extends Locution>>();

        // Nobody has its turn
        this.currentToMove = null;

        // Reset the move counter
        PersuasionMove.resetMoveCounter();

        // For each agent, create a participant data structure and initialize it
        int participantCount = 0;
        this.agents = new ArrayList<PersuasionParticipatingAgent>();
        for (PersuasionAgent agent : agents) {
            // Create coupling
            PersuasionParticipatingAgent pa = PersuasionParticipatingAgent.createParticipant(agent, participantCount++);
            this.agents.add(pa);

            // Initialize agent
            agent.initialize(pa.getParticipant());
        }

        // Indicate that the dialogue has been started
        this.broadcastMessage(new PersuasionDialogueStartedMessage(topic));

        // Indicate that the platform is initialized
        this.initialized = true;
    }

    public void run() {
        // Have we initialized yet?
        if (!initialized) {
            this.broadcastException(new PersuasionPlatformException("The platform was started before it was initialized.", true));
            return;
        }

        // Determine if we should pause
        this.paused = startPaused;

        // As long as the dialogue is ongoing and we are not asked to pause, play a full game round
        while (dialogue.getState() != PersuasionDialogueState.Terminated) {
            if (this.paused) {
                // We are going to pause
                this.broadcastMessage(new PersuasionPlatformStateMessage(false));
            }
            synchronized (this) {
                while (this.paused) {
                    try {
                        this.wait();

                        // We are going to run again if wait() succeeded (on notify())
                        this.broadcastMessage(new PersuasionPlatformStateMessage(true));
                    } catch (Exception e) {
                    }
                }
            }

            // Play a round
            this.playDialogueRound();

            if (this.stopAfterEachStep) {
                this.paused = true;
            }
        }
    }

    public void setStartMode(boolean startPaused) {
        this.startPaused = startPaused;
    }

    public void requestStart() {
        this.paused = false;
        this.notify();
    }

    public void requestPause() {
        this.paused = true;
    }

    public boolean isPaused() {
        return this.paused;
    }

    public void setSingleStepping(boolean stopAfterEachStep) {
        this.stopAfterEachStep = stopAfterEachStep;
    }

    private void playDialogueRound() {
        switch (this.dialogue.getState()) {
            case Unopened:
                // Unopened; only switch to the opening state
                this.setDialogueState(PersuasionDialogueState.Joining);
                break;
            case Joining:
                this.joinAllAgents();
                break;
            case Active:
                // Deliberating; play a normal game round
                this.playPersuasionRound();
                break;
            case Terminating:
                // Terminating: determine the dialogue outcome
                this.determineOutcome();
                break;
            case Terminated:
                // Terminated; the dialogue will be stopped now
                break;
        }
    }

    private void joinAllAgents() {
        for (PersuasionParticipatingAgent agent : this.agents) {
            agent.getAgent().join(this.dialogue);

            // Check if the agent is a proponent or opponent of the dialogue topic
            try {
                if (agent.getAgent().isProponent(dialogue)) {
                    this.proponents.add(agent);
                } else {
                    this.opponents.add(agent);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            } catch (ReasonerException e) {
                e.printStackTrace();
            }
        }

        // Set the iterators
        this.proponentIterator = this.proponents.iterator();
        this.opponentIterator = this.opponents.iterator();

        this.setDialogueState(PersuasionDialogueState.Active);
    }

    private void setDialogueState(PersuasionDialogueState newState) {
        this.broadcastMessage(new PersuasionDialogueStateChangeMessage(newState));
        this.dialogue.setState(newState);
    }

    private PersuasionParticipatingAgent advanceProponentIterator() {
        PersuasionParticipatingAgent proponent = this.proponentIterator.next();
        if (!this.proponentIterator.hasNext()) {
            this.proponentIterator = this.proponents.iterator();
        }
        return proponent;
    }

    private PersuasionParticipatingAgent advanceOpponentIterator() {
        PersuasionParticipatingAgent opponent = this.opponentIterator.next();
        if (!this.opponentIterator.hasNext()) {
            this.opponentIterator = this.opponents.iterator();
        }
        return opponent;
    }

    private PersuasionParticipatingAgent nextToMove() {
        PersuasionParticipatingAgent agent = null;

        if (this.agents.size() == 0) {
            agent = null;
        } else {
            // Nobody moved, return this first proponent
            if (this.currentToMove == null) {
                agent = this.advanceProponentIterator();
            } else {
                try {
                    if (this.currentToMove.getAgent().isProponent(this.dialogue)) {
                        // Check for the next opponent to move
                        agent =  this.advanceOpponentIterator();
                    } else {
                        // Check for the next proponent to move
                        agent = this.advanceProponentIterator();
                    }
                } catch (ParseException e) {
                    e.printStackTrace();

                    agent = null;
                } catch (ReasonerException e) {
                    e.printStackTrace();

                    agent = null;
                }
            }
        }

        this.currentToMove = agent;

        return this.currentToMove;
    }

    private void playPersuasionRound() {
        // Give the next agent to move a single turn to make new moves
        PersuasionParticipatingAgent toMove = this.nextToMove();
        List<PersuasionMove<? extends Locution>> moves = toMove.getAgent().makeMoves();

        if (moves != null && moves.size() > 0) {
            // Someone has moved
            this.skipsInRound = 0;

            // Check validity of the moves
            for (Iterator<PersuasionMove<? extends Locution>> iter = moves.iterator(); iter.hasNext(); ) {
                PersuasionMove<? extends Locution> check = iter.next();

                List<PersuasionProtocolException> reasons = this.checkMoveValidity(check);
                if (reasons != null && reasons.size() > 0) {

                    // Some moves were invalid: send the reasons to the agent and notify any platform listeners
                    for (PersuasionProtocolException reason : reasons) {
                        toMove.getAgent().onDialogueException(reason);
                    }
                    this.broadcastException(new PersuasionPlatformException(toMove.toString() + " made (some) invalid moves: " + reasons.toString(), false));

                    // Since it was invalid, remove them from the actual played moves by this agent this turn
                    iter.remove();
                }
            }

            // Update the dialogue with the forwarded moves
            this.allMoves.addAll(moves);
            try {
                this.dialogue.update(moves);
            } catch (PersuasionDialogueException e) {
                broadcastException(new PersuasionPlatformException(e.getMessage(), false));
            }

            // Broadcast the moves made
            this.broadcastDialogeMoves(moves);
        } else {
            ++this.skipsInRound;

            this.broadcastMessage(new PersuasionSkipMoveMessage(toMove.getParticipant()));
        }

        // Determine if the dialogue should end; if so, broadcast message and advance the dialogue to the terminating state
        PersuasionTerminationMessage terminationCause = this.shouldTerminate();
        if (terminationCause != null) {
            // Broadcast entering of termination state of the dialogue
            this.broadcastMessage(terminationCause);
            this.setDialogueState(PersuasionDialogueState.Terminating);
        } else if (terminationCause == null && moves == null) {
            this.broadcastException(new PersuasionPlatformException("", false));
        }
    }

    private List<PersuasionProtocolException> checkMoveValidity(PersuasionMove<? extends Locution> check) {
        // Ask all protocol rules if there is a reason why this move violates the protocol
        List<PersuasionProtocolException> reasons = new ArrayList<PersuasionProtocolException>();
        for (PersuasionRule rule : this.settings.getPersuasionRules()) {
            PersuasionProtocolException reason = rule.evaluateMove(dialogue, check);
            if (reason != null) {
                reasons.add(reason);
            }
        }
        return reasons;
    }

    private PersuasionTerminationMessage shouldTerminate() {
        // Look to the termination rules until there is one that says we should terminate
        for (PersuasionTerminationRule rule : this.settings.getTerminationRules()) {
            ArrayList<PersuasionAgent> agents = new ArrayList<PersuasionAgent>();
            for (PersuasionParticipatingAgent agent : this.agents) {
                agents.add(agent.getAgent());
            }

            PersuasionTerminationMessage cause = rule.shouldTerminate(this.dialogue, agents, this.skipsInRound);
            if (cause != null) {
                return cause;
            }
        }
        return null;
    }

    private void determineOutcome() {
        // Get the dialogue outcome and broadcast this message
        List<PersuasionParticipant> participants = new ArrayList<PersuasionParticipant>();
        for (PersuasionParticipatingAgent participant : this.agents) {
            participants.add(participant.getParticipant());
        }
        this.broadcastMessage(new PersuasionOutcomeMessage(settings.getOutcomeSelectionRule().determineWinners(this.dialogue, participants)));

        // Set the dialogue state to terminated
        this.setDialogueState(PersuasionDialogueState.Terminated);
    }

    public void addListener(PersuasionPlatformListener listener) {
        this.listeners.add(listener);
    }

    private void broadcastDialogeMove(PersuasionMove<? extends Locution> playedMove) {
        // Note that Arrays.asList() doesn't work here because of the <? extends Locution>
        ArrayList<PersuasionMove<? extends Locution>> list = new ArrayList<PersuasionMove<? extends Locution>>();
        list.add(playedMove);
        this.broadcastDialogeMoves(list);
    }

    private void broadcastDialogeMoves(List<PersuasionMove<? extends Locution>> playedMoves) {

        // Broadcast to agents
        List<PersuasionParticipatingAgent> sendToAgents = this.agents;
        for (PersuasionParticipatingAgent sendToAgent : sendToAgents) {
            sendToAgent.getAgent().onNewMovesReceived(playedMoves);
        }

        // Broadcast to platform listeners
        for (PersuasionPlatformListener listener : listeners) {
            synchronized (listener) {
                listener.onMoves(playedMoves);
            }
        }

    }

    private void broadcastMessage(PersuasionDialogueMessage message) {
        broadcastMessages(Arrays.asList(message));
    }

    private void broadcastMessages(List<PersuasionDialogueMessage> messages) {

        // Broadcast to agents
        List<PersuasionParticipatingAgent> sendToAgents = this.agents;
        for (PersuasionParticipatingAgent sendToAgent : sendToAgents) {
            sendToAgent.getAgent().onDialogueMessagesReceived(messages);
        }

        // Broadcast to platform listeners
        for (PersuasionPlatformListener listener : listeners) {
            synchronized (listener) {
                listener.onMessagesReceived(messages);
            }
        }

    }

    private boolean broadcastException(PersuasionPlatformException e) {

        // If there are no listeners, return false
        if (listeners.size() == 0) {
            return false;
        }

        // Send the error message to each listener
        for (PersuasionPlatformListener listener : listeners) {
            synchronized (listener) {
                listener.onExceptionThrown(e);
            }
        }
        return true;

    }
}