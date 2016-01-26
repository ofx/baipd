package nl.uu.cs.arg.persuasion.model.dialogue;

public class PersuasionOutcomeMessage extends PersuasionDialogueMessage {

	public PersuasionOutcomeMessage(boolean topicIsIn) {
		super(String.format("the dialogue outcome is: %s", topicIsIn ? "topic is in" : "topic is out"));
	}
	
}
