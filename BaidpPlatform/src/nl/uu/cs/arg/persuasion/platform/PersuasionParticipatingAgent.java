package nl.uu.cs.arg.persuasion.platform;

import nl.uu.cs.arg.persuasion.model.PersuasionAgent;
import nl.uu.cs.arg.persuasion.model.PersuasionParticipant;
import nl.uu.cs.arg.shared.Agent;
import nl.uu.cs.arg.shared.Participant;

/**
 * The glue between the {@link Participant}, which is a data structure
 * object, and the {@link Agent} which is an operational object. Use the
 * createParticipant() function to create instances.
 *
 * @author erickok
 *
 */
public class PersuasionParticipatingAgent {

    /**
     * Internal participant reference
     */
    private PersuasionParticipant participant;

    /**
     * Internal agent reference
     */
    private PersuasionAgent agent;

    private PersuasionParticipatingAgent(PersuasionParticipant participant, PersuasionAgent agent) {
        this.participant = participant;
        this.agent = agent;
    }

    /**
     * Creates a participant object for an agent and provides the actual coupling between the two classes
     * @param agent The agent
     * @param id The unique participant ID to be assigned to the agent in this dialogue
     * @return A couple object between some {@link Agent} and its data representation as a {@link Participant}
     */
    public static PersuasionParticipatingAgent createParticipant(PersuasionAgent agent, int id) {
        return new PersuasionParticipatingAgent(new PersuasionParticipant(id, agent.getName()), agent);
    }

    public PersuasionParticipant getParticipant() {
        return this.participant;
    }

    public PersuasionAgent getAgent() {
        return this.agent;
    }

    /**
     * A participating agents equals another if their participant ID's match
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PersuasionParticipatingAgent) {
            return ((PersuasionParticipatingAgent) obj).getParticipant().getID() == this.getParticipant().getID();
        }
        return super.equals(obj);
    }

    public String toString() {
        return "<" + this.getParticipant().toString() + ", " + this.getAgent().toString() + ">";
    }

}
