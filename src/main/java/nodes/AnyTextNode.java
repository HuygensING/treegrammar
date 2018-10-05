package nodes;

public class AnyTextNode implements NonTerminalNode {

  public AnyTextNode() {
  }

  @Override
  public String toString() {
    return "/.*/";
  }

  @Override
  public boolean matches(Node node) {
    return node instanceof TextNode;
  }
}
