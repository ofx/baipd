package nl.uu.cs.arg.persuasion.platform.local.agentimpl;

import nl.uu.cs.arg.persuasion.model.PersuasionAgent;
import nl.uu.cs.arg.persuasion.model.PersuasionParticipant;
import nl.uu.cs.arg.persuasion.model.dialogue.PersuasionDialogue;
import nl.uu.cs.arg.persuasion.model.dialogue.PersuasionDialogueException;
import nl.uu.cs.arg.persuasion.model.dialogue.PersuasionDialogueMessage;
import nl.uu.cs.arg.persuasion.model.dialogue.PersuasionMove;
import nl.uu.cs.arg.persuasion.platform.local.AgentXmlData;

import nl.uu.cs.arg.shared.dialogue.locutions.Locution;
import org.aspic.inference.Constant;
import org.aspic.inference.KnowledgeBase;
import org.aspic.inference.ReasonerException;
import org.aspic.inference.parser.ParseException;

import java.util.*;

public abstract class PersuadingAgent implements PersuasionAgent, StrategyExposer {

    private static final String NAME = "Persuading agent";

    protected String name = NAME;

    protected StrategyHelper helper = StrategyHelper.DefaultHelper;

    protected PersuasionParticipant participant;

    protected List<PersuasionParticipant> participants = new ArrayList<PersuasionParticipant>();

    protected PersuasionDialogue dialogue;

    protected KnowledgeBase beliefs;

    protected Set<Constant> initialBeliefs;

    protected PersuadingAgent(String name, Map<String, Object> rawProperties, KnowledgeBase beliefs) {
        this.name = name;
        this.beliefs = beliefs;
        this.initialBeliefs = new HashSet<Constant>();
        this.initialBeliefs.addAll(this.beliefs.getRules());
    }

    public PersuadingAgent(AgentXmlData xmlDataFile) {
        this(xmlDataFile.getName(),
                xmlDataFile.getRawProperties(),
                xmlDataFile.getBeliefBase());
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void initialize(PersuasionParticipant participant) {
        this.participant = participant;
    }

    @Override
    public List<PersuasionMove<? extends Locution>> makeMoves() {

        /*try {

            // 1: Move evaluation
            // See onNewMovesReceived

            // 2: Option generation
            List<Constant> options = generateOptions();

            // 3: Option evaluation
            List<ValuedOption> valuedOptions = evaluateAllOptions(options);

            // 4: Option analysis
            analyseOptions(valuedOptions);

            // 5: Move generation
            return generateMoves(valuedOptions);

        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        } catch (ReasonerException e) {
            e.printStackTrace();
            return null;
        } catch (DialogueException e) {
            e.printStackTrace();
            return null;
        }*/

        return null;
    }

    protected abstract void storeNewBeliefs(List<PersuasionMove<? extends Locution>> moves) throws ParseException, ReasonerException;

    protected abstract List<PersuasionMove<? extends Locution>> generateMoves() throws PersuasionDialogueException, ParseException, ReasonerException;

    @Override
    public void onNewMovesReceived(List<PersuasionMove<? extends Locution>> moves) {
        /*// Update our knowledge of the agents that are playing
        for (Move<? extends Locution> move : moves) {
            if (move.getLocution() instanceof JoinDialogueLocution) {
                participants.add(move.getPlayer());
            }
        }

        // Update the skip count
        skipCount = 0;

        // Update our internal dialogue model
        try {
            if (this.dialogue != null) {
                this.dialogue.update(moves);
            }
        } catch (DialogueException e) {
            // Invalid moves were played by some agent: ignore this
        }

        // Evaluate whether new move beliefs should be adopted in our knowledge base
        try {
            storeNewBeliefs(moves);
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (ReasonerException e) {
            e.printStackTrace();
        }*/
    }

    @Override
    public void onDialogueException(PersuasionDialogueException e) {
        // Do nothing
    }

    @Override
    public void onDialogueMessagesReceived(List<? extends PersuasionDialogueMessage> messages) {
        /*for (DialogueMessage message : messages) {
            if (dialogue != null && message instanceof DialogueStateChangeMessage) {
                // Update the state of our dialogue
                dialogue.setState(((DialogueStateChangeMessage)message).getNewState());
            } else if (dialogue != null && message instanceof SkipMoveMessage) {
                // Update the skip count
                skipCount++;
            }
        }*/
    }

    public String toString() {
        return getName();
    }

    @Override
    public Set<Constant> getInitialBeliefs() {
        return initialBeliefs;
    }

    @Override
    public Map<String, Object> getStategyProperties() {
        return new HashMap<String, Object>();
    }
}
