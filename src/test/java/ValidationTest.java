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
  public void testXMLDoesNotParse() {
    XMLValidatorUsingTreeGrammars validator = new XMLValidatorUsingTreeGrammars(defaultTransitionRules());
    try {
      validator.parse("<root><markup>tekst and <b>more</b> markup</markup></root>");
      fail("Expected an exception");
    } catch (Exception e) {
      assertThat(e).hasMessage("No transition rule found! Current state: /.*/ -> [b]");
    }
  }

  private List<TransitionRule> defaultTransitionRules() {
    final List<TransitionRule> defaultTransitionRules = new ArrayList<>();
    defaultTransitionRules.add(TransitionRuleFactory.fromString("{} => {ROOT}"));
    defaultTransitionRules.add(TransitionRuleFactory.fromString("{ROOT} => (root)[{MARKUP}]"));
    defaultTransitionRules.add(TransitionRuleFactory.fromString("{MARKUP} => (markup)[\"*\"]"));
    return defaultTransitionRules;
  }

//  private List<TransitionRule> defaultTransitionRules() {
//    final List<TransitionRule> defaultTransitionRules = new ArrayList<>();
//    // {} => {ROOT}
//    NonTerminalNode lhs0 = new StartNode();
//    Tree<Node> rhs0 = new Tree<>(new NonTerminalMarkupNode("ROOT"));
//    defaultTransitionRules.add(new TransitionRule(lhs0, rhs0));
//
//    // {ROOT} => (root)[{MARKUP}]
//    NonTerminalNode lhs1 = new NonTerminalMarkupNode("ROOT");
//    Tree<Node> rhs1 = new Tree<>(new TagNode("root"), asList(new NonTerminalMarkupNode("MARKUP")));
//    defaultTransitionRules.add(new TransitionRule(lhs1, rhs1));
//
//    // {MARKUP} => (markup)["*"]
//    NonTerminalNode lhs2 = new NonTerminalMarkupNode("MARKUP");
//    Tree<Node> rhs2 = new Tree<>(new TagNode("markup"), asList(new AnyTextNode()));
//    defaultTransitionRules.add(new TransitionRule(lhs2, rhs2));
//
//    return defaultTransitionRules;
//  }
}