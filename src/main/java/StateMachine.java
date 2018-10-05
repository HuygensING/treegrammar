import nodes.Node;
import nodes.StartNode;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

/*
 * author: Ronald Haentjens Dekker
 * date: 11-09-2018
 *
 * in een tree automata willen we een tree bijhouden
 * In een state machine ga je van state naar statae
 * Echter in een tree model zijn er meerdere mogelijke non terminls
 * die je kunt vervangen.
 */
public class StateMachine {
  private Tree<Node> completeTree; // tree die we aan het opbouwen zijn
  private Node pointerToCurrentNode;
  private List<TransitionRule> rules;

  public StateMachine() {
    Node startNode = new StartNode();
    this.completeTree = new Tree<>(startNode);
    this.pointerToCurrentNode = startNode;
    // nu hebben we nog transitie rules nodig.
    this.rules = new ArrayList<>();
  }

  public void addTransitionRule(TransitionRule transitionRule) {
    this.rules.add(transitionRule);
  }

  // bij de state machine komen zaken binnen; input
  // dan moeten we kijk aan den hand van de input of er een transitie rule voor is.
  // zo niet; dan zitten we in een error.
  // input zou eigenlijk tree moeten zijn.
  public void processInput(Node node) {
    System.out.println("processInput(" + node + ")");
    // We zoeken eerst op naar welke node de huidige pointer verwijst.
    // Dan kijken we welke transitierules er zijn voor dat type node.
    List<TransitionRule> applicableRules = rules.stream()
        .filter(rule -> rule.lefthandsideIsApplicableFor(pointerToCurrentNode))
        .collect(toList());

    System.out.println("applicableRules=" + applicableRules);
    if (applicableRules.isEmpty()) {
      throw new RuntimeException("No transition rule found! Current state: " + this.pointerToCurrentNode + " -> " + node);
    }
    TransitionRule theRule = applicableRules.get(0);
    System.out.println("Transition rule found: " + theRule + "! We want to know the right hand side");
    System.out.println("Expectation: " + theRule.righthandside);

    // Nu moeten we checken of de transitierule die we gevonden hebben ook past bij de wat binnen kregen
    // Ik weet nog niet hoe dat moet gewoon bij
    // We vervangen de aangewezen node door de nieuwe van de RHS
    // de current pointer moet dan naar het eerste kind van de RHS
    // NB: Dit is te simplistisch.

    // check whether tag of right hand side is the same as the incoming tag.
    if (theRule.righthandside.root.matches(node)) {
      // woohoo expectations matched!
      //throw new RuntimeException("expectations are met!");
      // set the current pointer
      // aargh we now need a replace, with a parent value, which we don't have yet
      // het gebeuren hier is wat moeilijk, want het kan zijn dat de root vervangen wordt..
      TransitionRule finalTheRule = theRule;

      // als het om de root node gaat vervangen we gewoon de hele tree
      if (pointerToCurrentNode == completeTree.root) {
        completeTree = finalTheRule.righthandside;
      } else {
        completeTree.mergeTreeIntoCurrentTree(finalTheRule.righthandside, pointerToCurrentNode);
      }
      // gaat dit altijd goed... we will see
//      pointerToCurrentNode = finalTheRule.righthandside.children.get(finalTheRule.righthandside.root).get(0);
      pointerToCurrentNode = finalTheRule.firstNonTerminalNode().orElse(null);
      System.out.println("pointerToCurrentNode=" + pointerToCurrentNode);
      if (finalTheRule.hasNoRHSTerminals()) {
        processInput(node);
      }
    } else {
      throw new RuntimeException("no match: found " + node + ", expected " + theRule.righthandside.root);
    }
    //! Dan gaan we opzoek naar de transitierule van de huidige state
    //! Gegeven de transitierule en de nieuwe op basis van de input.
    // we gaan alle transitierules af.
    // het zou beter zijn om dit te indexeren; maar ok..
  }
}
