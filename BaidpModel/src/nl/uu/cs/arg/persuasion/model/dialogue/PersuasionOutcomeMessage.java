package nl.uu.cs.arg.persuasion.model.dialogue;

import nl.uu.cs.arg.persuasion.model.PersuasionParticipant;
import nl.uu.cs.arg.persuasion.model.dialogue.protocol.PersuasionOutcome;

import java.util.Set;

public class PersuasionOutcomeMessage extends PersuasionDialogueMessage {

	private static String prettyPrint(Set<PersuasionParticipant> set) {
		String s = "{";
		for (PersuasionParticipant participant : set) {
			s += String.format("%s, ", participant.getName());
		}
		s = s.substring(0, s.length() - 2);
		s += "}";
		return s;
	}

	public PersuasionOutcomeMessage(PersuasionOutcome outcome) {
		super(String.format("the dialogue outcome is: %s, winners: %s", outcome.TopicIsIn ? "topic is in" : "topic is out", prettyPrint(outcome.Winners)));
	}
	
}
