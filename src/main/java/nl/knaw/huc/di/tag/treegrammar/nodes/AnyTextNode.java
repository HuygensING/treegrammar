package nl.knaw.huc.di.tag.treegrammar.nodes;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;

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
  public List<Node> firstNonTerminals() {
    return emptyList();
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
