package nl.knaw.huc.di.tag.treegrammar.nodes;

public class StartNode implements NonTerminalNode {
  public static final String CIPHER = "#";

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

  @Override
  public Node copy() {
    return this;
  }
}
