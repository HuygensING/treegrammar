package nl.knaw.huc.di.tag.treegrammar;

import nl.knaw.huc.di.tag.treegrammar.nodes.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

// * author: Ronald Haentjens Dekker
// * date: 11-09-2018
//\
// Een rule heeft een lefthandside (dat is een non terminal markup node
// Een right hand side: that is een tree.
class TransitionRule {
  final NonTerminalNode leftHandSide;
  final private Tree<Node> rightHandSide;
  final private Supplier<Tree<Node>> rightHandSideSupplier;

  public TransitionRule(NonTerminalNode lhs, Supplier<Tree<Node>> rhsSupplier) {
    this.leftHandSide = lhs;
    this.rightHandSide = rhsSupplier.get();
    this.rightHandSideSupplier = rhsSupplier;
  }

  public Tree<Node> getRightHandSide() {
    return rightHandSideSupplier.get();
  }

  public Supplier<Tree<Node>> getRightHandSideSupplier() {
    return rightHandSideSupplier;
  }

//  public boolean leftHandSideIsApplicableFor(Node node) {
//    // LOG.info("Checking with "+lefthandside.tag+" and "+tag);
//    // de lefthandside is een tree node zonder kinderen...
//    // We kijken of de tag vergelijkbaar is
//    return leftHandSide.matches(node);
//  }

//  public Optional<Node> firstNonTerminalNode() {
//    if (rightHandSide.root instanceof NonTerminalNode) {
//      return Optional.of(rightHandSide.root);
//    }
//    return rightHandSide.getRootChildren()
//        .stream()
//        .filter(NonTerminalNode.class::isInstance)
//        .findFirst();
//  }

//  boolean hasNoRHSTerminals() {
//    if (rightHandSide.root instanceof TerminalNode) {
//      return false;
//    }
//    return rightHandSide.getRootChildren()
//        .stream()
//        .noneMatch(TerminalNode.class::isInstance);
//  }

  public NonTerminalNode leftHandSideNode() {
    return leftHandSide;
  }

  public Stream<NonTerminalMarkupNode> righthandsideNonTerminalMarkupNodes() {
    List<Node> list = new ArrayList<>();
    if (rightHandSide.root instanceof NonTerminalMarkupNode) {
      list.add(rightHandSide.root);
    }
    list.addAll(rightHandSide.getRootChildren());
    return list.stream()
        .flatMap(Node::nonTerminalNodeStream)
        .filter(NonTerminalMarkupNode.class::isInstance)
        .map(NonTerminalMarkupNode.class::cast);
  }

//  public List<NonTerminalNode> rightHandSideNonTerminals() {
//    List<Node> list = new ArrayList<>();
//    if (rightHandSide.root instanceof NonTerminalMarkupNode) {
//      list.add(rightHandSide.root);
//    }
//    list.addAll(rightHandSide.getRootChildren());
//    return list.stream()
//        .filter(NonTerminalNode.class::isInstance)
//        .map(NonTerminalNode.class::cast)
//        .collect(toList());
//  }

  boolean isTerminating() {
    List<Node> rootChildren = rightHandSide.getRootChildren();
    return rightHandSide.root instanceof TagNode
        && (rootChildren.isEmpty()
        || (rootChildren.size() == 1
        && rootChildren.get(0) instanceof AnyTextNode
    ));
  }

  @Override
  public String toString() {
    return leftHandSide + " => " + rightHandSide;
  }

  public Function<Node, Boolean> getNodeMatcher() {
    return rightHandSide.root::matches;
  }
}
