package nl.uu.cs.arg.persuasion.platform.local.agentimpl.attitudes;

import nl.uu.cs.arg.persuasion.model.dialogue.PersuasionDialogue;
import nl.uu.cs.arg.persuasion.model.dialogue.PersuasionDialogueException;
import nl.uu.cs.arg.persuasion.model.dialogue.PersuasionMove;
import nl.uu.cs.arg.persuasion.platform.local.agentimpl.PersuadingAgent;
import nl.uu.cs.arg.persuasion.platform.local.agentimpl.StrategyHelper;
import nl.uu.cs.arg.shared.dialogue.Move;
import nl.uu.cs.arg.shared.dialogue.locutions.Locution;
import org.aspic.inference.ReasonerException;
import org.aspic.inference.parser.ParseException;

import java.util.Iterator;
import java.util.List;

/**
 * Created by argon on 13-3-16.
 */
public abstract class Attitude
{

    protected StrategyHelper helper = StrategyHelper.DefaultHelper;

    public abstract List<PersuasionMove<? extends Locution>> generateMoves(PersuadingAgent agent, PersuasionDialogue dialogue) throws PersuasionDialogueException, ParseException, ReasonerException;

    public List<PersuasionMove<? extends Locution>> generateValidatedMoves(PersuadingAgent agent, PersuasionDialogue dialogue) throws PersuasionDialogueException, ParseException, ReasonerException {
        List<PersuasionMove<? extends Locution>> moves = this.generateMoves(agent, dialogue);
        Iterator<PersuasionMove<? extends Locution>> it = moves.iterator();
        while (it.hasNext()) {
            if (dialogue.isRepeatedMove(it.next())) {
                it.remove();
                PersuasionMove.decrementMoveCounter();
            }
        }
        return moves;
    }

}