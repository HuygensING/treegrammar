import nodes.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.*;

class TransitionRuleFactory {

  private static final Pattern RULE_PATTERN = Pattern.compile("\\s*(\\S+)\\s*=>\\s*(.+)\\s*");
  private static final Pattern RHS_PATTERN = Pattern.compile("([A-Za-z0-9]+)(\\[(.*?)\\])?");

  static TransitionRule fromString(final String transitionRuleString) {
    Matcher ruleMatcher = RULE_PATTERN.matcher(transitionRuleString);
    if (!ruleMatcher.matches()) {
      throw new TransitionRuleParseException("Not a valid transition rule: " + transitionRuleString);
    }

    String rawLHS = ruleMatcher.group(1);
    Matcher lhsMatcher = NonTerminalMarkupNode.PATTERN.matcher(rawLHS);
    String startNodeRepresentation = new StartNode().toString();

    NonTerminalNode lhs;
    if (rawLHS.equals(startNodeRepresentation)) {
      lhs = new StartNode();
    } else if (lhsMatcher.matches()) {
      lhs = new NonTerminalMarkupNode(lhsMatcher.group(1));
    } else {
      throw new TransitionRuleParseException("The left-hand side of the rule should be a non-terminal, but was " + rawLHS);
    }

    String rawRHS = ruleMatcher.group(2).trim();
    Matcher rhsMatcher = RHS_PATTERN.matcher(rawRHS);
    if (!rhsMatcher.matches()) {
      String was = rawRHS.isEmpty() ? "empty." : "'" + rawRHS + "'";
      throw new TransitionRuleParseException("The right-hand side of the rule should have a root and zero or more children, but was " + was);
    }
    Node rhsRoot = toNode(rhsMatcher.group(1));
    final List<Node> rhsChildren = new ArrayList<>();
    String rawChildren = rhsMatcher.group(3);
    if (rawChildren != null) {
      String[] splitChildren = rawChildren.split("\\s+");
      stream(splitChildren)
          .map(TransitionRuleFactory::toNode)
          .forEach(rhsChildren::add);

    } else {
      rhsRoot = toNode(rawRHS);
    }

    final Tree<Node> rhs = new Tree<>(rhsRoot, rhsChildren);
    return new TransitionRule(lhs, rhs);
  }

  static Node toNode(final String rawNodeSerialization) {
    String nodeSerialization = rawNodeSerialization.trim();
    // TagNode
    if (nodeSerialization.substring(0, 1).matches("[a-z]")) {
      return new TagNode(nodeSerialization);
    }
    // NonTerminalMarkupNode
    if (nodeSerialization.substring(0, 1).matches("[A-Z]")) {
      return new NonTerminalMarkupNode(nodeSerialization);
    }
    // AnyTextNode
    if (nodeSerialization.equals(AnyTextNode.CIPHER)) {
      return new AnyTextNode();
    }
    // StartNode
    if (nodeSerialization.equals(StartNode.CIPHER)) {
      return new StartNode();
    }
    throw new RuntimeException("Unexpected node in transition rule: " + nodeSerialization);
  }

  public static void validateRuleSet(final List<TransitionRule> ruleSet) {
    Set<String> lhsNonTerminals = ruleSet.stream()
        .map(TransitionRule::lefthandsideNode)
        .map(Object::toString)
        .collect(toSet());

    String startNode = new StartNode().toString();
    boolean startNodeTransitionRuleIsPresent = lhsNonTerminals.contains(startNode);
    if (!startNodeTransitionRuleIsPresent) {
      throw new TransitionRuleSetValidationException(
          "No start node transition rule (" + StartNode.CIPHER + " => ...) found!");
    }

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

    detectCycle(ruleSet, startNode);

  }

  private static void detectCycle(final List<TransitionRule> ruleSet, final String startNode) {
    // calculate which nonterminals are connected through the transition rules
    Map<String, Set<String>> nonTerminalConnections = new HashMap<>();
    ruleSet.forEach(r -> {
      String key = r.lefthandside.toString();
      nonTerminalConnections.putIfAbsent(key, new HashSet<>());
      Set<String> values = nonTerminalConnections.get(key);
      r.righthandsideNonTerminalMarkupNodes()
          .map(Object::toString)
          .forEach(values::add);
    });

    List<String> toVisit = new ArrayList<>();
    Set<String> visited = new HashSet<>();
    toVisit.add(startNode);
    while (!toVisit.isEmpty()) {
      String next = toVisit.remove(0);
      Set<String> newNodes = nonTerminalConnections.get(next);
      Set<String> revisits = newNodes.stream().filter(visited::contains).collect(toSet());
      if (!revisits.isEmpty()) {
        String offendingRules = ruleSet.stream()
            .filter(r -> r.lefthandside.toString().equals(next))
            .filter(r -> r.righthandsideNonTerminalMarkupNodes()
                .map(Object::toString)
                .anyMatch(revisits::contains))
            .map(Object::toString)
            .collect(joining("\n"));
        String head = revisits.size() == 1
            ? "This transition rule introduces a cycle"
            : "These transition rules introduce cycles";
        String message = head + ":\n" + offendingRules;
        throw new TransitionRuleSetValidationException(message);
      }
      toVisit.addAll(newNodes);
      visited.add(next);
    }

    // all rules should have been visited.
    // for now, we don't keep track of which rules have been visited, just which nonterminals
    // but each nonterminal appears as lhs in at least 1 transition rule
    // and since we connected the nonterminals with all the nonterminals from all the rhs of all relevant transition rules, it's as if we followed all branches.
    // so transition rules with unvisited nonterminals as their lhs are transition rules that are unreachable from the start node
    Set<String> unvisitedNonTerminals = new HashSet(nonTerminalConnections.keySet());
    unvisitedNonTerminals.removeAll(visited);
    if (!unvisitedNonTerminals.isEmpty()) {
      List<String> unreachedRules = ruleSet.stream()
          .filter(r -> unvisitedNonTerminals.contains(r.lefthandside.toString()))
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
