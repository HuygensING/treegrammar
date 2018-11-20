package nl.knaw.huc.di.tag.treegrammar.nodes;

import nl.knaw.huc.di.tag.treegrammar.Tree;

import java.util.List;
import java.util.stream.Stream;

public interface Node {
  boolean matches(Node node);

  Stream<NonTerminalNode> nonTerminalNodeStream();

  void postProcess(Tree<Node> completeTree, List<Node> rootChildren);

  List<Node> firstNonTerminals(Tree<Node> completeTree);
}
