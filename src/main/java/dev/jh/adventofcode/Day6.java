package dev.jh.adventofcode;

import com.google.common.base.Charsets;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Day6 {

  public static final int INFINITE_AREA = -1;

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

  public static class Coordinate {
    public final char name;
    public final int x;
    public final int y;

    public Coordinate(char name, int x, int y) {
      this.name = name;
      this.x = x;
      this.y = y;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Coordinate that = (Coordinate) o;
      return name == that.name && x == that.x && y == that.y;
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(name, x, y);
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("name", name)
          .add("x", x)
          .add("y", y)
          .toString();
    }
  }

  /**
   * Converts the given list of lines into named coordinates.
   *
   * @param lines Lines to convert
   * @return Coordinates
   */
  public static ImmutableList<Coordinate> parseLines(ImmutableList<String> lines) {
    Pattern pattern = Pattern.compile("(\\d+), (\\d+)");
    char name = 'a';

    ImmutableList.Builder<Coordinate> coordinates = ImmutableList.builder();
    for (String line : lines) {
      Matcher matcher = pattern.matcher(line);
      if (matcher.matches()) {
        coordinates.add(new Coordinate(
            name,
            Integer.parseInt(matcher.group(1)),
            Integer.parseInt(matcher.group(2))
        ));

        name++;
        if (name > 'z') {
          name = 'A';
        }
      }
    }

    return coordinates.build();
  }

  /**
   * Returns the largest x and y values out of the list of coordinates.
   *
   * @param coordinates List of coordinates
   * @return Largest independent x and y values in the list of coordinates
   */
  private static Point gridExtent(ImmutableList<Coordinate> coordinates) {
    int width = coordinates.stream()
        .mapToInt(c -> c.x + 1)
        .max()
        .orElseThrow(() -> new IllegalArgumentException("No coordinates"));

    int height = coordinates.stream()
        .mapToInt(c -> c.y + 1)
        .max()
        .orElseThrow(() -> new IllegalArgumentException("No coordinates"));

    return new Point(width, height);
  }

  /**
   * Fills a grid with the name of the coordinate that's closest to each cell in the grid.  '.' indicates that
   * multiple coordinates are closest.
   *
   * @param coordinates List of coordinates
   * @return Grid filled with the closest coordinate to each point.  Rotation is grid[y][x].
   */
  public static char[][] fillGrid(ImmutableList<Coordinate> coordinates) {
    Point extent = gridExtent(coordinates);
    int width = extent.x;
    int height = extent.y;

    // Flood fill the grid.
    // Untouched cells start at 0,
    // coordinate names indicate that another coordinate was closest,
    // '.' indicates that two coordinates have the same distance from a point.
    char[][] grid = new char[height][width];

    // pointsSet: # of values of the grid that are set.
    // distance: width of the circle around each coordinate in this pass.
    for (int pointsSet = 0, distance = 0; pointsSet < width * height; distance ++) {
      // Given the distance circle around each point, plan the coordinates that will be set
      List<Coordinate> passCoordinates = new ArrayList<>();
      for (Coordinate coordinate : coordinates) {
        for (Point point : distanceAway(coordinate, distance, width, height)) {
          if (grid[point.y][point.x] == 0) {
            passCoordinates.add(new Coordinate(coordinate.name, point.x, point.y));
          }
        }
      }

      // Resolve coordinate collisions
      Map<Point, List<Coordinate>> distinctPoints = passCoordinates.stream()
          .collect(Collectors.groupingBy(coordinate -> new Point(coordinate.x, coordinate.y)));

      // Write the coordinates to the grid
      for (Map.Entry<Point, List<Coordinate>> entry : distinctPoints.entrySet()) {
        Point point = entry.getKey();
        List<Coordinate> pointCoordinates = entry.getValue();

        if (pointCoordinates.size() == 1) {
          grid[point.y][point.x] = pointCoordinates.get(0).name;
        } else {
          grid[point.y][point.x] = '.';
        }

        pointsSet++;
      }

    }

    return grid;
  }

  /**
   * Returns a set of points in the grid that are Manhattan distance squares away from the coordinate.
   *
   * @param coordinate Coordinate that will be in the center of the points.
   * @param distance Distance away from the coordinate
   * @param width Width of the grid
   * @param height Height of the grid
   * @return Set of points within the bounds of the grid that are distance away from the coordinate
   */
  public static ImmutableSet<Point> distanceAway(Coordinate coordinate, int distance, int width, int height) {
    Set<Point> points = new HashSet<>();

    for (int yOffset = 0; yOffset <= distance; yOffset ++) {
      int xOffset = distance - yOffset;
      points.add(new Point(coordinate.x + xOffset, coordinate.y + yOffset));
      points.add(new Point(coordinate.x - xOffset, coordinate.y + yOffset));
      points.add(new Point(coordinate.x + xOffset, coordinate.y - yOffset));
      points.add(new Point(coordinate.x - xOffset, coordinate.y - yOffset));
    }

    return points.stream()
        .filter(point -> point.x >= 0 && point.x < width && point.y >= 0 && point.y < height)
        .collect(ImmutableSet.toImmutableSet());
  }

  /**
   * Calculates the number of points that are closest to each point in the grid, counting coordinates that have
   * an infinite area as INFINITE_AREA.
   *
   * @param grid Filled grid
   * @return Map of coordinate name to area
   */
  public static ImmutableMap<Character, Integer> coordinateAreas(char[][] grid) {
    int height = grid.length;
    int width = grid[0].length;

    Map<Character, Integer> areas = new HashMap<>();

    for (int y = 0; y < height; y ++) {
      for (int x = 0; x < width; x ++) {
        char name = grid[y][x];
        if (name == '.') {
          continue;
        }

        if (y == 0 || x == 0 || y == height - 1 || x == width - 1) {
          // Coordinates on the edge are infinite.
          areas.put(name, INFINITE_AREA);
        } else {
          areas.compute(name, (sameName, oldValue) -> {
            if (oldValue == null) {
              return 1;
            } else if (oldValue == INFINITE_AREA) {
              return INFINITE_AREA;
            } else {
              return oldValue + 1;
            }
          });
        }
      }
    }
    return ImmutableMap.copyOf(areas);
  }

  /**
   * Returns the largest non-infinite area of points closest to each coordinate.
   *
   * @param coordinates List of coordinates
   * @return Largest non-infinite area
   */
  public static int largestArea(ImmutableList<Coordinate> coordinates) {
    char[][] grid = fillGrid(coordinates);

    // Count the size of each coordinate's area.  Coordinates touching the side of the grid are infinite (-1 canary).
    ImmutableMap<Character, Integer> areas = coordinateAreas(grid);

    // Return the biggest non-infinite area.
    return areas.values().stream()
        .mapToInt(value -> value)
        .max()
        .orElseThrow(() -> new IllegalStateException("No areas"));
  }

  /**
   * Returns the sum of manhattan distances from the given point to each of the coordinates.
   *
   * @param point Point to start at
   * @param coordinates Coordinates to finish at
   * @return Total distance between the point and all coordinates
   */
  public static int totalDistance(Point point, ImmutableList<Coordinate> coordinates) {
    return coordinates.stream()
        .mapToInt(coordinate -> Math.abs(coordinate.x - point.x) + Math.abs(coordinate.y - point.y))
        .sum();
  }

  /**
   * Returns the size of the region consisting of points whose sum of manhattan distance to the given coordinates
   * is less than the given distance
   *
   * @param coordinates List of coordinates
   * @param distance Sum distance points must be less than
   * @return Number of points in the region
   */
  public static int totalDistanceRegionSize(ImmutableList<Coordinate> coordinates, int distance) {
    Point extent = gridExtent(coordinates);

    int regionSize = 0;
    for (int y = 0; y < extent.y; y ++) {
      for (int x = 0; x < extent.x; x ++) {
        if (totalDistance(new Point(x, y), coordinates) < distance) {
          regionSize++;
        }
      }
    }

    return regionSize;
  }


  public static void main(String[] args) throws Exception {
    File file = new File(Day4.class.getResource("/day6.txt").getFile());
    ImmutableList<String> lines = ImmutableList.copyOf(Files.readLines(file, Charsets.UTF_8));
    ImmutableList<Coordinate> coordinates = parseLines(lines);

    // Part 1: find the largest non-infinite area that's closest to a coordinate.
    System.out.println("Part 1: " + largestArea(coordinates));
    // Part 2: size of the region consisting of points whose sum distance to all coordinates is < 10000
    System.out.println("Part 2: " + totalDistanceRegionSize(coordinates, 10000));
  }
}
