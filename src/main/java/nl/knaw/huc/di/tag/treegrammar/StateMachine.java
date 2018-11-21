package nl.knaw.huc.di.tag.treegrammar;

import nl.knaw.huc.di.tag.treegrammar.nodes.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.Location;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

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
  //  private final List<TransitionRule> rules = new ArrayList<>();
  private final Map<NonTerminalNode, NodeReplacementInfo> nodeReplacementInfoMap = new HashMap<>();

  class NodeReplacementInfo {
    Supplier<Tree<Node>> replacementSupplier;
    Function<Node, Boolean> nodeMatcher;

    boolean matches(final Node inputNode) {
      return nodeMatcher.apply(inputNode);
    }
  }

  StateMachine() {
    StartNode startNode = new StartNode();
    this.completeTree = new Tree<>(startNode);
  }

  public Tree<Node> getTree() {
    return completeTree;
  }

  public void exit() {
    // remove choicenodes and groupnodes
    // if any nonterminals remain, throw error
    LOG.info("\n\n* completeTree (before exit())=\n{}", TreeVisualizer.asText(completeTree));
    walkSubTreeWithRoot(completeTree.root);
    LOG.info("\n\n* completeTree (after exit())=\n{}", TreeVisualizer.asText(completeTree));
  }

  private void walkSubTreeWithRoot(final Node root) {
    completeTree.children.putIfAbsent(root, emptyList());
    List<Node> rootChildren = completeTree.children.get(root);
    root.postProcess(completeTree, rootChildren);
    List<Node> originalChildNodes = (rootChildren == null)
        ? emptyList()
        : new ArrayList<>(rootChildren);
    originalChildNodes.forEach(this::walkSubTreeWithRoot);
  }

  void addTransitionRule(TransitionRule transitionRule) {
//    this.rules.add(transitionRule);
    NodeReplacementInfo nri = new NodeReplacementInfo();
    nri.replacementSupplier = transitionRule.getRightHandSideSupplier();
    nri.nodeMatcher = transitionRule.getNodeMatcher();
    nodeReplacementInfoMap.put(transitionRule.leftHandSide, nri);
  }

  void processInput(Node inputNode, final Location location) {
    // bij de state machine komen nodes binnen
    // In de tree die we aan het bouwen zijn zitten nog NonTerminal nodes, waarvan er 1 aan de beurt is om vervangen te worden.
    // We zoeken nu een transition rule die deze NonTerminal aan de linkerkant heeft, en
    // matched met de binnenkomende node
    // zo niet; dan zitten we in een error.
    // We zoeken eerst op naar welke node de huidige pointer verwijst.
    // Dan kijken we welke transitierules er zijn voor dat type node.
    String position = position(location);
    LOG.info("\n\n* completeTree=\n{}", TreeVisualizer.asText(completeTree));

    List<Node> nextNonTerminals = nextNonTerminals();
    LOG.info("nextNonTerminals={}", nextNonTerminals);
    AnyTextNode anyTextNode = new AnyTextNode();
    if (nextNonTerminals.contains(anyTextNode) && inputNode instanceof TextNode) {
      NodeReplacementInfo nri = new NodeReplacementInfo();
      nri.replacementSupplier = () -> new Tree<>(inputNode);
      nri.nodeMatcher = inputNode::matches;
      nodeReplacementInfoMap.put(anyTextNode, nri);
    }
    List<Node> replaceableNodes = nextNonTerminals.stream()
        .filter(nodeReplacementInfoMap::containsKey)
        .collect(toList());

    LOG.info("inputNode={}", inputNode);
    final List<Supplier<Tree<Node>>> acceptableReplacementSuppliers = replaceableNodes.stream()
        .filter(t -> nodeReplacementInfoMap.containsKey(t) && nodeReplacementInfoMap.get(t).matches(inputNode))
        .map(nodeReplacementInfoMap::get)
        .map(nri -> nri.replacementSupplier)
        .collect(toList());
    LOG.info("acceptable replacements={}", acceptableReplacementSuppliers);

    Node nodeToReplace = null;
    Tree<Node> replacementTree = null;
    List<Node> rejectedNonTerminalNodes = new ArrayList<>();
    for (Node n : nextNonTerminals) {
      Supplier<Tree<Node>> treeSupplier = nodeReplacementInfoMap.get(n).replacementSupplier;
      if (acceptableReplacementSuppliers.contains(treeSupplier)) {
        nodeToReplace = n;
        replacementTree = treeSupplier.get();
      } else {
        rejectedNonTerminalNodes.add(n);
      }
    }
    if (nodeToReplace == null) {
      final List<Tree<Node>> possibleReplacements = replaceableNodes.stream()
          .map(nodeReplacementInfoMap::get)
          .map(nri -> nri.replacementSupplier)
          .map(Supplier::get)
          .collect(toList());
      LOG.info("possible replacements={}", possibleReplacements);

      List<Node> acceptableNodes = possibleReplacements.stream()
          .map(t -> t.root)
          .collect(toList());
      LOG.info("acceptable nodes={}", acceptableNodes);
      if (acceptableNodes.isEmpty()) {
        throw new RuntimeException(format("{0}: Unexpected node: {1}", position, inputNode));
      }
      String expected = acceptableNodes.stream()
          .map(Object::toString)
          .collect(joining(" or "));
      throw new RuntimeException(format("{0}: No match: expected {1}, but got {2}", position, expected, inputNode));
    }

    Node parent = completeTree.parents.get(nodeToReplace);
    boolean parentIsXOrMoreNode = (parent instanceof ZeroOrMoreNode || parent instanceof OneOrMoreNode);
    LOG.info("action: replace node ({}) with tree ({})", nodeToReplace, replacementTree);
    replaceNodeWithTree(nodeToReplace, replacementTree);
    if (parentIsXOrMoreNode) {
      completeTree.connect(parent, nodeToReplace);
    }

    LOG.info("action: reject nodes {}", rejectedNonTerminalNodes);
    rejectedNonTerminalNodes.forEach(n -> {
      Node nodeParent = completeTree.parents.get(n);
      if (nodeParent instanceof GroupNode) {
        completeTree.removeSubTreeWithRootNode(nodeParent);
      } else {
        completeTree.removeSubTreeWithRootNode(n);
      }
    });

    nodeReplacementInfoMap.remove(anyTextNode);
  }

  private String position(final Location location) {
    return "@" + location.getLineNumber() + "," + (location.getColumnNumber() - 1);
  }

  private List<Node> nextNonTerminals() {
    final List<Node> list = new ArrayList<>();
    Node root = completeTree.root;
    if (root instanceof NonTerminalNode) {
      list.add(root);
    } else {
      List<Node> firstNonTerminals = completeTree.getRootChildren().stream()
          .map(n -> n.firstNonTerminals(completeTree))
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

}