package dev.jh.adventofcode;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.util.*;
import java.util.function.UnaryOperator;

public class Day22 {

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

  public enum Erosion {
    ROCKY('.', 0),
    WET('=', 1),
    NARROW('|', 2);

    public final char name;
    public final int risk;

    Erosion(char name, int risk) {
      this.name = name;
      this.risk = risk;
    }

    public static Erosion fromErosionLevel(int erosionLevel) {
      switch (erosionLevel % 3) {
        case 0: return ROCKY;
        case 1: return WET;
        case 2: return NARROW;
        default: throw new IllegalStateException("Geologic index wasn't mod 3.");
      }
    }

    public static Erosion fromName(char name) {
      if (name == 'M' || name == 'T') {
        return ROCKY;
      }

      for (Erosion erosion : values()) {
        if (erosion.name == name) {
          return erosion;
        }
      }

      throw new IllegalArgumentException(name + " is not a valid erosion.");
    }
  }

  public static class Cave {
    public final Point target;
    public final int depth;
    public final Erosion[][] erosion;

    public Cave(Point target, int depth) {
      this.target = target;
      this.depth = depth;

      this.erosion = calculateErosion(target, depth);
    }

    private Erosion[][] calculateErosion(Point target, int depth) {
      Erosion[][] erosion = new Erosion[target.y * 15][target.x * 15];
      int[][] erosionLevels = new int[erosion.length][erosion[0].length];

      for (int diagonalY = 0; diagonalY < erosion.length + erosion[0].length; diagonalY ++) {
        for (int y = diagonalY >= erosion.length ? erosion.length - 1 : diagonalY; y >= 0; y --) {
          int x = diagonalY - y;

          if (x < erosion[y].length) {
            int erosionLevel;
            if ((x == 0 && y == 0) || x == target.x && y == target.y) {
              erosionLevel = depth % 20183;
            } else if (y == 0) {
              erosionLevel = (x * 16807 + depth) % 20183;
            } else if (x == 0) {
              erosionLevel = (y * 48271 + depth) % 20183;
            } else {
              erosionLevel = (erosionLevels[y - 1][x] * erosionLevels[y][x - 1] + depth) % 20183;
            }

            erosionLevels[y][x] = erosionLevel;
            erosion[y][x] = Erosion.fromErosionLevel(erosionLevel);
          }
        }
      }

      return erosion;
    }

    public int risk() {
      int risk = 0;
      for (int y = 0; y <= target.y; y ++) {
        for (int x = 0; x <= target.x; x ++) {
          risk += erosion[y][x].risk;
        }
      }
      return risk;
    }

    @Override
    public String toString() {
      StringBuilder bldr = new StringBuilder();

      for (int y = 0; y < erosion.length; y ++) {
        bldr.append('\n');
        for (int x = 0; x < erosion[y].length; x ++) {
          bldr.append(erosion[y][x].name);
        }
      }

      return MoreObjects.toStringHelper(this)
          .add("targetX", target.x)
          .add("targetY", target.y)
          .add("depth", depth)
          .add("erosion", bldr.toString())
          .toString();
    }

    public int fastestMinutes() {
      // A*.  h(n) = manhattan distance from current point to goal, g(n) = actual cost including switching.
      State start = new State(new Point(0, 0), Tool.TORCH);
      State goal = new State(target, Tool.TORCH);

      Map<State, State> cameFrom = new HashMap<>();
      Map<State, Integer> gScores = new HashMap<>(); // Cost of getting from the start node to the key
      Map<State, Integer> fScores = new HashMap<>(); // Total cost of getting from the start node to the goal by passing through a node

      Set<State> visited = new HashSet<>();

      Set<State> toVisit = new HashSet<>();
      toVisit.add(start);
      gScores.put(start, 0);
      fScores.put(start, start.pointDistance(goal));

      while (!toVisit.isEmpty()) {
        State current = toVisit.stream()
            .min(Comparator.comparingInt(fScores::get))
            .get();

        toVisit.remove(current);

        if (current.equals(goal)) {
          List<State> states = new ArrayList<>();

          int time = 0;
          for (State backTrack = current; cameFrom.containsKey(backTrack); backTrack = cameFrom.get(backTrack)) {
            states.add(backTrack);
            time += backTrack.timeDistance(cameFrom.get(backTrack));
          }

          return time;
        }

        visited.add(current);

        for (State neighbor : neighbors(current)) {
          if (!visited.contains(neighbor)) {
            int gScore = gScores.get(current) + current.timeDistance(neighbor);

            if (!toVisit.contains(neighbor)) {
              toVisit.add(neighbor);
            } else if (gScore >= gScores.get(neighbor)) {
              continue;
            }

            cameFrom.put(neighbor, current);
            gScores.put(neighbor, gScore);
            fScores.put(neighbor, gScore + neighbor.pointDistance(goal));
          }
        }
      }

      throw new IllegalStateException("No path to target found - consider expanding the erosion grid.");
    }

