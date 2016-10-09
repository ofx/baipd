package nl.uu.cs.arg.persuasion.platform.local.agentimpl.attitudes.challenge;

import nl.uu.cs.arg.persuasion.model.dialogue.PersuasionDialogueException;
import nl.uu.cs.arg.persuasion.model.dialogue.locutions.ClaimLocution;
import nl.uu.cs.arg.persuasion.platform.local.agentimpl.PersuadingAgent;
import nl.uu.cs.arg.shared.dialogue.Move;
import nl.uu.cs.arg.shared.dialogue.locutions.ArgueLocution;
import nl.uu.cs.arg.shared.dialogue.locutions.Locution;

import java.util.LinkedList;
import java.util.List;
import nl.uu.cs.arg.persuasion.model.dialogue.PersuasionDialogue;
import nl.uu.cs.arg.persuasion.model.dialogue.PersuasionDialogue;
import nl.uu.cs.arg.persuasion.model.dialogue.PersuasionMove;
import nl.uu.cs.arg.shared.dialogue.locutions.WhyLocution;
import org.aspic.inference.ReasonerException;
import org.aspic.inference.RuleArgument;
import org.aspic.inference.parser.ParseException;

public class TentativeAttitude extends ChallengeAttitude
{
    public TentativeAttitude()
    {
        super("Tentative");
    }

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

            // Check if we can construct an proof for the proposition
            if (attacker instanceof ClaimLocution) {
                moves.add(
                        PersuasionMove.buildMove(
                                agent.getParticipant(),
                                attackMove,
                                new WhyLocution(((ClaimLocution)attacker).getProposition())
                        )
                );
            } else if (attacker instanceof ArgueLocution) {
                // Check if we can construct arguments for the sub arguments of the argue move, if not, we're allowed
                // to challenge those arguments
                for (RuleArgument sub : ((ArgueLocution) attacker).getArgument().getSubArgumentList().getArguments()) {
                    // Hacky
                    if (sub.getClaim().getFunctor().startsWith("r")) {
                        continue;
                    }

                    moves.add(
                            PersuasionMove.buildMove(
                                    agent.getParticipant(),
                                    attackMove,
                                    new WhyLocution(sub.getClaim())
                            )
                    );
                }
            }
        }

        return moves;
    }

}
