package nl.uu.cs.arg.persuasion.model;

import java.util.List;

import nl.uu.cs.arg.persuasion.model.dialogue.PersuasionDialogue;
import nl.uu.cs.arg.persuasion.model.dialogue.PersuasionDialogueException;
import nl.uu.cs.arg.persuasion.model.dialogue.PersuasionDialogueMessage;
import nl.uu.cs.arg.persuasion.model.dialogue.PersuasionMove;
import nl.uu.cs.arg.shared.dialogue.locutions.Locution;
import org.aspic.inference.ReasonerException;
import org.aspic.inference.parser.ParseException;

public interface PersuasionAgent {

    public boolean outOfMoves();

    public String getName();

    public void initialize(PersuasionParticipant participant);

    public List<PersuasionMove<? extends Locution>> makeMoves();

    public void join(PersuasionDialogue dialogue);

    public void onNewMovesReceived(List<PersuasionMove<? extends Locution>> moves);

    public void onDialogueMessagesReceived(List<? extends PersuasionDialogueMessage> messages);

    public void onDialogueException(PersuasionDialogueException e);

    public boolean isProponent(PersuasionDialogue dialogue) throws ParseException, ReasonerException;

}
