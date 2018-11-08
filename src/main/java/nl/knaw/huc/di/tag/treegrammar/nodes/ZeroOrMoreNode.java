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
}
