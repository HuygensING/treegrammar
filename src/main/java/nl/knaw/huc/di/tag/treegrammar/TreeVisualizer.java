package nl.knaw.huc.di.tag.treegrammar;

import java.util.ArrayList;
import java.util.List;

class TreeVisualizer {

  public static <T> String asText(Tree<T> tree) {
    StringBuilder stringBuilder = new StringBuilder();
    String indent = "\n| ";
    stringBuilder.append(tree.root);
    appendChildren(stringBuilder, indent, tree, tree.root);
    return stringBuilder.toString();
  }

  private static <T> void appendChildren(StringBuilder stringBuilder, String indent, Tree<T> tree, T parent) {
    String newIndent = indent + "| ";
    List<T> children = tree.children.getOrDefault(parent, new ArrayList<>());
    children.forEach(child -> {
          stringBuilder.append(indent).append(child);
          appendChildren(stringBuilder, newIndent, tree, child);
        }
    );
  }

}
