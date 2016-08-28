package nl.uu.cs.arg.persuasion.platform.local.agentimpl.attitudes.argue;

import nl.uu.cs.arg.persuasion.model.dialogue.PersuasionDialogueException;
import nl.uu.cs.arg.persuasion.platform.local.agentimpl.PersuadingAgent;
import nl.uu.cs.arg.shared.dialogue.locutions.ArgueLocution;
import nl.uu.cs.arg.shared.dialogue.locutions.Locution;
import nl.uu.cs.arg.persuasion.model.dialogue.PersuasionDialogue;
import nl.uu.cs.arg.persuasion.model.dialogue.PersuasionMove;
import nl.uu.cs.arg.shared.dialogue.locutions.WhyLocution;
import org.aspic.inference.ReasonerException;
import org.aspic.inference.RuleArgument;
import org.aspic.inference.parser.ParseException;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by argon on 21-5-16.
 */
public class HopefulAttitude extends ArgueAttitude
{

    @Override
    public List<PersuasionMove<? extends Locution>> generateMoves(PersuadingAgent agent, PersuasionDialogue dialogue) throws PersuasionDialogueException, ParseException, ReasonerException
    {
        List<PersuasionMove<? extends Locution>> moves = new LinkedList<>();

        // Fetch the active attackers of the dialogue topic
        List<PersuasionMove<? extends Locution>> attackers = dialogue.getActiveAttackers();
        for (PersuasionMove<? extends Locution> attackMove : attackers) {
            Locution attacker = attackMove.getLocution();

            RuleArgument newArgue = null;

            List<PersuasionMove<? extends Locution>> replies = dialogue.getReplies(attackMove);

            // Check if we can construct a supporting claim
            if (attacker instanceof WhyLocution) {
                newArgue = helper.generateArgument(agent.getBeliefs(), ((WhyLocution) attacker).getAttackedPremise(), 0.0, attackMove, dialogue.getReplies(attackMove), null);
            }
            // Check if we can construct the negation
            else if (attacker instanceof ArgueLocution) {
                newArgue = helper.generateCounterAttack(
                        agent.getBeliefs(),
                        ((ArgueLocution)attacker).getArgument(),
                        (PersuasionMove<ArgueLocution>) attackMove,
                        replies
                );
            }

            // Success?
            if (newArgue != null) {
                moves.add(
                        PersuasionMove.buildMove(
                                agent.getParticipant(),
                                attackMove,
                                new ArgueLocution(newArgue)
                        )
                );
            }
        }

        return moves;
    }

}