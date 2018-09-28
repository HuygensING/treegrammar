

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.StringReader;
import java.util.Arrays;

// * author: Ronald Haentjens Dekker
// * date: 11-09-2018
public class XMLValidatorUsingTreeGrammars {

    public void parse(String XML_input) throws XMLStreamException {
        // hier maken we een stax parser aan die de XML in stukjes binnen laat komen

        XMLInputFactory factory = XMLInputFactory.newInstance();
        System.out.println("FACTORY: " + factory);

        XMLEventReader reader = factory.createXMLEventReader(new StringReader(XML_input));

        // nu moet ik een state machine creeren, die van de ene naaar de andere state gaat
        // door middel van transitierules
        StateMachine stateMachine = new StateMachine();
        createTransitionRules(stateMachine);

        // we gaan de input af, event voor event.


        while (reader.hasNext()) {
            XMLEvent xmlEvent = reader.nextEvent();
            System.out.println(xmlEvent);
            // nu checken we of we van state naar state kunnen gaan.
            if (xmlEvent.isStartElement()) {
                StartElement s = xmlEvent.asStartElement();
                String tag = s.getName().getLocalPart();
                stateMachine.processInput(tag);
                // als ik de rule wel kan vinden gebeurt er iets eigenaardigs.
                // dan moet ik de boom aanpassen, maar alleen de laatste level
                // nou ja dat is dus eigenlijk een knd aanpassen en vervangen door een ander
            }
        }



    }

    // we moeten verschillende soorten nodes gaan ondersteunen.
    private void createTransitionRules(StateMachine stateMachine) {
        // We maken de non termin al root aan. (hoofdletters)
        // Die kunen we vervagnen door ene terminal root (kleine letters) + non terminal MARKUP node
        // Dit klinkt ingewikkelder dan nodig. hmm
        // de huidige state s dan meer ene tree, waarbij steeds een stukje vervangen wordt.
        // Tree zou je eigenlijk een kunnen aanmaken op basis van een string, maar ja nu even niet.
        TagNode lhs1 = new TagNode("ROOT");
        TreeContainer<TagNode> rhs2 =  new TreeContainer<>(new TagNode("root"), Arrays.asList(new TagNode("MARKUP")));
        TransitionRule transitionRule1 =  new TransitionRule(lhs1, rhs2);
        stateMachine.addTransitionRule(transitionRule1);
        TagNode lhs2_1 = new TagNode("MARKUP");
        TreeContainer<TagNode> rhs2_2 =  new TreeContainer<TagNode>(new TagNode("markup"), Arrays.asList(new TagNode("tekst")));
        TransitionRule transitionRule2 =  new TransitionRule(lhs2_1, rhs2_2);
        stateMachine.addTransitionRule(transitionRule2);
    }
}
