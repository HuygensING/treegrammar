package nodes;

public class StartNode implements NonTerminalNode {

  public StartNode() {
  }

  @Override
  public String toString() {
    return "{}";
  }

  @Override
  public boolean matches(Node node) {
    return node instanceof StartNode;
  }
}
