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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day23 {

  public static class Position {
    public static final Position ORIGIN = new Position(0, 0, 0);

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



  private static ImmutableMap<Nanobot, ImmutableSet<Nanobot>> buildVisibilityMap(ImmutableList<Nanobot> nanobots) {
    Map<Nanobot, Set<Nanobot>> map = new HashMap<>();

    for (int a = 0; a < nanobots.size(); a ++) {
      for (int b = a + 1; b < nanobots.size(); b ++) {
        Nanobot nanobotA = nanobots.get(a);
        Nanobot nanobotB = nanobots.get(b);

        if (nanobotA.inRange(nanobotB)) {
          map.computeIfAbsent(nanobotA, (key) -> new HashSet<>()).add(nanobotB);
        }

        if (nanobotB.inRange(nanobotA)) {
          map.computeIfAbsent(nanobotB, (key) -> new HashSet<>()).add(nanobotA);
        }
      }
    }

    return map.entrySet().stream().collect(ImmutableMap.toImmutableMap(
        Map.Entry::getKey,
        entry -> ImmutableSet.copyOf(entry.getValue())
    ));
  }

  public static long mostNanobotsDistance(ImmutableList<Nanobot> nanobots) {
    // Build up a graph of nanobots that can see each other
    ImmutableMap<Nanobot, ImmutableSet<Nanobot>> visibilityMap = buildVisibilityMap(nanobots);

    // Bron-Kerbosch to identify the maximal clique?
    // Graph is directed, so doesn't apply - pick the bot that can see the most nodes
    ImmutableSet<Nanobot> mostVisible = visibilityMap.values().stream()
        .max(Comparator.comparing(bots -> bots.size()))
        .orElseThrow(() -> new IllegalStateException("No nanobots in the visibility map."));

    // Furthest nanobot sphere - closest point to the origin on it's sphere is the answer
    return mostVisible.stream()
        .mapToLong(nanobot -> nanobot.position.distance(Position.ORIGIN) - nanobot.range)
        .max()
        .orElseThrow(() -> new IllegalStateException("No nanobots in the maximal clique."));
  }

  public static void main(String[] args) throws IOException {
    File file = new File(Day23.class.getResource("/day23.txt").getFile());
    ImmutableList<Nanobot> nanobots = Files.readLines(file, Charsets.UTF_8).stream()
        .map(Nanobot::parse)
        .collect(ImmutableList.toImmutableList());

    // Part 1: how many nanobots are in range of the nanobot with the largest signal radius?
    System.out.println("Part 1: " + mostBotsInRange(nanobots));

    // Part 2: what is the shortest manhattan distance from the origin to the position
    // in the radius of the largest number of nanobots?
    System.out.println("Part 2: " + mostNanobotsDistance(nanobots));
  }
}
