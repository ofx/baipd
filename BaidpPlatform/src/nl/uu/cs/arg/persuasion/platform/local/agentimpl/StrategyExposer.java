package nl.uu.cs.arg.persuasion.platform.local.agentimpl;

import org.aspic.inference.Constant;

import java.util.Map;
import java.util.Set;

public interface StrategyExposer {
    Set<Constant> getInitialBeliefs();

    Map<String, Object> getStategyProperties();
}
