import java.util.Optional;

// * author: Ronald Haentjens Dekker
// * date: 11-09-2018
//\
// Een rule heeft een lefthandside (dat is een non terminal markup node
// Een right hand side: that is een tree.
public class TransitionRule {
  private Node lefthandside;
  Tree<Node> righthandside;

  public TransitionRule(Node lefthandside, Tree<Node> righthandside) {
    this.lefthandside = lefthandside;
    this.righthandside = righthandside;
  }

  public boolean lefthandsideIsApplicableFor(Node node) {
    // System.out.println("Checking with "+lefthandside.tag+" and "+tag);
    // de lefthandside is een tree node zonder kinderen...
    // We kijken of de tag vergelijkbaar is
    return lefthandside.matches(node);
  }

  @Override
  public String toString() {
    return lefthandside + " => " + righthandside;
  }

  public Optional<Node> firstNonTerminalNode() {
    if (righthandside.root instanceof NonTerminalNode) {
      return Optional.of(righthandside.root);
    }
    return righthandside.children.get(righthandside.root)
        .stream()
        .filter(NonTerminalNode.class::isInstance)
        .findFirst();
  }

  boolean hasNoRHSTerminals() {
    if (!(righthandside.root instanceof NonTerminalNode)) {
      return false;
    }
    return righthandside.children.get(righthandside.root)
        .stream()
        .allMatch(NonTerminalNode.class::isInstance);
  }
}
