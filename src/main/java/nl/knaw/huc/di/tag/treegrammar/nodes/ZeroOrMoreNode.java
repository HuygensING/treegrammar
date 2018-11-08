package nl.knaw.huc.di.tag.treegrammar.nodes;

import java.util.stream.Stream;

public class ZeroOrMoreNode implements Node {
  int count = 0;
  Node childNode;

  public ZeroOrMoreNode(final Node childNode) {
    this.childNode = childNode;
  }

  @Override
  public boolean matches(final Node node) {
    return node instanceof ZeroOrMoreNode
        && ((ZeroOrMoreNode) node).childNode.matches(childNode);
  }

  @Override
  public Node copy() {
    return new ZeroOrMoreNode(childNode.copy());
  }

  @Override
  public Stream<NonTerminalNode> nonTerminalNodeStream() {
    return childNode.nonTerminalNodeStream();
  }

  @Override
  public String toString() {
    return childNode + "+";
  }

  @Override
  public int hashCode() {
    return getClass().hashCode() + childNode.hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    return obj instanceof ZeroOrMoreNode
        && ((ZeroOrMoreNode) obj).childNode.equals(childNode);
  }

}
