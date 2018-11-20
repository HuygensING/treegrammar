package nl.knaw.huc.di.tag.treegrammar;

import nl.knaw.huc.di.tag.treegrammar.nodes.AnyTextNode;
import nl.knaw.huc.di.tag.treegrammar.nodes.Node;
import nl.knaw.huc.di.tag.treegrammar.nodes.NonTerminalMarkupNode;
import nl.knaw.huc.di.tag.treegrammar.nodes.TagNode;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

class TransitionRuleSetFactoryTest {
  private final Logger LOG = LoggerFactory.getLogger(getClass());

  @Test
  void fromTGS1() {
    String rules = "# => root[A B]\n" +
        "A=>a[_]\n" +
        "B=>b[_]";
    List<TransitionRule> transitionRules = new TransitionRuleSetFactory().fromTGS(rules);
    LOG.info("transitionRules={}", transitionRules);
    assertThat(transitionRules).hasSize(3);
  }

  @Test
  void parseTransitionRuleWithChoiceAndGroup() {
    String rule = "NAME=>name[({FIRST LAST}|ARTIST_NAME)]";
    List<TransitionRule> transitionRules = new TransitionRuleSetFactory().fromTGS(rule);
    LOG.info("transitionRules={}", transitionRules);
    assertThat(transitionRules).hasSize(1);
    TransitionRule transitionRule = transitionRules.get(0);
    assertThat(transitionRule.leftHandSide).isInstanceOf(NonTerminalMarkupNode.class);
    assertThat((NonTerminalMarkupNode) transitionRule.leftHandSide).extracting("variableName").contains("NAME");
    Tree<Node> righthandside = transitionRule.getRightHandSideSupplier().get();
    // expecting (artist (choice(group(FIRST LAST) ARTIST_NAME)))
    Node root = righthandside.root;
    Node expectedRoot = new TagNode("name");
    assertThat(root).isEqualToComparingFieldByField(expectedRoot);
    assertThat(righthandside.children.keySet().stream().map(Object::toString))
        .contains("name", "({FIRST LAST}|ARTIST_NAME)", "{FIRST LAST}");
    Node choiceNode = righthandside.getRootChildren().get(0);
    assertThat(righthandside.children.get(choiceNode).stream().map(Object::toString))
        .containsExactly("{FIRST LAST}", "ARTIST_NAME");
    Node groupNode = righthandside.children.get(choiceNode).get(0);
    assertThat(righthandside.children.get(groupNode).stream().map(Object::toString))
        .containsExactly("FIRST", "LAST");
  }

  @Disabled
  @Test
  void testParseTransitionRule1() {
    String input = "# => ROOT";
    String expectedExceptionMessage = "The right-hand side of the rule should have a root that is not a  non-terminal";
    assertParsingFailsWithExceptionMessage(input, expectedExceptionMessage);
  }

  @Test
  void testParseTransitionRule2() {
    String input = "ROOT => root[MARKUP]";
    TransitionRule tr = parseTransitionRule(input);

    String actualLHS = tr.leftHandSide.toString();
    String expectedLHS = new NonTerminalMarkupNode("ROOT").toString();
    assertThat(actualLHS).isEqualTo(expectedLHS);

    String actualRHSRoot = tr.getRightHandSide().root.toString();
    String expectedRHSRoot = new TagNode("root").toString();
    assertThat(actualRHSRoot).isEqualTo(expectedRHSRoot);

    List<Node> rhsChildren = tr.getRightHandSide().getRootChildren();
    assertThat(rhsChildren).hasSize(1);
    Node expectedRHSChild = new NonTerminalMarkupNode("MARKUP");
    assertThat(rhsChildren.get(0).toString()).isEqualTo(expectedRHSChild.toString());
  }

  @Test
  void testParseTransitionRule3() {
    String input = "MARKUP => markup[_]";
    TransitionRule tr = parseTransitionRule(input);

    String actualLHS = tr.leftHandSide.toString();
    String expectedLHS = new NonTerminalMarkupNode("MARKUP").toString();
    assertThat(actualLHS).isEqualTo(expectedLHS);

    String actualRHSRoot = tr.getRightHandSide().root.toString();
    String expectedRHSRoot = new TagNode("markup").toString();
    assertThat(actualRHSRoot).isEqualTo(expectedRHSRoot);

    List<Node> rhsChildren = tr.getRightHandSide().getRootChildren();
    assertThat(rhsChildren).hasSize(1);
    Node expectedRHSChild = new AnyTextNode();
    String actual = rhsChildren.get(0).toString();
    String expected = expectedRHSChild.toString();
    assertThat(actual).isEqualTo(expected);
  }

  @Disabled
  @Test
  void testTransitionRuleDoesNotParse1() {
    String input = "This is not a valid rule";
    String expectedExceptionMessage = "Not a valid transition rule: This is not a valid rule";
    assertParsingFailsWithExceptionMessage(input, expectedExceptionMessage);
  }

  @Disabled
  @Test
  void testTransitionRuleDoesNotParse2() {
    String input = "M => ";
    String expectedExceptionMessage = "The right-hand side of the rule should have a root and/or one or more children, but was empty.";
    assertParsingFailsWithExceptionMessage(input, expectedExceptionMessage);
  }

  @Disabled
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
    TransitionRuleSetFactory.validateRuleSet(ruleSet);
  }

  private List<TransitionRule> processRuleStrings(final String[] rules) {
    String script = String.join("\n", rules);
    return new TransitionRuleSetFactory().fromTGS(script);
  }

  private void assertValidationFailsWithExceptionMessage(final String[] rules, final String expectedExceptionMessage) {
    List<TransitionRule> ruleSet = processRuleStrings(rules);
    try {
      TransitionRuleSetFactory.validateRuleSet(ruleSet);
      fail("expected a TransitionRuleSetValidationException with message: " + expectedExceptionMessage);
    } catch (TransitionRuleSetValidationException e) {
      assertThat(e).hasMessage(expectedExceptionMessage);
    }
  }

  private void assertParsingFailsWithExceptionMessage(String input, String expectedExceptionMessage) {
    try {
      List<TransitionRule> tr = new TransitionRuleSetFactory().fromTGS(input);
      fail("expected a TransitionRuleParseException with message: " + expectedExceptionMessage);
    } catch (TransitionRuleParseException e) {
      assertThat(e).hasMessage(expectedExceptionMessage);
    }
  }

  private TransitionRule parseTransitionRule(final String input) {
    return new TransitionRuleSetFactory().fromTGS(input).get(0);
  }

}