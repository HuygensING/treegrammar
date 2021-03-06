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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Supplier;

import static java.util.stream.Collectors.*;

public class TransitionRuleSetFactory {

  private static final Logger LOG = LoggerFactory.getLogger(TransitionRuleSetFactory.class);

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
    final Supplier<Tree<Node>> rhs = toRHSSupplier(transitionruleContext.rhs());
    return new TransitionRule(lhs, rhs);
  }

  private NonTerminalNode toLHS(final TGSParser.LhsContext lhs) {
    return (lhs.startNode() != null)
        ? new StartNode()
        : new NonTerminalMarkupNode(lhs.nonTerminalMarkup().getText());
  }

  private Supplier<Tree<Node>> toRHSSupplier(final RhsContext rhs) {
    return () -> {
      final Node root = toNode(rhs.root());
      final List<Node> children = new ArrayList<>();
      final Map<Node, List<Node>> subTrees = new HashMap<>();
      rhs.child().forEach(c ->
          handleChildContext(c, children, subTrees)
      );
      final Tree<Node> rhsTree = new Tree<>(root, children);
      subTrees.forEach((r, c) -> c.forEach(n -> rhsTree.connect(r, n)));
      return rhsTree;
    };
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

      case zeroOrOne:
        TGSParser.RepeatableChildContext repeatableChildContext = c.zeroOrOne().repeatableChild();
        Node childNode = toNode(repeatableChildContext);
        ZeroOrOneNode zeroOrOneNode = new ZeroOrOneNode(childNode);
        children.add(zeroOrOneNode);
        subTrees.put(zeroOrOneNode, Collections.singletonList(childNode));
        break;

      case zeroOrMore:
        TGSParser.RepeatableChildContext repeatableChildContext1 = c.zeroOrMore().repeatableChild();
        Node childNode1 = toNode(repeatableChildContext1);
        ZeroOrMoreNode zeroOrMoreNode = new ZeroOrMoreNode(childNode1);
        children.add(zeroOrMoreNode);
        subTrees.put(zeroOrMoreNode, Collections.singletonList(childNode1));
        break;

      case oneOrMore:
        TGSParser.RepeatableChildContext repeatableChildContext2 = c.oneOrMore().repeatableChild();
        Node childNode2 = toNode(repeatableChildContext2);
        OneOrMoreNode oneOrMoreNode = new OneOrMoreNode(childNode2);
        children.add(oneOrMoreNode);
        subTrees.put(oneOrMoreNode, Collections.singletonList(childNode2));
        break;

      default:
        throw new RuntimeException("Unhandled ChildType " + childType);
    }
  }

  private Node toNode(final TGSParser.RepeatableChildContext repeatableChildContext) {
    Node node = null;
    ChildType childType = childType(repeatableChildContext);
    switch (childType) {
      case nonTerminal:
        String name = repeatableChildContext.nonTerminalMarkup().NONTERMINAL().getText();
        node = new NonTerminalMarkupNode(name);
        break;

      case group:
        final List<Node> groupElements = new ArrayList<>();
//        c.group().child().forEach(gc -> handleChildContext(gc, groupElements, subTrees));
//        GroupNode groupNode = new GroupNode(groupElements);
//        children.add(groupNode);
//        subTrees.put(groupNode, groupElements);
        break;

      case choice:
        final List<Node> choices = new ArrayList<>();
//        c.choice().child().forEach(gc -> handleChildContext(gc, choices, subTrees));
//        ChoiceNode choiceNode = new ChoiceNode(choices);
//        children.add(choiceNode);
//        subTrees.put(choiceNode, choices);
        break;

      default:
        throw new RuntimeException("Unhandled ChildType " + childType);
    }
    return node;
  }

  enum ChildType {
    nonTerminal, terminal, group, choice, zeroOrOne, zeroOrMore, oneOrMore, text
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
    if (c.zeroOrOne() != null) {
      return ChildType.zeroOrOne;
    }
    if (c.zeroOrMore() != null) {
      return ChildType.zeroOrMore;
    }
    if (c.oneOrMore() != null) {
      return ChildType.oneOrMore;
    }

    throw new RuntimeException("Unable to determine ChildType from ChildContext " + c);
  }

  private ChildType childType(final TGSParser.RepeatableChildContext c) {
    if (c.nonTerminalMarkup() != null) {
      return ChildType.nonTerminal;
    }
    if (c.group() != null) {
      return ChildType.group;
    }
    if (c.choice() != null) {
      return ChildType.choice;
    }

    throw new RuntimeException("Unable to determine ChildType from RepeatableChildContext " + c);
  }

  private Node toNode(final RootContext root) {
    TerminalMarkupContext terminalMarkupContext = root.terminalMarkup();

    return (terminalMarkupContext != null)
        ? new TagNode(terminalMarkupContext.TERMINAL().getText())
        : new NonTerminalMarkupNode(root.nonTerminalMarkup().NONTERMINAL().getText());
  }

  public static void validateRuleSet(final List<TransitionRule> ruleSet) {
    detectNonTerminalsWithMultipleRules(ruleSet);

    List<String> lhsNonTerminals = ruleSet.stream()
        .map(TransitionRule::leftHandSideNode)
        .map(Object::toString)
        .collect(toList());

    String startNode = detectStartNodeTransitionRule(lhsNonTerminals);
    detectNontermination(ruleSet, lhsNonTerminals);
    detectCycle(ruleSet, startNode);
  }

  private static void detectNonTerminalsWithMultipleRules(final List<TransitionRule> ruleSet) {
    final List<String> nonTerminalsWithMultipleRules = new ArrayList<>();
    final Set<String> nonTerminalsWithRule = new HashSet<>();

    ruleSet.forEach(r -> {
      String lhs = r.leftHandSide.toString();
      if (nonTerminalsWithRule.contains(lhs)) {
        nonTerminalsWithMultipleRules.add(lhs);
      } else {
        nonTerminalsWithRule.add(lhs);
      }
    });
    if (!nonTerminalsWithMultipleRules.isEmpty()) {
      throw new TransitionRuleSetValidationException(
          "Multiple rules found for " + nonTerminalsWithMultipleRules + ": only 1 rule allowed per nonterminal; use choice rule: (A|B)");
    }
  }

  private static String detectStartNodeTransitionRule(final List<String> lhsNonTerminals) {
    String startNode = new StartNode().toString();
    boolean startNodeTransitionRuleIsPresent = lhsNonTerminals.contains(startNode);
    if (!startNodeTransitionRuleIsPresent) {
      throw new TransitionRuleSetValidationException(
          "No start node transition rule (" + StartNode.CIPHER + " => ...) found!");
    }
    return startNode;
  }

  private static void detectNontermination(final List<TransitionRule> ruleSet, final List<String> lhsNonTerminals) {
    Set<String> rhsNonTerminals = ruleSet.stream()
        .flatMap(TransitionRule::righthandsideNonTerminalMarkupNodes)
        .map(Object::toString)
        .collect(toSet());

    Set<String> nonTerminalsWithoutTransitionRules = rhsNonTerminals.stream()
        .filter(n -> !lhsNonTerminals.contains(n))
        .collect(toSet());
    if (!nonTerminalsWithoutTransitionRules.isEmpty()) {
      throw new TransitionRuleSetValidationException(
          "No terminating transition rules found for "
              + String.join(",", nonTerminalsWithoutTransitionRules));
    }
  }

  private static void detectCycle(final List<TransitionRule> ruleSet, final String startNode) {
    // calculate which nonterminals are connected through the transition rules
    Map<String, Set<String>> nonTerminalConnections = new HashMap<>();
    ruleSet.forEach(r -> {
      String key = r.leftHandSide.toString();
      nonTerminalConnections.putIfAbsent(key, new HashSet<>());
      Set<String> values = nonTerminalConnections.get(key);
      r.righthandsideNonTerminalMarkupNodes()
          .map(Object::toString)
          .forEach(values::add);
    });

    // if there is a cycle, that means there are rules that don't terminate
    // so first find the lhs non-terminals of the terminating rules
    // terminating rules have a tagnode as root and either no children, or just 1 AnyText node
    List<String> nonTerminalsWithTerminatingRule = ruleSet.stream()
        .filter(TransitionRule::isTerminating)
        .map(r -> r.leftHandSide)
        .map(Object::toString)
        .collect(toList());

    Set<String> nonTerminalsWithRules = new HashSet<>(nonTerminalConnections.keySet());
    nonTerminalsWithRules.removeAll(nonTerminalsWithTerminatingRule);
    boolean goOn = true;
    while (goOn) {
      List<String> indirectlyTerminated = nonTerminalsWithRules.stream()
          .filter(n -> nonTerminalsWithTerminatingRule.containsAll(nonTerminalConnections.get(n)))
          .collect(toList());
      nonTerminalsWithRules.removeAll(indirectlyTerminated);
      nonTerminalsWithTerminatingRule.addAll(indirectlyTerminated);
      goOn = !indirectlyTerminated.isEmpty();
    }
    if (!nonTerminalsWithRules.isEmpty()) {
      String offendingRules = ruleSet.stream()
          .filter(r -> nonTerminalsWithRules.contains(r.leftHandSide.toString()))
          .map(Object::toString)
          .collect(joining("\n"));
      String head = nonTerminalsWithRules.size() == 1
          ? "This transition rule introduces a cycle"
          : "These transition rules introduce (a) cycle(s)";
      String message = head + ":\n" + offendingRules;
      throw new TransitionRuleSetValidationException(message);
//      throw new TransitionRuleSetValidationException("cycle found!" + nonTerminalsWithRules);
    }

    List<String> toVisit = new ArrayList<>();
    Set<String> visited = new HashSet<>();
    toVisit.add(startNode);
    while (!toVisit.isEmpty()) {
      String next = toVisit.remove(0);
      Set<String> newNodes = nonTerminalConnections.get(next);
      visited.add(next);
      toVisit.removeAll(visited);
      toVisit.addAll(newNodes);
    }

    // all rules should have been visited.
    // for now, we don't keep track of which rules have been visited, just which nonterminals
    // but each nonterminal appears as lhs in at least 1 transition rule
    // and since we connected the nonterminals with all the nonterminals from all the rhs of all relevant transition rules, it's as if we followed all branches.
    // so transition rules with unvisited nonterminals as their lhs are transition rules that are unreachable from the start node
    Set<String> unvisitedNonTerminals = new HashSet<>(nonTerminalConnections.keySet());
    unvisitedNonTerminals.removeAll(visited);
    if (!unvisitedNonTerminals.isEmpty()) {
      List<String> unreachedRules = ruleSet.stream()
          .filter(r -> unvisitedNonTerminals.contains(r.leftHandSide.toString()))
          .map(Object::toString)
          .collect(toList());
      String head = unreachedRules.size() == 1
          ? "This transition rule is"
          : "These transition rules are";
      String message = head + " unreachable from the start node.:\n" + String.join("\n", unreachedRules);
      throw new TransitionRuleSetValidationException(message);
    }
  }

}
