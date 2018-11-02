package nl.knaw.huc.di.tag.treegrammar.nodes;

import java.util.stream.Stream;

public class AnyTextNode implements NonTerminalNode {

  private static final String CIPHER = "_";

  public AnyTextNode() {
  }

  @Override
  public boolean matches(Node node) {
    return node instanceof TextNode;
  }

  @Override
  public Stream<NonTerminalNode> nonTerminalNodeStream() {
    return Stream.empty();
  }

  @Override
  public Node copy() {
    return this;
  }

  @Override
  public String toString() {
    return CIPHER;
  }
}
