import nodes.Node;
import nodes.NonTerminalMarkupNode;
import nodes.NonTerminalNode;
import nodes.TerminalNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

// * author: Ronald Haentjens Dekker
// * date: 11-09-2018
//\
// Een rule heeft een lefthandside (dat is een non terminal markup node
// Een right hand side: that is een tree.
class TransitionRule {
  final NonTerminalNode lefthandside;
  final Tree<Node> righthandside;

  public TransitionRule(NonTerminalNode lefthandside, Tree<Node> righthandside) {
    this.lefthandside = lefthandside;
    this.righthandside = righthandside;
  }

  public boolean lefthandsideIsApplicableFor(Node node) {
    // System.out.println("Checking with "+lefthandside.tag+" and "+tag);
    // de lefthandside is een tree node zonder kinderen...
    // We kijken of de tag vergelijkbaar is
    return lefthandside.matches(node);
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
    if (righthandside.root instanceof TerminalNode) {
      return false;
    }
    return righthandside.children.get(righthandside.root)
        .stream()
        .noneMatch(TerminalNode.class::isInstance);
  }

  public NonTerminalNode lefthandsideNode() {
    return lefthandside;
  }

  public Stream<NonTerminalMarkupNode> righthandsideNonTerminalMarkupNodes() {
    List<Node> list = new ArrayList<>();
    if (righthandside.root instanceof NonTerminalMarkupNode) {
      list.add(righthandside.root);
    }
    list.addAll(righthandside.children.get(righthandside.root));
    return list.stream()
        .filter(NonTerminalMarkupNode.class::isInstance)
        .map(NonTerminalMarkupNode.class::cast);
  }

  @Override
  public String toString() {
    return lefthandside + " => " + righthandside;
  }

  public List<NonTerminalNode> righthandsideNonTerminals() {
    List<Node> list = new ArrayList<>();
    if (righthandside.root instanceof NonTerminalMarkupNode) {
      list.add(righthandside.root);
    }
    list.addAll(righthandside.children.get(righthandside.root));
    return list.stream()
        .filter(NonTerminalNode.class::isInstance)
        .map(NonTerminalNode.class::cast)
        .collect(toList());
  }
}
