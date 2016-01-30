package nl.uu.cs.arg.persuasion.platform;

import nl.uu.cs.arg.persuasion.model.dialogue.PersuasionDialogueMessage;
import nl.uu.cs.arg.persuasion.model.dialogue.PersuasionMove;
import nl.uu.cs.arg.shared.dialogue.locutions.Locution;

import java.util.List;

public class PersuasionPlatformOutputPrinter implements PersuasionPlatformListener {

    public static PersuasionPlatformOutputPrinter defaultPlatformOutputPrinter = new PersuasionPlatformOutputPrinter();

    private PersuasionPlatformOutputLevel outputLevel;

    private PersuasionPlatformOutputPrinter() {
        this.outputLevel = PersuasionPlatformOutputLevel.Moves;
    }

    public void setLevel(PersuasionPlatformOutputLevel value) {
        this.outputLevel = value;
    }

    @Override
    public void onExceptionThrown(PersuasionPlatformException e) {
        // Exceptions are always printed
        System.out.println(e.toString());
    }

    @Override
    public void onMessagesReceived(List<PersuasionDialogueMessage> messages) {
        if (outputLevel == PersuasionPlatformOutputLevel.Messages ||
                outputLevel == PersuasionPlatformOutputLevel.Moves) {
            System.out.println(messages.toString());
        }
    }

    @Override
    public void onMoves(List<PersuasionMove<? extends Locution>> moves) {
        if (outputLevel == PersuasionPlatformOutputLevel.Moves) {
            for (PersuasionMove<? extends Locution> move : moves) {
                System.out.println(move.toString());
            }
        }
    }

}
