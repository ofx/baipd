package nl.uu.cs.arg.persuasion.platform.local.agentimpl;

import nl.uu.cs.arg.persuasion.model.PersuasionAgent;
import nl.uu.cs.arg.persuasion.model.PersuasionParticipant;
import nl.uu.cs.arg.persuasion.model.dialogue.*;
import nl.uu.cs.arg.persuasion.platform.local.AgentXmlData;

import nl.uu.cs.arg.shared.dialogue.locutions.Locution;
import org.aspic.inference.*;
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

    public PersuasionParticipant getParticipant() { return this.participant; }

    public KnowledgeBase getBeliefs() { return this.beliefs; }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void initialize(PersuasionParticipant participant) {
        this.participant = participant;
    }

    @Override
    public boolean isProponent(PersuasionDialogue dialogue) throws ParseException, ReasonerException {
        // Check if we can make a proof for the topic
        Constant topic = this.dialogue.getTopic();
        List<RuleArgument> proofs = helper.findProof(new ConstantList(topic), 0.0, this.beliefs, null);
        return proofs.size() != 0;
    }

    @Override
    public void join(PersuasionDialogue dialogue) {
        // Store the dialogue (with topic and goal)
        this.dialogue = new PersuasionDialogue(dialogue.getTopic());
        this.dialogue.setState(dialogue.getState());
    }

    @Override
    public List<PersuasionMove<? extends Locution>> makeMoves() {
        try {
            return this.generateMoves();
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        } catch (ReasonerException e) {
            e.printStackTrace();
            return null;
        } catch (PersuasionDialogueException e) {
            e.printStackTrace();
            return null;
        }
    }

    protected abstract void storeNewBeliefs(List<PersuasionMove<? extends Locution>> moves) throws ParseException, ReasonerException;

    protected abstract List<PersuasionMove<? extends Locution>> generateMoves() throws PersuasionDialogueException, ParseException, ReasonerException;

    @Override
    public void onNewMovesReceived(List<PersuasionMove<? extends Locution>> moves) {
        // Check if we already know the participant, if not: add
        for (PersuasionMove move : moves) {
            if (!this.participants.contains(move.getPlayer())) {
                this.participants.add(move.getPlayer());
            }
        }

        // Update our internal dialogue model
        try {
            if (this.dialogue != null) {
                this.dialogue.update(moves);
            }
        } catch (PersuasionDialogueException e) {
            // Invalid moves were played by some agent: ignore this
        }

        // Evaluate whether new move beliefs should be adopted in our knowledge base
        try {
            this.storeNewBeliefs(moves);
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (ReasonerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDialogueException(PersuasionDialogueException e) {
        // Do nothing
    }

    @Override
    public void onDialogueMessagesReceived(List<? extends PersuasionDialogueMessage> messages) {
        for (PersuasionDialogueMessage message : messages) {
            if (this.dialogue != null && message instanceof PersuasionDialogueStateChangeMessage) {
                // Update the state of our dialogue
                this.dialogue.setState(((PersuasionDialogueStateChangeMessage)message).getNewState());
            }
        }
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
