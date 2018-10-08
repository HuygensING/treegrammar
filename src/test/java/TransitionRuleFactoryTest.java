import nodes.*;
import org.junit.jupiter.api.Test;

import java.util.List;

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
    String input = "{MARKUP} => (markup)[\"*\"]";
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
    String expectedExceptionMessage = "Not a valid transition rule: {M} => ";
    assertParsingFailsWithExceptionMessage(input, expectedExceptionMessage);
  }

  @Test
  public void testTransitionRuleDoesNotParse3() {
    String input = "(ROOT)=>(hello)[(world)]";
    String expectedExceptionMessage = "The left-hand side of the rule should be a non-terminal, but was (ROOT)";
    assertParsingFailsWithExceptionMessage(input, expectedExceptionMessage);
  }

  private void assertParsingFailsWithExceptionMessage(String input, String expectedExceptionMessage) {
    try {
      TransitionRule tr = TransitionRuleFactory.fromString(input);
      fail("expected a TransitionRuleParseException");
    } catch (TransitionRuleParseException e) {
      assertThat(e).hasMessage(expectedExceptionMessage);
    }
  }
}