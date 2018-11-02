package nl.knaw.huc.di.tag.treegrammar.nodes;

import java.util.List;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;

/**
 * Created by ronalddekker on 28/09/18.
 */
public class TagNode implements TerminalNode {
  private final String tag;

  public TagNode(String tag) {
    this.tag = tag;
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
  public List<Node> firstNonTerminals() {
    return emptyList();
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
