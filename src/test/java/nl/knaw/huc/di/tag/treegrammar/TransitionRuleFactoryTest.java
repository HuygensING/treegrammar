package nl.knaw.huc.di.tag.treegrammar;

import nl.knaw.huc.di.tag.treegrammar.expectations.*;
import nl.knaw.huc.di.tag.treegrammar.nodes.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

public class TransitionRuleFactoryTest {

  @Test
  void testTagNodeSerialization() {
    Node node = TransitionRuleFactory.toNode("element");
    assertThat(node).isInstanceOf(TagNode.class);
  }

  @Test
  void testNonTerminalMarkupNodeSerialization() {
    Node node = TransitionRuleFactory.toNode("Variable");
    assertThat(node).isInstanceOf(NonTerminalMarkupNode.class);
  }

  @Test
  void testStartNodeSerialization() {
    Node node = TransitionRuleFactory.toNode("#");
    assertThat(node).isInstanceOf(StartNode.class);
  }

  @Test
  void testAnyTextNodeSerialization() {
    Node node = TransitionRuleFactory.toNode("_");
    assertThat(node).isInstanceOf(AnyTextNode.class);
  }

  @Test
  void testParseTransitionRule1() {
    String input = "# => ROOT";
    String expectedExceptionMessage = "The right-hand side of the rule should have a root that is not a  non-terminal";
    assertParsingFailsWithExceptionMessage(input, expectedExceptionMessage);
  }

  @Test
  void testParseTransitionRule2() {
    String input = "ROOT => root[MARKUP]";
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
  void testParseTransitionRule3() {
    String input = "MARKUP => markup[_]";
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
  void testTransitionRuleDoesNotParse1() {
    String input = "This is not a valid rule";
    String expectedExceptionMessage = "Not a valid transition rule: This is not a valid rule";
    assertParsingFailsWithExceptionMessage(input, expectedExceptionMessage);
  }

  @Test
  void testTransitionRuleDoesNotParse2() {
    String input = "M => ";
    String expectedExceptionMessage = "The right-hand side of the rule should have a root and zero or more children, but was empty.";
    assertParsingFailsWithExceptionMessage(input, expectedExceptionMessage);
  }

  @Test
  void testTransitionRuleDoesNotParse3() {
    String input = "root => hello[world]";
    String expectedExceptionMessage = "The left-hand side of the rule should be a non-terminal, but was root";
    assertParsingFailsWithExceptionMessage(input, expectedExceptionMessage);
  }

  @Test
  void testCycleDetection() {
    String[] rules = {
        "# => name[FIRST LAST]",
        "FIRST => first[_]",
        "LAST => last[LAST]" // cycle!
    };
    final String expectedExceptionMessage = "These transition rules introduce (a) cycle(s):\n" +
        "# => name[FIRST LAST]\n" +
        "LAST => last[LAST]";
    assertValidationFailsWithExceptionMessage(rules, expectedExceptionMessage);
  }

  @Test
  void thisIsNotACycle() {
    String[] rules = {
        "# => review[BOOK REVIEWER]",
        "BOOK => book[TITLE AUTHOR]",
        "TITLE => title[_]",
        "AUTHOR => author[PERSON]",
        "REVIEWER => reviewer[PERSON]",
        "PERSON => person[NAME]",
        "NAME => name[_]"
    };
    assertRuleSetIsValid(rules);
  }

  @Test
  void thereShouldBeAStartNodeTransitionRule() {
    String[] rules = {
        "NAME => name[FIRST LAST]",
        "FIRST => first[_]",
        "LAST => last[_]"
    };
    final String expectedExceptionMessage = "No start node transition rule (# => ...) found!";
    assertValidationFailsWithExceptionMessage(rules, expectedExceptionMessage);
  }

  @Test
  void everyNonTerminalShouldTerminateEventually() {
    String[] rules = {
        "# => name[FIRST LAST]",
        "FIRST => first[_]" // {LAST} does not terminate
    };
    final String expectedExceptionMessage = "No terminating transition rules found for LAST";
    assertValidationFailsWithExceptionMessage(rules, expectedExceptionMessage);
  }

  @Test
  void allTransactionRulesShouldBeReachableFromTheStartNode() {
    String[] rules = {
        "# => name[FIRST LAST]",
        "FIRST => first[_]",
        "LAST => last[_]",
        "HELLO => hello[OH _]",
        "OH => oh[_]"
    };
    final String expectedExceptionMessage = "These transition rules are unreachable from the start node.:\n" +
        "HELLO => hello[OH _]\n" +
        "OH => oh[_]";
    assertValidationFailsWithExceptionMessage(rules, expectedExceptionMessage);
  }

  @Test
  void testExpectations1() {
    String rule = "# => name[FIRST LAST]";
    Expectation[] expectedResult = new Expectation[]{
        new MarkupStartExpectation("name"),
        new PlaceHolderExpectation(new NonTerminalMarkupNode("FIRST")),
        new PlaceHolderExpectation(new NonTerminalMarkupNode("LAST")),
        new MarkupEndExpectation("name")
    };
    assertExpectationsMatch(rule, expectedResult);
  }

  @Test
  void testExpectations2() {
    String rule = "FIRST => first[_]";
    Expectation[] expectedResult = new Expectation[]{
        new MarkupStartExpectation("first"),
        new TextExpectation(),
        new MarkupEndExpectation("first")
    };
    assertExpectationsMatch(rule, expectedResult);
  }

  private void assertExpectationsMatch(final String rule, final Expectation[] expectedResult) {
    TransitionRule transitionRule = TransitionRuleFactory.fromString(rule);
    List<Expectation> expectations = TransitionRuleFactory.getExpectations(transitionRule);
    assertThat(expectations).containsExactly(expectedResult);
  }

  private void assertRuleSetIsValid(final String[] rules) {
    List<TransitionRule> ruleSet = processRuleStrings(rules);
    TransitionRuleFactory.validateRuleSet(ruleSet);
  }

  private List<TransitionRule> processRuleStrings(final String[] rules) {
    return stream(rules)
        .map(TransitionRuleFactory::fromString)
        .collect(toList());
  }

  private void assertValidationFailsWithExceptionMessage(final String[] rules, final String expectedExceptionMessage) {
    List<TransitionRule> ruleSet = processRuleStrings(rules);
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