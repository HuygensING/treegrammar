import nodes.*;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Arrays.stream;

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

}
