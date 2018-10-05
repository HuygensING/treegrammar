package nodes;

public class StartNode implements NonTerminalNode {

  public StartNode() {
  }

  @Override
  public String toString() {
    return "{start}";
  }

  @Override
  public boolean matches(Node node) {
    return node instanceof StartNode;
  }
}
