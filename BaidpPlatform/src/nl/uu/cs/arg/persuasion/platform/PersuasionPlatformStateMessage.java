package nl.uu.cs.arg.persuasion.platform;

import nl.uu.cs.arg.persuasion.model.dialogue.PersuasionDialogueMessage;
import nl.uu.cs.arg.shared.dialogue.DialogueMessage;

/**
 * A message that indicates that the platform has either started
 * execution or stopped running.
 *
 * @author erickok
 */
public class PersuasionPlatformStateMessage extends PersuasionDialogueMessage {

    private boolean nowRunning;

    public PersuasionPlatformStateMessage(boolean nowRunning) {
        super((nowRunning? "The platform is now running.": "The platform is now paused."));
        this.nowRunning = nowRunning;
    }

    /**
     * Returns an indication of the platform's new state
     * @return True is the platform just started running; false if it just stopped
     */
    public boolean isPlatformNowRunning() {
        return nowRunning;
    }

}
