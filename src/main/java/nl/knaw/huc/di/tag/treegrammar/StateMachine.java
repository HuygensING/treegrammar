package nl.knaw.huc.di.tag.treegrammar;

import nl.knaw.huc.di.tag.treegrammar.nodes.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

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

  StateMachine() {
    init();
  }

  public Tree<Node> getTree() {
    return completeTree;
  }

  public void exit() {
    // remove choicenodes and groupnodes
    // if any nonterminals remain, throw error
    walkSubTreeWithRoot(completeTree.root);
  }

  private void walkSubTreeWithRoot(final Node root) {
    List<Node> childNodes = new ArrayList<>(completeTree.children.get(root));
    if (childNodes == null) {
      childNodes = emptyList();
    }
    if (root instanceof ChoiceNode) {
      if (childNodes.isEmpty()) {
        throw new RuntimeException("None of the options of " + root + " were found.");
      } else if (childNodes.size() > 1) {
        throw new RuntimeException(root + " still has a choice between " + childNodes);
      } else {
        completeTree.removeNode(root);
      }
    } else if (root instanceof GroupNode) {
      completeTree.removeNode(root);
    } else if (root instanceof NonTerminalMarkupNode) {
      throw new RuntimeException("unresolved NonTerminal: " + root);
    }
    childNodes.forEach(this::walkSubTreeWithRoot);
  }

  private void init() {
    StartNode startNode = new StartNode();
    this.completeTree = new Tree<>(startNode);
  }

  void addTransitionRule(TransitionRule transitionRule) {
    this.rules.add(transitionRule);
    nodeReplacementMap.put(transitionRule.lefthandside, transitionRule.righthandside);
  }

  void processInput(Node inputNode) {
    // bij de state machine komen nodes binnen
    // In de tree die we aan het bouwen zijn zitten nog NonTerminal nodes, waarvan er 1 aan de beurt is om vervangen te worden.
    // We zoeken nu een transition rule die deze NonTerminal aan de linkerkant heeft, en
    // matched met de binnenkomende node
    // zo niet; dan zitten we in een error.
    // We zoeken eerst op naar welke node de huidige pointer verwijst.
    // Dan kijken we welke transitierules er zijn voor dat type node.
    LOG.info("\n\n* completeTree=\n{}", TreeVisualizer.asText(completeTree));

    List<Node> nextNonTerminals = nextNonTerminals();
    LOG.info("nextNonTerminals={}", nextNonTerminals);
    AnyTextNode anyTextNode = new AnyTextNode();
    if (nextNonTerminals.contains(anyTextNode) && inputNode instanceof TextNode) {
      nodeReplacementMap.put(anyTextNode, new Tree<>(inputNode));
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
        .filter(t -> t.root.matches(inputNode))
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
    if (nodeToReplace == null) {
      if (acceptableNodes.isEmpty()) {
        throw new RuntimeException("Unexpected node " + inputNode);
      }
      String expected = acceptableNodes.stream()
          .map(Object::toString)
          .collect(joining(" or "));
      throw new RuntimeException("No match: expected " + expected + ", but got " + inputNode);

    }
    LOG.info("action: replace node ({}) with tree ({})", nodeToReplace, replacementTree);
    Tree<Node> rhsCopy = cloneTree(replacementTree);
    replaceNodeWithTree(nodeToReplace, rhsCopy);

    LOG.info("action: reject nodes {}", rejectedNonTerminalNodes);
    rejectedNonTerminalNodes.forEach(n -> {
      Node nodeParent = completeTree.parents.get(n);
      if (nodeParent instanceof GroupNode) {
        completeTree.removeSubTreeWithRootNode(nodeParent);
      } else {
        completeTree.removeSubTreeWithRootNode(n);
      }
    });

    nodeReplacementMap.remove(new AnyTextNode());
  }

  private List<Node> nextNonTerminals() {
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

  private void replaceNodeWithTree(final Node nonTerminalNode, final Tree<Node> replacementTree) {
    if (nonTerminalNode == completeTree.root) {
      completeTree = replacementTree;
    } else {
      completeTree.mergeTreeIntoCurrentTree(nonTerminalNode, replacementTree);
    }
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