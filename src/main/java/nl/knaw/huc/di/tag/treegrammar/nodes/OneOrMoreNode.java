package nl.knaw.huc.di.tag.treegrammar.nodes;

import java.util.stream.Stream;

public class OneOrMoreNode implements Node {
  int count = 0;
  private Node childNode;

  public OneOrMoreNode(final Node childNode) {
    this.childNode = childNode;
  }

  @Override
  public boolean matches(final Node node) {
    return node instanceof OneOrMoreNode
        && ((OneOrMoreNode) node).childNode.matches(childNode);
  }

  @Override
  public Node copy() {
    return new OneOrMoreNode(childNode.copy());
  }

  @Override
  public Stream<NonTerminalNode> nonTerminalNodeStream() {
    return childNode.nonTerminalNodeStream();
  }

  @Override
  public String toString() {
    return childNode + "*";
  }
}
