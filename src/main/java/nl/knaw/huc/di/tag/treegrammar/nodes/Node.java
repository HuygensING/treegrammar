package nl.knaw.huc.di.tag.treegrammar.nodes;

import java.util.List;
import java.util.stream.Stream;

public interface Node {
  boolean matches(Node node);

  Node copy();

  Stream<NonTerminalNode> nonTerminalNodeStream();

  List<Node> firstNonTerminals();
}
