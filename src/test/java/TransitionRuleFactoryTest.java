import nodes.NonTerminalMarkupNode;
import nodes.StartNode;
import org.junit.jupiter.api.Test;

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

}