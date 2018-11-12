package nl.knaw.huc.di.tag.treegrammar.nodes;

import nl.knaw.huc.di.tag.treegrammar.Tree;

import java.util.List;
import java.util.stream.Stream;

public interface Node {
  boolean matches(Node node);

  Node copy();

  Stream<NonTerminalNode> nonTerminalNodeStream();

  void postProcess(Tree<Node> completeTree, List<Node> rootChildren);
}
