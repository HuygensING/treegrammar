package nl.knaw.huc.di.tag.treegrammar;

import nl.knaw.huc.di.tag.treegrammar.nodes.Node;
import nl.knaw.huc.di.tag.treegrammar.nodes.NonTerminalNode;

import java.util.stream.Stream;

public class RejectedNode implements Node {
  @Override
  public boolean matches(final Node node) {
    return false;
  }

  @Override
  public Node copy() {
    return null;
  }

  @Override
  public Stream<NonTerminalNode> nonTerminalNodeStream() {
    return null;
  }

  @Override
  public String toString() {
    return "(X)";
  }
}
