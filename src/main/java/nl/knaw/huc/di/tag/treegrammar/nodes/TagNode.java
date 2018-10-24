package nl.knaw.huc.di.tag.treegrammar.nodes;

/**
 * Created by ronalddekker on 28/09/18.
 */
public class TagNode implements TerminalNode {
  private final String tag;

  public TagNode(String tag) {
    this.tag = tag;
  }

  @Override
  public String toString() {
    return tag;
  }

  @Override
  public boolean matches(Node node) {
    return node instanceof TagNode && ((TagNode) node).tag.equals(tag);
  }

  @Override
  public Node copy() {
    return new TagNode(tag);
  }
}
