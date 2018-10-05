/*
 * Hier schetsen we eerst wat uit...
 * Ik wil een stukje code die stax parse events gooit en verwerkt.
 */


import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLStreamException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

public class ValidationTest {

  @Test
  public void testXMLParses() throws XMLStreamException {
    XMLValidatorUsingTreeGrammars validator = new XMLValidatorUsingTreeGrammars();
    validator.parse("<root><markup>tekst</markup></root>");
  }

  @Test
  public void testXMLDoesNotParse() {
    XMLValidatorUsingTreeGrammars validator = new XMLValidatorUsingTreeGrammars();
    try {
      validator.parse("<root><markup>tekst and <b>more</b> markup</markup></root>");
      fail("Expected an exception");
    } catch (Exception e) {
      assertThat(e).hasMessage("No transition rule found! Current state: /.*/ -> [b]");
    }
  }
}