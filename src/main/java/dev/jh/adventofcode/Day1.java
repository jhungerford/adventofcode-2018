package dev.jh.adventofcode;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Day1 {

  public static int parseFrequency(String str) {
    return Integer.parseInt(str);
  }

  public static int frequencyAfter(List<String> lines) {
    return lines.stream()
        .mapToInt(Day1::parseFrequency)
        .sum();
  }

  public static int firstFrequencyTwice(List<String> lines) {
    Set<Integer> seen = new HashSet<>();
    int current = 0;

    while (true) { // Input is crafted to guarantee a repeat - keep looping until we find one.
      for (String line : lines) {
        seen.add(current);
        current += parseFrequency(line);

        if (seen.contains(current)) {
          return current;
        }
      }
    }
  }

  public static void main(String[] args) throws Exception {
    File file = new File(Day1.class.getResource("/day1.txt").getFile());
    List<String> lines = Files.readLines(file, Charsets.UTF_8);

    // Starting with 0, figure out the frequency after summing the input
    int part1 = frequencyAfter(lines);
    // Starting with 0 and looping through the file as many times as required, find the frequency that repeats first.
    int part2 = firstFrequencyTwice(lines);

    System.out.println("Part 1: " + part1);
    System.out.println("Part 2: " + part2);
  }
}
