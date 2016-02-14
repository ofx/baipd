package nl.uu.cs.arg.persuasion.model.dialogue;

import nl.uu.cs.arg.persuasion.model.PersuasionParticipant;

public class PersuasionSkipMoveMessage extends PersuasionDialogueMessage {

    PersuasionParticipant participant;

    public PersuasionSkipMoveMessage(PersuasionParticipant participant) {
        super(participant.toString() + " skipped its turn.");
    }

    public PersuasionParticipant getSkippingAgent() {
        return this.participant;
    }

}
