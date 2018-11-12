package nl.knaw.huc.di.tag.treegrammar.nodes;

import nl.knaw.huc.di.tag.treegrammar.Tree;

import java.util.List;
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
  public void postProcess(Tree<Node> completeTree, List<Node> rootChildren) {
    // TODO
  }

  @Override
  public Node copy() {
    return this;
  }

  @Override
  public String toString() {
    return CIPHER;
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    return obj instanceof AnyTextNode;
  }
}
