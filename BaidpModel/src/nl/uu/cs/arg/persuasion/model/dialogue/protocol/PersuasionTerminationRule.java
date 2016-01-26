package nl.uu.cs.arg.persuasion.model.dialogue.protocol;

import nl.uu.cs.arg.persuasion.model.dialogue.PersuasionDialogue;
import nl.uu.cs.arg.shared.dialogue.Dialogue;

public enum PersuasionTerminationRule {

	NoParticipants {
		@Override
		public PersuasionTerminationMessage shouldTerminate(PersuasionDialogue dialogue, int participants, int skips) {
			if (participants <= 0) {
				return new PersuasionTerminationMessage("No (more) participants.");
			}
			return null;
		}
		
	},
	InactiveRound {
		@Override
		public PersuasionTerminationMessage shouldTerminate(PersuasionDialogue dialogue, int participants, int skips) {
			if (skips >= participants + 1) {
				return new PersuasionTerminationMessage("The dialogue was inactive for a full round: All of the agents skipped turn and the first skipped twice.");
			}
			return null;
		}
	};

	// TODO: Check skips!
	public abstract PersuasionTerminationMessage shouldTerminate(PersuasionDialogue dialogue, int participants, int skips);
	
}
