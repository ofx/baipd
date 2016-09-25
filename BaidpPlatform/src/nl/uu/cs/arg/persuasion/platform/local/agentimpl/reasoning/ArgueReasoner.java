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

        // Cannot provide support for any own proposition
        OutputVariable devious    = new OutputVariable();

        // Can provide support for any own proposition for which he can construct an argument, in addition, the agent can provide support for any own proposition for which the agent can construct an argument for the contrary
        OutputVariable fallacious = new OutputVariable();

        this.outputVariables.put("hopeful",    hopeful);
        this.outputVariables.put("dubious",    dubious);
        this.outputVariables.put("thorough",   thorough);
        this.outputVariables.put("misleading", misleading);
        this.outputVariables.put("devious",    devious);
        this.outputVariables.put("fallacious", fallacious);
    }

    @Override
    protected void defineRules()
    {
        String rules[] = {
                // R1
                "if achievementstriving is high " +
                "and selfdiscipline is high " +
                "and deliberation is low " +
                "and activity is high " +
                "and straightforwardness is high " +
                "and anxiety is low " +
                "then hopeful is favored",
                // R2
                "if achievementstriving is high " +
                "and selfdiscipline is high " +
                "and deliberation is med " +
                "and activity is high " +
                "and straightforwardness is high " +
                "and anxiety is low " +
                "then dubious is favored",
                // R3
                "if achievementstriving is high " +
                "and selfdiscipline is high " +
                "and deliberation is high " +
                "and activity is high " +
                "and straightforwardness is high " +
                "and anxiety is low " +
                "then thorough is favored",
                // R4
                "if deliberation is not low " +
                "then hopeful is disfavored",
                // R5
                "if deliberation is not med " +
                "then dubious is disfavored",
                // R6
                "if deliberation is not high " +
                "then thorough is disfavored",
                // R7
                "if achievementstriving is high " +
                "and selfdiscipline is med " +
                "and deliberation is low " +
                "and activity is high " +
                "and straightforwardness is low " +
                "then misleading is favored",
                // R8
                "if deliberation is not low " +
                "and straightforwardness is not low " +
                "then misleading is disfavored",
                // R9
                "if achievementstriving is high " +
                "and selfdiscipline is med " +
                "and deliberation is not low " +
                "and activity is high " +
                "and straightforwardness is low " +
                "and angryhostility is high " +
                "then fallacious is favored",
                // R10
                "if deliberation is low " +
                "then fallacious is disfavored",
                // R11
                "if achievementstriving is low " +
                "and selfdiscipline is low " +
                "and deliberation is low " +
                "and activity is low " +
                "and anxiety is high " +
                "and modesty is high " +
                "then devious is favored",
                // R12
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