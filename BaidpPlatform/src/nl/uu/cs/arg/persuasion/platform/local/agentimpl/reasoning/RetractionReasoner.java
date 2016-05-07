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

    public RetractionReasoner(double rho)
    {
        super("RetractionReasoningEngine", rho, RetractionAttitude.class);
    }

    @Override
    protected void defineInputVariables()
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

        this.inputVariables.put("achievementstriving", achievementstriving);
        this.inputVariables.put("dutifulness",         dutifulness);
        this.inputVariables.put("selfdiscipline",      selfdiscipline);
        this.inputVariables.put("deliberation",        deliberation);
        this.inputVariables.put("activity",            activity);
        this.inputVariables.put("straightforwardness", straightforwardness);
        this.inputVariables.put("modesty",             modesty);
        this.inputVariables.put("anxiety",             anxiety);
        this.inputVariables.put("impulsiveness",       impulsiveness);
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
            "if achievementstriving is low " +
                    "and deliberation is low " +
                    "and activity is high " +
                    "and modesty is high " +
                    "and impulsiveness is high " +
                    "then regretful is favored",
            "if achievementstriving is med " +
                    "and deliberation is med " +
                    "and activity is high " +
                    "and modesty is high " +
                    "and impulsiveness is med " +
                    "then sensible is favored",
            "if achievementstriving is high " +
                    "and deliberation is high " +
                    "and activity is high " +
                    "and modesty is high " +
                    "and impulsiveness is low " +
                    "then retentive is favored",
            "if achievementstriving is not low " +
                    "and deliberation is not low " +
                    "and impulsiveness is not high " +
                    "then regretful is disfavored",
            "if achievementstriving is not med " +
                    "and deliberation is not med " +
                    "and impulsiveness is not med " +
                    "then sensible is disfavored",
            "if achievementstriving is not high " +
                    "and deliberation is not high " +
                    "and impulsiveness is not low " +
                    "then retentive is disfavored",
            // TODO: Incongruous / Determined
        };

        for (String rule : rules) {
            this.rules.add(rule);
        }
    }

    @Override
    protected String getSearchPath()
    {
        return "nl.uu.cs.arg.persuasion.platform.local.agentimpl.attitudes.assertion";
    }

}