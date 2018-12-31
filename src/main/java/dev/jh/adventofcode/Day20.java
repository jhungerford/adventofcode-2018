package dev.jh.adventofcode;

import com.google.common.base.Charsets;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.UnaryOperator;

public class Day20 {

  public interface Instruction {
  }

  public interface InstructionBuilder {
    InstructionBuilder append(Instruction instruction);

    Instruction build();
  }

  public enum Direction implements Instruction {
    NORTH(point -> new Point(point.x, point.y - 1)),
    SOUTH(point -> new Point(point.x, point.y + 1)),
    EAST(point -> new Point(point.x + 1, point.y)),
    WEST(point -> new Point(point.x - 1, point.y));

    public final UnaryOperator<Point> nextPoint;

    Direction(UnaryOperator<Point> nextPoint) {
      this.nextPoint = nextPoint;
    }
  }

  public static class InstructionList implements Instruction {
    public final ImmutableList<Instruction> instructions;

    private InstructionList(ImmutableList<Instruction> instructions) {
      this.instructions = instructions;
    }

    public static class InstructionListBuilder implements InstructionBuilder {
      private ImmutableList.Builder<Instruction> instructions = ImmutableList.builder();

      @Override
      public InstructionListBuilder append(Instruction instruction) {
        instructions.add(instruction);
        return this;
      }

      @Override
      public InstructionList build() {
        return new InstructionList(instructions.build());
      }
    }
  }

  public static class OptionsInstruction implements Instruction {
    public final ImmutableSet<Instruction> options;

    public OptionsInstruction(ImmutableSet<Instruction> options) {
      this.options = options;
    }

    public static class OptionsInstructionBuilder implements InstructionBuilder {
      private final ImmutableSet.Builder<Instruction> options = ImmutableSet.builder();

      @Override
      public InstructionBuilder append(Instruction instruction) {
        options.add(instruction);
        return this;
      }

      @Override
      public OptionsInstruction build() {
        return new OptionsInstruction(options.build());
      }
    }
  }

  public static class Point {
    public final int x;
    public final int y;

    public Point(int x, int y) {
      this.x = x;
      this.y = y;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Point point = (Point) o;
      return x == point.x &&
          y == point.y;
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(x, y);
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("x", x)
          .add("y", y)
          .toString();
    }
  }

  public static class Edge {
    public final Point a;
    public final Point b;

    public Edge(Point a, Point b) {
      this.a = a;
      this.b = b;
    }
  }

  public static class Node {
    public final Point point;
    public final ImmutableSet<Edge> connections;

    public Node(Point point, ImmutableSet<Edge> connections) {
      this.point = point;
      this.connections = connections;
    }
  }

  public static Instruction parse(String line) {
    Stack<InstructionBuilder> nodes = new Stack<>();

    Instruction instuction;
    for (char c : line.toCharArray()) {
      switch (c) {
        case '^':
          nodes.push(new InstructionList.InstructionListBuilder());
          break;

        case '(':
          nodes.push(new OptionsInstruction.OptionsInstructionBuilder());
          nodes.push(new InstructionList.InstructionListBuilder());
          break;

        case '|':
          instuction = nodes.pop().build();
          nodes.peek().append(instuction);
          nodes.push(new InstructionList.InstructionListBuilder());
          break;

        case ')':
          // Add the current list to the group, then the group to the parent list.
          instuction = nodes.pop().build();
          nodes.peek().append(instuction);
          instuction = nodes.pop().build();
          nodes.peek().append(instuction);
          break;

        case '$':
          return nodes.pop().build();

        case 'N':
          nodes.peek().append(Direction.NORTH);
          break;

        case 'S':
          nodes.peek().append(Direction.SOUTH);
          break;

        case 'E':
          nodes.peek().append(Direction.EAST);
          break;

        case 'W':
          nodes.peek().append(Direction.WEST);
          break;

        default:
          throw new IllegalArgumentException("Unknown regex character '" + c + "'");
      }
    }

    throw new IllegalArgumentException("Regex must end with '$'");
  }

  public static ImmutableMap<Point, ImmutableSet<Point>> buildGraph(Instruction instruction) {
    Map<Point, ImmutableSet.Builder<Point>> graph = new HashMap<>();
    graph.put(new Point(0, 0), ImmutableSet.builder());

    buildGraphNode(instruction, ImmutableSet.of(new Point(0, 0)), graph);

    return graph.entrySet().stream()
        .collect(ImmutableMap.toImmutableMap(
            Map.Entry::getKey,
            entry -> entry.getValue().build()
        ));
  }

  private static ImmutableSet<Point> buildGraphNode(Instruction instruction, ImmutableSet<Point> currentPoints, Map<Point, ImmutableSet.Builder<Point>> graph) {
    if (instruction instanceof Direction) {
      ImmutableSet.Builder<Point> nextPoints = ImmutableSet.builder();
      for (Point point : currentPoints) {
        Point nextPoint = ((Direction) instruction).nextPoint.apply(point);

        graph.get(point).add(nextPoint);
        graph.computeIfAbsent(nextPoint, p -> ImmutableSet.builder());
        graph.get(nextPoint).add(point);

        nextPoints.add(nextPoint);
      }

      return nextPoints.build();

    } else if (instruction instanceof InstructionList) {
      ImmutableSet<Point> nextPoints = currentPoints;
      for (Instruction i : ((InstructionList) instruction).instructions) {
        nextPoints = buildGraphNode(i, nextPoints, graph);
      }

      return nextPoints;

    } else if (instruction instanceof OptionsInstruction) {
      ImmutableSet.Builder<Point> nextPoints = ImmutableSet.builder();
      for (Instruction option : ((OptionsInstruction) instruction).options) {
        nextPoints.addAll(buildGraphNode(option, currentPoints, graph));
      }

      return nextPoints.build();

    } else {
      throw new IllegalArgumentException("Unknown instruction type " + instruction.getClass());
    }
  }

  public static int furthestRoom(ImmutableMap<Point, ImmutableSet<Point>> graph) {
    Map<Point, Integer> distances = new HashMap<>();
    distances.put(new Point(0, 0), 0);

    Queue<Point> toVisit = new ArrayDeque<>();
    toVisit.add(new Point(0, 0));

    while (!toVisit.isEmpty()) {
      Point point = toVisit.remove();
      int pointDistance = distances.get(point);

      for (Point next : graph.get(point)) {
        if (!distances.containsKey(next)) {
          distances.put(next, pointDistance + 1);
          toVisit.add(next);
        }
      }
    }

    int furthest = 0;
    for (int distance : distances.values()) {
      if (distance > furthest) {
        furthest = distance;
      }
    }

    return furthest;
  }

  public static void main(String[] args) throws IOException {
    File file = new File(Day20.class.getResource("/day20.txt").getFile());
    String line = Files.asCharSource(file, Charsets.UTF_8).readFirstLine();

    ImmutableMap<Point, ImmutableSet<Point>> graph = buildGraph(parse(line));

    // Part 1: what is the largest number of doors required to pass through to reach a room?
    System.out.println("Part 1: " + furthestRoom(graph));
  }
}
