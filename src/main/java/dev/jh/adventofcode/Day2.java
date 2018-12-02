package dev.jh.adventofcode;

import com.google.common.base.Charsets;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Day2 {

  public static class RepeatedLetters {
    public final int two;
    public final int three;

    public RepeatedLetters(int two, int three) {
      this.two = two;
      this.three = three;
    }

    public static RepeatedLetters sum(RepeatedLetters a, RepeatedLetters b) {
      return new RepeatedLetters(a.two + b.two, a.three + b.three);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      RepeatedLetters that = (RepeatedLetters) o;
      return two == that.two &&
          three == that.three;
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(two, three);
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("two", two)
          .add("three", three)
          .toString();
    }
  }

  public static class CloseBoxIds {
    public final String left;
    public final String right;

    public CloseBoxIds(String left, String right) {
      this.left = left;
      this.right = right;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      CloseBoxIds that = (CloseBoxIds) o;
      return Objects.equal(left, that.left) &&
          Objects.equal(right, that.right);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(left, right);
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("left", left)
          .add("right", right)
          .toString();
    }
  }

  public static ImmutableMap<Character, Integer> letterCounts(String boxId) {
    return ImmutableMap.copyOf(boxId.chars()
        .mapToObj(i -> (char) i)
        .collect(Collectors.groupingBy(
            // Character
            Function.identity(),
            // Count, but with integer instead of long from Collectors.counting()
            Collectors.reducing(0, e -> 1, Integer::sum))));
  }

  public static RepeatedLetters uniqueLetterRepeats(Map<Character, Integer> letterCounts) {
    return new RepeatedLetters(
        letterCounts.containsValue(2) ? 1 : 0,
        letterCounts.containsValue(3) ? 1 : 0
    );
  }

  public static int computeChecksum(ImmutableList<RepeatedLetters> repeatedLettersList) {
    return repeatedLettersList.stream()
        .reduce(RepeatedLetters::sum)
        .map(times -> times.two * times.three)
        .orElse(0);
  }

  public static CloseBoxIds closeBoxIds(ImmutableList<String> boxIds) {
    ImmutableList<String> sorted = boxIds.stream().sorted().collect(ImmutableList.toImmutableList());

    for (int i = 0; i < sorted.size() - 1; i ++) {
      CloseBoxIds closeBoxIds = new CloseBoxIds(sorted.get(i), sorted.get(i + 1));

      if (commonLetters(closeBoxIds).length() == closeBoxIds.left.length() - 1) {
        return closeBoxIds;
      }
    }

    throw new IllegalArgumentException("None of the box ids were close");
  }

  public static String commonLetters(CloseBoxIds closeBoxIds) {
    if (closeBoxIds.left.length() != closeBoxIds.right.length()) {
      throw new IllegalArgumentException("Box ids must have the same length");
    }

    StringBuilder bldr = new StringBuilder();
    for (int i = 0; i < closeBoxIds.left.length(); i ++) {
      if (closeBoxIds.left.charAt(i) == closeBoxIds.right.charAt(i)) {
        bldr.append(closeBoxIds.left.charAt(i));
      }
    }

    return bldr.toString();
  }

  public static void main(String[] args) throws Exception {
    File file = new File(Day1.class.getResource("/day2.txt").getFile());
    ImmutableList<String> lines = ImmutableList.copyOf(Files.readLines(file, Charsets.UTF_8));

    ImmutableList<RepeatedLetters> repeatedLettersList = lines.stream()
        .map(Day2::letterCounts)
        .map(Day2::uniqueLetterRepeats)
        .collect(ImmutableList.toImmutableList());

    // Part 1: compute a checksum by multiplying the number of box ids with 2 letters repeated * 3 letters repeated
    int part1 = computeChecksum(repeatedLettersList);

    // Part 2: letters that are in common between the two boxes that have one letter different in the same position.
    String part2 = commonLetters(closeBoxIds(lines));

    System.out.println("Part 1: " + part1);
    System.out.println("Part 2: " + part2);
  }
}
