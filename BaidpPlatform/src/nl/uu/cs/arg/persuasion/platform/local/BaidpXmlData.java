package nl.uu.cs.arg.persuasion.platform.local;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import nl.uu.cs.arg.persuasion.model.PersuasionAgent;
import nl.uu.cs.arg.persuasion.model.dialogue.protocol.PersuasionOutcomeSelectionRule;
import nl.uu.cs.arg.persuasion.model.dialogue.protocol.PersuasionRule;

import nl.uu.cs.arg.persuasion.model.dialogue.protocol.PersuasionTerminationRule;
import org.aspic.inference.Constant;
import org.aspic.inference.Term;
import org.aspic.inference.Variable;
import org.aspic.inference.parser.ParseException;
import org.aspic.inference.parser.PrologSyntax;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class BaidpXmlData {

    private static final String FILEEXT_AGENT = ".agent";
    private static final String XML_MAS = "baidp-mas";
    private static final String XML_TOPIC = "topic";
    private static final String XML_PROTOCOL = "protocol";
    private static final String XML_RULE = "rule";
    private static final String XML_OUTCOME = "outcome-rule";
    private static final String XML_PERSUASION = "persuasion-rules";
    private static final String XML_TERMINATION = "termination-rules";
    private static final String XML_AGENTS = "agents";
    private static final String XML_AGENT = "agent";
    private static final String XML_NAME = "name";
    private static final String XML_TYPE = "type";
    private static final String XML_FILE = "file";

    private final Constant topic;

    private final List<PersuasionAgent> localAgents;

    private final List<AgentXmlData> agentXmlDatas;

    private final List<PersuasionRule> persuasionRules;

    private final List<PersuasionTerminationRule> terminationRules;

    private final PersuasionOutcomeSelectionRule outcomeSelectionRule;

    public static BaidpXmlData loadAgentDataFromXml(File xmlFile) throws SAXException, IOException, ParserConfigurationException, ParseException {
        // Read the XML file using a DOM parser
        DocumentBuilderFactory xmlDocFactory = DocumentBuilderFactory.newInstance();
        //xmlDocFactory.setCoalescing(true);
        xmlDocFactory.setIgnoringComments(true);
        xmlDocFactory.setIgnoringElementContentWhitespace(true);
        Document xmlDoc = xmlDocFactory.newDocumentBuilder().parse(xmlFile);
        PrologSyntax prologSyntax = new PrologSyntax(new StringReader("")); // A reusable Prolog syntax parser

        // Read the <topic> tag
        NodeList topicTags = xmlDoc.getElementsByTagName(XML_TOPIC);
        Constant topic = null;
        if (topicTags != null && topicTags.getLength() > 0) {
            // Read the topic text
            Node topicTag = topicTags.item(0);
            String topicText = topicTag.getTextContent();
            if (topicText != null && !topicText.equals("")) {
                prologSyntax.ReInit(new StringReader(topicText));
                topic = prologSyntax.Term();
            }
        }

        // If no topic was specified: throw an exception (note that the topic goal is allow to be null, for now)
        if (topic == null) {
            throw new ParseException("No topic was specified in the XML file.");
        }

        // Read the protocol (outcome, deliberation and termination) rules
        PersuasionOutcomeSelectionRule outcomeRule = PersuasionOutcomeSelectionRule.ConflictResolution;
        List<PersuasionRule> persuasionRules = new ArrayList<PersuasionRule>();
        List<PersuasionTerminationRule> terminationRules = new ArrayList<PersuasionTerminationRule>();
        NodeList protocolTags = xmlDoc.getElementsByTagName(XML_PROTOCOL);
        if (protocolTags != null && protocolTags.getLength() > 0) {

            // The outcome rule is an attribute to the <protocol> tag
            Node protocolNode = protocolTags.item(0);
            if (protocolNode.hasAttributes()) {
                Node outcomeItem = protocolNode.getAttributes().getNamedItem(XML_OUTCOME);
                if (outcomeItem != null) {
                    outcomeRule = PersuasionOutcomeSelectionRule.valueOf(outcomeItem.getTextContent());
                }
            }

            // The <protocol> tag may have a <deliberation-rules> and a <termination-rules> tag
            NodeList rulesTags = protocolTags.item(0).getChildNodes();
            for (int i = 0; i < rulesTags.getLength(); i++) {
                Node ruleTag = rulesTags.item(i);

                // Depending on the tag name, add its child nodes, which are rules, to the correct rules list
                NodeList ruleNodes = ruleTag.getChildNodes();
                for (int j = 0; j < ruleNodes.getLength(); j++) {
                    String ruleContent = ruleNodes.item(j).getTextContent().trim();

                    if (ruleContent != null && !ruleContent.equals("")) {
                        if (ruleTag.getNodeName() == XML_PERSUASION) {
                            persuasionRules.add
                                    (PersuasionRule.valueOf(ruleContent));

                        } else if (ruleTag.getNodeName() == XML_TERMINATION) {
                            terminationRules.add(
                                    PersuasionTerminationRule.valueOf(ruleContent));
                        }
                    }
                }
            }
        }


        // Read all <agent> tags
        ArrayList<MasXmlAgentLine> agentLines = new ArrayList<MasXmlAgentLine>();
        NodeList agentTags = xmlDoc.getElementsByTagName(XML_AGENT);
        for (int i = 0; i < agentTags.getLength(); i++) {

            // Read the attributes of this <agent> tag
            NamedNodeMap agentAttributes = agentTags.item(i).getAttributes();
            if (agentAttributes != null && agentAttributes.getLength() > 0) {
                Node name = agentAttributes.getNamedItem(XML_NAME);
                Node type = agentAttributes.getNamedItem(XML_TYPE);
                Node file = agentAttributes.getNamedItem(XML_FILE);

                // Add the read <agent> line with the available attribute values
                agentLines.add(
                        new MasXmlAgentLine(
                                (name != null? name.getNodeValue(): null),
                                (type != null? type.getNodeValue(): null),
                                (file != null? file.getNodeValue(): null)));
            }
        }

        // Read a list local agent types and XML agent data filenames and instantiate the LocalAgents
        ArrayList<PersuasionAgent> typedLocalAgents = new ArrayList<PersuasionAgent>();
        ArrayList<AgentXmlData> xmlAgents = new ArrayList<AgentXmlData>();
        for (MasXmlAgentLine rawLocalAgent : agentLines) {

            // Read the agent XML specification file
            AgentXmlData xmlAgent = AgentXmlData.loadAgentDataFromXml(
                    rawLocalAgent.getName(),
                    new File(xmlFile.getParent() + File.separator + rawLocalAgent.getFile()));
            xmlAgents.add(xmlAgent);

            String a = rawLocalAgent.getType();
            LocalAgent agent = LocalAgent.valueOf(a);

            // Instantiate a LocalAgent based on the type (the String is converted to an LocalAgent type)
            typedLocalAgents.add(agent.createAgent(xmlAgent));
        }

        return new BaidpXmlData(topic, typedLocalAgents, xmlAgents, persuasionRules, terminationRules, outcomeRule);
    }

    public BaidpXmlData(Constant topic, List<PersuasionAgent> localAgents, List<AgentXmlData> agentXmlDatas, List<PersuasionRule> deliberationRules, List<PersuasionTerminationRule> terminationRules, PersuasionOutcomeSelectionRule outcomeSelectionRule) {
        this.topic = topic;
        this.localAgents = localAgents;
        this.agentXmlDatas = agentXmlDatas;
        this.persuasionRules = deliberationRules;
        this.terminationRules = terminationRules;
        this.outcomeSelectionRule = outcomeSelectionRule;
    }

    public Constant getTopic() {
        return this.topic;
    }

    public List<PersuasionAgent> getLocalAgents() {
        return this.localAgents;
    }

    public List<AgentXmlData> getAgentXmlDatas() {
        return this.agentXmlDatas;
    }

    public List<PersuasionRule> getPersuasionRules() {
        return this.persuasionRules;
    }

    public List<PersuasionTerminationRule> getTerminationRules() {
        return this.terminationRules;
    }

    public PersuasionOutcomeSelectionRule getOutcomeSelectionRule() {
        return this.outcomeSelectionRule;
    }

    private static class MasXmlAgentLine {
        private String name;
        private String type;
        private String file;

        public MasXmlAgentLine(String name, String type, String file) {
            this.name = name;
            this.type = type;
            this.file = file;
        }

        public String getName() { return this.name; }
        public String getType() { return this.type; }
        public String getFile() { return this.file; }
    }
}
