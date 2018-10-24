package nl.knaw.huc.di.tag.treegrammar;

import nl.knaw.huc.di.tag.treegrammar.expectations.*;
import nl.knaw.huc.di.tag.treegrammar.nodes.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.XMLEvent;
import java.util.*;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

/*
 * author: Ronald Haentjens Dekker
 * date: 11-09-2018
 *
 * in een tree automata willen we een tree bijhouden
 * In een state machine ga je van state naar state
 * Echter in een tree model zijn er meerdere mogelijke non terminls
 * die je kunt vervangen.
 */
class StateMachine {
  private static final Logger LOG = LoggerFactory.getLogger(StateMachine.class);
  private Tree<Node> completeTree; // tree die we aan het opbouwen zijn
  private final List<TransitionRule> rules = new ArrayList<>();
  private final Map<NonTerminalNode, Tree<Node>> nodeReplacementMap = new HashMap<>();
  private Deque<List<NonTerminalNode>> nonTerminalsStack = new ArrayDeque<>();
  private List<Expectation> expectations = new ArrayList<>();

  public StateMachine() {
    init();
  }

  private void init() {
    StartNode startNode = new StartNode();
    this.completeTree = new Tree<>(startNode);
//    this.pointerToCurrentNode = startNode;
    // nu hebben we nog transitie rules nodig.
    nonTerminalsStack.clear();
    ArrayList<NonTerminalNode> nonTerminals = new ArrayList<>();
    nonTerminals.add(startNode);
    nonTerminalsStack.push(nonTerminals);
    expectations.add(new PlaceHolderExpectation(startNode));
  }

  public void addTransitionRule(TransitionRule transitionRule) {
    this.rules.add(transitionRule);

    final NonTerminalNode lhs = transitionRule.lefthandside;
    if (nodeReplacementMap.containsKey(lhs)) {
      ChoiceNode choiceNode = new ChoiceNode(nodeReplacementMap.get(lhs), transitionRule.righthandside);

      final Tree<Node> choiceNodeAsTree = new Tree<>(choiceNode);
      nodeReplacementMap.put(lhs, choiceNodeAsTree);

    } else {
      nodeReplacementMap.put(lhs, transitionRule.righthandside);
    }
  }

  // bij de state machine komen nodes binnen
  // In de tree die we aan het bouwen zijn zitten nog NonTerminal nodes, waarvan er 1 aan de beurt is om vervangen te worden.
  // We zoeken nu een transition rule die deze NonTerminal aan de linkerkant heeft, en
  // matched met de binnenkomende node

  // zo niet; dan zitten we in een error.
  // input zou eigenlijk tree moeten zijn.
  public void processInput(Node node, XMLEvent xmlEvent) {
    Expectation expectation = nextExpectation();
    LOG.info("Input: {}, Expectation: {}", node, expectation);
    boolean matches;
    if (expectation instanceof ChoiceExpectation) {
      ChoiceExpectation che = (ChoiceExpectation) expectation;
      matches = che.matches(xmlEvent);
    } else {
      ConcreteExpectation ce = (ConcreteExpectation) expectation;
      matches = ce.matches(xmlEvent);
    }

    if (!matches) {
      throw new RuntimeException("Unexpected XMLEvent: expected " + expectation + ", got " + xmlEvent);
    }

    // We zoeken eerst op naar welke node de huidige pointer verwijst.
    // Dan kijken we welke transitierules er zijn voor dat type node.
    List<NonTerminalNode> nonTerminalsToProcess = nonTerminalsStack.peek();
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

    if (nonTerminalNode instanceof ChoiceNode) {
      final ChoiceNode choice = (ChoiceNode) nonTerminalNode;
      List<Tree<Node>> collect = choice.choices.stream().filter(t -> t.root.matches(node)).collect(toList());
      LOG.info("collect={}", collect);
    }

//    LOG.info("applicableRules={}", applicableRules);
//    if (!nodeReplacementMap.containsKey(nonTerminalNode)) {
//      throw new RuntimeException("No transition rule found! Current state: " + node + ", expected: " + nonTerminalNode);
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

    Tree<Node> potentialReplacement = nodeReplacementMap.get(nonTerminalNode);
    if (potentialReplacement == null) {
      throw new RuntimeException("No replacement for " + nonTerminalNode);
    }
    // check whether tag of right hand side is the same as the incoming tag.
    if (potentialReplacement.root.matches(node)) {
      // woohoo expectations matched!
      //throw new RuntimeException("expectations are met!");
      // set the current pointer
      // aargh we now need a replace, with a parent value, which we don't have yet
      // het gebeuren hier is wat moeilijk, want het kan zijn dat de root vervangen wordt..

      // als het om de root node gaat vervangen we gewoon de hele tree
      final Node rootCopy = potentialReplacement.root.copy();
      final List<Node> childrenCopy = potentialReplacement.children
          .get(potentialReplacement.root)
          .stream()
          .map(Node::copy)
          .collect(toList());
      Tree<Node> rhsCopy = new Tree<>(rootCopy, childrenCopy);
//      Tree<Node> rhsCopy = righthandside;
      if (nonTerminalNode == completeTree.root) {
        completeTree = rhsCopy;
      } else {
        completeTree.mergeTreeIntoCurrentTree(nonTerminalNode, rhsCopy);
      }
      // gaat dit altijd goed... we will see
//      nonTerminalNode = finalTheRule.righthandside.children.get(finalTheRule.righthandside.root).get(0);
      List<NonTerminalNode> nonTerminalNodeList = nonTerminals(potentialReplacement);
      nonTerminalsStack.push(nonTerminalNodeList);
//      nonTerminalNode = theRule.firstNonTerminalNode().orElse(null);

      LOG.info("nonTerminalNode={}", nonTerminalNode);
//      if (theRule.hasNoRHSTerminals()) {
//        processInput(node);
//      }
    } else {
      throw new RuntimeException("No match: expected " + potentialReplacement.root + " but found " + node);
    }
    //! Dan gaan we op zoek naar de transitierule van de huidige state
    //! Gegeven de transitierule en de nieuwe op basis van de input.
    // we gaan alle transitierules af.
    // het zou beter zijn om dit te indexeren; maar ok..
  }

