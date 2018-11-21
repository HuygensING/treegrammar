package nl.knaw.huc.di.tag.treegrammar.nodes;

import nl.knaw.huc.di.tag.treegrammar.Tree;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static java.text.MessageFormat.format;

public class NonTerminalMarkupNode implements NonTerminalNode {
  private final String variableName;

  public NonTerminalMarkupNode(String variableName) {
    this.variableName = variableName;
  }

  @Override
  public boolean matches(Node node) {
    return (node instanceof NonTerminalMarkupNode && ((NonTerminalMarkupNode) node).variableName.equals(variableName))
        || (node instanceof TagNode);
  }

  @Override
  public Stream<NonTerminalNode> nonTerminalNodeStream() {
    return Stream.of(this);
  }

  @Override
  public void postProcess(Tree<Node> completeTree, List<Node> rootChildren) {
    Node parentNode = completeTree.parents.get(this);
    if (parentNode instanceof ZeroOrOneNode
        || parentNode instanceof ZeroOrMoreNode) {
      completeTree.removeSubTreeWithRootNode(parentNode);

    } else if (parentNode instanceof OneOrMoreNode) {
      completeTree.removeNode(this);
      completeTree.removeNode(parentNode);

    } else {
      throw new RuntimeException(format("unresolved NonTerminal: {0}", this));
    }
  }

  @Override
  public List<NonTerminalNode> firstNonTerminals(Tree<Node> completeTree) {
    return Collections.singletonList(this);
  }

  @Override
  public String toString() {
    return variableName;
  }

  @Override
  public int hashCode() {
    return variableName.hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    return (obj instanceof NonTerminalMarkupNode)
        && ((NonTerminalMarkupNode) obj).variableName.equals(variableName);
  }
}
