package nl.knaw.huc.di.tag.treegrammar.nodes;

public interface Node {
  boolean matches(Node node);

  Node copy();
}
