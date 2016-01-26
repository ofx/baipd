package nl.uu.cs.arg.persuasion.model.dialogue;

public class PersuasionDialogueStateChangeMessage extends PersuasionDialogueMessage {

	private PersuasionDialogueState newState;
	
	public PersuasionDialogueStateChangeMessage(PersuasionDialogueState newState, String message) {
		super(message);
		this.newState = newState;
	}

	public PersuasionDialogueStateChangeMessage(PersuasionDialogueState newState) {
		this(newState, "the dialogue state was changed to " + newState.toString());
	}

	/**
	 * Return the new state of the dialogue after the state change
	 * @return A dialogue state
	 */
	public PersuasionDialogueState getNewState() {
		return this.newState;
	}
	
}
