package nl.uu.cs.arg.persuasion.platform.local.agentimpl.attitudes;

import nl.uu.cs.arg.shared.dialogue.Move;
import nl.uu.cs.arg.shared.dialogue.locutions.Locution;

import java.util.List;

/**
 * Created by argon on 13-3-16.
 */
public abstract class Attitude
{

    public abstract List<Move<? extends Locution>> generateMoves();

}