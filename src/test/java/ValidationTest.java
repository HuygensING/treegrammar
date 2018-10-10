/*
 * Hier schetsen we eerst wat uit...
 * Ik wil een stukje code die stax parse events gooit en verwerkt.
 */

import nodes.Node;
import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

public class ValidationTest {

  @Test
  public void testXMLParses() throws XMLStreamException {
    XMLValidatorUsingTreeGrammars validator = new XMLValidatorUsingTreeGrammars(defaultTransitionRules());
    validator.parse("<root><markup>tekst</markup></root>");
    Tree<Node> tree = validator.getTree();
    System.out.println(TreeVisualizer.asText(tree));
  }

  @Test
  public void testXMLDoesNotParse1() {
    XMLValidatorUsingTreeGrammars validator = new XMLValidatorUsingTreeGrammars(defaultTransitionRules());
    try {
      validator.parse("<root><markup>tekst and <b>more</b> markup</markup></root>");
      fail("Expected an exception");
    } catch (Exception e) {
      assertThat(e).hasMessage("Expected text node, but got (b)");
    }
  }

  @Test
  public void testXMLDoesNotParse2() {
    XMLValidatorUsingTreeGrammars validator = new XMLValidatorUsingTreeGrammars(defaultTransitionRules());
    try {
      validator.parse("<root>plain tekst and <markup>marked-up tekst</markup></root>");
      fail("Expected an exception");
    } catch (Exception e) {
      assertThat(e).hasMessage("No match: expected (markup) but found \"plain tekst and \"");
    }
  }

  private List<TransitionRule> defaultTransitionRules() {
    final List<TransitionRule> defaultTransitionRules = new ArrayList<>();
    defaultTransitionRules.add(TransitionRuleFactory.fromString("{} => {ROOT}"));
    defaultTransitionRules.add(TransitionRuleFactory.fromString("{ROOT} => (root)[{MARKUP}]"));
    defaultTransitionRules.add(TransitionRuleFactory.fromString("{MARKUP} => (markup)[#]"));
    return defaultTransitionRules;
  }

}