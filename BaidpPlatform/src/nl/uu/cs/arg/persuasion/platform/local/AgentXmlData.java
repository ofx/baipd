package nl.uu.cs.arg.persuasion.platform.local;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.aspic.inference.KnowledgeBase;
import org.aspic.inference.parser.ParseException;
import org.aspic.inference.parser.PrologSyntax;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class AgentXmlData {

    private static final String XML_AGENT = "baidp-agent";
    private static final String XML_BELIEFBASE = "beliefbase";
    private static final String XML_PROPERTIES = "properties";
    private static final String XML_PROPERTY = "property";
    private static final String XML_NAME = "name";
    private static final String XML_TYPE = "type";

    private String name;

    private KnowledgeBase beliefBase;

    private Map<String, Object> properties;

    public static AgentXmlData loadAgentDataFromXml(String name, File xmlFile) throws SAXException, IOException, ParserConfigurationException, ParseException {
        // Read the XML file using a DOM parser
        DocumentBuilderFactory xmlDocFactory = DocumentBuilderFactory.newInstance();
        //xmlDocFactory.setCoalescing(true);
        xmlDocFactory.setIgnoringComments(true);
        //xmlDocFactory.setIgnoringElementContentWhitespace(true);
        Document xmlDoc = xmlDocFactory.newDocumentBuilder().parse(xmlFile);
        PrologSyntax prologSyntax = new PrologSyntax(new StringReader("")); // A reuseable Prolog syntax parser

        // Read the <beliefbase> tag
        NodeList bbTags = xmlDoc.getElementsByTagName(XML_BELIEFBASE);
        KnowledgeBase beliefbase = null;
        if (bbTags != null && bbTags.getLength() > 0 && bbTags.item(0).getChildNodes().getLength() > 0) {
            for (int i = 0; i < bbTags.item(0).getChildNodes().getLength(); i++) {
                Node node = bbTags.item(0).getChildNodes().item(i);
                if (node != null && node.getTextContent() != null) {
                    // Read the string contents into a parsed KnowledgeBase
                    String content = node.getTextContent().trim();
                    if (!content.equals("")) {
                        prologSyntax.ReInit(new StringReader(content));
                        beliefbase = prologSyntax.Knowledge();
                    }
                }
            }
        }
        if (beliefbase == null) {
            beliefbase = new KnowledgeBase();
        }

        // Read the <properties> tag
        NodeList propTags = xmlDoc.getElementsByTagName(XML_PROPERTIES);
        Map<String, Object> properties = new HashMap<String, Object>();
        if (propTags != null && propTags.getLength() > 0) {
            NodeList propTag = propTags.item(0).getChildNodes();
            if (propTag != null && propTag.getLength() > 0) {
                for (int i = 0; i < propTag.getLength(); i++) {
                    // Properties should have the format <property name="{name}" type="{type}">{value}</property>
                    // {name} is the property name (as String)
                    // {type} is the data type for the property value (used to parse the value)
                    // {value} is the actual property value
                    String optText = propTag.item(i).getTextContent().trim();
                    if (optText != null && !optText.equals("") && propTag.item(i).getAttributes().getLength() > 0) {
                        String optName = propTag.item(i).getAttributes().getNamedItem(XML_NAME).getNodeValue();
                        String optType = propTag.item(i).getAttributes().getNamedItem(XML_TYPE).getNodeValue();
                        if (optType != null && optType.toLowerCase().equals("boolean")) {
                            properties.put(optName, Boolean.parseBoolean(optText));
                        } else if (optType != null && optType.toLowerCase().equals("integer")) {
                            properties.put(optName, Integer.parseInt(optText));
                        } else if (optType != null && optType.toLowerCase().equals("double")) {
                            properties.put(optName, Double.parseDouble(optText));
                        } else {
                            properties.put(optName, optText);
                        }
                    }
                }
            }
        }

        return new AgentXmlData(name, beliefbase, properties);
    }

    public AgentXmlData(String name, KnowledgeBase beliefBase, Map<String, Object> properties) {
        this.name = name;
        this.beliefBase = beliefBase;
        this.properties = properties;
    }

    public String getName() {
        return name;
    }

    public KnowledgeBase getBeliefBase() {
        return beliefBase;
    }

    public Map<String, Object> getRawProperties() {
        return properties;
    }
}
