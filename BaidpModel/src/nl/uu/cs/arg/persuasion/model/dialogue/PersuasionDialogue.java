package nl.uu.cs.arg.persuasion.model.dialogue;

import nl.uu.cs.arg.persuasion.model.dialogue.locutions.ClaimLocution;
import nl.uu.cs.arg.shared.Participant;
import nl.uu.cs.arg.shared.dialogue.DialogueException;
import nl.uu.cs.arg.shared.dialogue.locutions.*;
import nl.uu.cs.arg.shared.util.IndexedNode;
import nl.uu.cs.arg.shared.util.IndexedTree;
import org.aspic.inference.Constant;
import org.aspic.inference.Term;

import java.util.ArrayList;
import java.util.List;

/**
 * Implements a dialogue for persuasion.
 *
 * @author Marlon Etheredge <m.e.etheredge@students.uu.nl>
 * @author erikkok
 */
public class PersuasionDialogue extends IndexedTree<PersuasionMove<? extends Locution>> {

    /**
     * The dialogue topic, for the dialogue to begin, the first move must be a claim topic.
     */
    private Constant topic;

    /**
     * The current state of the persuasion dialogue.
     */
    private PersuasionDialogueState state;

    /**
     * Instantiates a new PersuasionDialogue instance, sets the dialogue state to PersuasionDialogueState.Unopened.
     */
    public PersuasionDialogue(Constant topic)
    {
        // Start a dialogue in unopened state.
        this.state = PersuasionDialogueState.Unopened;

        // Set the topic
        this.topic = topic;
    }

    /**
     * Retrieves the current state of this dialogue.
     *
     * @return Current state
     */
    public PersuasionDialogueState getState() { return this.state; }

    /**
     * Sets the current state of this dialogue.
     *
     * @param state State to set
     * @return State before setting
     */
    public PersuasionDialogueState setState(PersuasionDialogueState state) {
        PersuasionDialogueState oldState = this.state;
        this.state = state;
        return oldState;
    }

    /**
     * Adds a persuasion move to this dialogue. Make sure that the locution of the move to add is not of the types
     *
     * * ClaimLocution
     * * WhyLocution
     * * ArgueLocution
     * * ConcedeLocution
     * * RetractLocution
     *
     * since these locution types are for deliberation. A PersuasionDialogueException will be thrown upon attempt to
     * add a move featuring one of these types.
     *
     * @param newPersuasionMove Move to add to this dialogue
     * @return The target of the new move
     * @throws PersuasionDialogueException
     */
    public IndexedNode<PersuasionMove<? extends Locution>> addPersuasionMoveNode(PersuasionMove<? extends Locution> newPersuasionMove) throws PersuasionDialogueException {
        // Persuasion does allow for the locutions:
        // - claim
        // - why
        // - argue
        // - concede
        // - retract
        Locution locution = newPersuasionMove.getLocution();
        if (!(locution instanceof ClaimLocution ||
            locution instanceof WhyLocution ||
            locution instanceof ArgueLocution ||
            locution instanceof ConcedeLocution ||
            locution instanceof RetractLocution))
        {
            throw new PersuasionDialogueException(String.format("addPersuasionMoveNode called with unsupported locution type '%s'", locution.getClass().getName()));
        }

        // Find the newPersuasionMove's target
        IndexedNode<PersuasionMove<? extends Locution>> target = this.findNodeByIndex(newPersuasionMove.getTarget().getIndex());
        if (target == null) {
            // Not in this proposal tree
            return null;
        }

        // Found the target; add a node for this newPersuasionMove to its children
        target.addChild(new IndexedNode<PersuasionMove<? extends Locution>>(this, newPersuasionMove));
        return target;
    }

