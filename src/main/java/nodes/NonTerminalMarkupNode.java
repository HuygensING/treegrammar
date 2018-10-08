package nodes;

import java.util.regex.Pattern;

public class NonTerminalMarkupNode implements NonTerminalNode {
  public static final Pattern PATTERN = Pattern.compile("\\{([a-zA-Z_]*)\\}");
  final String variableName;

  public NonTerminalMarkupNode(String variableName) {
    this.variableName = variableName;
  }

  @Override
  public String toString() {
    return "[" + variableName + "]";
  }

  @Override
  public boolean matches(Node node) {
    return (node instanceof NonTerminalMarkupNode && ((NonTerminalMarkupNode) node).variableName.equals(variableName))
        || (node instanceof TagNode);
  }
}
