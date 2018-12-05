package dev.jh.adventofcode;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;

import java.io.File;

import static java.lang.Character.toLowerCase;

public class Day5 {

  public static String react(String polymer) {
    boolean reacted = true;
    String current = polymer;

    // Keep traversing the remaining units of the polymer until no more units react in a single pass.
    while (reacted) {
      StringBuilder remaining = new StringBuilder(current.length());
      reacted = false;

      for (int i = 0; i < current.length(); i ++) {
        char currentChar = current.charAt(i);

        // Last character - previous didn't react with it, so it stays
        if (i == current.length() - 1) {
          remaining.append(currentChar);
          continue;
        }

        char nextChar = current.charAt(i + 1);

        if (toLowerCase(currentChar) == toLowerCase(nextChar) && currentChar != nextChar) {
          reacted = true;
          i ++;
        } else {
          remaining.append(currentChar);
        }
      }

      current = remaining.toString();
    }

    return current;
  }

  public static void main(String[] args) throws Exception {
    File file = new File(Day4.class.getResource("/day5.txt").getFile());
    String polymer = ImmutableList.copyOf(Files.readLines(file, Charsets.UTF_8)).get(0);

    // Part 1: how many units remain after reacting the polymer?
    System.out.println("Part 1: " + react(polymer).length());
  }
}
