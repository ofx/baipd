package nl.uu.cs.arg.persuasion.platform.local.agentimpl.reasoning;

import com.fuzzylite.variable.InputVariable;
import com.fuzzylite.variable.OutputVariable;
import nl.uu.cs.arg.persuasion.platform.local.agentimpl.attitudes.acceptance.AcceptanceAttitude;

import java.util.Map;

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
        InputVariable impulsiveness       = new InputVariable();

        this.inputVariables.put("achievementstriving", achievementstriving);
        this.inputVariables.put("deliberation",        deliberation);
        this.inputVariables.put("activity",            activity);
        this.inputVariables.put("trust",               trust);
        this.inputVariables.put("modesty",             modesty);
        this.inputVariables.put("anxiety",             anxiety);
        this.inputVariables.put("angryhostility",      angryhostility);
        this.inputVariables.put("impulsiveness",       impulsiveness);
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
                The agent is more likely to be credulous if the agent is achievement striving, since an achievement
                striving agent will prefer to achieve its personal goal. By accepting a proposition by the opponent, the
                agent is not contributing towards its personal goal. Being credulous is not considered deliberate, so a
                low-keyed deliberation facet indicates a preference for a credulous attitude. A trusting agent is more likely
                to select a credulous attitude, since the move is not well-motivated, and the opponent is able to lie.
                The agent can not be unmodest, since this will disallow the agent to accept a proposition. The credulous
                attitude is considered to be an impulsive move.
             */
            "if achievementstriving is high " +
                    "and deliberation is low " +
                    "and trust is high " +
                    "and modesty is not low " +
                    "and impulsiveness is high " +
                    "and activity is high " +
                    "and (anxiety is not low or angryhostility is not low) " +
                    "then credulous is favored",
            "if activity is high" +
                    "and (anxiety is not low or angryhostility is not low)" +
                    "then cautious is favored",
            "if activity is high" +
                    "and (anxiety is not low or angryhostility is not low)" +
                    "then skeptical is favored",
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