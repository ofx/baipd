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

public class DeceptiveAttitude extends AssertionAttitude
{
    public DeceptiveAttitude()
    {
        super("Deceptive Attitude");
    }

    @Override
    public List<PersuasionMove<? extends Locution>> generateMoves(PersuadingAgent agent, PersuasionDialogue dialogue) throws PersuasionDialogueException, ParseException, ReasonerException
    {
        List<PersuasionMove<? extends Locution>> moves = new LinkedList<>();

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
                RuleArgument _newArgue = helper.generateArgument(
                        agent.getBeliefs(),
                        ((ClaimLocution) attacker).getProposition(),
                        0.0,
                        attackMove,
                        dialogue.getReplies(attackMove),
                        null
                );

                // We're fine as long as we can construct an argument for or against
                if (newArgue != null || _newArgue != null) {
                    moves.add(
                            PersuasionMove.buildMove(
                                    agent.getParticipant(),
                                    attackMove,
                                    new ClaimLocution(((ClaimLocution)attacker).getProposition().negation())
                            )
                    );
                }
            }
            else if (attacker instanceof ArgueLocution) {
                // For all sub arguments, check if we can claim the negation
                for (RuleArgument sub : ((ArgueLocution) attacker).getArgument().getSubArgumentList().getArguments()) {
                    // Note, this can get a bit stupid, since the agent will claim the negation of all sub arguments

                    // Always make a claim
                    /*moves.add(
                            PersuasionMove.buildMove(
                                    agent.getParticipant(),
                                    attackMove,
                                    new ClaimLocution(((ArgueLocution)attacker).getProposition().negation())
                            )
                    );*/
                }
            }
        }

        return moves;
    }

}
