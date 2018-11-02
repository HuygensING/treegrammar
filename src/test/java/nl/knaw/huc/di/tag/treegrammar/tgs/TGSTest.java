package nl.knaw.huc.di.tag.treegrammar.tgs;

import nl.knaw.huc.di.tag.treegrammar.tgs.TGSParser.ScriptContext;
import nl.knaw.huc.di.tag.treegrammar.tgs.TGSParser.TransitionruleContext;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TGSTest {
  private final Logger LOG = LoggerFactory.getLogger(getClass());

  @Test
  public void testCorrectTGS() {
    String rules = "# => root[A B]\n" +
        "A=>a[_]\n" +
        "B=>b[_]";
    ScriptContext script = parse(rules);
    assertThat(script.getChildCount()).isEqualTo(4);

    List<TransitionruleContext> transitionrules = script.transitionrule();
    assertThat(transitionrules).hasSize(3);

    TransitionruleContext transitionrule0 = transitionrules.get(0);
    assertThat(transitionrule0.lhs().startNode()).isNotNull();

    TransitionruleContext transitionrule1 = transitionrules.get(1);
    assertThat(transitionrule1.lhs().nonTerminalMarkup()).isNotNull();

    TransitionruleContext transitionrule2 = transitionrules.get(2);
    assertThat(transitionrule2.lhs().nonTerminalMarkup()).isNotNull();
  }

  @Test
  public void testRuleWithGroup() {
    String rule = "X => x[{Y Z} A]";
    ScriptContext script = parse(rule);
    assertThat(script).isNotNull();

    TransitionruleContext transitionrule = script.transitionrule(0);
    String lhs = transitionrule.lhs().nonTerminalMarkup().getText();
    assertThat(lhs).isEqualTo("X");

    TGSParser.RhsContext rhs = transitionrule.rhs();
    TGSParser.RootContext root = rhs.root();
    assertThat(root.terminalMarkup().getText()).isEqualTo("x");

    TGSParser.GroupContext group = rhs.child(0).group();
    assertThat(group).isNotNull();
    assertThat(group.child()).hasSize(2);

    String groupElement1 = group.child(0).nonTerminalMarkup().getText();
    assertThat(groupElement1).isEqualTo("Y");

    String groupElement2 = group.child(1).nonTerminalMarkup().getText();
    assertThat(groupElement2).isEqualTo("Z");

    String child1 = rhs.child(1).nonTerminalMarkup().getText();
    assertThat(child1).isEqualTo("A");
  }

  @Test
  public void testRuleWithChoice() {
    String rule = "X => x[(A|B) C]";
    ScriptContext script = parse(rule);
    assertThat(script).isNotNull();

    TransitionruleContext transitionrule = script.transitionrule(0);
    String lhs = transitionrule.lhs().nonTerminalMarkup().getText();
    assertThat(lhs).isEqualTo("X");

    TGSParser.RhsContext rhs = transitionrule.rhs();
    TGSParser.RootContext root = rhs.root();
    assertThat(root.terminalMarkup().getText()).isEqualTo("x");

    TGSParser.ChoiceContext choice = rhs.child(0).choice();
    assertThat(choice).isNotNull();
    assertThat(choice.child()).hasSize(2);

    String choice1 = choice.child(0).nonTerminalMarkup().getText();
    assertThat(choice1).isEqualTo("A");

    String choice2 = choice.child(1).nonTerminalMarkup().getText();
    assertThat(choice2).isEqualTo("B");

    String child1 = rhs.child(1).nonTerminalMarkup().getText();
    assertThat(child1).isEqualTo("C");
  }

  private ScriptContext parse(final String rules) {
    CharStream stream = CharStreams.fromString(rules);
    TGSLexer lex = new TGSLexer(stream);
    List<? extends Token> allTokens = lex.getAllTokens();
    for (Token token : allTokens) {
      LOG.info("token: [{}] <<{}>>", lex.getRuleNames()[token.getType() - 1], token.getText());
    }
    lex.reset();
    CommonTokenStream tokens = new CommonTokenStream(lex);
    TGSParser parser = new TGSParser(tokens);
    parser.setBuildParseTree(true);
    assertThat(parser.getNumberOfSyntaxErrors()).isEqualTo(0);

    ParseTree script = parser.script();
    LOG.info("script={}", script.toStringTree(parser));
    return (ScriptContext) script;
  }
}
