package nl.uu.cs.arg.persuasion.platform.local.agentimpl.reasoning;

import com.fuzzylite.Engine;
import com.fuzzylite.defuzzifier.Centroid;
import com.fuzzylite.norm.s.Maximum;
import com.fuzzylite.norm.t.Minimum;
import com.fuzzylite.rule.Rule;
import com.fuzzylite.rule.RuleBlock;
import com.fuzzylite.term.Triangle;
import com.fuzzylite.variable.InputVariable;
import com.fuzzylite.variable.OutputVariable;
import javafx.util.Pair;
import nl.uu.cs.arg.persuasion.platform.local.agentimpl.ext.OwaRule;
import org.reflections.Reflections;

import java.util.*;

public abstract class Reasoner<T>
{

    protected Engine engine;

    // Input classes
    protected Triangle low;
    protected Triangle med;
    protected Triangle high;

    // Output classes
    protected Triangle disfavored;
    protected Triangle favored;

    protected Map<String, InputVariable> inputVariables;
    protected Map<String, OutputVariable> outputVariables;

    protected ArrayList<String> rules;

    protected ArrayList<OwaRule> owaRules;

    protected double rho;

    protected RuleBlock ruleBlock;

    private Class<T> typeClass;

    public Reasoner(String name, double rho, Class<T> typeClass)
    {
        this.typeClass = typeClass;

        this.rho = rho;

        this.inputVariables = new HashMap<>();
        this.outputVariables = new HashMap<>();

        this.rules = new ArrayList<>();
        this.owaRules = new ArrayList<>();

        this.ruleBlock = new RuleBlock();

        this.engine = new Engine(name);

        this.low  = new Triangle("low", -0.5, 0.0, 0.5);
        this.med  = new Triangle("med",  0.0, 0.5, 1.0);
        this.high = new Triangle("high", 0.5, 1.0, 1.5);

        this.disfavored  = new Triangle("disfavored", -1.0, 0.0, 1.0);
        this.favored     = new Triangle("favored",     0.0, 1.0, 2.0);

        this.defineInputVariables();
        this.defineOutputVariables();
        this.defineRules();

        this.addInputVariables();
        this.addOutputVariables();

        this.createRules();
        this.createRuleBlock();

        this.initEngine();
    }

    protected abstract void defineInputVariables();
    protected abstract void defineOutputVariables();
    protected abstract void defineRules();

    private void addInputVariables()
    {
        for (Map.Entry<String, InputVariable> entry : this.inputVariables.entrySet())
        {
            InputVariable variable = entry.getValue();
            variable.setName(entry.getKey());
            variable.setRange(0.000, 1.000);
            variable.addTerm(this.low);
            variable.addTerm(this.med);
            variable.addTerm(this.high);
            this.engine.addInputVariable(variable);
        }
    }

    private void addOutputVariables()
    {
        for (Map.Entry<String, OutputVariable> entry : this.outputVariables.entrySet()) {
            OutputVariable variable = entry.getValue();
            variable.setName(entry.getKey());
            variable.setRange(0.000, 1.000);
            variable.setDefaultValue(Double.NaN);
            variable.addTerm(this.disfavored);
            variable.addTerm(this.favored);
            variable.setDefuzzifier(new Centroid());
            variable.fuzzyOutput().setAccumulation(new Maximum());
            this.engine.addOutputVariable(variable);
        }
    }

    public void setFacets(Pair<String, Double>... facets)
    {
        for (Pair<String, Double> facet : facets) {
            if (this.inputVariables.containsKey(facet.getKey())) {
                this.inputVariables.get(facet.getKey()).setInputValue(facet.getValue());
            } else {
                throw new RuntimeException("Invalid input variable: " + facet.getKey());
            }
        }
    }

    private void createRules()
    {
        for (String rule : this.rules) {
            this.owaRules.add(OwaRule.parse(rule, this.engine, this.rho));
        }
    }

    private void createRuleBlock()
    {
        for (Rule rule : this.owaRules) {
            this.ruleBlock.addRule(rule);
        }

        this.ruleBlock.setConjunction(new Minimum());
        this.ruleBlock.setDisjunction(new Maximum());
        this.ruleBlock.setActivation(new Minimum());
        this.engine.addRuleBlock(this.ruleBlock);
    }

    private void initEngine()
    {
        this.engine.configure("Minimum", "Maximum", "Minimum", "Maximum", "Centroid");

        StringBuilder status = new StringBuilder();
        if (!this.engine.isReady(status))
        {
            throw new RuntimeException("Engine not ready. " + "The following errors were encountered:\n" + status.toString());
        }
    }

    protected void process()
    {
        this.engine.process();
    }

    protected abstract String getSearchPath();

    public void setPersonalityVector(HashMap<String, Double> personalityVector)
    {
        int i = 0;
        for (Map.Entry<String, Double> entry : personalityVector.entrySet()) {
            if (this.inputVariables.containsKey(entry.getKey())) {
                this.inputVariables.get(entry.getKey()).setInputValue(entry.getValue());
                ++i;
            }
        }

        if (i != this.inputVariables.size()) {
            int n = 0;
            String missing[] = new String[Math.abs(this.inputVariables.size() - i)];
            for (Map.Entry<String, InputVariable> entry : this.inputVariables.entrySet()) {
                if (!personalityVector.containsKey(entry.getKey())) {
                    missing[n++] = entry.getKey();
                }
            }

            throw new RuntimeException("Not all input variables are defined (" + Arrays.toString(missing) + ")!");
        }
    }

    public ArrayList<T> run()
    {
        this.process();

        Reflections reflections = new Reflections(this.getSearchPath());
        Set<Class<? extends T>> classes = reflections.getSubTypesOf(this.typeClass);

        // Sort
        HashMap<String, Double> map = new HashMap<>();
        for (Map.Entry<String, OutputVariable> outPair : this.outputVariables.entrySet()) {
            map.put(outPair.getKey(), outPair.getValue().getOutputValue());
        }
        TreeMap<String, Double> sortedMap = new TreeMap<>(new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                return map.get(o1) >= map.get(o2) ? -1 : 1;
            }
        });
        sortedMap.putAll(map);

        ArrayList<T> ordering = new ArrayList<>();
        try {
            for (Map.Entry<String, Double> entry : sortedMap.entrySet()) {
                for (Class<? extends T> c : classes) {
                    if (c.getSimpleName().toLowerCase().contains(entry.getKey())) {
                        System.out.println(c);
                        ordering.add((T) c.newInstance());
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Something went terribly wrong in here (" + this.typeClass + "): " + e);
        }

        return ordering;
    }

}
