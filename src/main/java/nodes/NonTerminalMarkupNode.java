package nodes;

public class NonTerminalMarkupNode implements NonTerminalNode {
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
