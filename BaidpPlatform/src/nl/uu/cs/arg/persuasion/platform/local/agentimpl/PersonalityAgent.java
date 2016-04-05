package nl.uu.cs.arg.persuasion.platform.local.agentimpl;

import net.sourceforge.jFuzzyLogic.plot.JFuzzyChart;
import net.sourceforge.jFuzzyLogic.rule.Variable;
import nl.uu.cs.arg.persuasion.model.dialogue.PersuasionDialogueException;
import nl.uu.cs.arg.persuasion.model.dialogue.PersuasionMove;
import nl.uu.cs.arg.persuasion.model.dialogue.locutions.ClaimLocution;
import nl.uu.cs.arg.persuasion.platform.local.AgentXmlData;
import nl.uu.cs.arg.persuasion.platform.local.agentimpl.attitudes.Attitude;
import nl.uu.cs.arg.shared.dialogue.locutions.ConcedeLocution;
import nl.uu.cs.arg.shared.dialogue.locutions.Locution;
import nl.uu.cs.arg.shared.dialogue.locutions.RetractLocution;
import nl.uu.cs.arg.shared.dialogue.locutions.WhyLocution;
import org.aspic.inference.ReasonerException;
import org.aspic.inference.parser.ParseException;

import java.util.*;
import java.util.Collections;
import java.util.function.Function;

import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.FunctionBlock;

public class PersonalityAgent extends PersuadingAgent {

    private boolean outOfMoves;

    private HashMap<String, Double> personalityVector = new HashMap<String, Double>() {{
        put("actions", 0.0);
        put("ideas", 0.0);
        put("values", 0.0);
        put("competence", 0.0);
        put("dutifulness", 0.0);
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
        put("impulsiveness", 0.0);
    }};

    private String reasoningRulesFCLPath;

    private FIS fis;

    private static final boolean fisVerbose = true;

    private void output(String msg)
    {
        System.err.println(this.getClass().getName() + ": " + msg);
    }

    private void initFIS()
    {
        // Load FIS from file
        this.fis = FIS.load(reasoningRulesFCLPath, fisVerbose);
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

    private Attitude getAttitudeByLocution(Class locutionClass) throws ReasonerException {
        FunctionBlock fb = null;

        // Select function block depending on selected locution
        if (locutionClass == ClaimLocution.class) {
            fb = this.fis.getFunctionBlock("assertion");
        } else if (locutionClass == ConcedeLocution.class) {
            fb = this.fis.getFunctionBlock("acceptance");
        } else if (locutionClass == WhyLocution.class) {
            fb = this.fis.getFunctionBlock("challenge");
        } else if (locutionClass == RetractLocution.class) {
            fb = this.fis.getFunctionBlock("retraction");
        } else {
            this.output("Unknown locution class: " + locutionClass);
            throw new ReasonerException("Reasoning failed when selecting attitude, see output.");
        }

        // Define variables
        for (Map.Entry<String, Variable> variable : fb.getVariables().entrySet()) {
            if (variable.getValue().isInput() && this.personalityVector.keySet().contains(variable.getKey())) {
                fb.setVariable(variable.getKey(), this.personalityVector.get(variable.getKey()));

                System.out.println(variable.getKey() + " (low): " + variable.getValue().getMembership("low"));
                System.out.println(variable.getKey() + " (mid): " + variable.getValue().getMembership("mid"));
                System.out.println(variable.getKey() + " (high): " + variable.getValue().getMembership("high"));
            }
        }

        // Evaluate
        fb.evaluate();

        this.output("Input/output variables:");
        for (Map.Entry<String, Variable> variable : fb.getVariables().entrySet()) {
            if (true || variable.getValue().isOutput()) {
                this.output(variable.getKey() + ": " + variable.getValue().getValue());
            }
        }

        JFuzzyChart.get().chart(this.fis);

        return null;
    }

    /**
     * Returns a list of Class objects resembling *Locution classes. List returned describes the ordering of locutions
     * in descending order, based on the personality vector of the agent and the reasoning rules as specified in FCL.
     *
     * @return
     * @throws ReasonerException
     */
    private List<Class> getActionOrdering() throws ReasonerException
    {
        FunctionBlock fb = this.fis.getFunctionBlock("actionselection");

        // Define variables
        for (Map.Entry<String, Variable> variable : fb.getVariables().entrySet()) {
            if (variable.getValue().isInput() && this.personalityVector.keySet().contains(variable.getKey())) {
                fb.setVariable(variable.getKey(), this.personalityVector.get(variable.getKey()));
            }
        }

        // Evaluate the function block
        fb.evaluate();

        /*this.output("Input/output variables:");
        for (Map.Entry<String, Variable> variable : fb.getVariables().entrySet()) {
            this.output(variable.getKey() + ": " + variable.getValue().getValue());
        }*/

        // Cast output variables to *Locution class objects
        SortedMap<Double, Class> locutionOrder = new TreeMap<Double, Class>();
        for (Map.Entry<String, Variable> variable : fb.getVariables().entrySet()) {
            if (variable.getValue().isOutput()) {
                String className = variable.getValue().getName();
                className = Character.toUpperCase(className.charAt(0)) + className.substring(1) + "Locution";
                Class c = this.locutionClassByName(className);
                if (c != null) {
                    locutionOrder.put(variable.getValue().getValue(), c);
                } else {
                    this.output("Could not find locution class: " + className);
                    throw new ReasonerException("Error in reasoning process, see log.");
                }
            }
        }
        List<Class> order = new ArrayList<Class>(locutionOrder.values());
        Collections.reverse(order);

        return order;
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

        // Define path to fcl file
        this.reasoningRulesFCLPath = (String) xmlDataFile.getRawProperties().get("reasoningrules-fcl-path");

        // Initialize FIS
        this.initFIS();

        // Evaluate
        try
        {
            this.getActionOrdering();

            this.getAttitudeByLocution(ClaimLocution.class);
        }
        catch (Exception e)
        {
            System.out.println(e);
            e.printStackTrace();
        }

        this.outOfMoves = false;
    }

    @Override
    protected void storeNewBeliefs(List<PersuasionMove<? extends Locution>> moves) throws ParseException, ReasonerException {
        System.out.println("storeNewBeliefs");
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

        // Get the active attackers
        /*for (PersuasionMove<? extends Locution> attack : this.dialogue.getActiveAttackers()) {

        }*/

        return moves;
    }

}
