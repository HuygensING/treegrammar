package nl.knaw.huc.di.tag.treegrammar.nodes;

import nl.knaw.huc.di.tag.treegrammar.Tree;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.text.MessageFormat.format;
import static java.util.stream.Collectors.joining;

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
  public void postProcess(Tree<Node> completeTree, List<Node> childNodes) {
    if (childNodes.isEmpty()) {
      throw new RuntimeException(
          format("None of the options of {0} were found.", this)
      );
    } else if (childNodes.size() > 1) {
      throw new RuntimeException(
          format("{0} still has a choice between {1}", this, childNodes)
      );
    } else {
      completeTree.removeNode(this);
    }
  }

  @Override
  public List<Node> firstNonTerminals(Tree<Node> completeTree) {
    final List<Node> list = new ArrayList<>();
    completeTree.children.get(this)
        .stream()
        .map(n -> n.firstNonTerminals(completeTree))
        .filter(l -> !l.isEmpty())
        .forEach(list::addAll);
    return list;
  }

  @Override
  public String toString() {
    return choices.stream()
        .map(Object::toString)
        .collect(joining("|", "(", ")"));
  }

}
