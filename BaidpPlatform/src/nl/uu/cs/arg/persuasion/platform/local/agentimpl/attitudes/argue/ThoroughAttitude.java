package nl.uu.cs.arg.persuasion.platform.local.agentimpl.attitudes.argue;

import nl.uu.cs.arg.persuasion.platform.local.agentimpl.PersuadingAgent;
import nl.uu.cs.arg.shared.dialogue.locutions.Locution;
import nl.uu.cs.arg.persuasion.model.dialogue.PersuasionDialogue;
import nl.uu.cs.arg.persuasion.model.dialogue.PersuasionMove;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by argon on 21-5-16.
 */
public class ThoroughAttitude extends ArgueAttitude
{

    @Override
    public List<PersuasionMove<? extends Locution>> generateMoves(PersuadingAgent agent, PersuasionDialogue dialogue)
    {
        List<PersuasionMove<? extends Locution>> moves = new LinkedList<>();

        return moves;
    }

}