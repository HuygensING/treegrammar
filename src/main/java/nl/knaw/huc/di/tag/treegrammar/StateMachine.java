package nl.knaw.huc.di.tag.treegrammar;

import nl.knaw.huc.di.tag.treegrammar.nodes.AnyTextNode;
import nl.knaw.huc.di.tag.treegrammar.nodes.Node;
import nl.knaw.huc.di.tag.treegrammar.nodes.NonTerminalNode;
import nl.knaw.huc.di.tag.treegrammar.nodes.StartNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static java.text.MessageFormat.format;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

/*
 * author: Ronald Haentjens Dekker
 * date: 11-09-2018
 *
 * in een tree automata willen we een tree bijhouden
 * In een state machine ga je van state naar state
 * Echter in een tree model zijn er meerdere mogelijke non terminals
 * die je kunt vervangen.
 */
class StateMachine {
  private static final Logger LOG = LoggerFactory.getLogger(StateMachine.class);
  private Tree<Node> completeTree; // tree die we aan het opbouwen zijn
  private final List<TransitionRule> rules = new ArrayList<>();
  private final Map<NonTerminalNode, Tree<Node>> nodeReplacementMap = new HashMap<>();
  private final List<NonTerminalNode> nonTerminalsToProcess = new ArrayList<>();

  StateMachine() {
    init();
  }

  private void init() {
    StartNode startNode = new StartNode();
    this.completeTree = new Tree<>(startNode);
//    this.pointerToCurrentNode = startNode;
    // nu hebben we nog transitie rules nodig.
    nonTerminalsToProcess.clear();
    nonTerminalsToProcess.add(startNode);
  }

  public void addTransitionRule(TransitionRule transitionRule) {
    this.rules.add(transitionRule);
    nodeReplacementMap.put(transitionRule.lefthandside, transitionRule.righthandside);
  }

  // bij de state machine komen nodes binnen
  // In de tree die we aan het bouwen zijn zitten nog NonTerminal nodes, waarvan er 1 aan de beurt is om vervangen te worden.
  // We zoeken nu een transition rule die deze NonTerminal aan de linkerkant heeft, en
  // matched met de binnenkomende node

  // zo niet; dan zitten we in een error.
  // input zou eigenlijk tree moeten zijn.
  public void processInput(Node node) {
    // We zoeken eerst op naar welke node de huidige pointer verwijst.
    // Dan kijken we welke transitierules er zijn voor dat type node.

    LOG.info("nextNonTerminals={}", nextNonTerminals());

    if (nonTerminalsToProcess.isEmpty()) {
      throw new RuntimeException("Unexpected node " + node);
    }

    final Node nonTerminalNode = nonTerminalsToProcess.remove(0);
    if (nonTerminalNode instanceof AnyTextNode) {
      if (!nonTerminalNode.matches(node)) {
        throw new RuntimeException("Expected text node, but got " + node);
      }
      // replace the AnyText node
      if (nonTerminalNode == completeTree.root) {
        completeTree.root = node;
      } else {
        Tree nodeAsTree = new Tree(node);
        completeTree.mergeTreeIntoCurrentTree(nonTerminalNode, nodeAsTree);
      }
      return;
    }

    //    LOG.info("applicableRules={}", applicableRules);
    if (nonTerminalNode.nonTerminalNodeStream().noneMatch(nodeReplacementMap::containsKey)) {
      throw new RuntimeException(format("No transition rule found! Current state: {0}, expected: {1}", node, nonTerminalNode));
    }
//    TransitionRule theRule = applicableRules.get(0); // NOT correct
//    // if there are more transition rules, then make a ChoiceNode
//    LOG.info("Transition rule found: {}! We want to know the right hand side", theRule);
//    Tree<Node> righthandside = theRule.righthandside;
//    LOG.info("Expectation: {}", righthandside);

    // Nu moeten we checken of de transitierule die we gevonden hebben ook past bij wat we binnen kregen
    // Ik weet nog niet hoe dat moet gewoon bij
    // We vervangen de aangewezen node door de nieuwe van de RHS
    // de current pointer moet dan naar het eerste kind van de RHS
    // NB: Dit is te simplistisch.

    //    Tree<Node> potentialReplacement = nodeReplacementMap.get(nonTerminalNode);
    // check whether tag of right hand side is the same as the incoming tag.
    Tree<Node> replacementTree = nonTerminalNode.nonTerminalNodeStream()
        .map(nodeReplacementMap::get)
        .filter(t -> t.root == null
            ? t.getRootChildren().get(0).matches(node)
            : t.root.matches(node)
        )
        .findFirst()
        .orElseThrow(() -> {
          final String expectation = nonTerminalNode.nonTerminalNodeStream()
              .map(nodeReplacementMap::get)
              .map(t -> t.root)
              .filter(Objects::nonNull)
              .map(Object::toString)
              .collect(joining(" or "));
          String message = format("No match: expected {0} but found {1}", expectation, node);
          return new RuntimeException(message);
        });

    final Node rootCopy = replacementTree.root == null
        ? null
        : replacementTree.root.copy();
    final List<Node> childrenCopy = replacementTree.getRootChildren()
        .stream()
        .map(Node::copy)
        .collect(toList());
    Tree<Node> rhsCopy = new Tree<>(rootCopy, childrenCopy);
    if (nonTerminalNode == completeTree.root) {
      completeTree = rhsCopy;
    } else {
      completeTree.mergeTreeIntoCurrentTree(nonTerminalNode, rhsCopy);
    }
    List<NonTerminalNode> nonTerminalNodeList = nonTerminals(replacementTree);
    nonTerminalsToProcess.addAll(0, nonTerminalNodeList);
//      nonTerminalNode = theRule.firstNonTerminalNode().orElse(null);

    LOG.info("nonTerminalNode={}", nonTerminalNode);
    //! Dan gaan we op zoek naar de transitierule van de huidige state
    //! Gegeven de transitierule en de nieuwe op basis van de input.
    // we gaan alle transitierules af.
    // het zou beter zijn om dit te indexeren; maar ok..
  }

  public Tree<Node> getTree() {
    return completeTree;
  }

  public List<Node> nextNonTerminals() {
    final List<Node> list = new ArrayList<>();
    Node root = completeTree.root;
    if (root instanceof NonTerminalNode) {
      list.add(root);
    } else {
      List<Node> firstNonTerminals = completeTree.getRootChildren().stream()
          .map(Node::firstNonTerminals)
          .filter(l -> !l.isEmpty())
          .findFirst()
          .orElse(emptyList());
      list.addAll(firstNonTerminals);
    }
    return list;
  }

  void pop() {
    // TODO?
  }

  void reset() {
    init();
  }

  private List<NonTerminalNode> nonTerminals(final Tree<Node> nodeTree) {
    List<Node> treeNodes = new ArrayList<>();
    treeNodes.add(nodeTree.root);
    treeNodes.addAll(nodeTree.getRootChildren());
    return treeNodes.stream()
        .filter(NonTerminalNode.class::isInstance)
        .map(NonTerminalNode.class::cast)
        .collect(toList());
  }

}
