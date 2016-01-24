package nl.uu.cs.arg.persuasion.model;

import nl.uu.cs.arg.shared.Participant;
import org.aspic.inference.Constant;

import java.util.List;

public class PersuasionParticipant extends Participant {

    private List<Constant> commitments;

    public PersuasionParticipant(int id, String name) {
        super(id, name);
    }

    public void addCommitment(Constant constant) { this.commitments.add(constant); }

    public void removeCommitment(Constant constant) { this.commitments.remove(constant); }

    public boolean isCommittedTo(Constant constant) { return this.commitments.contains(constant); }

}
