package nl.uu.cs.arg.persuasion.platform.local.agentimpl;

import nl.uu.cs.arg.persuasion.model.PersuasionAgent;
import nl.uu.cs.arg.persuasion.platform.local.AgentXmlData;
import nl.uu.cs.arg.persuasion.platform.local.agentimpl.SimplePersuasionAgent;

public enum LocalAgent {

    SimplePersuasionAgent {
        @Override public PersuasionAgent createAgent(AgentXmlData xmlDataFile) { return new SimplePersuasionAgent(xmlDataFile); }
    };

    public abstract PersuasionAgent createAgent(AgentXmlData xmlDataFile);

}