    /**
     * Updates the dialogue with a set of new persuasion moves. When the current state of this dialogue is Unopened,
     * the input list of new persuasion moves is assumed to be of size 1, since the first move is assumed to be unique
     * (following Prakken). Upon attempt of moving multiple moves, a PersuasionDialogueException will be thrown.
     * If the current state of this dialogue is Terminated, this method will throw a PersuasionDialogueException.
     *
     * @param newPersuasionMoves
     * @throws DialogueException
     * @throws PersuasionDialogueException
     */
    public void update(List<PersuasionMove<? extends Locution>> newPersuasionMoves) throws PersuasionDialogueException {
        if (this.state == PersuasionDialogueState.Active)
        {
            // We have not yet received the first move
            if (this.getRootElement() == null) {
                // The first PersuasionMove is supposed to be unique.
                int l = 0;
                if ((l = newPersuasionMoves.size()) > 1) {
                    throw new PersuasionDialogueException(String.format("first PersuasionMove in a persuasion dialogue must be unique (length of newPersuasionMoves = %d)", l));
                }

                // Asserting that the PersuasionMove is validated, and the unique PersuasionMove is safely castable to a PersuasionMove<ClaimLocution>.
                PersuasionMove<ClaimLocution> move = (PersuasionMove<ClaimLocution>) newPersuasionMoves.get(0);

                // TODO: Want to specify this as a persuasion rule
                if (move.getLocution().getProposition() != this.topic) {
                    throw new PersuasionDialogueException("first move must be a claim move for topic");
                }

                // Add the PersuasionMove as the root element.
                this.setRootElement(new IndexedNode<PersuasionMove<? extends Locution>>(this, move));
            } else {
                // Add the new PersuasionMoves to the dialogue.
                for (PersuasionMove PersuasionMove : newPersuasionMoves) {
                    this.addPersuasionMoveNode(PersuasionMove);
                }
            }
        } else {
            throw new PersuasionDialogueException("tried to update a dialogue in an invalid state");
        }
    }

    /**
     * Retrieves a set of persuasion moves that are replies to the input persuasion move parameter.
     * If the specified move is not present in this dialogue tree, a PersuasionDialogueException will be thrown.
     *
     * @param move The move to retrieve replies for
     * @return List of replies to the input move
     * @throws DialogueException
     */
    public List<PersuasionMove<? extends Locution>> getReplies(PersuasionMove<? extends Locution> move) throws PersuasionDialogueException {
        // Fetch the specified move's node in this dialogue tree
        IndexedNode<PersuasionMove<? extends Locution>> node = findNodeByIndex(move.getIndex());
        if (node == null) {
            throw new PersuasionDialogueException(String.format("requested replies for %s, but this move is not present in the dialogue tree", move.toString()));
        }

        // Fetch the replies
        List<PersuasionMove<? extends Locution>> replies = new ArrayList<PersuasionMove<? extends Locution>>();
        for (IndexedNode<PersuasionMove<? extends Locution>> child : node.getChildren()) {
            replies.add(child.getData());
        }

        return replies;
    }

    /**
     * Returns the topic of this dialogue. The topic of the dialogue is determined by the first move of the dialogue
     * which is assumed to by a ClaimLocution-holding move.
     *
     * @return The topic of the dialogue
     */
    public Constant getTopic() {
        return this.topic;
    }

    public boolean isRepeatedMove(PersuasionMove<? extends Locution> newMove, IndexedNode<PersuasionMove<? extends Locution>> node) {
        PersuasionMove<? extends Locution> move = node.getData();
        if (newMove.getLocution().getClass().equals(move.getLocution().getClass())) {
            if (move.getLocution() instanceof ClaimLocution && ((ClaimLocution) move.getLocution()).getProposition().equals(((ClaimLocution)newMove.getLocution()).getProposition())) {
                return true;
            } else if (move.getLocution() instanceof ArgueLocution && ((ArgueLocution) move.getLocution()).getArgument().isSemanticallyEqual(((ArgueLocution)newMove.getLocution()).getArgument())) {
                return true;
            } else if (move.getLocution() instanceof WhyLocution && ((WhyLocution) move.getLocution()).getAttackedPremise().isEqualModuloVariables(((WhyLocution)newMove.getLocution()).getAttackedPremise())) {
                return true;
            }
        }

        for (IndexedNode<PersuasionMove<? extends Locution>> child : node.getChildren()) {
            if (this.isRepeatedMove(newMove, child)) {
                return true;
            }
        }

        return false;
    }

    public boolean isRepeatedMove(PersuasionMove<? extends Locution> newMove) {
        if (getRootElement() == null) {
            return false;
        }

        return this.isRepeatedMove(newMove, getRootElement());
    }

    /**
     * Returns a flag specifying whether or not the first move of the dialogue is in.
     *
     * @return Flag indicating whether or not the first move of the dialogue is in
     * @throws DialogueException
     */
    public boolean isTopicIn() throws PersuasionDialogueException {
        return this.isIn(this.getRootElement().getData());
    }

