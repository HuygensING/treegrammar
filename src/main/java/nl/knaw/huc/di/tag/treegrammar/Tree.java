package nl.knaw.huc.di.tag.treegrammar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;

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
public class Tree<T> {
  public final T root;
  // Every node in the tree has a list of children
  final Map<T, List<T>> children;
  // Every node has one parent; this map maps the node to the parent; so reverse
  public final Map<T, T> parents;

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

  void mergeTreeIntoCurrentTree(T nodeToReplace, Tree<T> toMerge) {
    // Copy the children map from the tree container to merge, and connect the parent of the node to replace
    // with the root of the tree to merge
//    this.children.remove(nodeToReplace);
    this.children.putAll(toMerge.children);
//    this.parents.remove(toMerge);
    this.parents.putAll(toMerge.parents);
    T parent = parents.get(nodeToReplace);
    if (parent == null) {
      throw new RuntimeException("nodeToReplace " + nodeToReplace + " not found in this tree.parents!");
    }
    replaceChild(parent, nodeToReplace, toMerge.root);
//    connect(parent, toMerge.root);
//    disconnect(parent, nodeToReplace);
  }

  List<T> getRootChildren() {
    this.children.putIfAbsent(root, emptyList());
    return this.children.get(root);
  }

  // NOTE: The toString method goes only one level deep
  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    String rootString = root == null
        ? ""
        : root.toString();
    result.append(rootString)
        .append("[");
    String children = getRootChildren()
        .stream()
        .map(Object::toString)
        .collect(joining(" "));
    result.append(children).append("]");
    return result.toString();
  }

  public void removeSubTreeWithRootNode(final T node) {
    // all descendants can be removed, too
    removeChildrenOf(node);
    T parent = parents.get(node);
    children.get(parent).remove(node);
  }

  public void removeNode(final T parent) {
    T grandParent = parents.get(parent);
    children.putIfAbsent(parent, emptyList());
    List<T> originalChildren = new ArrayList(children.remove(parent));
    for (T childNode : originalChildren) {
      disconnect(parent, childNode);
      connect(grandParent, childNode);
    }
    if (children.containsKey(grandParent)) {
      children.get(grandParent).remove(parent);
    }
  }

  private void replaceChild(final T parent, final T nodeToReplace, final T replacementNode) {
    int index = children.get(parent).indexOf(nodeToReplace);
    children.get(parent).remove(index);
    children.get(parent).add(index, replacementNode);
    parents.remove(nodeToReplace);
    parents.put(replacementNode, parent);
  }

  private void removeChildrenOf(final T node) {
    if (children.containsKey(node)) {
      children.get(node).forEach(this::removeChildrenOf);
      children.remove(node);
    }
  }

  void connect(T parent, T child) {
    children.putIfAbsent(parent, new ArrayList<>());
    children.get(parent).add(child);
    parents.put(child, parent);
  }

  private void disconnect(T parent, T child) {
    if (children.containsKey(parent)) {
      children.get(parent).remove(child);
    }
    parents.remove(child, parent);
  }

}