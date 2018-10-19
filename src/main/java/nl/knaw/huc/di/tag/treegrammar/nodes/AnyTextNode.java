package nl.knaw.huc.di.tag.treegrammar.nodes;

public class AnyTextNode implements NonTerminalNode {

  public static final String CIPHER = "_";

  public AnyTextNode() {
  }

  @Override
  public String toString() {
    return CIPHER;
  }

  @Override
  public boolean matches(Node node) {
    return node instanceof TextNode;
  }

  @Override
  public Node copy() {
    return this;
  }
}
