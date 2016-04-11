import com.fuzzylite.Engine;
import com.fuzzylite.FuzzyLite;
import com.fuzzylite.Op;
import com.fuzzylite.defuzzifier.Centroid;
import com.fuzzylite.defuzzifier.MeanOfMaximum;
import com.fuzzylite.imex.FldExporter;
import com.fuzzylite.norm.s.Maximum;
import com.fuzzylite.norm.t.Minimum;
import com.fuzzylite.rule.Rule;
import com.fuzzylite.rule.RuleBlock;
import com.fuzzylite.term.Triangle;
import com.fuzzylite.variable.InputVariable;
import com.fuzzylite.variable.OutputVariable;
import com.sun.corba.se.spi.orbutil.fsm.Input;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Main
{
    public static void main(String[] args)
    {
        FuzzyLite.setDebug(true);

        Engine engine = new Engine();
        engine.setName("simple-dimmer");

        // Input membership distributions
        Triangle low  = new Triangle("low", -0.5, 0.0, 0.5);
        Triangle med  = new Triangle("med",  0.0, 0.5, 1.0);
        Triangle high = new Triangle("high", 0.5, 1.0, 1.5);

        // Output membership distributions
        Triangle disfavored  = new Triangle("disfavored", -1.0, 0.0, 1.0);
        Triangle favored     = new Triangle("favored",     0.0, 1.0, 2.0);

        Map<String, InputVariable> inputVariables   = new HashMap<String, InputVariable>();
        Map<String, OutputVariable> outputVariables = new HashMap<String, OutputVariable>();

        // Create input variables
        {
            InputVariable achievementstriving = new InputVariable();
            InputVariable dutifulness         = new InputVariable();
            InputVariable selfdiscipline      = new InputVariable();
            InputVariable deliberation        = new InputVariable();
            InputVariable activity            = new InputVariable();
            InputVariable straightforwardness = new InputVariable();
            InputVariable modesty             = new InputVariable();
            InputVariable anxiety             = new InputVariable();
            InputVariable impulsiveness       = new InputVariable();
            InputVariable depression          = new InputVariable();
            InputVariable angryhostility      = new InputVariable();

            inputVariables.put("achievementstriving", achievementstriving);
            inputVariables.put("dutifulness",         dutifulness);
            inputVariables.put("selfdiscipline",      selfdiscipline);
            inputVariables.put("deliberation",        deliberation);
            inputVariables.put("activity",            activity);
            inputVariables.put("straightforwardness", straightforwardness);
            inputVariables.put("modesty",             modesty);
            inputVariables.put("anxiety",             anxiety);
            inputVariables.put("impulsiveness",       impulsiveness);
            inputVariables.put("depression",          depression);
            inputVariables.put("angryhostility",      angryhostility);

            for (Map.Entry<String, InputVariable> entry : inputVariables.entrySet())
            {
                InputVariable variable = entry.getValue();
                variable.setName(entry.getKey());
                variable.setRange(0.000, 1.000);
                variable.addTerm(low);
                variable.addTerm(med);
                variable.addTerm(high);
                engine.addInputVariable(variable);
            }
        }

        // Create output variables
        {
            OutputVariable thoughtful = new OutputVariable();
            OutputVariable careful = new OutputVariable();
            OutputVariable confident = new OutputVariable();
            OutputVariable spurious = new OutputVariable();
            OutputVariable deceptive = new OutputVariable();
            OutputVariable hesitant = new OutputVariable();

            outputVariables.put("thoughtful", thoughtful);
            outputVariables.put("careful",    careful);
            outputVariables.put("confident",  confident);
            outputVariables.put("spurious",   spurious);
            outputVariables.put("deceptive",  deceptive);
            outputVariables.put("hesitant",   hesitant);

            for (Map.Entry<String, OutputVariable> entry : outputVariables.entrySet()) {
                OutputVariable variable = entry.getValue();
                variable.setName(entry.getKey());
                variable.setRange(0.000, 1.000);
                variable.setDefaultValue(Double.NaN);
                variable.addTerm(disfavored);
                variable.addTerm(favored);
                variable.setDefuzzifier(new Centroid());
                variable.fuzzyOutput().setAccumulation(new Maximum());
                engine.addOutputVariable(variable);
            }
        }

        /*inputVariables.get("achievementstriving").setInputValue(1.0);
        inputVariables.get("selfdiscipline").setInputValue(1.0);
        inputVariables.get("deliberation").setInputValue(1.0);
        inputVariables.get("impulsiveness").setInputValue(1.0);
        inputVariables.get("anxiety").setInputValue(0.0);
        inputVariables.get("activity").setInputValue(1.0);
        inputVariables.get("straightforwardness").setInputValue(1.0);
        inputVariables.get("dutifulness").setInputValue(1.0);
        inputVariables.get("modesty").setInputValue(0.0);
        inputVariables.get("depression").setInputValue(0.0);*/

        String rules[] = {
                /*
                If the agent is achievement striving, dutiful, straightforward, not anxious and not modest, the agent
                would prefer to make a claim supported by an argument the agent can construct.
                 */
                "if achievementstriving is high " +
                        "and dutifulness is high " +
                        "and selfdiscipline is high " +
                        "and straightforwardness is high " +
                        "and modesty is low " +
                        "and anxiety is low " +
                        "and activity is high " +
                        "and deliberation is high " +
                        "and impulsiveness is low " +
                        "then thoughtful is favored",
                "if achievementstriving is high " +
                        "and dutifulness is high " +
                        "and selfdiscipline is high " +
                        "and straightforwardness is high " +
                        "and modesty is low " +
                        "and anxiety is low " +
                        "and activity is high " +
                        "and deliberation is med " +
                        "and impulsiveness is med " +
                        "then careful is favored",
                "if achievementstriving is high " +
                        "and dutifulness is high " +
                        "and selfdiscipline is high " +
                        "and straightforwardness is high " +
                        "and modesty is low " +
                        "and anxiety is low " +
                        "and activity is high " +
                        "and deliberation is low " +
                        "and impulsiveness is high " +
                        "then confident is favored",
                /*
                A deliberate agent that is not impulsive would prefer to only make the claim if the agent can provide
                a justified argument. Contrariwise, if the agent is not deliberate and is impulsive to some extent, the
                agent would rather prefer to select an attitude that allows the agent to make a claim on a weaker
                argument.
                 */
                "if deliberation is not high " +
                        "and impulsiveness is not low " +
                        "then thoughtful is disfavored",
                /*
                A mildly deliberate agent that is mildly impulsive would allow for claiming based on an argument that
                is weaker, however not strong at all. Contrariwise, if the agent is not deliberate at all, or impulsive
                the agent would rather allow for making a claim based on a weaker argument.
                 */
                "if deliberation is not med " +
                        "and impulsiveness is not med " +
                        "then careful is disfavored",
                /*
                A non-deliberate agent that is impulsive will be okay with selecting an attitude that allows the agent
                to make a claim based on a weak argument.
                 */
                "if deliberation is not low " +
                        "and impulsiveness is not high " +
                        "then confident is disfavored",
                /*
                If the agent is not impulsive, but is non-straightforward and has low self-discipline, the agent is
                likely to not make a claim. In addition, being non-achievement striving and showing low activity will
                cause the agent to be leaning more towards an attitude that makes the agent not claim at all. Depression
                on its own can cause the agent to not make a claim at all.

                Contrariwise, if the agent is impulsive the agent is more likely to make a claim. In addition, the agent
                being straightforward to some extent, having self-discipline to some extent, showing activity to some
                extent and being non-depressed make the agent lean more towards an attitude that makes the agent claim.
                 */
                "if impulsiveness is low " +
                        "and deliberation is low " +
                        "and straightforwardness is low " +
                        "and selfdiscipline is low " +
                        "and achievementstriving is low " +
                        "and activity is low " +
                        "and modesty is high " +
                        "and anxiety is high " +
                        "or depression is not low " +
                        "then hesitant is favored",
                "if impulsiveness is not low " +
                        "and straightforwardness is not low " +
                        "and selfdiscipline is not low " +
                        "and activity is not low " +
                        "and deliberation is not low " +
                        "and depression is low " +
                        "then hesitant is disfavored",
                /*

                 */
                "if achievementstriving is high " +
                        "and angryhostility is not low " +
                        "and depression is not low " +
                        "and deliberation is not high " +
                        "and activity is high " +
                        "and straightforwardness is low " +
                        "and modesty is low " +
                        "and anxiety is low " +
                        "and impulsiveness is high " +
                        "and dutifulness is low " +
                        "and selfdiscipline is low " +
                        "then deceptive is favored",
                "if achievementstriving is high " +
                        "and angryhostility is not low " +
                        "and depression is not low " +
                        "and deliberation is not high " +
                        "and activity is high " +
                        "and straightforwardness is low " +
                        "and modesty is low " +
                        "and anxiety is low " +
                        "and impulsiveness is high " +
                        "and dutifulness is med " +
                        "and selfdiscipline is med " +
                        "then spurious is favored",
                "if dutifulness is not low " +
                        "and selfdiscipline is not low " +
                        "then deceptive is disfavored",
                "if dutifulness is not med " +
                        "and selfdiscipline is not med " +
                        "then spurious is disfavored"
        };

        // Construct rules
        OwaRule r[] = new OwaRule[rules.length];
        {
            int i = 0;
            for (String rule : rules) {
                r[i++] = OwaRule.parse(rule, engine, 0.2);
            }
        }

        // Construct rule block
        RuleBlock ruleBlock = new RuleBlock();
        {
            for (Rule rule : r) {
                ruleBlock.addRule(rule);
            }

            ruleBlock.setConjunction(new Minimum());
            ruleBlock.setDisjunction(new Maximum());
            ruleBlock.setActivation(new Minimum());
            engine.addRuleBlock(ruleBlock);
        }

        engine.configure("Minimum", "Maximum", "Minimum", "Maximum", "Centroid");

        StringBuilder status = new StringBuilder();
        if (!engine.isReady(status))
        {
            throw new RuntimeException("Engine not ready. " + "The following errors were encountered:\n" + status.toString());
        }

        for (int i = 0; i < 1; ++i)
        {
            /*double rho = i * (1.0 / 50.0);
            for (OwaRule rule : r) {
                rule.setAndness(rho);
            }*/

            // Randomize input variables
            for (Map.Entry<String, InputVariable> entry : inputVariables.entrySet()) {
                entry.getValue().setInputValue(new Random().nextDouble());
            }

            engine.process();
            /*FuzzyLite.logger().info(String.format("Andness = %s -> Thoughtful = %s", Op.str(rho), outputVariables.get("thoughtful").getOutputValue()));
            FuzzyLite.logger().info(String.format("Andness = %s -> Careful = %s", Op.str(rho), outputVariables.get("careful").getOutputValue()));
            FuzzyLite.logger().info(String.format("Andness = %s -> Confident = %s", Op.str(rho), outputVariables.get("confident").getOutputValue()));
            FuzzyLite.logger().info(String.format("Andness = %s -> Hesitant = %s", Op.str(rho), outputVariables.get("hesitant").getOutputValue()));*/

            // Aggregate personality vector configuration
            HashMap<String, Double> personalityVector = new HashMap<String, Double>();
            for (Map.Entry<String, InputVariable> entry : inputVariables.entrySet()) {
                personalityVector.put(entry.getKey(), entry.getValue().getInputValue());
            }

            // Aggregate fuzzy result
            HashMap<String, Double> fuzzyResult = new HashMap<String, Double>();
            fuzzyResult.put("Thoughtful", outputVariables.get("thoughtful").getOutputValue());
            fuzzyResult.put("Careful", outputVariables.get("careful").getOutputValue());
            fuzzyResult.put("Confident", outputVariables.get("confident").getOutputValue());
            fuzzyResult.put("Hesitant", outputVariables.get("hesitant").getOutputValue());
            fuzzyResult.put("Spurious", outputVariables.get("spurious").getOutputValue());
            fuzzyResult.put("Deceptive", outputVariables.get("deceptive").getOutputValue());

            new FuzzyResultChart(i, personalityVector, fuzzyResult);
        }
    }
}
