package nl.knaw.huc.di.tag.treegrammar;

import nl.knaw.huc.di.tag.treegrammar.nodes.*;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static nl.knaw.huc.di.tag.treegrammar.TransitionRuleFactory.CHOICE_PATTERN;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

class TransitionRuleFactoryTest {
  Logger LOG = LoggerFactory.getLogger(TransitionRuleFactoryTest.class);

  @Test
  void testChoicePattern() {
    String nodeSerialization = "(A|B)";
    Matcher m = CHOICE_PATTERN.matcher(nodeSerialization);
    assertThat(m.matches()).isTrue();
    assertThat(m.group(1)).isEqualTo("A|B");
  }

  @Test
  void testChoicePattern2() {
    String nodeSerialization = "(A|B|C)";
    Matcher m = CHOICE_PATTERN.matcher(nodeSerialization);
    assertThat(m.matches()).isTrue();
    assertThat(m.group(1)).isEqualTo("A|B|C");
  }

  @Test
  void testChoiceNodeSerialization() {
    Node node = TransitionRuleFactory.toNode("(CHOICE|OPTION)");
    assertThat(node).isInstanceOf(ChoiceNode.class);
    ChoiceNode choiceNode = (ChoiceNode) node;
    List<Node> choices = choiceNode.choices;
    assertThat(choices).hasSize(2);
    final Node choiceChoice = choices.get(0);
    final Node optionChoice = choices.get(1);
    assertThat(choiceChoice).isEqualTo(new NonTerminalMarkupNode("CHOICE"));
    assertThat(optionChoice).isEqualTo(new NonTerminalMarkupNode("OPTION"));
  }

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

    List<Node> rhsChildren = tr.righthandside.getRootChildren();
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

    List<Node> rhsChildren = tr.righthandside.getRootChildren();
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
    String expectedExceptionMessage = "The right-hand side of the rule should have a root and/or one or more children, but was empty.";
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
  void onlyOneTransitionRulePerNonTerminal() {
    String[] rules = {
        "# => name[FIRST LAST]",
        "FIRST => first[_]",
        "FIRST => initial[_]",
        "LAST => last[_]"
    };
    final String expectedExceptionMessage = "Multiple rules found for [FIRST]: only 1 rule allowed per nonterminal; use choice rule: (A|B)";
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