package nl.knaw.huc.di.tag.treegrammar.nodes;

import nl.knaw.huc.di.tag.treegrammar.Tree;

import java.util.ArrayList;
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
  public List<NonTerminalNode> firstNonTerminals(Tree<Node> completeTree) {
    List<NonTerminalNode> list = new ArrayList<>();
    completeTree.children.get(this)
        .stream()
        .map(n -> n.firstNonTerminals(completeTree))
        .filter(l -> !l.isEmpty())
        .findFirst()
        .ifPresent(list::addAll);
    return list;
  }


  @Override
  public String toString() {
    return elements.stream()
        .map(Object::toString)
        .collect(joining(" ", "{", "}"));
  }

}
