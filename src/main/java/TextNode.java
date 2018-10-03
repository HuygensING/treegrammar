/**
 * Created by ronalddekker on 28/09/18.
 */
public class TextNode implements Node {
  final String content;

  public TextNode(String content) {
    this.content = content;
  }

  @Override
  public String toString() {
    return "\"" + content + "\"";
  }
}
