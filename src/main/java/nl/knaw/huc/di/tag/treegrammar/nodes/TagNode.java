package nl.knaw.huc.di.tag.treegrammar.nodes;

import nl.knaw.huc.di.tag.treegrammar.Tree;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Created by ronalddekker on 28/09/18.
 */
public class TagNode implements TerminalNode {
  private final String tag;

  public TagNode(String tag) {
    this.tag = tag;
  }

  public String getTag() {
    return tag;
  }

  @Override
  public boolean matches(Node node) {
    return node instanceof TagNode && ((TagNode) node).tag.equals(tag);
  }

  @Override
  public Stream<NonTerminalNode> nonTerminalNodeStream() {
    return Stream.empty();
  }

  @Override
  public void postProcess(Tree<Node> completeTree, List<Node> rootChildren) {
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
    return tag;
  }
}
