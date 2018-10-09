import nodes.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;

public class TransitionRuleFactory {

  private static final Pattern RULE_PATTERN = Pattern.compile("\\s*(\\S+)\\s*=>\\s*(\\S+)\\s*");
  private static final Pattern RHS_PATTERN = Pattern.compile("(\\(\\w+\\)|\\{\\w+\\})(\\[(.*?)\\])?");

  static TransitionRule fromString(final String transitionRuleString) {
    Matcher rulematcher = RULE_PATTERN.matcher(transitionRuleString);
    if (!rulematcher.matches()) {
      throw new TransitionRuleParseException("Not a valid transition rule: " + transitionRuleString);
    }

    String rawLHS = rulematcher.group(1);
    Matcher lhsMatcher = NonTerminalMarkupNode.PATTERN.matcher(rawLHS);
    if (!lhsMatcher.matches()) {
      throw new TransitionRuleParseException("The left-hand side of the rule should be a non-terminal, but was " + rawLHS);
    }
    String lhsTag = lhsMatcher.group(1);
    NonTerminalNode lhs = lhsTag.isEmpty() ? new StartNode() : new NonTerminalMarkupNode(lhsTag);

    String rawRHS = rulematcher.group(2);
    Matcher rhsMatcher = RHS_PATTERN.matcher(rawRHS);
    if (!rhsMatcher.matches()) {
      throw new TransitionRuleParseException("The right-hand side of the rule should have a root and zero or more children, but was " + rawRHS);
    }
    Node rhsRoot = toNode(rhsMatcher.group(1));
    final List<Node> rhsChildren = new ArrayList<>();
    String rawChildren = rhsMatcher.group(3);
    if (rawChildren != null) {
      String[] splitChildren = rawChildren.split(",");
      stream(splitChildren)
          .map(TransitionRuleFactory::toNode)
          .forEach(rhsChildren::add);

    } else {
      rhsRoot = toNode(rawRHS);
    }

    final Tree<Node> rhs = new Tree<>(rhsRoot, rhsChildren);
    final TransitionRule transitionRule = new TransitionRule(lhs, rhs);
    return transitionRule;
  }

  private static Node toNode(final String nodeSerialization) {
    if (nodeSerialization.startsWith("(")) {
      String content = nodeSerialization.replaceAll("[\\(\\)]", "");
      return new TagNode(content);
    }
    if (nodeSerialization.startsWith("{")) {
      String content = nodeSerialization.replaceAll("[\\{\\}]", "");
      return new NonTerminalMarkupNode(content);
    }
    if (nodeSerialization.startsWith("\"")) {
      return new AnyTextNode();
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
      throw new TransitionRuleSetValidationException("No startnode transition rule ({} => ...) found!");
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
              + nonTerminalsWithoutTransitionRules.stream()
              .collect(joining(",")));
    }

    detectCycle(ruleSet, startNode);

  }

  private static void detectCycle(final List<TransitionRule> ruleSet, final String startNode) {
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
        String head = revisits.size() == 1 ? "This transition rule introduces a cycle"
            : "These transition rules introduce cycles";
        String message = head + ":\n" + offendingRules;
        throw new TransitionRuleSetValidationException(message);
      }
      toVisit.addAll(newNodes);
      visited.add(next);
    }
  }
}
