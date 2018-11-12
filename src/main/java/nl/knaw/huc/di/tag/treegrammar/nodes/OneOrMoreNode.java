package nl.knaw.huc.di.tag.treegrammar.nodes;

import nl.knaw.huc.di.tag.treegrammar.Tree;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.text.MessageFormat.format;

public class OneOrMoreNode implements Node {
  private final Node childNode;

  public OneOrMoreNode(final Node childNode) {
    this.childNode = childNode;
  }

  @Override
  public boolean matches(final Node node) {
    return node instanceof OneOrMoreNode
        && ((OneOrMoreNode) node).childNode.matches(childNode);
  }

  @Override
  public Node copy() {
    return new OneOrMoreNode(childNode.copy());
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
      throw new RuntimeException(format("{0} should have at least one NonTerminal, but has none.", this));
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
    return childNode + "*";
  }

  @Override
  public int hashCode() {
    return getClass().hashCode() + childNode.hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    return obj instanceof OneOrMoreNode
        && ((OneOrMoreNode) obj).childNode.equals(childNode);
  }
}
