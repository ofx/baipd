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
import nl.uu.cs.arg.shared.dialogue.locutions.*;
import org.aspic.inference.*;
import org.aspic.inference.parser.ParseException;

import java.util.*;
import java.util.stream.Collectors;

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
                //u += personalityVector.replace(property.getKey(), (Double) property.getValue()) != null ? 1 : 0;
                // Randomize personality
                personalityVector.replace(property.getKey(), 0.3 + (1.0 - 0.3) * new Random().nextDouble());
                ++u;
            }
        }

        // Check if every component of the personality vector is set
        if (u != this.personalityVector.size()) {
            this.output("Configuration incomplete, missing components of personality vector");
        }

        this.outOfMoves = false;
    }

    public HashMap<String, Double> getPersonalityVector()
    {
        return this.personalityVector;
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

    protected TreeMap<Reasoner, Double> actionSelection()
    {
        // Determine the preference ordering over types of speech acts
        ActionSelectionReasoner reasoner = new ActionSelectionReasoner();
        reasoner.setPersonalityVector(this.personalityVector);
        TreeMap<Reasoner, Double> ordering = reasoner.run();
        return ordering;
    }

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map)
    {
        List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>( map.entrySet() );
        Collections.sort( list, new Comparator<Map.Entry<K, V>>()
        {
            public int compare( Map.Entry<K, V> o1, Map.Entry<K, V> o2 )
            {
                return (o1.getValue()).compareTo( o2.getValue() );
            }
        });

        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list)
        {
            result.put( entry.getKey(), entry.getValue() );
        }
        return result;
    }

    public LinkedHashMap<Attitude, Double> getAttitudeOrdering(ArrayList<Reasoner> actionOrdering)
    {
        LinkedHashMap<Attitude, Double> attitudes = new LinkedHashMap<Attitude, Double>();
        ArrayList<TreeMap<Attitude, Double>> orderings = new ArrayList<>();

        final int maxAttitudes = 6;
        for (int i = 0 ; i < 6 ; ++i) {
            for (Reasoner reasoner : actionOrdering) {
                reasoner.setPersonalityVector(this.personalityVector);
                TreeMap<Attitude, Double> ats = reasoner.run();
                orderings.add(ats);

                Object[] atts = (Object[]) ats.keySet().toArray();
                if (i < atts.length) {
                    Attitude a = (Attitude) atts[i];
                    double d = 0.0;
                    for (Map.Entry<Attitude, Double> entry : ats.entrySet()) {
                        if (entry.getKey().getName().equals(a.getName())) {
                            d = entry.getValue();
                            break;
                        }
                    }
                    attitudes.put(a, d);
                }
            }
        }

        // Fetch the action revision orderings
        /*for (int i = 0 ; i < actionOrdering.size() ; ++i) {
            for (Reasoner reasoner : actionOrdering) {
                reasoner.setPersonalityVector(this.personalityVector);
                HashMap<Attitude, Double> ats = reasoner.run();
                Attitude at = (Attitude) (ats.keySet().toArray())[i];
                attitudes.put(at, ats.get(at));
            }
        }*/

        return attitudes;
    }

    public TreeMap<Reasoner, Double> getActionOrderingMap()
    {
        return this.actionSelection();
    }

    public LinkedHashMap<Attitude, Double> getAttitudeOrderingMap()
    {
        return this.getAttitudeOrdering(this.setToArrayList(this.actionSelection().keySet()));
    }

    public ArrayList<Attitude> getAttitudeOrderingList()
    {
        ArrayList<Reasoner> actionOrdering = this.setToArrayList(this.actionSelection().keySet());
        return this.setToArrayList(this.getAttitudeOrdering(actionOrdering).keySet());
    }

    public String attitudeOrderingToString()
    {
        String s = "Action Revision ordering for '" + this.getName() + "'\n";
        ArrayList<Attitude> attitudes = this.getAttitudeOrderingList();

        int n = 0;
        for (Attitude attitude : attitudes) {
            s += "(" + n++ + "): " + attitude + "\n";
        }

        return s;
    }

    private<T> ArrayList<T> setToArrayList(Set<T> s)
    {
        ArrayList<T> a = new ArrayList<>();
        a.addAll(s);
        return a;
    }

    protected ArrayList<PersuasionMove<? extends Locution>> actionRevision() throws ParseException, PersuasionDialogueException, ReasonerException {

        ArrayList<Class> used = new ArrayList<>();
        ArrayList<PersuasionMove<? extends Locution>> generatedMoves = new ArrayList<>();
        ArrayList<Attitude> attitudes = this.getAttitudeOrderingList();

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

        generatedMoves = this.filterInconsistentMoves(generatedMoves);

        return generatedMoves;
    }

    private ArrayList<PersuasionMove<? extends Locution>> filterInconsistentMoves(ArrayList<PersuasionMove<? extends Locution>> moves)
    {
        ArrayList<PersuasionMove<? extends Locution>> ret = new ArrayList<PersuasionMove<? extends Locution>>();

        ArrayList<String> attacks = new ArrayList<>();
        ArrayList<String> defends = new ArrayList<>();

        // Filter moves that are inconsistent e.g. why ~endangersHumanity and retract endangersHumanity in the same turn
        // That is, moves that attack and surrender at the same time
        Iterator<PersuasionMove<? extends  Locution>> it = moves.iterator();
        while (it.hasNext()) {
            PersuasionMove<? extends Locution> move = it.next();

            if (move.getLocution() instanceof ClaimLocution) {
                ClaimLocution locution = (ClaimLocution) move.getLocution();
                String f = locution.getProposition().inspect();
                f = f.replace("~", "");

                if (!defends.contains(f)) {
                    attacks.add(f);
                    ret.add(move);
                }
            } else if (move.getLocution() instanceof ArgueLocution) {
                ArgueLocution locution = (ArgueLocution) move.getLocution();
                String f = locution.getArgument().getClaim().inspect();
                f = f.replace("~", "");

                if (!defends.contains(f)) {
                    attacks.add(f);
                    ret.add(move);
                }
            } else if (move.getLocution() instanceof WhyLocution) {
                WhyLocution locution = (WhyLocution) move.getLocution();
                String f = locution.getAttackedPremise().inspect();
                f = f.replace("~", "");

                if (!defends.contains(f)) {
                    attacks.add(f);
                    ret.add(move);
                }
            } else if (move.getLocution() instanceof RetractLocution) {
                RetractLocution locution = (RetractLocution) move.getLocution();
                String f = locution.getRetractedTerm().inspect();
                f = f.replace("~", "");

                if (!attacks.contains(f)) {
                    defends.add(f);
                    ret.add(move);
                }
            } else if (move.getLocution() instanceof ConcedeLocution) {
                ConcedeLocution locution = (ConcedeLocution) move.getLocution();
                String f = locution.getConcededConstant().inspect();
                f = f.replace("~", "");

                if (!attacks.contains(f)) {
                    defends.add(f);
                    ret.add(move);
                }
            }
        }

        // We disallow double targetted moves
        /*ArrayList<PersuasionMove<? extends Locution>> usedTargets = new ArrayList<>();
        it = ret.iterator();
        while (it.hasNext()) {
            PersuasionMove<? extends Locution> move = it.next();
            if (!usedTargets.contains(move.getTarget())) {
                usedTargets.add(move.getTarget());
            } else {
                it.remove();
            }
        }*/

        return ret;
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
            ret = this.actionRevision();
        }

        return ret;
    }

}
