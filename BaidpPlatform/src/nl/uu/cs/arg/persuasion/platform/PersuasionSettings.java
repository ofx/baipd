package nl.uu.cs.arg.persuasion.platform;

import java.util.List;

import nl.uu.cs.arg.persuasion.model.dialogue.protocol.PersuasionOutcomeSelectionRule;
import nl.uu.cs.arg.persuasion.model.dialogue.protocol.PersuasionRule;
import nl.uu.cs.arg.persuasion.model.dialogue.protocol.PersuasionTerminationRule;
import nl.uu.cs.arg.shared.dialogue.protocol.DeliberationRule;
import nl.uu.cs.arg.shared.dialogue.protocol.OutcomeSelectionRule;
import nl.uu.cs.arg.shared.dialogue.protocol.TerminationRule;

public class PersuasionSettings {

    public final static String APPLICATION_NAME = "Baidp";

    public final static String APPLICATION_VERSION = "0.9";

    public final static String APPLICATION_NAME_VERSION = APPLICATION_NAME + " " + APPLICATION_VERSION;

    public PersuasionSettings(List<PersuasionRule> deliberationRules, List<PersuasionTerminationRule> terminationRules, PersuasionOutcomeSelectionRule outcomeSelectionRule) {
        this.persuasionRules = deliberationRules;
        this.terminationRules = terminationRules;
        this.outcomeSelectionRule = outcomeSelectionRule;
    }

    private final List<PersuasionRule> persuasionRules;
    public List<PersuasionRule> getPersuasionRules() {
        return persuasionRules;
    }

    private final List<PersuasionTerminationRule> terminationRules;
    public List<PersuasionTerminationRule> getTerminationRules() {
        return terminationRules;
    }

    private final PersuasionOutcomeSelectionRule outcomeSelectionRule;
    public PersuasionOutcomeSelectionRule getOutcomeSelectionRule() {
        return outcomeSelectionRule;
    }

}
