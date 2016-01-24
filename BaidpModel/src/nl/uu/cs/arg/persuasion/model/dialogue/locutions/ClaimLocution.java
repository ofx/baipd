package nl.uu.cs.arg.persuasion.model.dialogue.locutions;

import nl.uu.cs.arg.shared.dialogue.locutions.AttackingLocution;
import org.aspic.inference.Constant;

import java.util.Set;

/**
 * Claim locution.
 */
public class ClaimLocution extends AttackingLocution {

    private static final String LOCUTION_NAME = "claim";

    private Constant proposition;

    public ClaimLocution(Constant constant) {
        super(LOCUTION_NAME);
        this.proposition = constant;
    }

    public Constant getProposition() { return this.proposition; }

    @Override
    public void gatherPublicBeliefs(Set<Constant> exposedBeliefs) { exposedBeliefs.add(this.proposition); }

    @Override
    public String toLogicString() { return this.getName() + "(" + this.getProposition().inspect() + ")"; }
}
