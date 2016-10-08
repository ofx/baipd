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

public class SensibleAttitude extends RetractionAttitude
{
    public SensibleAttitude()
    {
        super("Sensible Attitude");
    }

    @Override
    public List<PersuasionMove<? extends Locution>> generateMoves(PersuadingAgent agent, PersuasionDialogue dialogue) throws PersuasionDialogueException, ParseException, ReasonerException
    {
        // Please note that there is no difference between the Sensible and Regretful attitudes in this implementation
        // since arguments are considered of equal strength. For a claim that is already made, we already know that
        // the agent was able to provide for an argument for the claimed proposition, therefore we have cannot make
        // a distinction anymore (due to the lack of argument strength)...

        List<PersuasionMove<? extends Locution>> moves = new LinkedList<>();

        // Fetch the active attackers of the dialogue topic
        List<PersuasionMove<? extends Locution>> ownMoves = dialogue.getPlayerMoves(agent.getParticipant());
        for (PersuasionMove<? extends Locution> ownMove : ownMoves) {
            if (ownMove.hasSurrendered(agent.getParticipant())) {
                continue;
            }

            Locution locution = ownMove.getLocution();

            RuleArgument newArgue = null;

            // Check if we can construct an argument for the negation of the attacked premise, there is no notion
            // of argument strength, so this is a simple check
            if (locution instanceof ClaimLocution) {
                newArgue = helper.generateArgument(
                        agent.getBeliefs(),
                        ((ClaimLocution) ownMove.getLocution()).getProposition().negation(),
                        0.0,
                        ownMove,
                        dialogue.getReplies(ownMove),
                        null
                );
            }

            // Success?
            if (newArgue != null) {
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
