package nl.uu.cs.arg.persuasion.model.dialogue.protocol;

import nl.uu.cs.arg.persuasion.model.dialogue.PersuasionDialogue;
import nl.uu.cs.arg.persuasion.model.dialogue.PersuasionMove;
import nl.uu.cs.arg.shared.dialogue.Move;
import nl.uu.cs.arg.shared.dialogue.locutions.Locution;
import nl.uu.cs.arg.shared.dialogue.protocol.ProtocolException;

public enum PersuasionRule {

    AttackOnOwnMove {
        @Override
        public PersuasionProtocolException evaluateMove(PersuasionDialogue dialogue, PersuasionMove<? extends Locution> newMove) {
            if (newMove != null && newMove.getTarget() != null && newMove.getTarget().getPlayer() == newMove.getPlayer()) {
                return new PersuasionProtocolException(newMove, "The new move attacks one's own: An agent may only attack moves of other players");
            }
            return null;
        }
    },
    NoRepeatInBranch {
        @Override
        public PersuasionProtocolException evaluateMove(PersuasionDialogue dialogue, PersuasionMove<? extends Locution> newMove) {
            // TODO: Implement it (using proposal's .isRepeatedMove or something)
            return null;
        }
    };

    public abstract PersuasionProtocolException evaluateMove(PersuasionDialogue dialogue, PersuasionMove<? extends Locution> newMove);

}
