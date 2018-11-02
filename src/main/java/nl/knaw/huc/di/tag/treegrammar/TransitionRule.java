package nl.knaw.huc.di.tag.treegrammar;

import nl.knaw.huc.di.tag.treegrammar.nodes.Node;
import nl.knaw.huc.di.tag.treegrammar.nodes.NonTerminalMarkupNode;
import nl.knaw.huc.di.tag.treegrammar.nodes.NonTerminalNode;
import nl.knaw.huc.di.tag.treegrammar.nodes.TerminalNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
    // LOG.info("Checking with "+lefthandside.tag+" and "+tag);
    // de lefthandside is een tree node zonder kinderen...
    // We kijken of de tag vergelijkbaar is
    return lefthandside.matches(node);
  }

  public Optional<Node> firstNonTerminalNode() {
    if (righthandside.root instanceof NonTerminalNode) {
      return Optional.of(righthandside.root);
    }
    return righthandside.getRootChildren()
        .stream()
        .filter(NonTerminalNode.class::isInstance)
        .findFirst();
  }

  boolean hasNoRHSTerminals() {
    if (righthandside.root instanceof TerminalNode) {
      return false;
    }
    return righthandside.getRootChildren()
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
    list.addAll(righthandside.getRootChildren());
    return list.stream()
        .flatMap(Node::nonTerminalNodeStream)
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
    list.addAll(righthandside.getRootChildren());
    return list.stream()
        .filter(NonTerminalNode.class::isInstance)
        .map(NonTerminalNode.class::cast)
        .collect(toList());
  }

}
