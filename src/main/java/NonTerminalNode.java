/**
 * Created by ronalddekker on 28/09/18.
 */
public class NonTerminalNode implements Node {
  final String variableName;

  public NonTerminalNode(String variableName) {
    this.variableName = variableName;
  }

  @Override
  public String toString() {
    return "[" + variableName + "]";
  }

  @Override
  public boolean matches(Node node) {
    return (node instanceof NonTerminalNode && ((NonTerminalNode) node).variableName.equals(variableName))
        || (node instanceof TagNode);
  }
}
