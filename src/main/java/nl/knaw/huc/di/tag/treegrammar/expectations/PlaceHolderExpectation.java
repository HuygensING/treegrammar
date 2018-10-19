package nl.knaw.huc.di.tag.treegrammar.expectations;

import nl.knaw.huc.di.tag.treegrammar.nodes.Node;

public class PlaceHolderExpectation implements AbstractExpectation {
  public Node node;

  public PlaceHolderExpectation(Node node) {
    this.node = node;
  }

  @Override
  public int hashCode() {
    return node.hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    return obj instanceof PlaceHolderExpectation
        && ((PlaceHolderExpectation) obj).node.equals(node);
  }

  @Override
  public String toString() {
    return "<% " + node + " %>";
  }
}
