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

public class Main
{
    public static void main(String[] args)
    {
        Engine engine = new Engine();
        engine.setName("simple-dimmer");

        // Input membership distributions
        Triangle low  = new Triangle("low", -0.5, 0.0, 0.5);
        Triangle med  = new Triangle("med",  0.0, 0.5, 1.0);
        Triangle high = new Triangle("high", 0.5, 1.0, 1.5);

        // Output membership distributions
        Triangle favored    = new Triangle("favored", 0.0, 1.0, 2.0);

        Map<String, InputVariable> inputVariables   = new HashMap<String, InputVariable>();
        Map<String, OutputVariable> outputVariables = new HashMap<String, OutputVariable>();

        // Create input variables
        {
            InputVariable achievementstriving = new InputVariable();
            InputVariable dutifulness = new InputVariable();
            InputVariable selfdiscipline = new InputVariable();
            InputVariable deliberation = new InputVariable();
            InputVariable activity = new InputVariable();
            InputVariable straightforwardness = new InputVariable();
            InputVariable modesty = new InputVariable();
            InputVariable anxiety = new InputVariable();
            InputVariable impulsiveness = new InputVariable();

            inputVariables.put("achievementstriving", achievementstriving);
            inputVariables.put("dutifulness",         dutifulness);
            inputVariables.put("selfdiscipline",      selfdiscipline);
            inputVariables.put("deliberation",        deliberation);
            inputVariables.put("activity",            activity);
            inputVariables.put("straightforwardness", straightforwardness);
            inputVariables.put("modesty",             modesty);
            inputVariables.put("anxiety",             anxiety);
            inputVariables.put("impulsiveness",       impulsiveness);

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
            //outputVariables.put("careful",    careful);
            //outputVariables.put("confident",  confident);
            //outputVariables.put("spurious",   spurious);
            //outputVariables.put("deceptive",  deceptive);
            //outputVariables.put("hesitant",   hesitant);

            for (Map.Entry<String, OutputVariable> entry : outputVariables.entrySet()) {
                OutputVariable variable = entry.getValue();
                variable.setName(entry.getKey());
                variable.setRange(0.000, 1.000);
                variable.setDefaultValue(Double.NaN);
                variable.addTerm(favored);
                //variable.addTerm(disfavored);
                variable.setDefuzzifier(new Centroid());
                variable.fuzzyOutput().setAccumulation(new Maximum());
                engine.addOutputVariable(variable);
            }
        }

        OwaRule rule = OwaRule.parse("if achievementstriving is high" +
                                     " and dutifulness is high" +
                                     " and selfdiscipline is high" +
                                     " and deliberation is high" +
                                     " and activity is high" +
                                     " and straightforwardness is high" +
                                     " and modesty is low" +
                                     " and anxiety is low" +
                                     " and impulsiveness is low" +
                                     " then thoughtful is favored", engine, 1.0);
        RuleBlock ruleBlock = new RuleBlock();
        ruleBlock.addRule(rule);
        ruleBlock.setConjunction(new Minimum());
        ruleBlock.setDisjunction(new Maximum());
        ruleBlock.setActivation(new Minimum());
        engine.addRuleBlock(ruleBlock);

        engine.configure("Minimum", "Maximum", "Minimum", "Maximum", "Centroid");

        StringBuilder status = new StringBuilder();
        if (!engine.isReady(status))
        {
            throw new RuntimeException("Engine not ready. " + "The following errors were encountered:\n" + status.toString());
        }

        inputVariables.get("achievementstriving").setInputValue(0.8);
        inputVariables.get("selfdiscipline").setInputValue(0.35);
        inputVariables.get("deliberation").setInputValue(0.4);
        inputVariables.get("impulsiveness").setInputValue(0.3);
        inputVariables.get("anxiety").setInputValue(0.7);
        inputVariables.get("activity").setInputValue(0.5);
        inputVariables.get("straightforwardness").setInputValue(0.6);
        inputVariables.get("dutifulness").setInputValue(0.25);
        inputVariables.get("modesty").setInputValue(0.65);

        for (int i = 0; i < 50; ++i)
        {
            double rho = i * (1.0 / 50.0);
            rule.setAndness(rho);

            engine.process();
            FuzzyLite.logger().info(String.format("Andness = %s -> Thoughtful = %s", Op.str(rho), outputVariables.get("thoughtful").getOutputValue()));
        }
    }
}
