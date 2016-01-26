package nl.uu.cs.arg.persuasion.model.dialogue.protocol;

import nl.uu.cs.arg.persuasion.model.dialogue.PersuasionDialogueMessage;
import nl.uu.cs.arg.shared.dialogue.DialogueMessage;

public class PersuasionTerminationMessage extends PersuasionDialogueMessage {

	public PersuasionTerminationMessage(String cause) {
		super(cause);
	}

	@Override
	public String toString() {
		return "Dialogue will terminate: " + this.getMessage();
	}
	
}
