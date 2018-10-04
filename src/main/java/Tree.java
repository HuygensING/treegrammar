import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
/* author: Ronald Haentjens Dekker
 * v1: date: 11-09-2018
 * v2: Created on 28/09/18.
 *
 * Just like with graphs we create a container class for trees.
 * Instead of specifying what kind of nodes are used we specify how to put things in the tree.
 * This makes it easier to specify the root.
 * Also it makes it easier to merge two trees etc.
 */
class Tree<T> {
  final T root;
  // Every node in the tree has a list of children
  final Map<T, List<T>> children;
  // Every node has one parent; this map maps the node to the parent; so reverse
  private final Map<T, T> parents;

  public Tree(T root) {
    this.root = root;
    this.children = new HashMap<>();
    children.put(root, new ArrayList<>());
    this.parents = new HashMap<>();
  }

  public Tree(T root, List<T> children) {
    this.root = root;
    this.children = new HashMap<>();
    this.parents = new HashMap<>();
    // new ArrayList is to container the Arrays.asList
    this.children.put(root, new ArrayList<>(children));
    for (T child : children) {
      parents.put(child, root);
    }
  }

  public void connect(T parent, T child) {
    children.putIfAbsent(parent, new ArrayList<T>());
    children.get(parent).add(child);
    parents.put(child, parent);
  }

  public void mergeTreeIntoCurrentTree(Tree<T> toMerge, T nodeToReplace) {
    // Copy the children map from the tree container to merge, and connect the parent of the node to replace
    // with the root of the tree to merge
    this.children.putAll(toMerge.children);
    this.parents.putAll(toMerge.parents);
    T parent = parents.get(nodeToReplace);
    connect(parent, toMerge.root);
  }

  // NOTE: The toString method goes only one level deep
  @Override
  public String toString() {
    StringBuffer result = new StringBuffer();
    result.append(root.toString());
    result.append("{");
    for (T child : children.get(root)) {
      result.append(child.toString());
    }
    result.append("}");
    return result.toString();
  }
}