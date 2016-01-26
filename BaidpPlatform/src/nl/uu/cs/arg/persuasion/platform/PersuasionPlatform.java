package nl.uu.cs.arg.persuasion.platform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import nl.uu.cs.arg.persuasion.model.PersuasionAgent;
import nl.uu.cs.arg.persuasion.model.dialogue.*;
import nl.uu.cs.arg.persuasion.model.dialogue.protocol.PersuasionProtocolException;
import nl.uu.cs.arg.persuasion.model.dialogue.protocol.PersuasionRule;
import nl.uu.cs.arg.persuasion.model.dialogue.protocol.PersuasionTerminationMessage;
import nl.uu.cs.arg.persuasion.model.dialogue.protocol.PersuasionTerminationRule;

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

    private int skippedTurnsCount;

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
        broadcastMessage(new PersuasionDialogueStartedMessage(topic));

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
                broadcastMessage(new PersuasionPlatformStateMessage(false));
            }
            synchronized (this) {
                while (this.paused) {
                    try {
                        wait();
                        // We are going to run again if wait() succeeded (on notify())
                        broadcastMessage(new PersuasionPlatformStateMessage(true));
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

    /**
     * Sets the platform start mode
     * @param startPaused Whether to require an explicit requestStart() before deliberating
     */
    public void setStartMode(boolean startPaused) {
        this.startPaused = startPaused;
    }

    /**
     * Ask to start the platform execution (again).
     */
    public void requestStart() {
        this.paused = false;
        notify();
    }

    /**
     * Ask to pause the platform execution; normally the platform will
     * pause after the current round is completed. If you need to resume
     * the process later on, use the .requestStart() method.
     */
    public void requestPause() {
        this.paused = true;
    }

    /**
     * Returns whether the platform is currently requested to be paused
     * @return Returns true is the platform is (requested to be) paused; false otherwise
     */
    public boolean isPaused() {
        return this.paused;
    }

    /**
     * Set the single stepping behaviour of this platform
     * @param stopAfterEachStep If set to true, the platform will stop running
     * after ever single dialogue round played; otherwise it will continue until
     * the dialogue terminates or requestStop() is called
     */
    public void setSingleStepping(boolean stopAfterEachStep) {
        this.stopAfterEachStep = stopAfterEachStep;
    }

    /**
     * Play a single dialogue round, depending on the current dialogue state
     */
    private void playDialogueRound() {
        switch (this.dialogue.getState()) {
            case Unopened:
                // Unopened; only switch to the opening state
                this.setDialogueState(PersuasionDialogueState.Opening);
                break;
            // TODO: Request agents to play first claim move
            /*case Opening:
                // Joining; ask all non-opening agents to join the dialogue
                askAgentsToJoin();
                break;*/
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

    private void setDialogueState(PersuasionDialogueState newState) {
        this.broadcastMessage(new PersuasionDialogueStateChangeMessage(newState));
        this.dialogue.setState(newState);
    }

    private PersuasionParticipatingAgent getNextToMove() {
        // TODO: Turn taking, check if this is correct!
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

    // TODO: Replace by request for first move!
    /*private void askAgentsToJoin() {

        // Create an open-dialogue locution, mimicking the starting of the dialogue by one agent
        if (openDialogueMove == null) {
            openDialogueMove = Move.buildMove(null, null, new OpenDialogueLocution(dialogue.getTopic(), dialogue.getTopicGoal()));
            broadcastDialogeMove(openDialogueMove, true);
        }

        // Ask the next connected agent if they want to join the dialogue
        PersuasionParticipatingAgent toMove = getNextToMove();
        Move<? extends Locution> answer = toMove.getAgent().decideToJoin(openDialogueMove.getLocution());

        if (answer == null) {
            // The agent returned null; do not add it to the joining agents and send an exception
            toMove.getAgent().onDialogueException(new DialogueException("You should always return a join-dialogue or deny-dialogue move; instead null was returned. You did not join the dialogue."));
            broadcastException(new PlatformException(toMove.toString() + ", when asked to join the dialogue, returned null instead of a move.", false));

        } else if (answer.getLocution() instanceof DenyDialogueLocution) {
            // The agent denied to join; just let other agents and platform listeners know
            broadcastDialogeMove(answer, false);

        } else if (answer.getLocution() instanceof JoinDialogueLocution) {
            // The agent accepted; add it to the joined agents and let everybody know of it
            joinedAgents.add(toMove);
            broadcastDialogeMove(answer, false);

        } else {
            // If no valid answer was given, do not add it to the joining agents and send an exception
            toMove.getAgent().onDialogueException(new ProtocolException(answer, "You can only play a join-dialogue or deny-dialogue move as a reply to an open-dialogue. A deny is assumed and you will not join the dialogue."));
            broadcastException(new PlatformException(toMove.toString() + ", when asked to join the dialogue, played an invalid move " + answer.toLogicString(), false));

        }

        // Advance to the next participant
        lastToMove = toMove;
        if (connectedAgents.size() > 0 && connectedAgents.get(connectedAgents.size() - 1).equals(lastToMove)) {
            // If all agents have denied or joined the dialogue, advance to the next step
            setDialogueState(DialogueState.Deliberating);
        }

    }*/

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

        // TODO: Skipping is not an option, check this!
        // TODO: Termination is based on skipping turns, should be different!
        // If the agent made no (valid) moves: he skipped turn
        /*if (moves == null || moves.size() == 0) {
            broadcastMessage(new SkipMoveMessage(toMove.getParticipant()), true);
            skippedTurnsCount++;

        } else {

            skippedTurnsCount = 0;

            // Update the dialogue with the forwarded moves
            allMoves.addAll(moves);
            try {
                dialogue.update(moves);
            } catch (DialogueException e) {
                broadcastException(new PlatformException(e.getMessage(), false));
            }

            // Broadcast the moves made
            broadcastDialogeMoves(moves, true);

        }*/

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
            // TODO: Check skips!
            PersuasionTerminationMessage cause = rule.shouldTerminate(this.dialogue, this.agents.size(), -1);
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