package nl.uu.cs.arg.persuasion.platform.local.agentimpl.attitudes.assertion;

import nl.uu.cs.arg.persuasion.model.dialogue.PersuasionMove;
import nl.uu.cs.arg.persuasion.model.dialogue.locutions.ClaimLocution;
import nl.uu.cs.arg.persuasion.platform.local.agentimpl.PersuadingAgent;
import nl.uu.cs.arg.shared.dialogue.Move;
import nl.uu.cs.arg.shared.dialogue.locutions.ArgueLocution;
import nl.uu.cs.arg.shared.dialogue.locutions.Locution;
import nl.uu.cs.arg.shared.dialogue.locutions.WhyLocution;
import org.aspic.inference.Constant;
import org.aspic.inference.ReasonerException;
import org.aspic.inference.RuleArgument;
import org.aspic.inference.parser.ParseException;
import nl.uu.cs.arg.persuasion.model.dialogue.PersuasionDialogue;

import java.util.LinkedList;
import java.util.List;

public class ConfidentAttitude extends AssertionAttitude
{

    @Override
    public List<PersuasionMove<? extends Locution>> generateMoves(PersuadingAgent agent, PersuasionDialogue dialogue)
    {
        List<PersuasionMove<? extends Locution>> moves = new LinkedList<>();

        try {
            // Fetch the active attackers of the dialogue topic
            List<PersuasionMove<? extends Locution>> attackers = dialogue.getActiveAttackers();
            for (PersuasionMove<? extends Locution> attackMove : attackers) {
                Locution attacker = attackMove.getLocution();

                if (attacker instanceof ClaimLocution) {
                    RuleArgument newArgue = helper.generateArgument(
                        agent.getBeliefs(),
                        ((ClaimLocution) attacker).getProposition().negation(),
                        0.0,
                        attackMove,
                        dialogue.getReplies(attackMove),
                        null
                    );

                    if (newArgue != null) {
                        moves.add(
                                PersuasionMove.buildMove(
                                        agent.getParticipant(),
                                        attackMove,
                                        new ClaimLocution(newArgue.getClaim())
                                )
                        );
                    }
                }
                else if (attacker instanceof ArgueLocution) {
                    // For all sub arguments, check if we can claim the negation
                    for (RuleArgument sub : ((ArgueLocution) attacker).getArgument().getSubArgumentList().getArguments()) {
                        RuleArgument newArgue = helper.generateArgument(
                                agent.getBeliefs(),
                                sub.getClaim().negation(),
                                0.0,
                                attackMove,
                                dialogue.getReplies(attackMove),
                                null
                        );

                        if (newArgue != null) {
                            moves.add(
                                    PersuasionMove.buildMove(
                                            agent.getParticipant(),
                                            attackMove,
                                            new ClaimLocution(sub.getClaim().negation())
                                    )
                            );
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            System.out.println(e);
        }

        return moves;
    }

}