    private ImmutableSet<State> neighbors(State state) {
      ImmutableSet.Builder<State> neighbors = ImmutableSet.builder();

      // Can switch tools to the other one valid for the region
      neighbors.add(new State(state.point, state.tool.otherTool(erosion[state.point.y][state.point.x])));

      // Or can move one square adjacent as long as the current tool is allowed in that zone.
      for (Direction direction : Direction.values()) {
        Point directionPoint = direction.move.apply(state.point);
        if (directionPoint.y >= 0
            && directionPoint.x >= 0
            && directionPoint.y < erosion.length
            && directionPoint.x < erosion[0].length
            && state.tool.allowed(erosion[directionPoint.y][directionPoint.x])
        ) {
          neighbors.add(new State(directionPoint, state.tool));
        }
      }

      return neighbors.build();
    }
  }

  public enum Direction {
    UP(point -> new Point(point.x, point.y - 1)),
    DOWN(point -> new Point(point.x, point.y + 1)),
    LEFT(point -> new Point(point.x - 1, point.y)),
    RIGHT(point -> new Point(point.x + 1, point.y));

    public final UnaryOperator<Point> move;

    Direction(UnaryOperator<Point> move) {
      this.move = move;
    }
  }

  public enum Tool {
    CLIMBING_GEAR,
    TORCH,
    NEITHER;

    private static final ImmutableMap<Erosion, ImmutableSet<Tool>> VALID_TOOLS = ImmutableMap.<Erosion, ImmutableSet<Tool>>builder()
        .put(Erosion.ROCKY, ImmutableSet.of(Tool.CLIMBING_GEAR, Tool.TORCH))
        .put(Erosion.WET, ImmutableSet.of(Tool.CLIMBING_GEAR, Tool.NEITHER))
        .put(Erosion.NARROW, ImmutableSet.of(Tool.TORCH, Tool.NEITHER))
        .build();

    public Tool otherTool(Erosion erosion) {
      return VALID_TOOLS.get(erosion).stream()
          .filter(tool -> tool != this)
          .findFirst()
          .orElseThrow(() -> new IllegalStateException("Erosion " + erosion + " doesn't have two valid tools."));
    }

    public boolean allowed(Erosion erosion) {
      return VALID_TOOLS.get(erosion).contains(this);
    }
  }

  public static class State {
    public final Point point;
    public final Tool tool;

    public State(Point point, Tool tool) {
      this.point = point;
      this.tool = tool;
    }

    public int pointDistance(State other) {
      // Manhattan distance between this point and the other point.
      return Math.abs(point.x - other.point.x) + Math.abs(point.y - other.point.y);
    }

    public int timeDistance(State adjacent) {
      int distance = pointDistance(adjacent);
      if (distance == 0) {
        if (tool == adjacent.tool) {
          throw new IllegalArgumentException("Time distance can't be applied to the same state twice.");
        } else {
          return 7; // Tool change.
        }
      } else if (distance == 1) {
        if (tool == adjacent.tool) {
          return 1;
        } else {
          throw new IllegalArgumentException("Time distance can only be applied to a move if the tool doesn't change.");
        }
      } else {
        throw new IllegalArgumentException("Time distance can only be applied to adjacent states.");
      }
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      State state = (State) o;
      return Objects.equal(point, state.point) &&
          tool == state.tool;
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(point, tool);
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("point", point)
          .add("tool", tool)
          .toString();
    }
  }
  public static void main(String[] args) {
    Point target = new Point(8, 701);
    int caveDepth = 5913;

    Cave cave = new Cave(target, caveDepth);

    // Part 1: what is the total risk level for the rectangle from 0,0 to the target?
    System.out.println("Part 1: " + cave.risk());

    // Part 2: what is the fewest number of minutes to reach the target?
    // TODO: 994 is too high.
    System.out.println("Part 2: " + cave.fastestMinutes());
  }
}
