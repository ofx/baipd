package nl.uu.cs.arg.persuasion.platform.local.agentimpl.reasoning;

import com.fuzzylite.variable.InputVariable;
import com.fuzzylite.variable.OutputVariable;
import nl.uu.cs.arg.persuasion.platform.local.agentimpl.attitudes.assertion.AssertionAttitude;

public class AssertionReasoner extends Reasoner<AssertionAttitude>
{

    public AssertionReasoner() { this(0.2); }

    public AssertionReasoner(double rho)
    {
        super("AssertionReasoningEngine", rho, AssertionAttitude.class);
    }

    @Override
    protected void defineInputVariables()
    {
        InputVariable achievementstriving = new InputVariable();
        InputVariable selfdiscipline      = new InputVariable();
        InputVariable deliberation        = new InputVariable();
        InputVariable activity            = new InputVariable();
        InputVariable straightforwardness = new InputVariable();
        InputVariable modesty             = new InputVariable();
        InputVariable anxiety             = new InputVariable();
        InputVariable depression          = new InputVariable();
        InputVariable angryhostility      = new InputVariable();

        this.inputVariables.put("achievementstriving", achievementstriving);
        this.inputVariables.put("selfdiscipline",      selfdiscipline);
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
        OutputVariable thoughtful = new OutputVariable();
        OutputVariable careful    = new OutputVariable();
        OutputVariable confident  = new OutputVariable();
        OutputVariable spurious   = new OutputVariable();
        OutputVariable hesitant   = new OutputVariable();
        OutputVariable deceptive   = new OutputVariable();

        this.outputVariables.put("thoughtful", thoughtful);
        this.outputVariables.put("careful",    careful);
        this.outputVariables.put("confident",  confident);
        this.outputVariables.put("spurious",   spurious);
        this.outputVariables.put("hesitant",   hesitant);
        this.outputVariables.put("deceptive",  deceptive);
    }

    @Override
    protected void defineRules()
    {
        String rules[] = {
                // R1
                "if achievementstriving is high " +
                "and selfdiscipline is high " +
                "and straightforwardness is high " +
                "and modesty is low " +
                "and anxiety is low " +
                "and activity is high " +
                "and deliberation is high " +
                "then thoughtful is favored",
                // R2
                "if achievementstriving is high " +
                "and selfdiscipline is high " +
                "and straightforwardness is high " +
                "and modesty is low " +
                "and anxiety is low " +
                "and activity is high " +
                "and deliberation is med " +
                "then careful is favored",
                // R3
                "if achievementstriving is high " +
                "and selfdiscipline is high " +
                "and straightforwardness is high " +
                "and modesty is low " +
                "and anxiety is low " +
                "and activity is high " +
                "and deliberation is low " +
                "then confident is favored",
                // R4
                "if deliberation is not high " +
                "then thoughtful is disfavored",
                // R5
                "if deliberation is not med " +
                "then careful is disfavored",
                // R6
                "if deliberation is not low " +
                "then confident is disfavored",
                // R7
                "if deliberation is low " +
                "and straightforwardness is low " +
                "and selfdiscipline is low " +
                "and achievementstriving is low " +
                "and activity is low " +
                "and modesty is high " +
                "and anxiety is high " +
                "then hesitant is favored",
                // R8
                "if activity is not low " +
                "and selfdiscipline is not low " +
                "and achievementstriving is not low " +
                "then hesitant is disfavored",
                // R9
                "if straightforwardness is low " +
                "and deliberation is low " +
                "and selfdiscipline is not low " +
                "and achievementstriving is high " +
                "then spurious is favored",
                // R10
                "if deliberation is not low " +
                "then spurious is disfavored",
                // R11
                "if straightforwardness is low " +
                "and deliberation is not low " +
                "and selfdiscipline is not low " +
                "and achievementstriving is high " +
                "and angryhostility is high " +
                "then deceptive is favored",
                // R12
                "if deliberation is low " +
                "then deceptive is disfavored"

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
