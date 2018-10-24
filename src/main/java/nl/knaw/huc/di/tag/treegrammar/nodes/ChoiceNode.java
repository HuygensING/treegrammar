package nl.knaw.huc.di.tag.treegrammar.nodes;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.joining;

public class ChoiceNode implements NonTerminalNode {

  public final List<Node> choices = new ArrayList<>();

  public ChoiceNode(final List<Node> choices) {
    this.choices.addAll(choices);
  }

  @Override
  public boolean matches(final Node node) {
    return choices.stream().anyMatch(c -> c.matches(node));
  }

  @Override
  public Node copy() {
    return this;
  }

  @Override
  public String toString() {
    return choices.stream()
        .map(Object::toString)
        .collect(joining("|", "(", ")"));
  }
}
