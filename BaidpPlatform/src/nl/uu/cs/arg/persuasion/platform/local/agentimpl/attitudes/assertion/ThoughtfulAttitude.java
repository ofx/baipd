package nl.uu.cs.arg.persuasion.platform.local.agentimpl.attitudes.assertion;

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

public class ThoughtfulAttitude extends AssertionAttitude
{
    public ThoughtfulAttitude()
    {
        super("Thoughtful Attitude");
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
                    // Is justified?
                    if (helper.hasJustifiedArgument(newArgue, agent.getBeliefs())) {
                        moves.add(
                                PersuasionMove.buildMove(
                                        agent.getParticipant(),
                                        attackMove,
                                        new ClaimLocution(newArgue.getClaim())
                                )
                        );
                    }
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
                        // Is justified?
                        if (helper.hasJustifiedArgument(newArgue, agent.getBeliefs())) {
                            moves.add(
                                    PersuasionMove.buildMove(
                                            agent.getParticipant(),
                                            attackMove,
                                            new ClaimLocution(newArgue.getClaim())
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
