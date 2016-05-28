package nl.uu.cs.arg.persuasion.platform.local.agentimpl.reasoning;

import com.fuzzylite.variable.InputVariable;
import com.fuzzylite.variable.OutputVariable;
import nl.uu.cs.arg.persuasion.platform.local.agentimpl.attitudes.acceptance.AcceptanceAttitude;
import nl.uu.cs.arg.persuasion.platform.local.agentimpl.attitudes.argue.ArgueAttitude;

public class ArgueReasoner extends Reasoner<ArgueAttitude>
{

    public ArgueReasoner() { this(0.2); }

    public ArgueReasoner(double rho)
    {
        super("ArgueReasoningEngine", rho, ArgueAttitude.class);
    }

    @Override
    protected void defineInputVariables()
    {
        InputVariable achievementstriving = new InputVariable();
        InputVariable deliberation        = new InputVariable();
        InputVariable activity            = new InputVariable();
        InputVariable modesty             = new InputVariable();
        InputVariable anxiety             = new InputVariable();
        InputVariable angryhostility      = new InputVariable();
        InputVariable straightforwardness = new InputVariable();
        InputVariable selfdiscipline      = new InputVariable();

        this.inputVariables.put("achievementstriving", achievementstriving);
        this.inputVariables.put("deliberation",        deliberation);
        this.inputVariables.put("activity",            activity);
        this.inputVariables.put("modesty",             modesty);
        this.inputVariables.put("anxiety",             anxiety);
        this.inputVariables.put("angryhostility",      angryhostility);
        this.inputVariables.put("straightforwardness", straightforwardness);
        this.inputVariables.put("selfdiscipline",      selfdiscipline);
    }

    @Override
    protected void defineOutputVariables()
    {
        // Provide support for any own proposition for which he can construct an argument
        OutputVariable hopeful    = new OutputVariable();

        // Provide support for any own proposition for which he can construct an argument and no stronger argument for the contrary
        OutputVariable dubious    = new OutputVariable();

        // Provide support for any own proposition for which he can construct a justified argument
        OutputVariable thorough   = new OutputVariable();

        // Provide support for any own proposition, regardless if he can construct an argument
        OutputVariable misleading = new OutputVariable();

        // Provide support for any own proposition for which he can construct an argument, in addition, the agent can provide support for any own proposition for which the agent can construct an argument for the contrary
        OutputVariable fallacious = new OutputVariable();

        // Cannot provide support for any own proposition
        OutputVariable devious    = new OutputVariable();

        this.outputVariables.put("hopeful",    hopeful);
        this.outputVariables.put("dubious",    dubious);
        this.outputVariables.put("thorough",   thorough);
        this.outputVariables.put("misleading", misleading);
        this.outputVariables.put("fallacious", fallacious);
        this.outputVariables.put("devious",    devious);
    }

    @Override
    protected void defineRules()
    {
        String rules[] = {
            "if achievementstriving is high " +
                    "and selfdiscipline is high " +
                    "and deliberation is low " +
                    "and activity is high " +
                    "and straightforwardness is high " +
                    "and anxiety is low " +
                    "then hopeful is favored",
            "if achievementstriving is high " +
                    "and selfdiscipline is high " +
                    "and deliberation is med " +
                    "and activity is high " +
                    "and straightforwardness is high " +
                    "and anxiety is low " +
                    "then dubious is favored",
            "if achievementstriving is high " +
                    "and selfdiscipline is high " +
                    "and deliberation is high " +
                    "and activity is high " +
                    "and straightforwardness is high " +
                    "and anxiety is low " +
                    "then thorough is favored",
            "if deliberation is not low " +
                    "then hopeful is disfavored",
            "if deliberation is not med " +
                    "then dubious is disfavored",
            "if deliberation is not high " +
                    "then thorough is disfavored",
            "if achievementstriving is high " +
                    "and selfdiscipline is med " +
                    "and deliberation is low " +
                    "and activity is high " +
                    "and straightforwardness is low " +
                    "then misleading is favored",
            "if deliberation is not low " +
                    "and straightforwardness is not low " +
                    "then misleading is disfavored",
            "if achievementstriving is high " +
                    "and selfdiscipline is med " +
                    "and deliberation is not low " +
                    "and activity is high " +
                    "and straightforwardness is low " +
                    "and angryhostility is high " +
                    "then fallacious is favored",
            "if deliberation is low " +
                    "then fallacious is disfavored",
            "if achievementstriving is low " +
                    "and selfdiscipline is low " +
                    "and deliberation is low " +
                    "and activity is low " +
                    "and anxiety is high " +
                    "and modesty is high " +
                    "then devious is favored",
            "if selfdiscipline is not low " +
                    "and deliberation is not low " +
                    "and activity is not low " +
                    "then devious is disfavored"
        };

        for (String rule : rules) {
            this.rules.add(rule);
        }
    }

    @Override
    protected String getSearchPath()
    {
        return "nl.uu.cs.arg.persuasion.platform.local.agentimpl.attitudes.argue";
    }

}