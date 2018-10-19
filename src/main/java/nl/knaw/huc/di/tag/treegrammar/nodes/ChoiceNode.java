package nl.knaw.huc.di.tag.treegrammar.nodes;

import nl.knaw.huc.di.tag.treegrammar.Tree;

import java.util.ArrayList;
import java.util.List;

public class ChoiceNode implements NonTerminalNode {

  private List<Tree<Node>> choices = new ArrayList<>();

  public ChoiceNode(final Tree<Node> choice1, final Tree<Node> choice2) {
    choices.add(choice1);
    choices.add(choice2);
  }

  @Override
  public boolean matches(final Node node) {
    return choices.stream().anyMatch(c -> c.root.matches(node));
  }

  @Override
  public Node copy() {
    return this;
  }
}
