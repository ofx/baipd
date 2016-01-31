package nl.uu.cs.arg.persuasion.platform.local.agentimpl;

import nl.uu.cs.arg.persuasion.model.dialogue.PersuasionDialogueException;
import nl.uu.cs.arg.persuasion.model.dialogue.PersuasionMove;
import nl.uu.cs.arg.persuasion.platform.local.AgentXmlData;
import nl.uu.cs.arg.persuasion.platform.local.ValuedOption;
import nl.uu.cs.arg.shared.dialogue.DialogueException;
import nl.uu.cs.arg.shared.dialogue.locutions.Locution;
import org.aspic.inference.Constant;
import org.aspic.inference.ReasonerException;
import org.aspic.inference.parser.ParseException;

import java.util.List;

public class SimplePersuasionAgent extends PersuadingAgent {

    public SimplePersuasionAgent(AgentXmlData xmlDataFile)
    {
        super(xmlDataFile);
    }

    @Override
    protected void storeNewBeliefs(List<PersuasionMove<? extends Locution>> moves) throws ParseException, ReasonerException {
        System.out.println("storeNewBeliefs");
    }

    @Override
    protected List<PersuasionMove<? extends Locution>> generateMoves() throws PersuasionDialogueException, ParseException, ReasonerException {
        System.out.println("generateMoves");
        return null;
    }

    @Override
    public boolean outOfMoves() {
        System.out.println("outOfMoves");
        return false;
    }
}
