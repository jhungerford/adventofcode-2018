package dev.jh.adventofcode;

import com.google.common.base.Charsets;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;

import java.io.File;

import static java.lang.Character.toLowerCase;

public class Day5 {

  /**
   * Result of reacting an original polymer with a single unit removed, with the unit that was removed.
   */
  public static class ShortestPolymer {
    public final String polymer;
    public final char unit;

    public ShortestPolymer(String polymer, char unit) {
      this.polymer = polymer;
      this.unit = unit;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      ShortestPolymer that = (ShortestPolymer) o;
      return unit == that.unit &&
          Objects.equal(polymer, that.polymer);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(polymer, unit);
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("polymer", polymer)
          .add("unit", unit)
          .toString();
    }
  }

  /**
   * Returns a set of unique units in the polymer.  Polarity doesn't matter for part 2, so all units will be lowercase.
   *
   * @param polymer Polymer to extract units from
   * @return Set of units in the polymer, converted to lowercase
   */
  public static ImmutableSet<Character> allUnits(String polymer) {
    return polymer.chars()
        .mapToObj(i -> (char) i)
        .map(Character::toLowerCase)
        .collect(ImmutableSet.toImmutableSet());
  }

  /**
   * Removes all units of the given type from the polymer regardless of polarity.
   *
   * @param polymer Original polymer
   * @param unit Unit to remove from the polymer, regardless of polarity
   * @return Polymer without the given unit
   */
  public static String removeUnit(String polymer, char unit) {
    StringBuilder bldr = new StringBuilder(polymer.length());

    char lowerUnit = Character.toLowerCase(unit);
    char upperUnit = Character.toUpperCase(unit);

    polymer.chars()
        .filter(i -> i != lowerUnit && i != upperUnit)
        .forEach(i -> bldr.append((char) i));

    return bldr.toString();
  }

  /**
   * Reacts the given polymer by removing adjacent pairs of the same unit (letter)
   * with different polarities (upper/lower casing).
   *
   * @param polymer Polymer to react
   * @return Resulting polymer with all reacting units removed.
   */
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

        // Reaction - letters match, but their polarity (casing) doesn't.
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

  /**
   * Returns the shortest polymer formed by removing a unit from the given polymer and reacting it.
   *
   * @param polymer Polymer to react
   * @return Shortest polymer resulting from reacting a polymer with a unit removed.
   */
  public static ShortestPolymer shortestWithUnitRemoved(String polymer) {
    ShortestPolymer shortest = null;

    for (char unit : allUnits(polymer)) {
      ShortestPolymer current = new ShortestPolymer(react(removeUnit(polymer, unit)), unit);

      if (shortest == null || current.polymer.length() < shortest.polymer.length()) {
        shortest = current;
      }
    }

    return shortest;
  }

  public static void main(String[] args) throws Exception {
    File file = new File(Day4.class.getResource("/day5.txt").getFile());
    String polymer = ImmutableList.copyOf(Files.readLines(file, Charsets.UTF_8)).get(0);

    // Part 1: how many units remain after reacting the polymer?
    System.out.println("Part 1: " + react(polymer).length());

    // Part 2: what's the shortest possible polymer formed by removing one unit and reacting?
    ShortestPolymer shortest = shortestWithUnitRemoved(polymer);
    System.out.println("Part 2: " + shortest.polymer.length() + " by removing " + shortest.unit);
  }
}
