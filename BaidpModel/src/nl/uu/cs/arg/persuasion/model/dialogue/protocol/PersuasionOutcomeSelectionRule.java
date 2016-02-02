package nl.uu.cs.arg.persuasion.model.dialogue.protocol;

import nl.uu.cs.arg.persuasion.model.PersuasionParticipant;
import nl.uu.cs.arg.persuasion.model.dialogue.PersuasionDialogue;
import nl.uu.cs.arg.persuasion.model.dialogue.PersuasionDialogueException;
import nl.uu.cs.arg.persuasion.model.dialogue.PersuasionMove;
import nl.uu.cs.arg.shared.dialogue.locutions.Locution;
import org.aspic.inference.Constant;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public enum PersuasionOutcomeSelectionRule {

    /**
     * For pure persuasion, every participant in Wt must also be committed to the dialogue topic.
     */
    PurePersuasion {
        @Override
        public PersuasionOutcome determineWinners(PersuasionDialogue dialogue, List<PersuasionParticipant> participants) {
            PersuasionOutcome outcome = new PersuasionOutcome();
            outcome.Winners = this.getWinners(dialogue);

            Constant topic = dialogue.getTopic();
            for (PersuasionParticipant participant : participants) {
                if (!participant.isCommittedTo(topic)) {
                    outcome.Winners = new HashSet<PersuasionParticipant>();
                    break;
                }
            }

            try {
                outcome.TopicIsIn = dialogue.isTopicIn();
            } catch (PersuasionDialogueException e) {
                e.printStackTrace();
            }

            return outcome;
        }
    },

    /**
     * For conflict resolution, the set of winners is the set Wt.
     */
    ConflictResolution {
        @Override
        public PersuasionOutcome determineWinners(PersuasionDialogue dialogue, List<PersuasionParticipant> participants) {
            PersuasionOutcome outcome = new PersuasionOutcome();

            outcome.Winners = this.getWinners(dialogue);
            try {
                outcome.TopicIsIn = dialogue.isTopicIn();
            } catch (PersuasionDialogueException e) {
                e.printStackTrace();
            }

            return outcome;
        }
    };

    /**
     * Retrieves the set Wt, defined as every participant committed to the dialogue topic.
     *
     * @param dialogue
     * @return
     */
    Set<PersuasionParticipant> getWinners(PersuasionDialogue dialogue)
    {
        HashSet<PersuasionParticipant> Wt = new HashSet<PersuasionParticipant>();

        // The set of active attackers will, for the first node (topic) being in, return
        // every move (including the first move) that makes the first move in, the player of every move
        // in the set of active attackers is added to a HashSet (and therefore distinct). The result
        // is a set Wt of players that make the first move in.
        // The set of active attackers will contain the set of moves that make the first move out
        // when its dialectical status is out.
        List<PersuasionMove<? extends Locution>> attackers = dialogue.getActiveAttackers();
        for (PersuasionMove<? extends Locution> move : attackers)
        {
            Wt.add(move.getPlayer());
        }
        return Wt;
    }

    public abstract PersuasionOutcome determineWinners(PersuasionDialogue dialogue, List<PersuasionParticipant> participants);

}
