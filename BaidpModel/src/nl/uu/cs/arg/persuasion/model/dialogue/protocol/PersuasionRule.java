package nl.uu.cs.arg.persuasion.model.dialogue.protocol;

import nl.uu.cs.arg.persuasion.model.dialogue.PersuasionDialogue;
import nl.uu.cs.arg.persuasion.model.dialogue.PersuasionMove;
import nl.uu.cs.arg.shared.dialogue.Move;
import nl.uu.cs.arg.shared.dialogue.locutions.Locution;
import nl.uu.cs.arg.shared.dialogue.locutions.RetractLocution;
import nl.uu.cs.arg.shared.dialogue.protocol.ProtocolException;

public enum PersuasionRule {

    AttackOnOwnMove {
        @Override
        public PersuasionProtocolException evaluateMove(PersuasionDialogue dialogue, PersuasionMove<? extends Locution> newMove) {
            if (newMove != null && newMove.getTarget() != null && newMove.getTarget().getPlayer() == newMove.getPlayer() && !(newMove.getLocution() instanceof RetractLocution)) {
                return new PersuasionProtocolException(newMove, "The new move attacks one's own: An agent may only attack moves of other players");
            }
            return null;
        }
    },
    NoRepeatInBranch {
        @Override
        public PersuasionProtocolException evaluateMove(PersuasionDialogue dialogue, PersuasionMove<? extends Locution> newMove) {
            if (dialogue.isRepeatedMove(newMove)) {
                return new PersuasionProtocolException(newMove, "The new move is a repeated move");
            }
            return null;
        }
    },
    IsRelevant {
        @Override
        public PersuasionProtocolException evaluateMove(PersuasionDialogue dialogue, PersuasionMove<? extends Locution> newMove) {
            if (dialogue.isRepeatedMove(newMove)) {
                return new PersuasionProtocolException(newMove, "The new move is a repeated move");
            }
            return null;
        }
    };

    public abstract PersuasionProtocolException evaluateMove(PersuasionDialogue dialogue, PersuasionMove<? extends Locution> newMove);

}
