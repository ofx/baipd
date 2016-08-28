package nl.uu.cs.arg.persuasion.platform.local.agentimpl.attitudes.challenge;

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
import org.aspic.inference.ConstantList;

public class SuspiciousAttitude extends ChallengeAttitude
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

                // Check if we can construct an proof for the proposition
                if (attacker instanceof ClaimLocution) {
                    // Check if we can construct a proof for the proposition
                    if (helper.findProof(new ConstantList(((ClaimLocution)attacker).getProposition()), 0.0, agent.getBeliefs(), null).size() == 0) {
                        // Check if we can construct a proof for the negation, in case we can construct a proof, we should check whether
                        // this proof is stronger than the other, in which case we are allowed to move
                        if (helper.findProof(new ConstantList(((ClaimLocution)attacker).getProposition().negation()), 0.0, agent.getBeliefs(), null).size() != 0) {
                            // TODO: Check
                            if (true) {
                                moves.add(
                                        PersuasionMove.buildMove(
                                                agent.getParticipant(),
                                                attackMove,
                                                new WhyLocution(((ClaimLocution)attacker).getProposition())
                                        )
                                );
                            }
                        }
                    }
                    // In case we cannot construct a proof, we're fine with moving a why move
                    else {
                        moves.add(
                                PersuasionMove.buildMove(
                                        agent.getParticipant(),
                                        attackMove,
                                        new WhyLocution(((ClaimLocution)attacker).getProposition())
                                )
                        );
                    }
                } else if (attacker instanceof ArgueLocution) {
                    if (true) {

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
