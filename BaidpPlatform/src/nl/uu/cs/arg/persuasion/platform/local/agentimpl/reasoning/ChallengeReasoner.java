package nl.uu.cs.arg.persuasion.platform.local.agentimpl.reasoning;

import com.fuzzylite.variable.InputVariable;
import com.fuzzylite.variable.OutputVariable;
import nl.uu.cs.arg.persuasion.platform.local.agentimpl.attitudes.Attitude;
import nl.uu.cs.arg.persuasion.platform.local.agentimpl.attitudes.assertion.AssertionAttitude;
import nl.uu.cs.arg.persuasion.platform.local.agentimpl.attitudes.challenge.ChallengeAttitude;

import java.util.Map;

public class ChallengeReasoner extends Reasoner<ChallengeAttitude>
{

    public ChallengeReasoner() { this(0.2); }

    public ChallengeReasoner(double rho)
    {
        super("ChallengeReasoningEngine", rho, ChallengeAttitude.class);
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
        InputVariable angryhostility      = new InputVariable();
        InputVariable trust               = new InputVariable();

        this.inputVariables.put("achievementstriving", achievementstriving);
        this.inputVariables.put("deliberation",        deliberation);
        this.inputVariables.put("activity",            activity);
        this.inputVariables.put("straightforwardness", straightforwardness);
        this.inputVariables.put("modesty",             modesty);
        this.inputVariables.put("anxiety",             anxiety);
        this.inputVariables.put("angryhostility",      angryhostility);
        this.inputVariables.put("trust",               trust);
    }

    @Override
    protected void defineOutputVariables()
    {
        OutputVariable judicial = new OutputVariable();
        OutputVariable suspicious = new OutputVariable();
        OutputVariable persistent = new OutputVariable();
        OutputVariable tentative = new OutputVariable();
        OutputVariable indifferent = new OutputVariable();

        this.outputVariables.put("judicial",    judicial);
        this.outputVariables.put("suspicious",  suspicious);
        this.outputVariables.put("persistent",  persistent);
        this.outputVariables.put("tentative",   tentative);
        this.outputVariables.put("indifferent", indifferent);
    }

    @Override
    protected void defineRules()
    {
        String rules[] = {
            "if achievementstriving is high " +
                    "and deliberation is low " +
                    "and activity is high " +
                    "and trust is high " +
                    "and modesty is low " +
                    "and anxiety is low " +
                    "then judicial is favored",
            "if achievementstriving is high " +
                    "and deliberation is med " +
                    "and activity is high " +
                    "and trust is med " +
                    "and modesty is low " +
                    "and anxiety is low " +
                    "then suspicious is favored",
            "if achievementstriving is high " +
                    "and deliberation is high " +
                    "and activity is high " +
                    "and trust is low " +
                    "and modesty is low " +
                    "and anxiety is low " +
                    "then persistent is favored",
            "if deliberation is not low " +
                    "and trust is not high " +
                    "then judicial is disfavored",
            "if deliberation is not med " +
                    "and trust is not med " +
                    "then suspicious is disfavored",
            "if deliberation is not high " +
                    "and trust is not low " +
                    "then persistent is disfavored",
            "if achievementstriving is high " +
                    "and deliberation is low " +
                    "and activity is high " +
                    "and straightforwardness is low " +
                    "and modesty is low " +
                    "and anxiety is low " +
                    "and angryhostility is high " +
                    "then tentative is favored",
            "if achievementstriving is not high " +
                    "and deliberation is not low " +
                    "and straightforwardness is not low " +
                    "then tentative is disfavored",
            "if achievementstriving is low " +
                    "and deliberation is low " +
                    "and activity is low " +
                    "and straightforwardness is low " +
                    "and anxiety is high " +
                    "and angryhostility is high " +
                    "then indifferent is favored",
            "if achievementstriving is not low " +
                    "and deliberation is not low " +
                    "and straightforwardness is not low " +
                    "then indifferent is disfavored"
        };

        for (String rule : rules) {
            this.rules.add(rule);
        }
    }

    @Override
    protected String getSearchPath()
    {
        return "nl.uu.cs.arg.persuasion.platform.local.agentimpl.attitudes.challenge";
    }

}