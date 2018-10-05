package nodes;

/**
 * Created by ronalddekker on 28/09/18.
 */
public class StartNode implements NonTerminalNode {

  public StartNode() {
  }

  @Override
  public String toString() {
    return "{start}";
  }

  @Override
  public boolean matches(Node node) {
    return node instanceof StartNode;
  }
}
