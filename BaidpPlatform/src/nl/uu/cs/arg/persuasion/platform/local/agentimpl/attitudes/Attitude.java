package nl.uu.cs.arg.persuasion.platform.local.agentimpl.attitudes;

import nl.uu.cs.arg.persuasion.model.dialogue.PersuasionDialogue;
import nl.uu.cs.arg.persuasion.model.dialogue.PersuasionMove;
import nl.uu.cs.arg.persuasion.platform.local.agentimpl.PersuadingAgent;
import nl.uu.cs.arg.persuasion.platform.local.agentimpl.StrategyHelper;
import nl.uu.cs.arg.shared.dialogue.Move;
import nl.uu.cs.arg.shared.dialogue.locutions.Locution;

import java.util.List;

/**
 * Created by argon on 13-3-16.
 */
public abstract class Attitude
{

    protected StrategyHelper helper = StrategyHelper.DefaultHelper;

    public abstract List<PersuasionMove<? extends Locution>> generateMoves(PersuadingAgent agent, PersuasionDialogue dialogue);

}