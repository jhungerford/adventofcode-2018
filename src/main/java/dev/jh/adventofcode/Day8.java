package dev.jh.adventofcode;

import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;

import java.io.File;
import java.util.*;

public class Day8 {

  public static class Node {
    public final ImmutableList<Node> children;
    public final ImmutableList<Integer> metadata;

    public Node(ImmutableList<Node> children, ImmutableList<Integer> metadata) {
      this.children = children;
      this.metadata = metadata;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Node node = (Node) o;
      return Objects.equal(children, node.children) &&
          Objects.equal(metadata, node.metadata);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(children, metadata);
    }
  }

  /**
   * Parses the given license file into a tree of nodes, returning the root of the tree.
   * The line consists of space-separated numbers indicating the quantity of child nodes,
   * the quantity of metadata entries, zero or more child nodes, and one or more metadata entries.
   *
   * @param line Line to parse
   * @return Root of the tree
   */
  public static Node parse(String line) {
    return parseNode(ImmutableList.copyOf(line.split(" ")).iterator());
  }

  private static Node parseNode(Iterator<String> iterator) {
    int numChildren = Integer.parseInt(iterator.next());
    int numMetadata = Integer.parseInt(iterator.next());

    ImmutableList.Builder<Node> children = ImmutableList.builder();
    for (int i = 0; i < numChildren; i ++) {
      children.add(parseNode(iterator));
    }

    ImmutableList.Builder<Integer> metadata = ImmutableList.builder();
    for (int i = 0; i < numMetadata; i ++) {
      metadata.add(Integer.parseInt(iterator.next()));
    }

    return new Node(children.build(), metadata.build());
  }

  /**
   * Returns the sum of the metadata of the given license tree.
   *
   * @param root Root of the tree
   * @return Sum of all metadata values in the tree.
   */
  public static int sumMetadata(Node root) {
    int sum = 0;

    Queue<Node> remaining = new ArrayDeque<>();
    remaining.add(root);

    while (!remaining.isEmpty()) {
      Node node = remaining.remove();

      sum += node.metadata.stream().mapToInt(Integer::intValue).sum();
      remaining.addAll(node.children);
    }

    return sum;
  }

  /**
   * Returns the sum of the referenced values on the node.  If the node has no children, it's value is the
   * sum of it's metadta entries.  If it does have children, the metadata references the index of the child nodes
   * (the first child is index 1), and the node's value is the sum of the child nodes' values that exist.
   *
   * @param node Node to calculate the referenced value of
   * @return Referenced value of the node
   */
  public static int sumReference(Node node) {
    if (node.children.isEmpty()) {
      return node.metadata.stream().mapToInt(Integer::intValue).sum();
    }

    return node.metadata.stream()
        .filter(meta -> meta <= node.children.size())
        .mapToInt(meta -> sumReference(node.children.get(meta - 1)))
        .sum();
  }

  public static void main(String[] args) throws Exception {
    File file = new File(Day8.class.getResource("/day8.txt").getFile());
    String line = Files.readLines(file, Charsets.UTF_8).get(0);
    Node root = parse(line);

    // Part 1: sum all metadata values.
    System.out.println("Part 1: " + sumMetadata(root));
    // Part 2: sum of referenced children.
    System.out.println("Part 2: " + sumReference(root));
  }
}
