package nl.uu.cs.arg.persuasion.model.dialogue;

import org.aspic.inference.Constant;
import org.aspic.inference.Term;

public class PersuasionDialogueStartedMessage extends PersuasionDialogueMessage {

	private Constant topic;
	
	public PersuasionDialogueStartedMessage(Constant topic, String message) {
		super(message);
		this.topic = topic;
	}

	public PersuasionDialogueStartedMessage(Constant topic) {
		this(topic, String.format("persuasion dialogue with topic '%s'", topic));
	}

	public Constant getTopic() {
		return this.topic;
	}

}
