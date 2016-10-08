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
    public IncongruousAttitude()
    {
        super("Incongruous Attitude");
    }

    @Override
    public List<PersuasionMove<? extends Locution>> generateMoves(PersuadingAgent agent, PersuasionDialogue dialogue) throws PersuasionDialogueException, ParseException, ReasonerException
    {
        List<PersuasionMove<? extends Locution>> moves = new LinkedList<>();

        // Fetch the active attackers of the dialogue topic
        List<PersuasionMove<? extends Locution>> ownMoves = dialogue.getPlayerMoves(agent.getParticipant());
        for (PersuasionMove<? extends Locution> ownMove : ownMoves) {
            if (ownMove.hasSurrendered(agent.getParticipant())) {
                continue;
            }

            Locution locution = ownMove.getLocution();

            boolean retract = false;

            // Always retract
            if (locution instanceof ClaimLocution) {
                retract = true;
            }

            // Success?
            if (retract) {
                PersuasionMove<RetractLocution> retractMove = PersuasionMove.buildMove(
                        agent.getParticipant(),
                        ownMove,
                        new RetractLocution(((ClaimLocution) ownMove.getLocution()).getProposition())
                );

                moves.add(retractMove);
                ownMove.addSurrender(retractMove);
            }
        }

        return moves;
    }

}
