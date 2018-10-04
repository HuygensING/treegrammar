

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.StringReader;

import static java.util.Arrays.asList;

// * author: Ronald Haentjens Dekker
// * date: 11-09-2018
public class XMLValidatorUsingTreeGrammars {

  public void parse(String XML_input) throws XMLStreamException {
    // hier maken we een stax parser aan die de XML in stukjes binnen laat komen
    XMLInputFactory factory = XMLInputFactory.newInstance();
    XMLEventReader reader = factory.createXMLEventReader(new StringReader(XML_input));

    // nu moet ik een state machine creeren, die van de ene naar de andere state gaat
    // door middel van transitierules
    StateMachine stateMachine = new StateMachine();
    createTransitionRules(stateMachine);

    // we gaan de input af, event voor event.
    while (reader.hasNext()) {
      XMLEvent xmlEvent = reader.nextEvent();
      System.out.println("xmlEvent=" + xmlEvent);
      // nu checken we of we van state naar state kunnen gaan.
      if (xmlEvent.isStartElement()) {
        StartElement s = xmlEvent.asStartElement();
        String tag = s.getName().getLocalPart();
        Node node = new TagNode(tag);
        stateMachine.processInput(node);
        // als ik de rule wel kan vinden gebeurt er iets eigenaardigs.
        // dan moet ik de boom aanpassen, maar alleen de laatste level
        // nou ja dat is dus eigenlijk een kind aanpassen en vervangen door een ander
//      } else if (xmlEvent.isCharacters()) {
//        Characters characters = xmlEvent.asCharacters();
//        String content = characters.toString();
//        Node textNode = new TextNode(content);
//        stateMachine.processInput(textNode);
      }
      System.out.println();
    }


  }

  // we moeten verschillende soorten nodes gaan ondersteunen.
  private void createTransitionRules(StateMachine stateMachine) {
    // We maken de non-terminal root aan. (hoofdletters)
    // Die kunen we vervangen door een terminal root (kleine letters) + non-terminal MARKUP node
    // Dit klinkt ingewikkelder dan nodig. hmm
    // de huidige state is dan meer een tree, waarbij steeds een stukje vervangen wordt.
    // Tree zou je eigenlijk een kunnen aanmaken op basis van een string, maar ja nu even niet.

    // 0 => ROOT
    Node lhs0 = new StartNode();
    Tree<Node> rhs0 = new Tree<>(new NonTerminalNode("ROOT"));
    TransitionRule transitionRule0 = new TransitionRule(lhs0, rhs0);
    stateMachine.addTransitionRule(transitionRule0);

    // ROOT => root[MARKUP]
    Node lhs1 = new NonTerminalNode("ROOT");
    Tree<Node> rhs1 = new Tree<>(new TagNode("root"), asList(new NonTerminalNode("MARKUP")));
    TransitionRule transitionRule1 = new TransitionRule(lhs1, rhs1);
    stateMachine.addTransitionRule(transitionRule1);

    // MARKUP => markup[tekst]
    Node lhs2 = new NonTerminalNode("MARKUP");
    Tree<Node> rhs2 = new Tree<>(new TagNode("markup"), asList(new AnyTextNode()));
    TransitionRule transitionRule2 = new TransitionRule(lhs2, rhs2);
    stateMachine.addTransitionRule(transitionRule2);
  }
}
