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
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

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

  public static void main(String[] args) throws Exception {
    File file = new File(Day1.class.getResource("/day2.txt").getFile());
    List<String> lines = Files.readLines(file, Charsets.UTF_8);

    ImmutableList<RepeatedLetters> repeatedLettersList = lines.stream()
        .map(Day2::letterCounts)
        .map(Day2::uniqueLetterRepeats)
        .collect(ImmutableList.toImmutableList());

    // Part 1: compute a checksum by multiplying the number of box ids with 2 letters repeated * 3 letters repeated
    int part1 = computeChecksum(repeatedLettersList);

    System.out.println("Part 1: " + part1);
  }
}
