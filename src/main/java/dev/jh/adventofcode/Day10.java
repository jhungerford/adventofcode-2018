package dev.jh.adventofcode;

import com.google.common.base.Charsets;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day10 {

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
      return "{x: " + x + ", y: " + y + "}";
    }
  }

  public static class Star {
    private static final Pattern PATTERN = Pattern.compile("position=< *(-?\\d+), *(-?\\d+)> velocity=< *(-?\\d+), *(-?\\d+)>");

    public final Point position;
    public final Point velocity;

    public Star(Point position, Point velocity) {
      this.position = position;
      this.velocity = velocity;
    }

    public Star tick() {
      return new Star(new Point(position.x + velocity.x, position.y + velocity.y), velocity);
    }

    public static Star parse(String line) {
      Matcher matcher = PATTERN.matcher(line);
      if (!matcher.matches()) {
        throw new IllegalArgumentException(line + " is not a valid point.");
      }

      return new Star(
          new Point(
              Integer.parseInt(matcher.group(1)),
              Integer.parseInt(matcher.group(2))
          ),
          new Point(
              Integer.parseInt(matcher.group(3)),
              Integer.parseInt(matcher.group(4))
          )
      );
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Star star = (Star) o;
      return Objects.equal(position, star.position) &&
          Objects.equal(velocity, star.velocity);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(position, velocity);
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("position", position)
          .add("velocity", velocity)
          .toString();
    }
  }

  /**
   * Upper left and lower right corners of the rectangle that just contains all of the points.
   */
  private static class Bounds implements Comparable<Bounds> {
    public final Point min;
    public final Point max;

    public Bounds(Point min, Point max) {
      this.min = min;
      this.max = max;
    }

    public static Bounds ofStars(ImmutableList<Star> stars) {
      int minX = stars.stream()
          .mapToInt(star -> star.position.x)
          .min()
          .orElseThrow(() -> new IllegalArgumentException("No stars"));

      int maxX = stars.stream()
          .mapToInt(star -> star.position.x)
          .max()
          .orElseThrow(() -> new IllegalArgumentException("No stars"));

      int minY = stars.stream()
          .mapToInt(star -> star.position.y)
          .min()
          .orElseThrow(() -> new IllegalArgumentException("No stars"));

      int maxY = stars.stream()
          .mapToInt(star -> star.position.y)
          .max()
          .orElseThrow(() -> new IllegalArgumentException("No stars"));

      return new Bounds(new Point(minX, minY), new Point(maxX, maxY));
    }

    @Override
    public int compareTo(Bounds other) {
      int thisMagnitude = (max.x - min.x) * (max.x - min.x)
          + (max.y - min.y) * (max.y - min.y);
      int otherMagnitude = (other.max.x - other.min.x) * (other.max.x - other.min.x)
          + (other.max.y - other.min.y) * (other.max.y - other.min.y);

      return Integer.signum(thisMagnitude - otherMagnitude);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Bounds bounds = (Bounds) o;
      return Objects.equal(min, bounds.min) &&
          Objects.equal(max, bounds.max);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(min, max);
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("min", min)
          .add("max", max)
          .toString();
    }
  }

  /**
   * Advances the stars until the rectangle that contains them is at it's smallest.
   *
   * @param stars Stars to advance.
   * @return Stars in an alignment where they're most contained.
   */
  public static ImmutableList<Star> smallestBounds(ImmutableList<Star> stars) {
    ImmutableList<Star> previousStars = stars;
    Bounds previousBounds = Bounds.ofStars(previousStars);

    ImmutableList<Star> newStars = tick(stars);
    Bounds newBounds = Bounds.ofStars(newStars);

    while (previousBounds.compareTo(newBounds) >= 0) {
      previousStars = newStars;
      previousBounds = newBounds;

      newStars = tick(previousStars);
      newBounds = Bounds.ofStars(newStars);
    }

    return previousStars;
  }

  /**
   * Advances the stars one position, returning a new list of stars in the new positions.
   *
   * @param stars Stars to advance
   * @return New list of stars
   */
  private static ImmutableList<Star> tick(ImmutableList<Star> stars) {
    return stars.stream()
        .map(Star::tick)
        .collect(ImmutableList.toImmutableList());
  }

  /**
   * Renders the given stars into a message.  May not make much sense if the stars aren't in alignment.
   *
   * @param stars Stars to render
   * @return Message in the stars.
   */
  public static String renderMessage(ImmutableList<Star> stars) {
    Bounds bounds = Bounds.ofStars(stars);

    StringBuilder bldr = new StringBuilder();

    for (int y = bounds.min.y; y <= bounds.max.y; y ++) {
      for (int x = bounds.min.x; x <= bounds.max.x; x++) {
        Point currentPosition = new Point(x, y);
        char c = stars.stream().anyMatch(star -> star.position.equals(currentPosition)) ? '#' : '.';

        bldr.append(c);
      }
      bldr.append('\n');
    }

    return bldr.toString();
  }

  public static void main(String[] args) throws Exception {
    File file = new File(Day10.class.getResource("/day10.txt").getFile());
    ImmutableList<Star> stars = Files.readLines(file, Charsets.UTF_8).stream()
        .map(Star::parse)
        .collect(ImmutableList.toImmutableList());

    // Part 1: when the stars are in alignment, what's their message?
    System.out.println("Part 1: \n" + renderMessage(smallestBounds(stars)));

  }
}
