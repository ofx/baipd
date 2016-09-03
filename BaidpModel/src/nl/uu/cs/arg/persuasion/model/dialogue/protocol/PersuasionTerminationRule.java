package nl.uu.cs.arg.persuasion.model.dialogue.protocol;

import nl.uu.cs.arg.persuasion.model.PersuasionAgent;
import nl.uu.cs.arg.persuasion.model.dialogue.PersuasionDialogue;
import nl.uu.cs.arg.shared.dialogue.Dialogue;

import java.util.List;

public enum PersuasionTerminationRule {

	NoParticipants {
		@Override
		public PersuasionTerminationMessage shouldTerminate(PersuasionDialogue dialogue, List<PersuasionAgent> agents, int skipCount) {
			if (agents.size() <= 0) {
				return new PersuasionTerminationMessage("no participants");
			}
			return null;
		}
		
	},
	InactiveRound {
		@Override
		public PersuasionTerminationMessage shouldTerminate(PersuasionDialogue dialogue, List<PersuasionAgent> agents, int skipCount) {
			if (skipCount >= agents.size()) {
				System.out.println(dialogue);
				return new PersuasionTerminationMessage("the dialogue is out of move since a participant has stated that it is out of moves");
			}
			return null;
		}
	};

	public abstract PersuasionTerminationMessage shouldTerminate(PersuasionDialogue dialogue, List<PersuasionAgent> agents, int skipCount);
	
}
