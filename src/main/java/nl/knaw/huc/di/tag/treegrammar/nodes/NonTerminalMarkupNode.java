package nl.knaw.huc.di.tag.treegrammar.nodes;

import java.util.stream.Stream;

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
  public Node copy() {
    return new NonTerminalMarkupNode(variableName);
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
