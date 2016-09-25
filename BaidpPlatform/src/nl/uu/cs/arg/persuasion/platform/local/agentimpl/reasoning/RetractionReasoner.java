package nl.uu.cs.arg.persuasion.platform.local.agentimpl.reasoning;

import com.fuzzylite.variable.InputVariable;
import com.fuzzylite.variable.OutputVariable;
import nl.uu.cs.arg.persuasion.platform.local.agentimpl.attitudes.Attitude;
import nl.uu.cs.arg.persuasion.platform.local.agentimpl.attitudes.acceptance.AcceptanceAttitude;
import nl.uu.cs.arg.persuasion.platform.local.agentimpl.attitudes.assertion.AssertionAttitude;
import nl.uu.cs.arg.persuasion.platform.local.agentimpl.attitudes.retraction.RetractionAttitude;

import java.util.Map;

public class RetractionReasoner extends Reasoner<RetractionAttitude>
{

    public RetractionReasoner() { this(0.2); }

    public RetractionReasoner(double rho)
    {
        super("RetractionReasoningEngine", rho, RetractionAttitude.class);
    }

    @Override
    protected void defineInputVariables()
    {
        InputVariable achievementstriving = new InputVariable();
        InputVariable deliberation        = new InputVariable();
        InputVariable activity            = new InputVariable();
        InputVariable straightforwardness = new InputVariable();
        InputVariable modesty             = new InputVariable();
        InputVariable anxiety             = new InputVariable();
        InputVariable depression          = new InputVariable();
        InputVariable angryhostility      = new InputVariable();

        this.inputVariables.put("achievementstriving", achievementstriving);
        this.inputVariables.put("deliberation",        deliberation);
        this.inputVariables.put("activity",            activity);
        this.inputVariables.put("straightforwardness", straightforwardness);
        this.inputVariables.put("modesty",             modesty);
        this.inputVariables.put("anxiety",             anxiety);
        this.inputVariables.put("depression",          depression);
        this.inputVariables.put("angryhostility",      angryhostility);
    }

    @Override
    protected void defineOutputVariables()
    {
        OutputVariable regretful = new OutputVariable();
        OutputVariable sensible = new OutputVariable();
        OutputVariable retentive = new OutputVariable();
        OutputVariable incongruous = new OutputVariable();
        OutputVariable determined = new OutputVariable();

        this.outputVariables.put("regretful",   regretful);
        this.outputVariables.put("sensible",    sensible);
        this.outputVariables.put("retentive",   retentive);
        this.outputVariables.put("incongruous", incongruous);
        this.outputVariables.put("determined",  determined);
    }

    @Override
    protected void defineRules()
    {
        String rules[] = {
                // R1
                "if achievementstriving is low " +
                "and deliberation is low " +
                "and activity is high " +
                "and modesty is not low " +
                "then regretful is favored",
                // R2
                "if achievementstriving is med " +
                "and deliberation is med " +
                "and activity is high " +
                "and modesty is not low " +
                "then sensible is favored",
                // R3
                "if achievementstriving is high " +
                "and deliberation is high " +
                "and activity is high " +
                "and modesty is not low " +
                "then retentive is favored",
                // R4
                "if achievementstriving is not low " +
                "and deliberation is not low " +
                "then regretful is disfavored",
                // R5
                "if achievementstriving is not med " +
                "and deliberation is not med " +
                "then sensible is disfavored",
                // R6
                "if achievementstriving is not high " +
                "and deliberation is not high " +
                "then retentive is disfavored",
                // R7
                "if achievementstriving is low " +
                "and deliberation is low " +
                "and activity is high " +
                "and modesty is high " +
                "and anxiety is high " +
                "and depression is high " +
                "then incongruous is favored",
                // R8
                "if achievementstriving is not low " +
                "and deliberation is not low " +
                "and modesty is not high " +
                "then incongruous is disfavored",
                // R9
                "if achievementstriving is high " +
                "and deliberation is low " +
                "and activity is low " +
                "and straightforwardness is low " +
                "and modesty is low " +
                "and angryhostility is high " +
                "then determined is favored",
                // R10
                "if achievementstriving is not high " +
                "and deliberation is not low " +
                "and modesty is not low " +
                "then determined is disfavored"
        };

        for (String rule : rules) {
            this.rules.add(rule);
        }
    }

    @Override
    protected String getSearchPath()
    {
        return "nl.uu.cs.arg.persuasion.platform.local.agentimpl.attitudes.retraction";
    }

}