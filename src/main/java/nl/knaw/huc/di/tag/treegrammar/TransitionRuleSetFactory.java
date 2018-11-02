package nl.knaw.huc.di.tag.treegrammar;

import nl.knaw.huc.di.tag.treegrammar.nodes.*;
import nl.knaw.huc.di.tag.treegrammar.tgs.ErrorListener;
import nl.knaw.huc.di.tag.treegrammar.tgs.TGSLexer;
import nl.knaw.huc.di.tag.treegrammar.tgs.TGSParser;
import nl.knaw.huc.di.tag.treegrammar.tgs.TGSParser.RhsContext;
import nl.knaw.huc.di.tag.treegrammar.tgs.TGSParser.RootContext;
import nl.knaw.huc.di.tag.treegrammar.tgs.TGSParser.TerminalMarkupContext;
import nl.knaw.huc.di.tag.treegrammar.tgs.TGSParser.TransitionruleContext;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;

public class TransitionRuleSetFactory {

  public List<TransitionRule> fromTGS(String tgs) {
    CharStream stream = CharStreams.fromString(tgs);
    ErrorListener errorListener = new ErrorListener();
    TGSLexer lexer = new TGSLexer(stream);
    lexer.addErrorListener(errorListener);

    CommonTokenStream tokens = new CommonTokenStream(lexer);
    TGSParser tgsParser = new TGSParser(tokens);
    tgsParser.addErrorListener(errorListener);

    return tgsParser.script()
        .transitionrule().stream()
        .map(this::toTransitionRule)
        .collect(toList());
  }

  private TransitionRule toTransitionRule(final TransitionruleContext transitionruleContext) {
    final NonTerminalNode lhs = toLHS(transitionruleContext.lhs());
    final Tree<Node> rhs = toRHS(transitionruleContext.rhs());
    return new TransitionRule(lhs, rhs);
  }

  private NonTerminalNode toLHS(final TGSParser.LhsContext lhs) {
    return (lhs.startNode() != null)
        ? new StartNode()
        : new NonTerminalMarkupNode(lhs.nonTerminalMarkup().getText());
  }

  private Tree<Node> toRHS(final RhsContext rhs) {
    final Node root = toNode(rhs.root());
    final List<Node> children = new ArrayList<>();
    final Map<Node, List<Node>> subTrees = new HashMap<>();
    rhs.child().forEach(c ->
        handleChildContext(c, children, subTrees)
    );
    final Tree<Node> rhsTree = new Tree<>(root, children);
    subTrees.forEach((k, v) -> rhsTree.children.put(k, v));

    return rhsTree;
  }

  private void handleChildContext(final TGSParser.ChildContext c, final List<Node> children, final Map<Node, List<Node>> subTrees) {
    ChildType childType = childType(c);
    switch (childType) {
      case nonTerminal:
        String name = c.nonTerminalMarkup().NONTERMINAL().getText();
        children.add(new NonTerminalMarkupNode(name));
        break;

      case terminal:
        String tagName = c.terminalMarkup().TERMINAL().getText();
        children.add(new TagNode(tagName));
        break;

      case text:
        children.add(new AnyTextNode());
        break;

      case group:
        final List<Node> groupElements = new ArrayList<>();
        c.group().child().forEach(gc -> handleChildContext(gc, groupElements, subTrees));
        GroupNode groupNode = new GroupNode(groupElements);
        children.add(groupNode);
        subTrees.put(groupNode, groupElements);
        break;

      case choice:
        final List<Node> choices = new ArrayList<>();
        c.choice().child().forEach(gc -> handleChildContext(gc, choices, subTrees));
        ChoiceNode choiceNode = new ChoiceNode(choices);
        children.add(choiceNode);
        subTrees.put(choiceNode, choices);
        break;

      default:
        throw new RuntimeException("Unhandled ChildType " + childType);
    }
  }

  enum ChildType {
    nonTerminal, terminal, group, choice, text
  }

  private ChildType childType(final TGSParser.ChildContext c) {
    if (c.nonTerminalMarkup() != null) {
      return ChildType.nonTerminal;
    }
    if (c.terminalMarkup() != null) {
      return ChildType.terminal;
    }
    if (c.textNode() != null) {
      return ChildType.text;
    }
    if (c.group() != null) {
      return ChildType.group;
    }
    if (c.choice() != null) {
      return ChildType.choice;
    }
    throw new RuntimeException("Unable to determine ChildType from ChildContext " + c);
  }

  private Node toNode(final RootContext root) {
    TerminalMarkupContext terminalMarkupContext = root.terminalMarkup();
    Node node = (terminalMarkupContext != null)
        ? new TagNode(terminalMarkupContext.TERMINAL().getText())
        : new NonTerminalMarkupNode(root.nonTerminalMarkup().NONTERMINAL().getText());
    ;

    return node;
  }
}
