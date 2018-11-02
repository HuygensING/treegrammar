package nl.knaw.huc.di.tag.treegrammar.nodes;

import java.util.stream.Stream;

public interface Node {
  boolean matches(Node node);

  Node copy();

  Stream<NonTerminalNode> nonTerminalNodeStream();
}