    /**
     * Returns a flag specifying whether or not the specified move is in.
     *
     * @param move Move to retrun the dialectical status of
     * @return Flag indicating the dialectical status of the specified move
     * @throws DialogueException
     */
    public boolean isIn(PersuasionMove<? extends Locution> move) throws PersuasionDialogueException {
        IndexedNode<PersuasionMove<? extends Locution>> node = findNodeByIndex(move.getIndex());
        if (node == null) {
            throw new PersuasionDialogueException(String.format("requested dialectical status of %s, but the move is not present in this dialogue tree", move.toString()));
        }

        // Definition:
        // ... Such a move m is in iff:
        //
        // 1). m is surrendered in d; or else
        // 2). all attacking replies to m are out
        //
        // otherwise m is out.

        // If the specified move is a SurrenderingLocution-holding move, the move is always out
        if (node.getData().getLocution() instanceof SurrenderingLocution) {
            return false;
        }

        boolean isIn = true;
        List<Participant> attackers = new ArrayList<Participant>();

        // Keep track of the attackers of this move
        for (IndexedNode<PersuasionMove<? extends Locution>> reply : node.getChildren()) {
            if (reply.getData().getLocution() instanceof AttackingLocution && this.isIn(reply.getData())) {
                // Found an attacker that is 'in' (not considering the surrenders)
                isIn = false;
                attackers.add(reply.getData().getPlayer());
            }
        }


        if (!isIn) {
            // Check, for every reply to this move if the move is a concede move, in which case the concede could
            // only partially concede, if this is not the case, or the move is not a concede-containing move, an
            // attempt is made to remove the player of that move from the set of attackers, if |attackers| becomes
            // 0, this is an indication that every attacker in the set of attackers has surrendered to the specified
            // move.
            for (IndexedNode<PersuasionMove<? extends Locution>> reply : node.getChildren()) {
                Locution locution = reply.getData().getLocution();

                if (locution instanceof SurrenderingLocution) {
                    /*if ((reply.getData().getLocution() instanceof ConcedeLocution &&
                            ((ConcedeLocution) reply.getData().getLocution()).getConcededConstant().equals(((ArgueLocution) move.getLocution()).getArgument().getClaim())) ||
                            !(reply.getData().getLocution() instanceof ConcedeLocution)) {

                        // Surrendered by an agent that attacked this move; then remove this from the original attackers list
                        attackers.remove(reply.getData().getPlayer());
                        // If no attacking agents are left, this move is 'in' again
                        if (attackers.size() <= 0) {
                            isIn = true;
                            break;
                        }

                    }*/
                    // TODO: Fix me
                }
            }
        }

        return isIn;
    }

    /**
     * Returns a list containing the attackers of the first move of this dialogue.
     *
     * @return Attackers of the first move of this dialogue
     */
    public List<PersuasionMove<? extends Locution>> getActiveAttackers() {
        List<PersuasionMove<? extends Locution>> attackers = new ArrayList<PersuasionMove<? extends Locution>>();
        try {
            // Find all active attackers in this dialogue (including the first move, if it is 'in')
            this.fillActiveAttackersList(attackers, getRootElement(), true);
        } catch (PersuasionDialogueException e) {
            e.printStackTrace();
        }
        return attackers;
    }

    private void fillActiveAttackersList(List<PersuasionMove<? extends Locution>> attackers, IndexedNode<PersuasionMove<? extends Locution>> node, boolean parentIsActiveAttacker) throws PersuasionDialogueException {
        // Add every attacking reply that is 'in'
        if (node.getData().getLocution() instanceof AttackingLocution && this.isIn(node.getData())) {
            attackers.add(node.getData());

            // For active attackers, also look into its children for active attackers
            for (IndexedNode<PersuasionMove<? extends Locution>> reply : node.getChildren()) {
                this.fillActiveAttackersList(attackers, reply, true);
            }
        } else if (parentIsActiveAttacker) {
            // For active attackers, also look into its children for active attackers
            for (IndexedNode<PersuasionMove<? extends Locution>> reply : node.getChildren()) {
                this.fillActiveAttackersList(attackers, reply, false);
            }
        }
    }

    public boolean isStarted() {
        return this.getRootElement() != null;
    }

    public String toDot(IndexedNode<PersuasionMove<? extends Locution>> node) {
        String s = "";
        for (IndexedNode<PersuasionMove<? extends Locution>> child : node.getChildren()) {
            s += "\"" + child.getData().getTarget().toSimpleString() + "\" -> \"" + child.getData().toSimpleString() + "\";\n";
            s += this.toDot(child);
        }
        return s;
    }

    public String toDot() {
        String s = "digraph g {\n";
        s += this.toDot(this.getRootElement());
        s += "}";
        return s;
    }

}
