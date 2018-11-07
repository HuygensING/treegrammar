package nl.knaw.huc.di.tag.treegrammar.nodes;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class ChoiceNode implements NonTerminalNode {

  private final List<Node> choices = new ArrayList<>();

  public ChoiceNode(final List<Node> choices) {
    this.choices.addAll(choices);
  }

  @Override
  public boolean matches(final Node node) {
    return choices.stream().anyMatch(c -> c.matches(node));
  }

  @Override
  public Stream<NonTerminalNode> nonTerminalNodeStream() {
    return choices.stream().flatMap(Node::nonTerminalNodeStream);
  }

  @Override
  public Node copy() {
    return this;
  } // TODO: copy choices?

  @Override
  public String toString() {
    return choices.stream()
        .map(Object::toString)
        .collect(joining("|", "(", ")"));
  }

}
