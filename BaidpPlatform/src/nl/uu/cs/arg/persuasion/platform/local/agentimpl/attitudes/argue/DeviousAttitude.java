package nl.uu.cs.arg.persuasion.platform.local.agentimpl.attitudes.argue;

import nl.uu.cs.arg.persuasion.model.dialogue.PersuasionDialogueException;
import nl.uu.cs.arg.persuasion.platform.local.agentimpl.PersuadingAgent;
import nl.uu.cs.arg.shared.dialogue.locutions.Locution;
import nl.uu.cs.arg.persuasion.model.dialogue.PersuasionDialogue;
import nl.uu.cs.arg.persuasion.model.dialogue.PersuasionMove;
import org.aspic.inference.ReasonerException;
import org.aspic.inference.parser.ParseException;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by argon on 21-5-16.
 */
public class DeviousAttitude extends ArgueAttitude
{

    @Override
    public List<PersuasionMove<? extends Locution>> generateMoves(PersuadingAgent agent, PersuasionDialogue dialogue) throws PersuasionDialogueException, ParseException, ReasonerException
    {
        List<PersuasionMove<? extends Locution>> moves = new LinkedList<>();

        // Never argue

        return moves;
    }

}
