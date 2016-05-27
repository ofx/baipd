package nl.uu.cs.arg.persuasion.platform.local.agentimpl.attitudes.assertion;

import nl.uu.cs.arg.persuasion.model.dialogue.PersuasionMove;
import nl.uu.cs.arg.persuasion.model.dialogue.locutions.ClaimLocution;
import nl.uu.cs.arg.persuasion.platform.local.agentimpl.PersuadingAgent;
import nl.uu.cs.arg.shared.dialogue.Move;
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

                RuleArgument newArgue = null;

                // Check if we can construct a supporting claim
                if (attacker instanceof WhyLocution) {
                    newArgue = helper.generateArgument(agent.getBeliefs(), ((WhyLocution) attacker).getAttackedPremise(), 0.0, attackMove, dialogue.getReplies(attackMove), null);
                }
                // Check if we can construct the negation
                else if (attacker instanceof ClaimLocution) {
                    newArgue = helper.generateArgument(agent.getBeliefs(), ((ClaimLocution) attacker).getProposition().negation(), 0.0, attackMove, dialogue.getReplies(attackMove), null);
                }

                // Success?
                if (newArgue != null) {
                    //ret.add(PersuasionMove.buildMove(this.participant, null, new ClaimLocution(this.dialogue.getTopic())));
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
        catch (Exception e)
        {
            System.out.println(e);
        }

        return moves;
    }

}
