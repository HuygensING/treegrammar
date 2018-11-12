package nl.knaw.huc.di.tag.treegrammar.nodes;

import nl.knaw.huc.di.tag.treegrammar.Tree;

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
  public Node copy() {
    return new TagNode(tag);
  }

  @Override
  public String toString() {
    return tag;
  }

}
