/*
 * Hier schetsen we eerst wat uit...
 * Ik wil een stukje code die stax parse events gooit en verwerkt.
 */


import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLStreamException;

public class ValidationTest {

    @Test
    public void testMyXML() throws XMLStreamException {
        XMLValidatorUsingTreeGrammars validator = new XMLValidatorUsingTreeGrammars();
        validator.parse("<root><markup>tekst</markup></root>");
    }
}