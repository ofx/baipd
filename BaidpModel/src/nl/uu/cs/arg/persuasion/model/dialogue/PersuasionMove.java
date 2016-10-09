package nl.uu.cs.arg.persuasion.model.dialogue;

import nl.uu.cs.arg.persuasion.model.PersuasionParticipant;
import nl.uu.cs.arg.shared.dialogue.locutions.Locution;
import nl.uu.cs.arg.shared.dialogue.locutions.SurrenderingLocution;
import nl.uu.cs.arg.shared.util.IndexedObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A Move is a single move in a dialogue played by an agent. It is defined
 * by a {@link Locution}, which also contains the contents, and meta-data
 * such as the id and target move. It cannot be instantiated directly, but
 * only via the buildMove method.
 *
 * @author erickok
 *
 */
public class PersuasionMove<T extends Locution> implements IndexedObject {

    private Object attitude;

    /**
     * A counter used to create unique move identifiers; buildMove() will use and increment this
     */
    private static int uniqueMoveCounter = 0;

    /**
     * The index of the move in the sequence of moves that are added to the dialogue.
     */
    private long sequenceIndex;

    /**
     * The internal, dialogue-unique identifier for a single move.
     */
    private long uniqueMoveIndex;

    /**
     * The agent that played this move.
     */
    private PersuasionParticipant player;

    /**
     * The {@link Move} that this move is a reply to.
     */
    private PersuasionMove<? extends Locution> target;

    /**
     * The locution played in this move, which contains the contents as
     * well, such as the actual {@link Argument}.
     */
    private T locution;

    /**
     * The set of moves that surrended to this move.
     */
    private List<PersuasionMove<? extends SurrenderingLocution>> surrenders = new ArrayList<PersuasionMove<? extends SurrenderingLocution>>();

    /**
     * Indicates that a move has surrendered to this move.
     *
     * @param surrender Move that surrenders to this move.
     */
    public void addSurrender(PersuasionMove<? extends SurrenderingLocution> surrender) { this.surrenders.add(surrender); }

    private boolean hasSurrenderedTarget(PersuasionParticipant participant)
    {
        if (this.getTarget() == null)
        {
            return false;
        }

        if (this.getTarget().hasSurrendered(participant))
        {
            return true;
        }

        return this.getTarget().hasSurrenderedTarget(participant);
    }

    /**
     * Checks whether a participant has surrendered to this move.
     *
     * @param participant
     * @return
     */
    public boolean hasSurrendered(PersuasionParticipant participant)
    {
        /*if (this.hasSurrenderedTarget(participant))
        {
            return true;
        }*/

        for (PersuasionMove<? extends SurrenderingLocution> surrender : surrenders) {
            if (surrender.getPlayer() == participant) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns the set of moves that surrended to this move.
     *
     * @return The set of moves that surrended to this move.
     */
    public List<PersuasionMove<? extends SurrenderingLocution>> getSurrenders() { return this.surrenders; }

    /**
     * Returns the internal, dialogue-unique identifier for a single move
     * @return The move id
     */
    public long getIndex() {
        return this.uniqueMoveIndex;
    }

    public void setSequenceIndex(long index) { this.sequenceIndex = index; }

    /**
     * Returns the player of this move
     * @return The participant object of the agent that player this move
     */
    public PersuasionParticipant getPlayer() {
        return this.player;
    }

    /**
     * Returns the target move to which this move is a reply to
     * @return The targeted move, or null if it had no target (e.g. an join-dialogue move)
     */
    public PersuasionMove<? extends Locution> getTarget() {
        return this.target;
    }

    /**
     * Return the wrapped locution in this dialogue move
     * @return The locution object that was played with this move
     */
    public T getLocution() {
        return this.locution;
    }

    public Object getAttitude() { return this.attitude; }

    public void setAttitude(Object attitude) { this.attitude = attitude; }

    /**
     * Internal, default constructor
     * @param id The unique move id
     * @param player The participant that plays this move
     * @param target The move this is a reply to (or null)
     * @param locution The move contents, which is a locution of some sort
     */
    private PersuasionMove(int id, PersuasionParticipant player, PersuasionMove<? extends Locution> target, T locution) {
        this.uniqueMoveIndex = id;
        this.player = player;
        this.target = target;
        this.locution = locution;
    }

    /**
     * Create a new Move to be submitted to the dialogue. This also assigns a
     * unique ID and creates the internal locution, which details can be
     * assigned afterwards
     * @param player The participant that plays this move
     * @param targetMove The move this is a reply to (or null)
     * @param locutionType The type of locution that will be contained in this move
     * @return The constructed Move, of which its locution is instantiated, but should still be assigned details to (e.g. the attacked premise of a why locution)
     */
    public static <T extends Locution> PersuasionMove<T> buildMove(PersuasionParticipant player, PersuasionMove<? extends Locution> targetMove, T locution) {
        // Note that the move id counter is incremented (after the Move object was instantiated)
        return new PersuasionMove<T>(uniqueMoveCounter++, player, targetMove, locution);
    }

    /**
     * Resets the internal move counter; to be used when starting a new dialogue (platform)
     */
    public static void resetMoveCounter() {
        uniqueMoveCounter = 0;
    }

    /**
     * Returns a string representing this move's contents in the form '(moveid) playername: locution(contents) --> targetmoveid'
     * @return A formatted and human-readable string of this move
     */
    public String toLogicString() {
        return "(" + sequenceIndex + ") " + (player == null? "<system>": player.getName()) + ": " + locution.toLogicString() + (target != null? " --> " + target.getIndex(): "");
    }

    public String toString() {
        return this.toLogicString();
    }

    public String toSimpleString() {
        return "(" + sequenceIndex + ") " + (player == null? "<system>": player.getName()) + ": " + locution.toSimpleString();
    }

}
