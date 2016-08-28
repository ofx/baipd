package nl.uu.cs.arg.persuasion.platform.local.agentimpl.attitudes.retraction;

import nl.uu.cs.arg.persuasion.model.dialogue.PersuasionDialogueException;
import nl.uu.cs.arg.persuasion.model.dialogue.locutions.ClaimLocution;
import nl.uu.cs.arg.persuasion.platform.local.agentimpl.PersuadingAgent;
import nl.uu.cs.arg.shared.dialogue.Move;
import nl.uu.cs.arg.shared.dialogue.locutions.Locution;

import java.util.LinkedList;
import java.util.List;
import nl.uu.cs.arg.persuasion.model.dialogue.PersuasionDialogue;
import nl.uu.cs.arg.persuasion.model.dialogue.PersuasionMove;
import nl.uu.cs.arg.shared.dialogue.locutions.RetractLocution;
import nl.uu.cs.arg.shared.dialogue.locutions.WhyLocution;
import org.aspic.inference.ReasonerException;
import org.aspic.inference.RuleArgument;
import org.aspic.inference.parser.ParseException;

public class IncongruousAttitude extends RetractionAttitude
{

    @Override
    public List<PersuasionMove<? extends Locution>> generateMoves(PersuadingAgent agent, PersuasionDialogue dialogue) throws PersuasionDialogueException, ParseException, ReasonerException
    {
        List<PersuasionMove<? extends Locution>> moves = new LinkedList<>();

        // Fetch the active attackers of the dialogue topic
        List<PersuasionMove<? extends Locution>> attackers = dialogue.getActiveAttackers();
        for (PersuasionMove<? extends Locution> attackMove : attackers) {
            if (attackMove.hasSurrendered(agent.getParticipant())) {
                continue;
            }

            Locution attacker = attackMove.getLocution();
            Locution ownLocution = attackMove.getTarget().getLocution();

            boolean retract = false;

            // Always retract
            if (attacker instanceof WhyLocution || attacker instanceof ClaimLocution) {
                // Check if this is our move
                if (attackMove.getTarget().getPlayer() == agent.getParticipant()) {
                    // Check if this was a claim (should be true)
                    if (ownLocution instanceof ClaimLocution) {
                        retract = true;
                    }
                }
            }

            // Success?
            if (retract) {
                PersuasionMove<RetractLocution> retractMove = PersuasionMove.buildMove(
                        agent.getParticipant(),
                        attackMove,
                        new RetractLocution(((ClaimLocution) attackMove.getTarget().getLocution()).getProposition())
                );

                moves.add(retractMove);
                attackMove.addSurrender(retractMove);
            }
        }

        return moves;
    }

}
