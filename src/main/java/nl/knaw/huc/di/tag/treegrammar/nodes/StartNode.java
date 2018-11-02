package nl.knaw.huc.di.tag.treegrammar.nodes;

import java.util.List;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;

public class StartNode implements NonTerminalNode {
  public static final String CIPHER = "#";

  public StartNode() {
  }

  @Override
  public boolean matches(Node node) {
    return node instanceof StartNode;
  }

  @Override
  public Stream<NonTerminalNode> nonTerminalNodeStream() {
    return Stream.of(this);
  }

  @Override
  public List<Node> firstNonTerminals() {
    return singletonList(this);
  }

  @Override
  public Node copy() {
    return this;
  }

  @Override
  public int hashCode() {
    return CIPHER.hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    return obj instanceof StartNode;
  }

  @Override
  public String toString() {
    return CIPHER;
  }
}
