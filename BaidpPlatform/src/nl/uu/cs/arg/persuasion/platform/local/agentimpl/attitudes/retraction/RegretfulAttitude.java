package nl.uu.cs.arg.persuasion.platform.local.agentimpl.attitudes.retraction;

import nl.uu.cs.arg.persuasion.platform.local.agentimpl.PersuadingAgent;
import nl.uu.cs.arg.shared.dialogue.Move;
import nl.uu.cs.arg.shared.dialogue.locutions.Locution;

import java.util.List;
import nl.uu.cs.arg.persuasion.model.dialogue.PersuasionDialogue;
import nl.uu.cs.arg.persuasion.model.dialogue.PersuasionMove;

public class RegretfulAttitude extends RetractionAttitude
{

    @Override
    public List<PersuasionMove<? extends Locution>> generateMoves(PersuadingAgent agent, PersuasionDialogue dialogue)
    {
        return null;
    }

}
