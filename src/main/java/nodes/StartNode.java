package nodes;

public class StartNode implements NonTerminalNode {
  public static String CIPHER = "#";

  public StartNode() {
  }

  @Override
  public String toString() {
    return CIPHER;
  }

  @Override
  public boolean matches(Node node) {
    return node instanceof StartNode;
  }
}
