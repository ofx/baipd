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
import nl.uu.cs.arg.persuasion.model.dialogue.PersuasionMove;
import nl.uu.cs.arg.shared.dialogue.locutions.WhyLocution;
import org.aspic.inference.ReasonerException;
import org.aspic.inference.RuleArgument;
import org.aspic.inference.parser.ParseException;

public class PersistentAttitude extends ChallengeAttitude
{

    @Override
    public List<PersuasionMove<? extends Locution>> generateMoves(PersuadingAgent agent, PersuasionDialogue dialogue) throws PersuasionDialogueException, ParseException, ReasonerException
    {
        List<PersuasionMove<? extends Locution>> moves = new LinkedList<>();

        // Fetch the active attackers of the dialogue topic
        List<PersuasionMove<? extends Locution>> attackers = dialogue.getActiveAttackers();
        for (PersuasionMove<? extends Locution> attackMove : attackers) {
            Locution attacker = attackMove.getLocution();

            // Check if we can construct an proof for the proposition
            if (attacker instanceof ClaimLocution) {
                RuleArgument newArgue = helper.generateArgument(
                        agent.getBeliefs(),
                        ((ClaimLocution)attacker).getProposition(),
                        0.0,
                        attackMove,
                        dialogue.getReplies(attackMove),
                        null
                );

                if (newArgue == null) {
                    moves.add(
                            PersuasionMove.buildMove(
                                    agent.getParticipant(),
                                    attackMove,
                                    new WhyLocution(((ClaimLocution) attacker).getProposition())
                            )
                    );
                } else {
                    // It exists, check if it is justified, if not, challenge
                    if (!helper.hasJustifiedArgument(newArgue, agent.getBeliefs())) {
                        moves.add(
                                PersuasionMove.buildMove(
                                        agent.getParticipant(),
                                        attackMove,
                                        new WhyLocution(((ClaimLocution) attacker).getProposition())
                                )
                        );
                    }
                }
            } else if (attacker instanceof ArgueLocution) {
                // Check if we can construct arguments for the sub arguments of the argue move, if not, we're allowed
                // to challenge those arguments
                for (RuleArgument sub : ((ArgueLocution) attacker).getArgument().getSubArgumentList().getArguments()) {
                    RuleArgument newArgue = helper.generateArgument(
                            agent.getBeliefs(),
                            sub.getClaim(),
                            0.0,
                            attackMove,
                            dialogue.getReplies(attackMove),
                            null
                    );

                    // We cannot construct an argument, let's challenge
                    if (newArgue != null && !helper.hasJustifiedArgument(newArgue, agent.getBeliefs())) {
                        moves.add(
                                PersuasionMove.buildMove(
                                        agent.getParticipant(),
                                        attackMove,
                                        new WhyLocution(sub.getClaim())
                                )
                        );
                    } else {
                        // It exists, check if it is justified, if not, challenge
                        if (!helper.hasJustifiedArgument(newArgue, agent.getBeliefs())) {
                            moves.add(
                                    PersuasionMove.buildMove(
                                            agent.getParticipant(),
                                            attackMove,
                                            new WhyLocution(((ClaimLocution) attacker).getProposition())
                                    )
                            );
                        }
                    }
                }
            }
        }

        return moves;
    }

}
