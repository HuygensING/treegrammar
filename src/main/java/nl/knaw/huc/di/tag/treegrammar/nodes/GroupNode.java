package nl.knaw.huc.di.tag.treegrammar.nodes;

import nl.knaw.huc.di.tag.treegrammar.Tree;

import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

public class GroupNode implements NonTerminalNode {

  private final List<Node> elements;

  public GroupNode(List<Node> elements) {
    this.elements = elements;
  }

  @Override
  public boolean matches(final Node node) {
    return elements.get(0).matches(node);
  }

  @Override
  public Stream<NonTerminalNode> nonTerminalNodeStream() {
    return elements.stream().flatMap(Node::nonTerminalNodeStream);
  }

  @Override
  public void postProcess(Tree<Node> completeTree, List<Node> rootChildren) {
    completeTree.removeNode(this);

  }

  @Override
  public Node copy() {
    return this;
  } //TODO: copy elements?

  @Override
  public String toString() {
    return elements.stream()
        .map(Object::toString)
        .collect(joining(" ", "{", "}"));
  }

}
