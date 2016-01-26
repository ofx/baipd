package nl.uu.cs.arg.persuasion.platform;

import java.util.List;

import nl.uu.cs.arg.persuasion.model.dialogue.PersuasionDialogueMessage;
import nl.uu.cs.arg.persuasion.model.dialogue.PersuasionMove;
import nl.uu.cs.arg.shared.dialogue.DialogueMessage;
import nl.uu.cs.arg.shared.dialogue.Move;
import nl.uu.cs.arg.shared.dialogue.locutions.Locution;

public interface PersuasionPlatformListener {

    public void onMoves(List<PersuasionMove<? extends Locution>> moves);

    public void onMessagesReceived(List<PersuasionDialogueMessage> messages);

    public void onExceptionThrown(PersuasionPlatformException e);

}
