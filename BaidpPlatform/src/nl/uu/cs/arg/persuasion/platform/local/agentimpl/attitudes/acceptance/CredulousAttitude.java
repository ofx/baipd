package nl.uu.cs.arg.persuasion.platform.local.agentimpl.attitudes.acceptance;

import nl.uu.cs.arg.persuasion.model.dialogue.PersuasionDialogue;
import nl.uu.cs.arg.persuasion.model.dialogue.PersuasionDialogueException;
import nl.uu.cs.arg.persuasion.model.dialogue.locutions.ClaimLocution;
import nl.uu.cs.arg.persuasion.platform.local.agentimpl.PersuadingAgent;
import nl.uu.cs.arg.shared.dialogue.Move;
import nl.uu.cs.arg.shared.dialogue.locutions.ArgueLocution;
import nl.uu.cs.arg.shared.dialogue.locutions.ConcedeLocution;
import nl.uu.cs.arg.shared.dialogue.locutions.Locution;
import nl.uu.cs.arg.persuasion.model.dialogue.PersuasionMove;
import org.aspic.inference.ReasonerException;
import org.aspic.inference.RuleArgument;
import org.aspic.inference.parser.ParseException;

import java.util.LinkedList;
import java.util.List;

public class CredulousAttitude extends AcceptanceAttitude
{
    public CredulousAttitude()
    {
        super("Credulous");
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

            // Check if we can accept
            if (attacker instanceof ArgueLocution) {
                // Check if we can accept the conclusions of the sub arguments of the argue locution
                for (RuleArgument sub : ((ArgueLocution) attacker).getArgument().getSubArgumentList().getArguments()) {
                    RuleArgument newArgue = helper.generateArgument(
                            agent.getBeliefs(),
                            sub.getClaim(),
                            0.0,
                            attackMove,
                            dialogue.getReplies(attackMove),
                            null
                    );

                    // If we can generate an argument, add a concede move
                    if (newArgue != null) {
                        PersuasionMove<ConcedeLocution> concedeMove = PersuasionMove.buildMove(
                                agent.getParticipant(),
                                attackMove,
                                new ConcedeLocution(sub.getClaim())
                        );

                        moves.add(concedeMove);
                        attackMove.addSurrender(concedeMove);
                    }
                }
            }
            // If we can accept the claim, we concede
            else if (attacker instanceof ClaimLocution) {
                RuleArgument newArgue = helper.generateArgument(
                        agent.getBeliefs(),
                        ((ClaimLocution)attacker).getProposition(),
                        0.0,
                        attackMove,
                        dialogue.getReplies(attackMove),
                        null
                );

                // If we can generate an argument, add a concede move
                if (newArgue != null) {
                    PersuasionMove<ConcedeLocution> concedeMove = PersuasionMove.buildMove(
                            agent.getParticipant(),
                            attackMove,
                            new ConcedeLocution(((ClaimLocution)attacker).getProposition())
                    );

                    moves.add(concedeMove);
                    attackMove.addSurrender(concedeMove);
                }
            }
        }

        return moves;
    }

}
