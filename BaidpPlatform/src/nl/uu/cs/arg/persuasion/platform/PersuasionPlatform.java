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

public class PersuasionPlatform implements Runnable {

    private boolean initialized = false;

    private boolean paused;

    private boolean stopAfterEachStep = false;

    private List<PersuasionPlatformListener> listeners;

    private final PersuasionSettings settings;

    private PersuasionDialogue dialogue;

    private List<PersuasionMove<? extends Locution>> allMoves;

    private List<PersuasionParticipatingAgent> agents;

    private PersuasionParticipatingAgent lastToMove;

    private boolean startPaused = true;

    public PersuasionPlatform(PersuasionSettings settings) {
        // Define settings
        this.settings = settings;

        // Create list of listeners
        this.listeners = new ArrayList<PersuasionPlatformListener>();

        // Create list of agents
        this.agents = new ArrayList<PersuasionParticipatingAgent>();
    }

    public void init(Constant topic, List<PersuasionAgent> agents) {
        // Create the persuasion dialogue, set the topic
        this.dialogue = new PersuasionDialogue(topic);

        // Create list for every move
        this.allMoves = new ArrayList<PersuasionMove<? extends Locution>>();

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

        // No agent was last to move
        this.lastToMove = null;

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
                this.setDialogueState(PersuasionDialogueState.Opening);
                break;
            case Opening:
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
        }

        this.dialogue.setState(PersuasionDialogueState.Active);
    }

    private void setDialogueState(PersuasionDialogueState newState) {
        this.broadcastMessage(new PersuasionDialogueStateChangeMessage(newState));
        this.dialogue.setState(newState);
    }

    private PersuasionParticipatingAgent getNextToMove() {
        if (this.agents.size() == 0) {
            return null;
        }
        if (lastToMove == null) {
            // Nobody moved yet: return the first participant
            return this.agents.get(0);
        }
        for (int i = 0; i < this.agents.size(); i++) {
            if (this.agents.get(i).equals(lastToMove)) {
                if ((i + 1) < this.agents.size()) {
                    // Return the next player in the row
                    return this.agents.get(i + 1);
                } else {
                    // The 'last' player moved last time; return to the first again
                    return this.agents.get(0);
                }
            }
        }
        // Last player to move not found...
        return null;
    }

    private void playPersuasionRound() {
        // Give the next agent to move a single turn to make new moves
        PersuasionParticipatingAgent toMove = getNextToMove();
        List<PersuasionMove<? extends Locution>> moves = toMove.getAgent().makeMoves();

        // Check validity of the moves
        if (moves != null) {
            for (Iterator<PersuasionMove<? extends Locution>> iter = moves.iterator(); iter.hasNext();) {
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

        // Determine if the dialogue should end; if so, broadcast message and advance the dialogue to the terminating state
        PersuasionTerminationMessage terminationCause = this.shouldTerminate();
        if (terminationCause != null) {
            // Broadcast entering of termination state of the dialogue
            this.broadcastMessage(terminationCause);
            this.setDialogueState(PersuasionDialogueState.Terminating);
        } else {
            // Advance to the next participant
            this.lastToMove = toMove;
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

            PersuasionTerminationMessage cause = rule.shouldTerminate(this.dialogue, agents);
            if (cause != null) {
                return cause;
            }
        }
        return null;
    }

    private void determineOutcome() {
        // TODO: Winner should be broadcasted!
        // Get the dialogue outcome and broadcast this message
        //this.broadcastMessage(new PersuasionOutcomeMessage(settings.getOutcomeSelectionRule().determineOutcome(this.dialogue, this.allMoves)), false);

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