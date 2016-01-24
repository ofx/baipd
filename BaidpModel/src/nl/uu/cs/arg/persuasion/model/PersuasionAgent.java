package nl.uu.cs.arg.persuasion.model;

import nl.uu.cs.arg.shared.Agent;
import nl.uu.cs.arg.shared.dialogue.Move;
import nl.uu.cs.arg.shared.dialogue.locutions.Locution;
import nl.uu.cs.arg.shared.dialogue.locutions.OpenDialogueLocution;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public abstract class PersuasionAgent implements Agent {

    @Override
    public Move<? extends Locution> decideToJoin(OpenDialogueLocution openDialogue) {
        throw new NotImplementedException();
    }

}
