import nodes.*;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.StringReader;
import java.util.Collections;
import java.util.List;

// * author: Ronald Haentjens Dekker
// * date: 11-09-2018
class XMLValidatorUsingTreeGrammars {
  private final StateMachine stateMachine = new StateMachine();

  XMLValidatorUsingTreeGrammars(List<TransitionRule> transitionRules) {
    TransitionRuleFactory.validateRuleSet(transitionRules);
    transitionRules.forEach(stateMachine::addTransitionRule);
  }

  public void parse(String XML_input) throws XMLStreamException {
    // hier maken we een stax parser aan die de XML in stukjes binnen laat komen
    XMLInputFactory factory = XMLInputFactory.newInstance();
    XMLEventReader reader = factory.createXMLEventReader(new StringReader(XML_input));

    // nu moet ik een state machine creeren, die van de ene naar de andere state gaat
    // door middel van transitierules

    // we gaan de input af, event voor event.
    while (reader.hasNext()) {
      XMLEvent xmlEvent = reader.nextEvent();
      System.out.println("xmlEvent=" + xmlEvent);
      // nu checken we of we van state naar state kunnen gaan.
      if (xmlEvent.isStartElement()) {
        StartElement s = xmlEvent.asStartElement();
        String tag = s.getName().getLocalPart();
        Node tagNode = new TagNode(tag);
        stateMachine.processInput(tagNode);
        // als ik de rule wel kan vinden gebeurt er iets eigenaardigs.
        // dan moet ik de boom aanpassen, maar alleen de laatste level
        // nou ja dat is dus eigenlijk een kind aanpassen en vervangen door een ander

      } else if (xmlEvent.isCharacters()) {
        Characters characters = xmlEvent.asCharacters();
        String content = characters.toString();
        nodes.Node textNode = new nodes.TextNode(content);
        stateMachine.processInput(textNode);
      }else if (xmlEvent.isEndElement()){
        stateMachine.pop();
      }
      System.out.println();
    }
  }

  // we moeten verschillende soorten nodes gaan ondersteunen.
  private void createTransitionRules() {
    // We maken de non-terminal root aan. (hoofdletters)
    // Die kunen we vervangen door een terminal root (kleine letters) + non-terminal MARKUP node
    // Dit klinkt ingewikkelder dan nodig. hmm
    // de huidige state is dan meer een tree, waarbij steeds een stukje vervangen wordt.
    // Tree zou je eigenlijk een kunnen aanmaken op basis van een string, maar ja nu even niet.

    // {} => {ROOT}
    NonTerminalNode lhs0 = new StartNode();
    Tree<Node> rhs0 = new Tree<>(new NonTerminalMarkupNode("ROOT"));
    TransitionRule transitionRule0 = new TransitionRule(lhs0, rhs0);
    stateMachine.addTransitionRule(transitionRule0);

    // {ROOT} => (root)[{MARKUP}]
    NonTerminalNode lhs1 = new NonTerminalMarkupNode("ROOT");
    Tree<Node> rhs1 = new Tree<>(new TagNode("root"), Collections.singletonList(new NonTerminalMarkupNode("MARKUP")));
    TransitionRule transitionRule1 = new TransitionRule(lhs1, rhs1);
    stateMachine.addTransitionRule(transitionRule1);

    // {MARKUP} => (markup)["*"]
    NonTerminalNode lhs2 = new NonTerminalMarkupNode("MARKUP");
    Tree<Node> rhs2 = new Tree<>(new TagNode("markup"), Collections.singletonList(new AnyTextNode()));
    TransitionRule transitionRule2 = new TransitionRule(lhs2, rhs2);
    stateMachine.addTransitionRule(transitionRule2);
  }

  // De graph die we willen construeren telt uiteindelijk (als het parsen succesvol beëindigd is) alleen TerminalNodes: Markup en Text (TerminalNodes)
  // Tijdens het parsen kan de graph ook nog NonTerminalNodes bevatten
  // Voor een serialisatie van de transition rules moeten we kunnen aangeven of het om een TerminalNode of NonTerminalNode gaat
  // {} : start symbol
  // {NODE} : NonTerminal Markup Node
  // (node) : Terminal markup node
  // /.*/ :  NonTerminal text node
  // "text" Terminal text node
  // [(node1), (node2) ] : 2 child nodes

  public Tree<Node> getTree() {
    return stateMachine.getTree();
  }
}
