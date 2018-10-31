package nl.knaw.huc.di.tag.treegrammar;

import nl.knaw.huc.di.tag.treegrammar.nodes.Node;

public class GroupNode implements Node {
  @Override
  public boolean matches(final Node node) {
    return false;
  }

  @Override
  public Node copy() {
    return this;
  }
}