  private Expectation nextExpectation() {
    Expectation expectation = null;
    boolean goOn = true;
    while (goOn) {
      expectation = expectations.remove(0);
      if (expectation instanceof PlaceHolderExpectation) {
        final Node n = ((PlaceHolderExpectation) expectation).node;
        List<TransitionRule> applicableRules = rules.stream()
            .filter(rule -> rule.lefthandsideIsApplicableFor(n))
            .collect(toList());
        if (applicableRules.size() > 1) {
          List<List<Expectation>> choices = new ArrayList<>();
          applicableRules.forEach(r -> {
            List<Expectation> optionalExpectations = TransitionRuleFactory.getExpectations(r);
            choices.add(optionalExpectations);
          });
          ChoiceExpectation choiceExpectation = new ChoiceExpectation(choices);
          expectations.add(0, choiceExpectation);

        } else {
          TransitionRule transitionRule = applicableRules.get(0);
          List<Expectation> newExpectations = TransitionRuleFactory.getExpectations(transitionRule);
          expectations.addAll(0, newExpectations);
        }
      } else if (expectation instanceof ChoiceExpectation) {
        ChoiceExpectation ce = (ChoiceExpectation) expectation;
        List<Expectation> roots = ce.choices.stream()
            .map(c -> c.remove(0))
            .collect(toList());
        Set<Expectation> rootSet = new HashSet<>(roots);
        if (rootSet.size() == 1) {
          expectation = roots.get(0);
        } else {
          List<List<Expectation>> param = roots.stream()
              .map(this::makeConcrete)
              .map(e -> singletonList(e))
              .collect(toList());
          expectation = new ChoiceExpectation(param);
        }
        expectations.add(0, ce);
        goOn = false;

      } else {
        goOn = false;
      }
    }
    LOG.info("expectation={}, next expectations={}", expectation, expectations);
    return expectation;
  }

  private Expectation makeConcrete(Expectation expectation) {
    if (expectation instanceof ConcreteExpectation) {
      return expectation;
    } else if (expectation instanceof PlaceHolderExpectation) {
      final Node n = ((PlaceHolderExpectation) expectation).node;
      List<TransitionRule> applicableRules = rules.stream()
          .filter(rule -> rule.lefthandsideIsApplicableFor(n))
          .collect(toList());
      if (applicableRules.size() > 1) {
        List<List<Expectation>> choices = new ArrayList<>();
        applicableRules.forEach(r -> {
          List<Expectation> optionalExpectations = TransitionRuleFactory.getExpectations(r);
          choices.add(optionalExpectations);
        });
        ChoiceExpectation choiceExpectation = new ChoiceExpectation(choices);
        expectations.add(0, choiceExpectation);

      } else {
        TransitionRule transitionRule = applicableRules.get(0);
        List<Expectation> newExpectations = TransitionRuleFactory.getExpectations(transitionRule);
        expectations.addAll(0, newExpectations);
      }
      return nextExpectation();
    }
    return null;
  }

  private List<NonTerminalNode> nonTerminals(final Tree<Node> nodeTree) {
    List<Node> treeNodes = new ArrayList<>();
    treeNodes.add(nodeTree.root);
    treeNodes.addAll(nodeTree.children.get(nodeTree.root));
    return treeNodes.stream()
        .filter(NonTerminalNode.class::isInstance)
        .map(NonTerminalNode.class::cast)
        .collect(toList());
  }

  public Tree<Node> getTree() {
    return completeTree;
  }

  public void pop(EndElement endElement) {
    nonTerminalsStack.pop();
    Expectation expectation = nextExpectation();
    LOG.info("Input: {endElement}, Expectation: {}", expectation);
    if (!(expectation instanceof MarkupEndExpectation)) {
      throw new RuntimeException("Expected " + expectation + ", got " + endElement);
    }

  }

  public void reset() {
    init();
  }
}
