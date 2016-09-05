package nl.uu.cs.arg.shared.dialogue.locutions;

import org.aspic.inference.Constant;

/**
 * The retract(p) locution is used to retract a previous term p
 * that was proposed as conclusion is some argue(A => p) or as a
 * premise as element of the set A in some argue(A => 1) move. In 
 * effect, the argue move that it was used in is no longer 'in'.
 * 
 * @author erickok
 *
 */
public final class RetractLocution extends SurrenderingLocution {

	private static final String LOCUTION_NAME = "retract";
	
	/**
	 * Some term that was either the conclusion or a premise used in an argue(A => p) move
	 */
	private Constant retractedProposition;
	
	public RetractLocution(Constant retractedProposition) {
		super(LOCUTION_NAME);
		this.retractedProposition = retractedProposition;
	}
	
	/**
	 * Returns the conceded term as used (as conclusion or premise) in the argue move this locution replies to
	 * @return The term that was conceded
	 */
	public Constant getRetractedTerm() {
		return this.retractedProposition;
	}

	@Override
	public String toLogicString() {
		return getName() + "(" + getRetractedTerm().inspect() + ")";
	}

	@Override
	public String toSimpleString() {
		return "retract " + this.retractedProposition;
	}

}
