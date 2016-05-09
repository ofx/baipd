package nl.uu.cs.arg.persuasion.platform.local.agentimpl.reasoning;

import com.fuzzylite.variable.InputVariable;
import com.fuzzylite.variable.OutputVariable;
import nl.uu.cs.arg.persuasion.platform.local.agentimpl.attitudes.acceptance.AcceptanceAttitude;

public class AcceptanceReasoner extends Reasoner<AcceptanceAttitude>
{

    public AcceptanceReasoner() { this(0.2); }

    public AcceptanceReasoner(double rho)
    {
        super("AcceptanceReasoningEngine", rho, AcceptanceAttitude.class);
    }

    @Override
    protected void defineInputVariables()
    {
        InputVariable achievementstriving = new InputVariable();
        InputVariable deliberation        = new InputVariable();
        InputVariable activity            = new InputVariable();
        InputVariable trust               = new InputVariable();
        InputVariable modesty             = new InputVariable();
        InputVariable anxiety             = new InputVariable();
        InputVariable angryhostility      = new InputVariable();
        InputVariable straightforwardness = new InputVariable();

        this.inputVariables.put("achievementstriving", achievementstriving);
        this.inputVariables.put("deliberation",        deliberation);
        this.inputVariables.put("activity",            activity);
        this.inputVariables.put("trust",               trust);
        this.inputVariables.put("modesty",             modesty);
        this.inputVariables.put("anxiety",             anxiety);
        this.inputVariables.put("angryhostility",      angryhostility);
        this.inputVariables.put("straightforwardness", straightforwardness);
    }

    @Override
    protected void defineOutputVariables()
    {
        // Accepts iff the agent can construct an argument
        OutputVariable credulous = new OutputVariable();

        // Accepts iff the agent can construct a stronger argument
        OutputVariable cautious  = new OutputVariable();

        // Accepts iff the agent can construct a justified argument
        OutputVariable skeptical = new OutputVariable();

        // Accepts regardless if the agent can construct an argument
        OutputVariable obedient  = new OutputVariable();

        // Never accepts
        OutputVariable rigid     = new OutputVariable();

        this.outputVariables.put("credulous", credulous);
        this.outputVariables.put("cautious",  cautious);
        this.outputVariables.put("skeptical", skeptical);
        this.outputVariables.put("obedient",  obedient);
        this.outputVariables.put("rigid",     rigid);
    }

    @Override
    protected void defineRules()
    {
        String rules[] = {
            "if achievementstriving is low " +
                "and deliberation is low " +
                "and trust is high " +
                "and modesty is not low " +
                "and activity is high " +
                "then credulous is favored",
            "if achievementstriving is med " +
                "and deliberation is med " +
                "and trust is med " +
                "and modesty is not low " +
                "and activity is high " +
                "then cautious is favored",
            "if achievementstriving is high " +
                "and deliberation is high " +
                "and trust is low " +
                "and modesty is not low " +
                "and activity is high " +
                "then skeptical is favored",
            "if achievementstriving is not low " +
                "and deliberation is not low " +
                "and trust is not high " +
                "then credulous is disfavored",
            "if achievementstriving is not med " +
                "and deliberation is not med " +
                "and trust is not med " +
                "then cautious is disfavored",
            "if achievementstriving is not high " +
                "and deliberation is not high " +
                "and trust is not low " +
                "then skeptical is disfavored",
            "if achievementstriving is low " +
                "and activity is high " +
                "and trust is high " +
                "and modesty is high " +
                "and anxiety is high " +
                "then obedient is favored",
            "if achievementstriving is not low " +
                "and trust is not high " +
                "and modesty is not high " +
                "then obedient is disfavored",
            "if achievementstriving is high " +
                "and activity is low " +
                "and trust is low " +
                "and straightforwardness is low " +
                "and modesty is low " +
                "and angryhostility is high " +
                "then rigid is favored",
            "if achievementstriving is not high " +
                "and trust is not low " +
                "and straightforwardness is not low " +
                "and modesty is not low " +
                "then rigid is disfavored"
        };

        for (String rule : rules) {
            this.rules.add(rule);
        }
    }

    @Override
    protected String getSearchPath()
    {
        return "nl.uu.cs.arg.persuasion.platform.local.agentimpl.attitudes.acceptance";
    }

}