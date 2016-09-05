package nl.uu.cs.arg.persuasion.platform.local.agentimpl;

import nl.uu.cs.arg.persuasion.model.dialogue.PersuasionDialogueException;
import nl.uu.cs.arg.persuasion.model.dialogue.PersuasionMove;
import nl.uu.cs.arg.persuasion.model.dialogue.locutions.ClaimLocution;
import nl.uu.cs.arg.persuasion.platform.local.AgentXmlData;
import nl.uu.cs.arg.persuasion.platform.local.agentimpl.attitudes.Attitude;
import nl.uu.cs.arg.persuasion.platform.local.agentimpl.attitudes.acceptance.AcceptanceAttitude;
import nl.uu.cs.arg.persuasion.platform.local.agentimpl.attitudes.assertion.AssertionAttitude;
import nl.uu.cs.arg.persuasion.platform.local.agentimpl.attitudes.assertion.CarefulAttitude;
import nl.uu.cs.arg.persuasion.platform.local.agentimpl.attitudes.assertion.ConfidentAttitude;
import nl.uu.cs.arg.persuasion.platform.local.agentimpl.attitudes.assertion.ThoughtfulAttitude;
import nl.uu.cs.arg.persuasion.platform.local.agentimpl.attitudes.challenge.ChallengeAttitude;
import nl.uu.cs.arg.persuasion.platform.local.agentimpl.attitudes.retraction.RetractionAttitude;
import nl.uu.cs.arg.persuasion.platform.local.agentimpl.reasoning.*;
import nl.uu.cs.arg.persuasion.platform.local.agentimpl.reasoning.Reasoner;
import nl.uu.cs.arg.shared.dialogue.Move;
import nl.uu.cs.arg.shared.dialogue.locutions.DeliberationLocution;
import nl.uu.cs.arg.shared.dialogue.locutions.Locution;
import nl.uu.cs.arg.shared.dialogue.locutions.ProposeLocution;
import org.aspic.inference.*;
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
        // Store newly publicized beliefs when we have no argument against them (or an argument for them)
        for (PersuasionMove<? extends Locution> move : moves) {
            if (move.hasSurrendered(this.getParticipant())) {
                // Store new move beliefs
                Set<Constant> exposed = new HashSet<Constant>();
                ((DeliberationLocution)move.getLocution()).gatherPublicBeliefs(exposed);
                for (Constant b : exposed) {
                    if (b instanceof Rule) {
                        // We add rules if it didn't exist yet and it doesn't cause any loops
                        //if (!beliefs.ruleExists((Rule)b) && !helper.causesLoop(beliefs, (Rule)b)) {
                        //  beliefs.addRule((Rule)b);
                        //}
                    } else {
                        // We add constants and terms directly, if they are not options or the mutual goal
                        if (!dialogue.getTopic().equals(b) && !dialogue.getTopic().isUnifiable(b) && !beliefs.ruleExists(new Rule(b))) {
                            beliefs.addRule(new Rule(b));
                        }
                    }
                }
            }
        }
    }

    protected ArrayList<Reasoner> actionSelection()
    {
        // Determine the preference ordering over types of speech acts
        ActionSelectionReasoner reasoner = new ActionSelectionReasoner(0.2);
        reasoner.setPersonalityVector(this.personalityVector);
        ArrayList<Reasoner> ordering = (ArrayList<Reasoner>) reasoner.run();

        return ordering;
    }

    protected ArrayList<PersuasionMove<? extends Locution>> actionRevision(ArrayList<Reasoner> actionOrdering) throws ParseException, PersuasionDialogueException, ReasonerException {
        ArrayList<Attitude> attitudes = new ArrayList<>();
        ArrayList<Class> used = new ArrayList<>();
        ArrayList<PersuasionMove<? extends Locution>> generatedMoves = new ArrayList<>();

        // Fetch the action revision orderings
        for (int i = 0 ; i < actionOrdering.size() ; ++i) {
            for (Reasoner reasoner : actionOrdering) {
                reasoner.setPersonalityVector(this.personalityVector);
                attitudes.add(((ArrayList<Attitude>) reasoner.run()).get(i));
            }
        }

        // Do reasoning
        for (Attitude attitude : attitudes) {
            Class c = attitude.getClass().getSuperclass();
            if (!used.contains(attitude.getClass().getSuperclass())) {
                List<PersuasionMove<? extends Locution>> moves = attitude.generateValidatedMoves(this, this.dialogue);
                if (moves.size() > 0) {
                    generatedMoves.addAll(moves);
                    used.add(attitude.getClass().getSuperclass());
                }
            }
        }

        // We disallow double targetted moves
        ArrayList<PersuasionMove<? extends Locution>> usedTargets = new ArrayList<>();
        Iterator<PersuasionMove<? extends  Locution>> it = generatedMoves.iterator();
        while (it.hasNext()) {
            PersuasionMove<? extends Locution> move = it.next();
            if (!usedTargets.contains(move.getTarget())) {
                usedTargets.add(move.getTarget());
            } else {
                it.remove();
            }
        }

        return generatedMoves;

        /*HashMap<Reasoner, ArrayList<Attitude>> actionRevisionOrderings = new HashMap<>();

        int failCount = 0;
        for (Reasoner reasoner : actionOrdering) {
            if (usedReasoners.contains(reasoner)) {
                continue;
            }

            ArrayList<Attitude> actionRevisionOrdering;
            if (!actionRevisionOrderings.containsKey(reasoner)) {
                reasoner.setPersonalityVector(this.personalityVector);
                actionRevisionOrdering = (ArrayList<Attitude>) reasoner.run();
            } else {
                actionRevisionOrdering = actionRevisionOrderings.get(reasoner);
            }

            final int l = actionRevisionOrdering.size();
            if (level < l) {
                Attitude attitude = actionRevisionOrdering.get(level);
                //List<PersuasionMove<? extends Locution>> moves = null;
                //moves = attitude.generateValidatedMoves(this, this.dialogue);
                int s = moves.size();
                moves.addAll(attitude.generateValidatedMoves(this, this.dialogue));
                usedReasoners.add(reasoner);
            } else {
                ++failCount;
            }
        }

        if (failCount == actionOrdering.size()) {
            return; // Nothing is allowed...
        }

        // No action selected, recurse
        this.actionRevision(actionOrdering, usedReasoners, moves, level + 1);*/
    }

    @Override
    protected List<PersuasionMove<? extends Locution>> generateMoves() throws PersuasionDialogueException, ParseException, ReasonerException {
        List<PersuasionMove<? extends Locution>> ret = new ArrayList<PersuasionMove<? extends Locution>>();

        // The first move, if we're proponent should be a claim locution move containing the topic
        if (!this.dialogue.isStarted()) {
            if (this.isProponent(this.dialogue)) {
                ret.add(PersuasionMove.buildMove(this.participant, null, new ClaimLocution(this.dialogue.getTopic())));
            } else {
                // Skip
                return null;
            }
        } else {
            ArrayList<Reasoner> ordering = this.actionSelection();
            ret = this.actionRevision(ordering);
        }

        return ret;
    }

}
