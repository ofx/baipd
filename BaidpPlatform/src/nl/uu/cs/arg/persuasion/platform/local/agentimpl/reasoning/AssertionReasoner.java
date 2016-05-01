package nl.uu.cs.arg.persuasion.platform.local.agentimpl.reasoning;

import com.fuzzylite.variable.InputVariable;
import com.fuzzylite.variable.OutputVariable;
import nl.uu.cs.arg.persuasion.platform.local.agentimpl.attitudes.assertion.AssertionAttitude;

public class AssertionReasoner extends Reasoner<AssertionAttitude>
{

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
        InputVariable impulsiveness       = new InputVariable();
        InputVariable depression          = new InputVariable();
        InputVariable angryhostility      = new InputVariable();

        this.inputVariables.put("achievementstriving", achievementstriving);
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
        OutputVariable thoughtful = new OutputVariable();
        OutputVariable careful    = new OutputVariable();
        OutputVariable confident  = new OutputVariable();
        OutputVariable spurious   = new OutputVariable();
        OutputVariable deceptive  = new OutputVariable();
        OutputVariable hesitant   = new OutputVariable();

        this.outputVariables.put("thoughtful", thoughtful);
        this.outputVariables.put("careful",    careful);
        this.outputVariables.put("confident",  confident);
        this.outputVariables.put("spurious",   spurious);
        this.outputVariables.put("deceptive",  deceptive);
        this.outputVariables.put("hesitant",   hesitant);
    }

    @Override
    protected void defineRules()
    {
        String rules[] = {
                /*
                If the agent is achievement striving, dutiful, straightforward, not anxious and not modest, the agent
                would prefer to make a claim supported by an argument the agent can construct.
                 */
                "if achievementstriving is high " +
                        "and selfdiscipline is high " +
                        "and straightforwardness is high " +
                        "and modesty is low " +
                        "and anxiety is low " +
                        "and activity is high " +
                        "and deliberation is high " +
                        "and impulsiveness is low " +
                        "then thoughtful is favored",
                "if achievementstriving is high " +
                        "and selfdiscipline is high " +
                        "and straightforwardness is high " +
                        "and modesty is low " +
                        "and anxiety is low " +
                        "and activity is high " +
                        "and deliberation is med " +
                        "and impulsiveness is med " +
                        "then careful is favored",
                "if achievementstriving is high " +
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
                        "and selfdiscipline is low " +
                        "then spurious is favored",
                "if selfdiscipline is not low " +
                        "then spurious is disfavored",
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
