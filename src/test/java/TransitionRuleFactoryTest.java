import nodes.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
    String input = "{MARKUP} => (markup)[\"*\"])";
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
}