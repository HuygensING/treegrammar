package nl.knaw.huc.di.tag.treegrammar.nodes;

import java.util.List;

public class GroupNode implements NonTerminalNode {

  private final List<Node> elements;

  public GroupNode(List<Node> elements) {
    this.elements = elements;
  }

  @Override
  public boolean matches(final Node node) {
    return elements.get(0).matches(node);
  }

  @Override
  public Node copy() {
    return this;
  }
}
