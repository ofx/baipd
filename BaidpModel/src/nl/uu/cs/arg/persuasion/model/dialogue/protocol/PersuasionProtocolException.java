package nl.uu.cs.arg.persuasion.model.dialogue.protocol;

import nl.uu.cs.arg.persuasion.model.dialogue.PersuasionDialogueException;
import nl.uu.cs.arg.persuasion.model.dialogue.PersuasionMove;
import nl.uu.cs.arg.shared.dialogue.locutions.Locution;

public class PersuasionProtocolException extends PersuasionDialogueException {

	private static final long serialVersionUID = 1L;

	private PersuasionMove<? extends Locution> cause;

	public PersuasionProtocolException(PersuasionMove<? extends Locution> cause, String violationDescription) {
		super(violationDescription);
		this.cause = cause;
	}

	public PersuasionMove<? extends Locution> getCauseMove() {
		return this.cause;
	}
	
	@Override
	public String toString() {
		return this.cause.toLogicString() + " caused a protocol exception: " + super.toString();
	}
	
}
