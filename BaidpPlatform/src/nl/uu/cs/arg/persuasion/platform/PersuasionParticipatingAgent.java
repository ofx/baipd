package nl.uu.cs.arg.persuasion.platform;

import nl.uu.cs.arg.persuasion.model.PersuasionAgent;
import nl.uu.cs.arg.persuasion.model.PersuasionParticipant;
import nl.uu.cs.arg.shared.Agent;
import nl.uu.cs.arg.shared.Participant;

public class PersuasionParticipatingAgent {

    private PersuasionParticipant participant;

    private PersuasionAgent agent;

    private PersuasionParticipatingAgent(PersuasionParticipant participant, PersuasionAgent agent) {
        this.participant = participant;
        this.agent = agent;
    }

    public static PersuasionParticipatingAgent createParticipant(PersuasionAgent agent, int id) {
        return new PersuasionParticipatingAgent(new PersuasionParticipant(id, agent.getName()), agent);
    }

    public PersuasionParticipant getParticipant() {
        return this.participant;
    }

    public PersuasionAgent getAgent() {
        return this.agent;
    }

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
