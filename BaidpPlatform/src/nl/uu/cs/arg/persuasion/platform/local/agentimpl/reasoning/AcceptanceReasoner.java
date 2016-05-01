package nl.uu.cs.arg.persuasion.platform.local.agentimpl.reasoning;

import com.fuzzylite.variable.InputVariable;
import com.fuzzylite.variable.OutputVariable;
import nl.uu.cs.arg.persuasion.platform.local.agentimpl.attitudes.acceptance.AcceptanceAttitude;

public class AcceptanceReasoner extends Reasoner<AcceptanceAttitude>
{

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
            /*
                The agent is more likely to be credulous if the agent is not achievement striving, since an achievement
                striving agent will prefer to achieve its personal goal. By accepting a proposition by the opponent, the
                agent is not contributing towards its personal goal. Being credulous is not considered deliberate, so a
                low-keyed deliberation facet indicates a preference for a credulous attitude. A trusting agent is more likely
                to select a credulous attitude, since the move is not well-motivated, and the opponent is able to lie.
                The agent can not be immodest, since this will disallow the agent to accept a proposition. The credulous
                attitude is considered to be an impulsive move.
             */
            "if achievementstriving is not high " +
                    "and deliberation is low " +
                    "and trust is high " +
                    "and modesty is not low " +
                    "and activity is high " +
                    //"or anxiety is not low " +
                    "then credulous is favored",
            /*
                Same holds for a cautious attitude, except for trust being med, deliberation being med and impulsiveness
                being med.
             */
            "if achievementstriving is not high " +
                    "and deliberation is med " +
                    "and trust is med " +
                    "and modesty is not low " +
                    "and activity is high " +
                    //"or anxiety is not low " +
                    "then cautious is favored",
            /*
                Same holds for a skeptical attitude, except for trust being low, deliberation being high and impulsiveness
                being low.
             */
            "if achievementstriving is not high " +
                    "and deliberation is high " +
                    "and trust is low " +
                    "and modesty is not low " +
                    "and activity is high " +
                    //"or anxiety is not low " +
                    "then skeptical is favored",

            "if deliberation is not low " +
                    "and trust is not high " +
                    "then credulous is disfavored",
            "if deliberation is not med " +
                    "and trust is not med " +
                    "then cautious is disfavored",
            "if deliberation is not high " +
                    "and trust is not low " +
                    "then skeptical is disfavored",

            "if achievementstriving is low " +
                    "and activity is high " +
                    "and trust is high " +
                    "and modesty is high " +
                    "or anxiety is high " +
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
                    "or angryhostility is high " +
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