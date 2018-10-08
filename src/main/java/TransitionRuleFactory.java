import nodes.*;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.stream;

public class TransitionRuleFactory {

//  private void parseTransitionRules(final List<String> transitionRules) {
//    transitionRules.stream()
//        .map(XMLValidatorUsingTreeGrammars::parseTransitionRule)
//        .forEach(stateMachine::addTransitionRule);
//  }

  static TransitionRule fromString(final String transitionRuleString) {
    final String[] parts = transitionRuleString.split("=>", 2);
    String rawLHS = parts[0].trim();
    String rawRHS = parts[1].trim();
    String lhsTag = rawLHS.replace("{", "").replace("}", "");

    Node rhsRoot = null;
    final List<Node> rhsChildren = new ArrayList<>();
    if (rawRHS.contains("[")) {
      final String[] rParts = rawRHS.replace("]", "").split("\\[", 2);
      String rawRoot = rParts[0];
      rhsRoot = toNode(rawRoot);
      String[] rawChildren = rParts[1].split(",");
      stream(rawChildren)
          .map(TransitionRuleFactory::toNode)
          .forEach(rhsChildren::add);

    } else {
      rhsRoot = toNode(rawRHS);
    }
    final NonTerminalNode lhs = lhsTag.isEmpty() ? new StartNode() : new NonTerminalMarkupNode(lhsTag);
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
