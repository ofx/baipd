package nl.uu.cs.arg.persuasion.platform.local.agentimpl.reasoning;

import com.fuzzylite.variable.InputVariable;
import com.fuzzylite.variable.OutputVariable;
import nl.uu.cs.arg.persuasion.platform.local.agentimpl.attitudes.Attitude;
import nl.uu.cs.arg.persuasion.platform.local.agentimpl.attitudes.acceptance.AcceptanceAttitude;
import nl.uu.cs.arg.persuasion.platform.local.agentimpl.attitudes.assertion.AssertionAttitude;
import nl.uu.cs.arg.persuasion.platform.local.agentimpl.attitudes.retraction.RetractionAttitude;

import java.util.Map;

public class ActionSelectionReasoner extends Reasoner<Reasoner>
{

    public ActionSelectionReasoner(double rho)
    {
        super("ActionSelectionEngine", rho, Reasoner.class);
    }

    @Override
    protected void defineInputVariables()
    {
        InputVariable selfconsciousness = new InputVariable();
        InputVariable assertiveness     = new InputVariable();
        InputVariable actions           = new InputVariable();
        InputVariable ideas             = new InputVariable();
        InputVariable values            = new InputVariable();
        InputVariable competence        = new InputVariable();

        this.inputVariables.put("selfconsciousness", selfconsciousness);
        this.inputVariables.put("assertiveness",     assertiveness);
        this.inputVariables.put("actions",           actions);
        this.inputVariables.put("ideas",             ideas);
        this.inputVariables.put("values",            values);
        this.inputVariables.put("competence",        competence);
    }

    @Override
    protected void defineOutputVariables()
    {
        OutputVariable acceptance = new OutputVariable();
        OutputVariable assertion  = new OutputVariable();
        OutputVariable challenge  = new OutputVariable();
        OutputVariable retraction = new OutputVariable();

        this.outputVariables.put("acceptance", acceptance);
        this.outputVariables.put("assertion",  assertion);
        this.outputVariables.put("challenge",  challenge);
        this.outputVariables.put("retraction", retraction);
    }

    @Override
    protected void defineRules()
    {
        String rules[] = {
                "if actions is high " +
                        "or selfconsciousness is high " +
                        "then acceptance is favored",
                "if ideas is high " +
                        "then challenge is favored",
                "if values is high " +
                        "then retraction is favored",
                "if competence is high " +
                        "then retraction is disfavored",
                "if competence is high " +
                        "then acceptance is disfavored",
                "if selfconsciousness is high " +
                        "then assertion is disfavored",
                "if assertiveness is high " +
                        "then assertion is favored"
        };

        for (String rule : rules) {
            this.rules.add(rule);
        }
    }

    @Override
    protected String getSearchPath()
    {
        return "nl.uu.cs.arg.persuasion.platform.local.agentimpl.reasoning";
    }

}