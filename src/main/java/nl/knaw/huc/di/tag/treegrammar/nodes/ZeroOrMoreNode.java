package nl.knaw.huc.di.tag.treegrammar.nodes;

import nl.knaw.huc.di.tag.treegrammar.Tree;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ZeroOrMoreNode implements Node {
  int count = 0;
  private final Node childNode;

  public ZeroOrMoreNode(final Node childNode) {
    this.childNode = childNode;
  }

  @Override
  public boolean matches(final Node node) {
    return node instanceof ZeroOrMoreNode
        && ((ZeroOrMoreNode) node).childNode.matches(childNode);
  }

  @Override
  public Stream<NonTerminalNode> nonTerminalNodeStream() {
    return childNode.nonTerminalNodeStream();
  }

  @Override
  public void postProcess(Tree<Node> completeTree, List<Node> rootChildren) {
    List<Node> childNodes = new ArrayList<>(rootChildren);
    if (childNodes.size() == 1 && childNodes.get(0) instanceof NonTerminalMarkupNode) {
      completeTree.removeSubTreeWithRootNode(this);
    } else {
      childNodes.stream()
          .filter(NonTerminalMarkupNode.class::isInstance)
          .forEach(completeTree::removeNode);
      completeTree.removeNode(this);
    }
  }

  @Override
  public List<Node> firstNonTerminals(Tree<Node> completeTree) {
    List<Node> list = new ArrayList<>();
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
    return childNode + "+";
  }

  @Override
  public int hashCode() {
    return getClass().hashCode() + childNode.hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    return obj instanceof ZeroOrMoreNode
        && ((ZeroOrMoreNode) obj).childNode.equals(childNode);
  }

}
