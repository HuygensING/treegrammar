package nl.knaw.huc.di.tag.treegrammar;

import nl.knaw.huc.di.tag.treegrammar.nodes.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static java.util.Collections.emptyList;
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

  public void processInput(Node inputNode) {
    // We zoeken eerst op naar welke node de huidige pointer verwijst.
    // Dan kijken we welke transitierules er zijn voor dat type node.
    LOG.info("\n\n* completeTree=\n{}", TreeVisualizer.asText(completeTree));

    List<Node> nextNonTerminals = nextNonTerminals();
    LOG.info("nextNonTerminals={}", nextNonTerminals);
    AnyTextNode anyTextNode = new AnyTextNode();
    if (nextNonTerminals.contains(anyTextNode) && inputNode instanceof TextNode) {
      nodeReplacementMap.put(anyTextNode, new Tree(inputNode));
    }
    List<Tree<Node>> possibleReplacements = nextNonTerminals.stream()
        .map(nodeReplacementMap::get)
        .filter(Objects::nonNull)
        .collect(toList());
    LOG.info("possible replacements={}", possibleReplacements);

    List<Node> acceptableNodes = possibleReplacements.stream().map(t -> t.root).collect(toList());
    LOG.info("acceptable nodes={}", acceptableNodes);

    LOG.info("inputNode={}", inputNode);
    final List<Tree<Node>> acceptableReplacements = possibleReplacements.stream()
        .filter(t -> nodesMatch(t.root, inputNode))
        .collect(toList());
    LOG.info("acceptable replacements={}", acceptableReplacements);

    Node nodeToReplace = null;
    Tree<Node> replacementTree = null;
    List<Node> rejectedNonTerminalNodes = new ArrayList<>();
    for (Node n : nextNonTerminals) {
      Tree<Node> nodeTree = nodeReplacementMap.get(n);
      if (acceptableReplacements.contains(nodeTree)) {
        nodeToReplace = n;
        replacementTree = nodeTree;
      } else {
        rejectedNonTerminalNodes.add(n);
      }
    }
    LOG.info("action: replace node ({}) with tree ({})", nodeToReplace, replacementTree);
    Tree<Node> rhsCopy = cloneTree(replacementTree);
    replaceNodeWithTree(nodeToReplace, rhsCopy);

    LOG.info("action: reject nodes {}", rejectedNonTerminalNodes);
    rejectedNonTerminalNodes.forEach(n -> {
      Tree<Node> tree = new Tree(new RejectedNode());
      replaceNodeWithTree(n, tree);
    });

    if (nextNonTerminals.isEmpty()) {
      throw new RuntimeException("Unexpected node " + inputNode);
    }

    List<Node> matchingNonTerminals = nextNonTerminals.stream()
        .filter(n -> n.matches(inputNode))
        .collect(toList());
    LOG.info("matchingNonTerminals={}", matchingNonTerminals);

//    final Node nonTerminalNode = nonTerminalsToProcess.remove(0);
//    if (nonTerminalNode instanceof AnyTextNode) {
//      if (!nonTerminalNode.matches(inputNode)) {
//        throw new RuntimeException("Expected text node, but got " + inputNode);
//      }
//      // replace the AnyText node
//      Tree nodeAsTree = new Tree(inputNode);
//      replaceNodeWithTree(nonTerminalNode, nodeAsTree);
//      return;
//    }
//
//    //    LOG.info("applicableRules={}", applicableRules);
//    if (nonTerminalNode.nonTerminalNodeStream().noneMatch(nodeReplacementMap::containsKey)) {
//      throw new RuntimeException(format("No transition rule found! Current state: {0}, expected: {1}", inputNode, nonTerminalNode));
//    }
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
//    Tree<Node> replacementTree = nonTerminalNode.nonTerminalNodeStream()
//        .map(nodeReplacementMap::get)
//        .filter(t -> t.root == null
//            ? t.getRootChildren().get(0).matches(inputNode)
//            : t.root.matches(inputNode)
//        )
//        .findFirst()
//        .orElseThrow(() -> {
//          final String expectation = nonTerminalNode.nonTerminalNodeStream()
//              .map(nodeReplacementMap::get)
//              .map(t -> t.root)
//              .filter(Objects::nonNull)
//              .map(Object::toString)
//              .collect(joining(" or "));
//          String message = format("No match: expected {0} but found {1}", expectation, inputNode);
//          return new RuntimeException(message);
//        });
//
//    Tree<Node> rhsCopy = cloneTree(replacementTree);
//    replaceNodeWithTree(nonTerminalNode, rhsCopy);
//    List<NonTerminalNode> nonTerminalNodeList = nonTerminals(replacementTree);
//    nonTerminalsToProcess.addAll(0, nonTerminalNodeList);
////      nonTerminalNode = theRule.firstNonTerminalNode().orElse(null);
//
//    LOG.info("nonTerminalNode={}", nonTerminalNode);
    //! Dan gaan we op zoek naar de transitierule van de huidige state
    //! Gegeven de transitierule en de nieuwe op basis van de input.
    // we gaan alle transitierules af.
    // het zou beter zijn om dit te indexeren; maar ok..
  }

  private void replaceNodeWithTree(final Node nonTerminalNode, final Tree replacementTree) {
    if (nonTerminalNode == completeTree.root) {
      completeTree = replacementTree;
    } else {
      completeTree.mergeTreeIntoCurrentTree(nonTerminalNode, replacementTree);
    }
  }

  private boolean nodesMatch(final Node node1, final Node node2) {
    if (node1 instanceof TagNode && node2 instanceof TagNode) {
      final TagNode tagNode1 = (TagNode) node1;
      final TagNode tagNode2 = (TagNode) node2;
      return tagNode1.getTag().equals(tagNode2.getTag());
    }
    if (node1 instanceof TextNode && node2 instanceof TextNode) {
      return ((TextNode) node1).content.equals(((TextNode) node2).content);
    }
    throw new RuntimeException("Unhandled comparison between " + node1 + " and " + node2);
  }

  private Tree<Node> cloneTree(final Tree<Node> replacementTree) {
    final Node rootCopy = cloneRoot(replacementTree.root);
    List<Node> rootChildren = replacementTree.getRootChildren();
    final List<Node> rootChildrenCopy = cloneNodeList(rootChildren);
    Tree<Node> nodeTree = new Tree<>(rootCopy, rootChildrenCopy);
    replacementTree.children.entrySet().stream()
        .filter(e -> e.getKey() != replacementTree.root)
        .forEach(e -> {
          final Node parent = cloneRoot(e.getKey());
          final List<Node> valueClone = cloneNodeList(e.getValue());
          valueClone.forEach(child -> nodeTree.connect(parent, child));
        });
    return nodeTree;
  }

  private List<Node> cloneNodeList(final List<Node> rootChildren) {
    return rootChildren.stream()
        .map(Node::copy)
        .collect(toList());
  }

  private Node cloneRoot(final Node rootNode) {
    return rootNode == null
        ? null
        : rootNode.copy();
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
          .map(this::firstNonTerminals)
          .filter(l -> !l.isEmpty())
          .findFirst()
          .orElse(emptyList());
      list.addAll(firstNonTerminals);
    }
    return list;
  }

  private List<Node> firstNonTerminals(final Node node) {
    final List<Node> list = new ArrayList<>();
    if (node instanceof NonTerminalMarkupNode || node instanceof StartNode || node instanceof AnyTextNode) {
      list.add(node);

    } else if (node instanceof TagNode || node instanceof GroupNode) {
      completeTree.children.get(node)
          .stream()
          .map(this::firstNonTerminals)
          .filter(l -> !l.isEmpty())
          .findFirst()
          .ifPresent(list::addAll);

    } else if (node instanceof ChoiceNode) {
      completeTree.children.get(node)
          .stream()
          .map(this::firstNonTerminals)
          .filter(l -> !l.isEmpty())
          .forEach(list::addAll);
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
