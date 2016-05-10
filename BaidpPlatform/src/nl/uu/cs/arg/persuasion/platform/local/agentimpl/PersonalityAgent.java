package nl.uu.cs.arg.persuasion.platform.local.agentimpl;

import nl.uu.cs.arg.persuasion.model.dialogue.PersuasionDialogueException;
import nl.uu.cs.arg.persuasion.model.dialogue.PersuasionMove;
import nl.uu.cs.arg.persuasion.model.dialogue.locutions.ClaimLocution;
import nl.uu.cs.arg.persuasion.platform.local.AgentXmlData;
import nl.uu.cs.arg.persuasion.platform.local.agentimpl.attitudes.Attitude;
import nl.uu.cs.arg.persuasion.platform.local.agentimpl.attitudes.acceptance.AcceptanceAttitude;
import nl.uu.cs.arg.persuasion.platform.local.agentimpl.attitudes.assertion.AssertionAttitude;
import nl.uu.cs.arg.persuasion.platform.local.agentimpl.attitudes.challenge.ChallengeAttitude;
import nl.uu.cs.arg.persuasion.platform.local.agentimpl.attitudes.retraction.RetractionAttitude;
import nl.uu.cs.arg.persuasion.platform.local.agentimpl.reasoning.*;
import nl.uu.cs.arg.shared.dialogue.locutions.Locution;
import org.aspic.inference.ReasonerException;
import org.aspic.inference.parser.ParseException;

import java.util.*;

public class PersonalityAgent extends PersuadingAgent {

    private boolean outOfMoves;

    private HashMap<String, Double> personalityVector = new HashMap<String, Double>() {{
        put("actions", 0.0);
        put("ideas", 0.0);
        put("values", 0.0);
        put("competence", 0.0);
        put("achievementstriving", 0.0);
        put("selfdiscipline", 0.0);
        put("deliberation", 0.0);
        put("assertiveness", 0.0);
        put("activity", 0.0);
        put("trust", 0.0);
        put("straightforwardness", 0.0);
        put("modesty", 0.0);
        put("anxiety", 0.0);
        put("angryhostility", 0.0);
        put("depression", 0.0);
        put("selfconsciousness", 0.0);
    }};

    private void output(String msg)
    {
        System.err.println(this.getClass().getName() + ": " + msg);
    }

    private Class locutionClassByName(String className)
    {
        LinkedList<String> packages = new LinkedList<String>() {{
            add("nl.uu.cs.arg.shared.dialogue.locutions");
            add("nl.uu.cs.arg.persuasion.model.dialogue.locutions");
        }};

        Class c = null;
        for (String packageName : packages) {
            try {
                c = Class.forName(packageName + '.' + className);
            } catch (Exception e) {
            }
        }

        return c;
    }

    public PersonalityAgent(AgentXmlData xmlDataFile) {
        super(xmlDataFile);

        // Filter personality vector components
        int u = 0;
        for (Map.Entry<String, Object> property : xmlDataFile.getRawProperties().entrySet()) {
            if (property.getValue() instanceof  Double) {
                u += personalityVector.replace(property.getKey(), (Double) property.getValue()) != null ? 1 : 0;
            }
        }

        // Check if every component of the personality vector is set
        if (u != this.personalityVector.size()) {
            this.output("Configuration incomplete, missing components of personality vector");
        }

        this.outOfMoves = false;
    }

    @Override
    protected void storeNewBeliefs(List<PersuasionMove<? extends Locution>> moves) throws ParseException, ReasonerException {
        System.out.println("storeNewBeliefs");
    }

    protected void generateOptions()
    {

    }

    protected ArrayList<Reasoner> actionSelection()
    {
        // Determine the preference ordering over types of speech acts
        ActionSelectionReasoner reasoner = new ActionSelectionReasoner(0.2);
        reasoner.setPersonalityVector(this.personalityVector);
        ArrayList<Reasoner> ordering = (ArrayList<Reasoner>) reasoner.run();

        return ordering;
    }

    protected void actionRevision(ArrayList<Reasoner> actionOrdering)
    {
        this.actionRevision(actionOrdering, 0);
    }

    protected void actionRevision(ArrayList<Reasoner> actionOrdering, int level)
    {
        int failCount = 0;
        for (Reasoner reasoner : actionOrdering) {
            reasoner.setPersonalityVector(this.personalityVector);
            ArrayList<Attitude> actionRevisionOrdering = (ArrayList<Attitude>) reasoner.run();

            final int l = actionRevisionOrdering.size();
            if (level < l) {
                Attitude attitude = actionRevisionOrdering.get(level);

                boolean valid = true;
                if (valid) {
                    // Do action
                    return; // Must return
                }
            } else {
                ++failCount;
            }
        }

        if (failCount == actionOrdering.size()) {
            // No move is allowed...
        }

        // No action selected, recurse
        this.actionRevision(actionOrdering, level + 1);
    }

    protected void determinePreferences()
    {
        ArrayList<Reasoner> ordering = this.actionSelection();

        this.actionRevision(ordering);
    }

    @Override
    protected List<PersuasionMove<? extends Locution>> generateMoves() throws PersuasionDialogueException, ParseException, ReasonerException {
        List<PersuasionMove<? extends Locution>> moves = new ArrayList<PersuasionMove<? extends Locution>>();

        // The first move, if we're proponent should be a claim locution move containing the topic
        if (!this.dialogue.isStarted()) {
            if (this.isProponent(this.dialogue)) {
                moves.add(PersuasionMove.buildMove(this.participant, null, new ClaimLocution(this.dialogue.getTopic())));
            } else {
                // Skip
                return null;
            }
        }

        // Generate all available options
        this.generateOptions();

        // Determine preferences
        this.determinePreferences();

        //

        // Get the active attackers
        /*for (PersuasionMove<? extends Locution> attack : this.dialogue.getActiveAttackers()) {

        }*/

        return moves;
    }

}
