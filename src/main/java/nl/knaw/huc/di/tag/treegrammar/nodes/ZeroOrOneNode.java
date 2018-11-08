package nl.knaw.huc.di.tag.treegrammar.nodes;

import java.util.stream.Stream;

public class ZeroOrOneNode implements Node {
  int count = 0;
  private Node childNode;

  public ZeroOrOneNode(final Node childNode) {
    this.childNode = childNode;
  }

  @Override
  public boolean matches(final Node node) {
    return false;
  }

  @Override
  public Node copy() {
    return new ZeroOrOneNode(childNode.copy());
  }

  @Override
  public Stream<NonTerminalNode> nonTerminalNodeStream() {
    return childNode.nonTerminalNodeStream();
  }

  @Override
  public String toString() {
    return childNode + "?";
  }

  @Override
  public int hashCode() {
    return getClass().hashCode() + childNode.hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    return obj instanceof ZeroOrOneNode
        && ((ZeroOrOneNode) obj).childNode.equals(childNode);
  }
}
