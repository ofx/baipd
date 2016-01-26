package nl.uu.cs.arg.persuasion.model.dialogue;

public class PersuasionDialogueMessage {

	private String message;
	
	public PersuasionDialogueMessage(String message) {
		this.message = message;
	}

	public String getMessage() {
		return this.message;
	}

	@Override
	public String toString() {
		return this.message;
	}
}
