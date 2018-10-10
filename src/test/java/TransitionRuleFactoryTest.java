import nodes.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

class TransitionRuleFactoryTest {

  @Test
  public void testParseTransitionRule1() {
    String input = "{} => {ROOT}";
    TransitionRule tr = TransitionRuleFactory.fromString(input);

    String actualLHS = tr.lefthandside.toString();
    String expectedLHS = new StartNode().toString();
    assertThat(actualLHS).isEqualTo(expectedLHS);

    String actualRHSRoot = tr.righthandside.root.toString();
    String expectedRHSRoot = new NonTerminalMarkupNode("ROOT").toString();
    assertThat(actualRHSRoot).isEqualTo(expectedRHSRoot);

    assertThat(tr.righthandside.children.get(tr.righthandside.root)).isEmpty();
  }

  @Test
  public void testParseTransitionRule2() {
    String input = "{ROOT} => (root)[{MARKUP}]";
    TransitionRule tr = TransitionRuleFactory.fromString(input);

    String actualLHS = tr.lefthandside.toString();
    String expectedLHS = new NonTerminalMarkupNode("ROOT").toString();
    assertThat(actualLHS).isEqualTo(expectedLHS);

    String actualRHSRoot = tr.righthandside.root.toString();
    String expectedRHSRoot = new TagNode("root").toString();
    assertThat(actualRHSRoot).isEqualTo(expectedRHSRoot);

    List<Node> rhsChildren = tr.righthandside.children.get(tr.righthandside.root);
    assertThat(rhsChildren).hasSize(1);
    Node expectedRHSChild = new NonTerminalMarkupNode("MARKUP");
    assertThat(rhsChildren.get(0).toString()).isEqualTo(expectedRHSChild.toString());
  }

  @Test
  public void testParseTransitionRule3() {
    String input = "{MARKUP} => (markup)[#]";
    TransitionRule tr = TransitionRuleFactory.fromString(input);

    String actualLHS = tr.lefthandside.toString();
    String expectedLHS = new NonTerminalMarkupNode("MARKUP").toString();
    assertThat(actualLHS).isEqualTo(expectedLHS);

    String actualRHSRoot = tr.righthandside.root.toString();
    String expectedRHSRoot = new TagNode("markup").toString();
    assertThat(actualRHSRoot).isEqualTo(expectedRHSRoot);

    List<Node> rhsChildren = tr.righthandside.children.get(tr.righthandside.root);
    assertThat(rhsChildren).hasSize(1);
    Node expectedRHSChild = new AnyTextNode();
    String actual = rhsChildren.get(0).toString();
    String expected = expectedRHSChild.toString();
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void testTransitionRuleDoesNotParse1() {
    String input = "This is not a valid rule";
    String expectedExceptionMessage = "Not a valid transition rule: This is not a valid rule";
    assertParsingFailsWithExceptionMessage(input, expectedExceptionMessage);
  }

  @Test
  public void testTransitionRuleDoesNotParse2() {
    String input = "{M} => ";
    String expectedExceptionMessage = "The right-hand side of the rule should have a root and zero or more children, but was empty.";
    assertParsingFailsWithExceptionMessage(input, expectedExceptionMessage);
  }

  @Test
  public void testTransitionRuleDoesNotParse3() {
    String input = "(ROOT)=>(hello)[(world)]";
    String expectedExceptionMessage = "The left-hand side of the rule should be a non-terminal, but was (ROOT)";
    assertParsingFailsWithExceptionMessage(input, expectedExceptionMessage);
  }

  @Test
  public void testCycleDetection() {
    String[] rules = {
        "{} => {NAME}",
        "{NAME} => (name)[{FIRST},{LAST}]",
        "{FIRST} => (first)[#]",
        "{LAST} => (last)[{NAME}]" // cycle!
    };
    final String expectedExceptionMessage = "This transition rule introduces a cycle:\n" +
        "{LAST} => (last)[{NAME}]";
    assertValidationFailsWithExceptionMessage(rules, expectedExceptionMessage);
  }

  @Test
  public void thereShouldBeAStartNodeTransitionRule() {
    String[] rules = {
        "{NAME} => (name)[{FIRST},{LAST}]",
        "{FIRST} => (first)[#]",
        "{LAST} => (last)[#]"
    };
    final String expectedExceptionMessage = "No startnode transition rule ({} => ...) found!";
    assertValidationFailsWithExceptionMessage(rules, expectedExceptionMessage);
  }

  @Test
  public void everyNonTerminalShouldTerminateEventually() {
    String[] rules = {
        "{} => {NAME}",
        "{NAME} => (name)[{FIRST},{LAST}]",
        "{FIRST} => (first)[#]" // {LAST} does not terminate
    };
    final String expectedExceptionMessage = "No terminating transition rules found for {LAST}";
    assertValidationFailsWithExceptionMessage(rules, expectedExceptionMessage);
  }

  @Test
  public void allTransactionRulesShouldBeReachableFromTheStartNode() {
    String[] rules = {
        "{} => {NAME}",
        "{NAME} => (name)[{FIRST},{LAST}]",
        "{FIRST} => (first)[#]",
        "{LAST} => (last)[#]",
        "{HELLO} => (hello)[{OH}, #]",
        "{OH} => (oh)[#]"
    };
    final String expectedExceptionMessage = "These transition rules are unreachable from the start node.:\n" +
        "{HELLO} => (hello)[{OH}, #]\n" +
        "{OH} => (oh)[#]";
    assertValidationFailsWithExceptionMessage(rules, expectedExceptionMessage);
  }

  private void assertValidationFailsWithExceptionMessage(final String[] rules, final String expectedExceptionMessage) {
    List<TransitionRule> ruleSet = stream(rules)
        .map(TransitionRuleFactory::fromString)
        .collect(toList());
    try {
      TransitionRuleFactory.validateRuleSet(ruleSet);
      fail("expected a TransitionRuleSetValidationException with message: " + expectedExceptionMessage);
    } catch (TransitionRuleSetValidationException e) {
      assertThat(e).hasMessage(expectedExceptionMessage);
    }
  }

  private void assertParsingFailsWithExceptionMessage(String input, String expectedExceptionMessage) {
    try {
      TransitionRule tr = TransitionRuleFactory.fromString(input);
      fail("expected a TransitionRuleParseException with message: " + expectedExceptionMessage);
    } catch (TransitionRuleParseException e) {
      assertThat(e).hasMessage(expectedExceptionMessage);
    }
  }

}