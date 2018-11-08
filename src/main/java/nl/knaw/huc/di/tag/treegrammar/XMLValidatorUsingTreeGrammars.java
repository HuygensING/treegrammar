package nl.knaw.huc.di.tag.treegrammar;

import nl.knaw.huc.di.tag.treegrammar.nodes.Node;
import nl.knaw.huc.di.tag.treegrammar.nodes.TagNode;
import nl.knaw.huc.di.tag.treegrammar.nodes.TextNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.StringReader;
import java.util.List;

// * author: Ronald Haentjens Dekker
// * date: 11-09-2018
class XMLValidatorUsingTreeGrammars {
  private static final Logger LOG = LoggerFactory.getLogger(XMLValidatorUsingTreeGrammars.class);
  private final StateMachine stateMachine = new StateMachine();

  XMLValidatorUsingTreeGrammars(List<TransitionRule> transitionRules) {
    TransitionRuleSetFactory.validateRuleSet(transitionRules);
    transitionRules.forEach(stateMachine::addTransitionRule);
  }

  public Tree<Node> getTree() {
    return stateMachine.getTree();
  }

  public void parse(String XML_input) throws XMLStreamException {
    // hier maken we een stax parser aan die de XML in stukjes binnen laat komen
    XMLInputFactory factory = XMLInputFactory.newInstance();
    XMLEventReader reader = factory.createXMLEventReader(new StringReader(XML_input));

    // we gaan de input af, event voor event.
    // maar we zijn alleen geïnteresseerd in startElement en characters
    while (reader.hasNext()) {
      XMLEvent xmlEvent = reader.nextEvent();
      LOG.info("xmlEvent={}", xmlEvent);
      if (xmlEvent.isStartElement()) {
        Node tagNode = createTagNode(xmlEvent);
        stateMachine.processInput(tagNode);

      } else if (xmlEvent.isCharacters()) {
        Node textNode = createTextNode(xmlEvent);
        stateMachine.processInput(textNode);
      }
    }
    stateMachine.exit();
  }

  private Node createTextNode(final XMLEvent xmlEvent) {
    String content = xmlEvent.asCharacters()
        .toString();
    return new TextNode(content);
  }

  private Node createTagNode(final XMLEvent xmlEvent) {
    String tag = xmlEvent.asStartElement()
        .getName()
        .getLocalPart();
    return new TagNode(tag);
  }

}
