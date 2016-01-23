package dialogue;

import dialogue.locutions.ClaimLocution;
import nl.uu.cs.arg.shared.Participant;
import nl.uu.cs.arg.shared.dialogue.DialogueException;
import nl.uu.cs.arg.shared.dialogue.Move;
import nl.uu.cs.arg.shared.dialogue.Proposal;
import nl.uu.cs.arg.shared.dialogue.locutions.*;
import nl.uu.cs.arg.shared.util.IndexedNode;
import nl.uu.cs.arg.shared.util.IndexedTree;
import org.aspic.inference.Term;

import java.util.ArrayList;
import java.util.List;

public class PersuasionDialogue extends IndexedTree<Move<? extends Locution>> {

    private PersuasionDialogueState state;

    public PersuasionDialogue()
    {
        // Start a dialogue in unopened state.
        this.state = PersuasionDialogueState.Unopened;
    }

    public PersuasionDialogueState getState() { return this.state; }

    public PersuasionDialogueState setState(PersuasionDialogueState state) {
        PersuasionDialogueState oldState = this.state;
        this.state = state;
        return oldState;
    }

    public IndexedNode<Move<? extends Locution>> addMoveNode(Move<? extends Locution> newMove) throws PersuasionDialogueException {
        // Persuasion does allow for the locutions:
        // - claim
        // - why
        // - argue
        // - concede
        // - retract
        Locution locution = newMove.getLocution();
        if (!(locution instanceof ClaimLocution ||
            locution instanceof WhyLocution ||
            locution instanceof ArgueLocution ||
            locution instanceof ConcedeLocution ||
            locution instanceof RetractLocution))
        {
            throw new PersuasionDialogueException(String.format("addMoveNode called with unsupported locution type '%s'", locution.getClass().getName()));
        }

        // Find the newMove's target
        IndexedNode<Move<? extends Locution>> target = findNodeByIndex(newMove.getTarget().getIndex());
        if (target == null) {
            // Not in this proposal tree
            return null;
        }

        // Found the target; add a node for this newMove to its children
        target.addChild(new IndexedNode<Move<? extends Locution>>(this, newMove));
        return target;
    }

    public void update(List<Move<? extends Locution>> newMoves) throws DialogueException, PersuasionDialogueException {
        if (this.state == PersuasionDialogueState.Unopened)
        {
            // The first move is supposed to be unique.
            int l = 0;
            if ((l = newMoves.size()) > 1)
            {
                throw new PersuasionDialogueException(String.format("first move in a persuasion dialogue must be unique (length of newMoves = %d)", l));
            }

            // Asserting that the move is validated, and the unique move is safely castable to a Move<ClaimLocution>.
            Move<ClaimLocution> move = (Move<ClaimLocution>) newMoves.get(0);

            // Add the move as the root element.
            this.setRootElement(new IndexedNode<Move<? extends Locution>>(this, move));

            // Switch the dialogue state to active.
            this.state = PersuasionDialogueState.Active;
        }
        else if (this.state == PersuasionDialogueState.Active)
        {
            // Add the new moves to the dialogue.
            for (Move move : newMoves)
            {
                this.addMoveNode(move);
            }
        }
        else
        {
            throw new PersuasionDialogueException("tried to update a terminated dialogue");
        }
    }

    public List<Move<? extends Locution>> getReplies(Move<? extends Locution> move) throws DialogueException {
        IndexedNode<Move<? extends Locution>> node = findNodeByIndex(move.getIndex());
        if (node == null) {
            throw new DialogueException("Asked for the replies of '" + move.toLogicString() + "', but it is not present in this proposal tree.");
        }
        List<Move<? extends Locution>> replies = new ArrayList<Move<? extends Locution>>();
        for (IndexedNode<Move<? extends Locution>> child : node.getChildren()) {
            replies.add(child.getData());
        }
        return replies;
    }

    public boolean isTopicIn() throws DialogueException {
        return this.isIn(this.getRootElement().getData());
    }

    public boolean isIn(Move<? extends Locution> move) throws DialogueException {
        IndexedNode<Move<? extends Locution>> node = findNodeByIndex(move.getIndex());
        if (node == null) {
            throw new DialogueException("Asked for the dialectical status of '" + move.toLogicString() + "', but it is not present in this proposal tree.");
        }

        // Surrendering replies are always out
        if (node.getData().getLocution() instanceof SurrenderingLocution) {
            return false;
        }

        boolean isIn = true;
        List<Participant> attackers = new ArrayList<Participant>();

        // Check for attacking replies
        for (IndexedNode<Move<? extends Locution>> reply : node.getChildren()) {
            if (reply.getData().getLocution() instanceof AttackingLocution && this.isIn(reply.getData())) {
                // Found an attacker that is 'in' (not considering the surrenders)
                isIn = false;
                attackers.add(reply.getData().getPlayer());
            }
        }

        // Check for surrendering replies by agents that also attacked this move
        if (!isIn) {
            for (IndexedNode<Move<? extends Locution>> reply : node.getChildren()) {
                if (reply.getData().getLocution() instanceof SurrenderingLocution) {

                    // Does it fully surrender to the targeting move?
                    // Only concede moves may not surrender to the whole move but only a part
                    if ((reply.getData().getLocution() instanceof ConcedeLocution &&
                            ((ConcedeLocution)reply.getData().getLocution()).getConcededTerm().equals(((ArgueLocution)move.getLocution()).getArgument().getClaim())) ||
                            !(reply.getData().getLocution() instanceof ConcedeLocution)) {

                        // Surrendered by an agent that attacked this move; then remove this from the original attackers list
                        attackers.remove(reply.getData().getPlayer());
                        // If no attacking agents are left, this move is 'in' again
                        if (attackers.size() <= 0) {
                            isIn = true;
                            break;
                        }

                    }

                }
            }
        }

        return isIn;
    }

}
