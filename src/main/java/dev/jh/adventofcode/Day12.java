package dev.jh.adventofcode;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.IntStream;

public class Day12 {

  public static class Rule {
    public final boolean[] pattern;
    public final boolean result;

    private Rule(boolean[] pattern, boolean result) {
      this.pattern = pattern;
      this.result = result;
    }

    /**
     * Returns whether this rule applies to the plants around the given index.  Check result on this rule
     * to determine whether there's a plant in the pot in the next generation if this rule matches.
     *
     * @param plants Array of plants.  Pots outside the bounds of the array don't contain plants.
     * @param center Index of the center of this rule.  Rules always have 5 plants, so this is the plant at index 2.
     * @return Whether this rule applies to the plant at the center position in the list of plants.
     */
    public boolean matches(boolean[] plants, int center) {
      for (int i = 0; i < pattern.length; i ++) {
        // Out of bounds pots are not filled with plants, but still match against the pattern.
        int plantIndex = i - 2 + center;
        boolean plant = (plantIndex >= 0 && plantIndex < plants.length) && plants[plantIndex];

        if (plant != pattern[i]) {
          return false;
        }
      }

      return true;
    }

    /**
     * Parses the given string into a rule.  Rule strings must look like ##.## => #, where each plant character
     * is either '.' for no plant or '#' for a plant.  The matching portion of the rule must have length 5.
     *
     * @param line Line to parse
     * @return Rule parsed from the given line.
     */
    public static Rule parse(String line) {
      // No invalid input checking on the line - expect it to have the form '..#.. => #'
      boolean[] pattern = new boolean[5];

      for (int i = 0; i < 5; i ++) {
        pattern[i] = line.charAt(i) == '#';
      }

      boolean result = line.charAt(9) == '#';

      return new Rule(pattern, result);
    }
  }

  public static class Plants {
    public final boolean[] plants;
    public final int zeroOffset;

    public Plants(boolean[] plants, int zeroOffset) {
      this.plants = plants;
      this.zeroOffset = zeroOffset;
    }

    /**
     * Advances this row of plants one generation according to the rules, returning a new Plants.
     *
     * @param rules List of rules that govern which pots contain plants in the next generation.
     * @return New plants which represents the new generation.
     */
    public Plants tick(ImmutableList<Rule> rules) {
      // Based on the rules, the pots _can_ (but don't have to) expand by one pot in either direction each tick.
      boolean[] newPlants = new boolean[plants.length + 2];

      for (int i = 0; i < newPlants.length; i ++) {
        // Subtract 1 to go from the new plant indexes to the old.
        newPlants[i] = newPlant(rules, i - 1);
      }

      // Contract the plants to have a plant on either end.
      int firstPlant = firstPlant(newPlants);
      if (firstPlant == -1) {
        return new Plants(new boolean[]{}, 0);
      }

      int lastPlant = lastPlant(newPlants);

      // Add 1 since the newPlants expands plants by 1.
      boolean[] contractedPlants = Arrays.copyOfRange(newPlants, firstPlant, lastPlant + 1);
      return new Plants(contractedPlants, zeroOffset - firstPlant + 1);
    }

    /**
     * Finds the rule that matches the plant at the center index.  Indexes are in terms of plants.
     * Returns whether the center will have a plant in the next tick.
     *
     * @param rules List of rules to search through
     * @param center Index of the plant in question.
     * @return Whether the pot in the center index will have a plant in the next tick.
     */
    private boolean newPlant(ImmutableList<Rule> rules, int center) {
      return rules.stream()
          .filter(rule -> rule.matches(plants, center))
          .findFirst()
          .map(rule -> rule.result)
          .orElse(false);
    }

    /**
     * Returns the index of the first plant in the given list of plants.
     *
     * @return Index of the first plant, or -1 if none of the pots contain plants.
     */
    private int firstPlant(boolean[] plants) {
      for (int i = 0; i < plants.length; i ++) {
        if (plants[i]) {
          return i;
        }
      }

      return -1;
    }

    /**
     * Returns the index of the last plant in the given list of plants.
     *
     * @return Index of the last plant, or -1 if none of the pots contain plants.
     */
    private int lastPlant(boolean[] plants) {
      for (int i = plants.length - 1; i >= 0; i --) {
        if (plants[i]) {
          return i;
        }
      }

      return -1;
    }

    /**
     * Adds up the numbers of pots that contain a plant.
     *
     * @return Sum of the number of plant containing pots.
     */
    public int count() {
      int count = 0;

      for (int i = 0; i < plants.length; i ++) {
        if (plants[i]) {
          count += (i - zeroOffset);
        }
      }

      return count;
    }

    /**
     * Parses a line (starting with 'initial state: ') into plants.  The leftmost plant starts as plant 0.
     *
     * @param line Line to parse
     * @return Plants corresponding to the line.
     */
    public static Plants parse(String line) {
      char[] pots = line.replace("initial state: ", "").toCharArray();

      boolean[] plants = new boolean[pots.length];
      for (int i = 0; i < pots.length; i ++) {
        plants[i] = pots[i] == '#';
      }

      return new Plants(plants, 0);
    }

    @Override
    public String toString() {
      StringBuilder bldr = new StringBuilder();

      for (boolean plant : plants) {
        bldr.append(plant ? '#' : '.');
      }

      return bldr.toString();
    }
  }

  private static class PlantGeneration {
    public final Plants plants;
    public final long generation;

    public PlantGeneration(Plants plants, long generation) {
      this.plants = plants;
      this.generation = generation;
    }
  }

  /**
   * Returns the sum of pots with plants after the given number of generations.
   *
   * @param initialPlants Starting position of the plants
   * @param rules List of rules to apply to the plants
   * @param generations Number of generations to simulate
   * @return Sum of the positions of pots with plants after the generations
   */
  public static int count(Plants initialPlants, ImmutableList<Rule> rules, long generations) {
    Plants plants = initialPlants;
    long generation = 0;

    PlantGeneration cycleStart;
    PlantGeneration cycleEnd;

    // Map of plant positions to the generation where those positions happened.
    Map<boolean[], PlantGeneration> previousGenerations = new HashMap<>();

    // Find a cycle where the plants aligned.  The plants can shift together - zeroOffset doesn't have to match.
    while (generation < generations) {
      plants = plants.tick(rules);
      generation ++;

      PlantGeneration currentGeneration = new PlantGeneration(plants, generation);
      PlantGeneration previousGeneration = previousGenerations.get(plants.plants);
      if (previousGeneration == null) {
        previousGenerations.put(plants.plants, currentGeneration);
      } else {
        cycleStart = previousGeneration;
        cycleEnd = currentGeneration;
        break;
      }
    }

    // Finish off the generations.
    while (generation < generations) {
      plants.tick(rules);
      generation ++;
    }

    // Return the count.
    return plants.count();
  }

  public static void main(String[] args) throws IOException {
    File file = new File(Day12.class.getResource("/day12.txt").getFile());
    List<String> lines = Files.readLines(file, Charsets.UTF_8);

    Plants initialPlants = Plants.parse(lines.get(0));
    ImmutableList<Rule> rules = IntStream.range(2, lines.size())
        .mapToObj(i -> Rule.parse(lines.get(i)))
        .collect(ImmutableList.toImmutableList());

    // Part 1: after 20 generations, what is the sum of the numbers of all pots that contain a plant?
//    System.out.println("Part 1: " + count(initialPlants, rules, 20));

    // Part 2: after 50 billion generations, what is the sum of numbers of pots that contain a plant?
    System.out.println("Part 2: " + count(initialPlants, rules, 50000000000L));
  }
}
