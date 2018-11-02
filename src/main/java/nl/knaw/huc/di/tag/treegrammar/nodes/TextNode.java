package nl.knaw.huc.di.tag.treegrammar.nodes;

import java.util.stream.Stream;

/**
 * Created by ronalddekker on 28/09/18.
 */
public class TextNode implements TerminalNode {
  private final String content;

  public TextNode(String content) {
    this.content = content;
  }

  @Override
  public boolean matches(Node node) {
    return node instanceof TextNode
        && ((TextNode) node).content.equals(content);
  }

  @Override
  public Stream<NonTerminalNode> nonTerminalNodeStream() {
    return Stream.empty();
  }

  @Override
  public Node copy() {
    return new TextNode(content);
  }

  @Override
  public String toString() {
    return "\"" + content + "\"";
  }
}
