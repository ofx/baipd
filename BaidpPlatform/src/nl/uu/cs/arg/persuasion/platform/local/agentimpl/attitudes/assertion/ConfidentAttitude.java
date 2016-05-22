package nl.uu.cs.arg.persuasion.platform.local.agentimpl.attitudes.assertion;

import nl.uu.cs.arg.persuasion.platform.local.agentimpl.PersuadingAgent;
import nl.uu.cs.arg.shared.dialogue.Move;
import nl.uu.cs.arg.shared.dialogue.locutions.Locution;
import org.aspic.inference.Constant;
import org.aspic.inference.ReasonerException;
import org.aspic.inference.parser.ParseException;

import java.util.List;

public class ConfidentAttitude extends AssertionAttitude
{

    @Override
    public List<Move<? extends Locution>> generateMoves(PersuadingAgent agent)
    {
        List<Constant> options = null;
        try
        {
            options = agent.generateOptions();
        }
        catch (ParseException | ReasonerException ex)
        {
            // Oops...
        }

        return null;
    }

}
