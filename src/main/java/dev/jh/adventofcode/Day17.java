package dev.jh.adventofcode;

import com.google.common.base.Charsets;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day17 {
  public enum VeinDirection {
    HORIZONTAL, VERTICAL
  }

  public static class ClayVein {
    private static Pattern pattern = Pattern.compile("^([xy])=(\\d+), [xy]=(\\d+)..(\\d+)$");

    public final VeinDirection direction;
    public final int fixed;
    public final int lineStart;
    public final int lineEnd;

    public ClayVein(VeinDirection direction, int fixed, int lineStart, int lineEnd) {
      this.direction = direction;
      this.fixed = fixed;
      this.lineStart = lineStart;
      this.lineEnd = lineEnd;
    }

    public static ClayVein parseLine(String line) {
      Matcher matcher = pattern.matcher(line);
      if (!matcher.matches()) {
        throw new IllegalArgumentException(line + " is not a valid clay vein");
      }

      return new ClayVein(
          "x".equals(matcher.group(1)) ? VeinDirection.VERTICAL : VeinDirection.HORIZONTAL,
          Integer.parseInt(matcher.group(2)),
          Integer.parseInt(matcher.group(3)),
          Integer.parseInt(matcher.group(4))
      );
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("direction", direction)
          .add("fixed", fixed)
          .add("lineStart", lineStart)
          .add("lineEnd", lineEnd)
          .toString();
    }
  }

  public enum Square {
    SAND('.'),
    CLAY('#'),
    SPRING_WATER('+'),
    FALLING_WATER('|'),
    SPREADING_WATER('_'),
    POOLING_WATER('-'),
    STILL_WATER('~');

    public final char name;
    public final boolean isWater;

    Square(char name) {
      this.name = name;
      this.isWater = name().contains("WATER");
    }

    public static Square valueOf(char c) {
      for (Square square : values()) {
        if (square.name == c) {
          return square;
        }
      }

      throw new IllegalArgumentException(c + " is not a valid square");
    }

    @Override
    public String toString() {
      return Character.toString(name);
    }
  }

  public static final ImmutableList<Mask> MASKS = ImmutableList.of(
      // -------------------------------------------------- Falling
      Mask.builder()
          .withPattern(
              "+",
              ".")
          .withResult(
              "+",
              "|")
          .withRegistration(0, 0)
          .withNextPoints(point -> ImmutableList.of(new Point(point.x, point.y + 1)))
          .build(),

      Mask.builder()
          .withPattern(
              "|",
              ".")
          .withResult(
              "|",
              "|")
          .withRegistration(0, 0)
          .withNextPoints(point -> ImmutableList.of(new Point(point.x, point.y + 1)))
          .build(),

      Mask.builder()
          .withPattern(
              "|",
              "~")
          .withResult(
              "_",
              "~")
          .withRegistration(0, 0)
          .withNextPoints(point -> ImmutableList.of(
              new Point(point.x, point.y),
              new Point(point.x, point.y)))
          .build(),

      Mask.builder()
          .withPattern(
              "|",
              "#")
          .withResult(
              "_",
              "#")
          .withRegistration(0, 0)
          .withNextPoints(point -> ImmutableList.of(
              new Point(point.x, point.y),
              new Point(point.x, point.y)))
          .build(),

      // -------------------------------------------------- Spreading

      Mask.builder()
          .withPattern("|_")
          .withResult("|_")
          .withRegistration(1, 0)
          .withNextPoints(point -> ImmutableList.of())
          .build(),

      Mask.builder()
          .withPattern("_|")
          .withResult("_|")
          .withRegistration(0, 0)
          .withNextPoints(point -> ImmutableList.of())
          .build(),

      Mask.builder()
          .withPattern("-|")
          .withResult("-|")
          .withRegistration(0, 0)
          .withNextPoints(point -> ImmutableList.of())
      .build(),

      Mask.builder()
          .withPattern("|-")
          .withResult("|-")
          .withRegistration(1, 0)
          .withNextPoints(point -> ImmutableList.of())
      .build(),


      Mask.builder()
          .withPattern(
              "_",
              ".")
          .withResult(
              "|",
              "|")
          .withRegistration(0, 0)
          .withNextPoints(point -> ImmutableList.of(new Point(point.x, point.y + 1)))
          .build(),

      Mask.builder()
          .withPattern("._")
          .withResult("__")
          .withRegistration(1, 0)
          .withNextPoints(point -> ImmutableList.of(new Point(point.x - 1, point.y)))
          .build(),

      Mask.builder()
          .withPattern("_.")
          .withResult("__")
          .withRegistration(0, 0)
          .withNextPoints(point -> ImmutableList.of(new Point(point.x + 1, point.y)))
          .build(),

      Mask.builder()
          .withPattern("#_")
          .withResult("#-")
          .withRegistration(1, 0)
          .withNextPoints(point -> ImmutableList.of(new Point(point.x, point.y)))
          .build(),

      Mask.builder()
          .withPattern("_#")
          .withResult("-#")
          .withRegistration(0, 0)
          .withNextPoints(point -> ImmutableList.of(new Point(point.x, point.y)))
          .build(),

      // -------------------------------------------------- Pooling
      Mask.builder()
          .withPattern("_-")
          .withResult("--")
          .withRegistration(1, 0)
          .withNextPoints(point -> ImmutableList.of(new Point(point.x - 1, point.y)))
          .build(),

      Mask.builder()
          .withPattern("-_")
          .withResult("--")
          .withRegistration(0, 0)
          .withNextPoints(point -> ImmutableList.of(new Point(point.x + 1, point.y)))
          .build(),

      Mask.builder()
          .withPattern("#-#")
          .withResult("#~#")
          .withRegistration(1, 0)
          .withNextPoints(point -> ImmutableList.of(new Point(point.x, point.y)))
          .build(),

      Mask.builder()
          .withPattern("--")
          .withResult("~~")
          .withRegistration(0, 0)
          .withNextPoints(point -> ImmutableList.of(
              new Point(point.x, point.y),
              new Point(point.x + 1, point.y)))
          .build(),

      // -------------------------------------------------- Pooling

      Mask.builder()
          .withPattern(
              "|",
              "~")
          .withResult(
              "_",
              "~")
          .withRegistration(0, 1)
          .withNextPoints(point -> ImmutableList.of(
              new Point(point.x, point.y),
              new Point(point.x, point.y - 1),
              new Point(point.x, point.y - 1)))
          .build(),

      Mask.builder()
          .withPattern("~-")
          .withResult("~~")
          .withRegistration(0, 0)
          .withNextPoints(point -> ImmutableList.of(new Point(point.x + 1, point.y)))
          .build(),

      Mask.builder()
          .withPattern("-~")
          .withResult("~~")
          .withRegistration(1, 0)
          .withNextPoints(point -> ImmutableList.of(new Point(point.x - 1, point.y)))
          .build()

/*
      Mask.builder()
          .withPattern("-~")
          .withResult("~~")
          .withRegistration(0, 0)
          .withNextPoints(point -> ImmutableList.of(new Point(point.x - 1, point.y)))
          .build(),

      Mask.builder()
          .withPattern("~-")
          .withResult("~~")
          .withRegistration(1, 0)
          .withNextPoints(point -> ImmutableList.of(new Point(point.x + 1, point.y)))
          .build()
*/
  );

  public static class Mask {
    public final Point registration;
    public final Square[][] pattern;
    public final Square[][] result;
    public final Function<Point, ImmutableList<Point>> nextPoints;

    public Mask(
        Point registration,
        Square[][] pattern,
        Square[][] result,
        Function<Point, ImmutableList<Point>> nextPoints
    ) {
      this.registration = registration;
      this.pattern = pattern;
      this.result = result;
      this.nextPoints = nextPoints;
    }

    /**
     * Returns whether this mask matches the grid at the given point.
     * @param grid Grid of squares
     * @param point Point in grid coordinates to align  this mask's registration point.
     */
    public boolean canApply(Square[][] grid, Point point) {

      int yExtent = point.y + pattern.length - registration.y;
      if (yExtent > grid.length || point.x - registration.x < 0 || point.x + pattern[0].length - registration.x > grid[point.y].length) {
        return false;
      }

      for (int y = 0; y < pattern.length; y ++) {
        for (int x = 0; x < pattern[y].length; x ++) {
          if (grid[y + point.y - registration.y][x + point.x - registration.x] != pattern[y][x]) {
            return false;
          }
        }
      }

      return true;
    }

    /**
     * Applies this mask result to the grid at the given point, returning the modified grid.
     *
     * @param grid Grid to apply this mask to
     * @param point Point in grid coordinates where this mask will be applied
     * @return The modified grid.
     */
    public void apply(Square[][] grid, Point point) {
      for (int y = 0; y < pattern.length; y ++) {
        for (int x = 0; x < pattern[y].length; x ++) {
          grid[y + point.y - registration.y][x + point.x - registration.x] = result[y][x];
        }
      }
    }

    public static Builder builder() {
      return new Builder();
    }

    public static class Builder {
      private Point registration;
      private Square[][] pattern;
      private Square[][] result;
      private Function<Point, ImmutableList<Point>> nextPoints;

      private Builder() {}

      public Builder withPattern(String... lines) {
        this.pattern = parseLines(lines);
        return this;
      }

      public Builder withResult(String... lines) {
        this.result = parseLines(lines);
        return this;
      }

      public Builder withRegistration(int x, int y) {
        this.registration = new Point(x, y);
        return this;
      }

      public Builder withNextPoints(Function<Point, ImmutableList<Point>> nextPoints) {
        this.nextPoints = nextPoints;
        return this;
      }

      public Mask build() {
        return new Mask(registration, pattern, result, nextPoints);
      }

      private static Square[][] parseLines(String[] lines) {
        Square[][] squares = new Square[lines.length][lines[0].length()];

        for (int y = 0; y < lines.length; y ++) {
          for (int x = 0; x < lines[y].length(); x ++) {
            squares[y][x] = Square.valueOf(lines[y].charAt(x));
          }
        }

        return squares;
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
      return x == point.x && y == point.y;
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

  /**
   * Parses the given list of scan lines into a grid.
   *
   * @param lines Lines with x and y extends of clay
   * @return Grid
   */
  public static Grid parseLines(ImmutableList<String> lines) {
    ImmutableList<ClayVein> clayVeins = lines.stream()
        .map(ClayVein::parseLine)
        .collect(ImmutableList.toImmutableList());

    Point min = new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);
    Point max = new Point(0, 0);

    for (ClayVein vein : clayVeins) {
      if (vein.direction == VeinDirection.VERTICAL) {
        if (vein.fixed < min.x) {
          min = new Point(vein.fixed, min.y);
        }

        if (vein.fixed > max.x) {
          max = new Point(vein.fixed, max.y);
        }

        if (vein.lineStart < min.y) {
          min = new Point(min.x, vein.lineStart);
        }

        if (vein.lineEnd > max.y) {
          max = new Point(max.x, vein.lineEnd);
        }
      } else {
        if (vein.fixed < min.y) {
          min = new Point(min.x, vein.fixed);
        }

        if (vein.fixed > max.y) {
          max = new Point(max.x, vein.fixed);
        }

        if (vein.lineStart < min.x) {
          min = new Point(vein.lineStart, min.y);
        }

        if (vein.lineEnd > max.x) {
          max = new Point(vein.lineEnd, max.y);
        }
      }
    }

    Point offset = new Point(min.x - 1, min.y);

    // Spring starts at x=500, y=0.
    Square[][] grid = new Square[max.y + 1][max.x - min.x + 3];

    for (int y = 0; y < grid.length; y ++) {
      for (int x = 0; x < grid[y].length; x ++) {
        grid[y][x] = Square.SAND;
      }
    }

    grid[0][500 - offset.x] = Square.SPRING_WATER;

    for (ClayVein vein : clayVeins) {
      if (vein.direction == VeinDirection.HORIZONTAL) {
        for (int x = vein.lineStart - offset.x; x <= vein.lineEnd - offset.x; x ++) {
          grid[vein.fixed][x] = Square.CLAY;
        }
      } else {
        for (int y = vein.lineStart; y <= vein.lineEnd; y ++) {
          grid[y][vein.fixed - offset.x] = Square.CLAY;
        }
      }
    }

    return new Grid(grid, offset);
  }

  public static class Grid {
    public final Square[][] grid;
    public final Point offset;

    public Grid(Square[][] grid, Point offset) {
      this.grid = grid;
      this.offset = offset;
    }

    /**
     * Flows water through this grid, modifying it in the process.
     *
     * @return This grid with water fully flowed.
     */
    public Grid flow() {
      Queue<Point> points = new ArrayDeque<>();
      points.add(new Point(500 - offset.x, 0));

      while (!points.isEmpty()) {
        Point point = points.remove();

        for (Mask mask : MASKS) {
          if (mask.canApply(grid, point)) {
            mask.apply(grid, point);
            points.addAll(mask.nextPoints.apply(point));
            break;
          }
        }

//        System.out.println(point);
//        System.out.println("Points: " + points);
//        System.out.println(this + "\n");
      }

      return this;
    }

    /**
     * Counts the number of squares that the water can reach within the range of y values in the scan.
     *
     * @return Number of squares that water can reach.
     */
    public int waterCount() {
      int count = 0;
      for (int y = offset.y; y < grid.length; y ++) {
        for (int x = 0; x < grid[y].length; x ++) {
          if (grid[y][x].isWater) {
            count++;
          }
        }
      }

      return count;
    }

    /**
     * Counts the number of squares that contain still water after the source runs dry.
     *
     * @return Number of squares that contain still water.
     */
    public int stillCount() {
      int count = 0;
      for (int y = offset.y; y < grid.length; y ++) {
        for (int x = 0; x < grid[y].length; x ++) {
          if (grid[y][x] == Square.STILL_WATER) {
            count ++;
          }
        }
      }

      return count;
    }

    @Override
    public String toString() {
      StringBuilder bldr = new StringBuilder();

      for (int y = 0; y < grid.length; y ++) {
        for (int x = 0; x < grid[y].length; x ++) {
          bldr.append(grid[y][x].name);
        }
        bldr.append('\n');
      }

      return MoreObjects.toStringHelper(this)
          .add("grid", '\n' + bldr.toString())
          .add("offset", offset)
          .toString();
    }
  }

  public static void main(String[] args) throws IOException {
    File file = new File(Day17.class.getResource("/day17.txt").getFile());
    ImmutableList<String> lines = ImmutableList.copyOf(Files.readLines(file, Charsets.UTF_8));
    Grid grid = parseLines(lines).flow();

    // Part 1: how many tiles can the water reach?
    System.out.println("Part 1: " + grid.waterCount());

    // Part 2: how many tiles are still after the spring runs dry and the moving water has drained?
    System.out.println("Part 2: " + grid.stillCount());
  }
}
