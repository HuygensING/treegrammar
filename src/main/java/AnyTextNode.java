/**
 * Created by ronalddekker on 28/09/18.
 */
public class AnyTextNode extends TextNode {

  public AnyTextNode() {
    super("*");
  }

  @Override
  public String toString() {
    return "/.*/";
  }

  @Override
  public boolean matches(Node node) {
    return node instanceof TextNode;
  }
}
