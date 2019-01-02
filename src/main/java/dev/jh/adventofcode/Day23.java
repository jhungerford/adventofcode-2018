package dev.jh.adventofcode;

import com.google.common.base.Charsets;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day23 {

  public static class Position {
    public final long x;
    public final long y;
    public final long z;

    public Position(long x, long y, long z) {
      this.x = x;
      this.y = y;
      this.z = z;
    }

    public long distance(Position other) {
      return Math.abs(x - other.x) + Math.abs(y - other.y) + Math.abs(z - other.z);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Position position = (Position) o;
      return x == position.x &&
          y == position.y &&
          z == position.z;
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(x, y, z);
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("x", x)
          .add("y", y)
          .add("z", z)
          .toString();
    }
  }

  public static class Nanobot {
    public final Position position;
    public final long range;

    public Nanobot(Position position, long range) {
      this.position = position;
      this.range = range;
    }

    public boolean inRange(Nanobot other) {
      return position.distance(other.position) <= range;
    }

    private static final Pattern NANOBOT_PATTERN = Pattern.compile("^pos=<(-?\\d+),(-?\\d+),(-?\\d+)>, r=(\\d+)$");
    public static Nanobot parse(String line) {
      Matcher matcher = NANOBOT_PATTERN.matcher(line);
      if (!matcher.matches()) {
        throw new IllegalArgumentException(line + " is not a valid nanobot");
      }

      return new Nanobot(
          new Position(
              Long.parseLong(matcher.group(1)),
              Long.parseLong(matcher.group(2)),
              Long.parseLong(matcher.group(3))
          ),
          Long.parseLong(matcher.group(4))
      );
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Nanobot nanobot = (Nanobot) o;
      return range == nanobot.range &&
          Objects.equal(position, nanobot.position);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(position, range);
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("position", position)
          .add("range", range)
          .toString();
    }
  }

  public static long mostBotsInRange(ImmutableList<Nanobot> nanobots) {
    Nanobot longestRange = nanobots.stream()
        .max(Comparator.comparing(nanobot -> nanobot.range))
        .orElseThrow(() -> new IllegalArgumentException("No nanobots"));

    return nanobots.stream()
        .filter(longestRange::inRange)
        .count();
  }

  public static void main(String[] args) throws IOException {
    File file = new File(Day23.class.getResource("/day23.txt").getFile());
    ImmutableList<Nanobot> nanobots = Files.readLines(file, Charsets.UTF_8).stream()
        .map(Nanobot::parse)
        .collect(ImmutableList.toImmutableList());

    // Part 1: how many nanobots are in range of the nanobot with the largest signal radius?
    System.out.println("Part 1: " + mostBotsInRange(nanobots));
  }
}
