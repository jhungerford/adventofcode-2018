package dev.jh.adventofcode;

import com.google.common.base.Charsets;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Day12 {

  public static class Plants {
    public final byte[] plants;
    public final long zeroOffset;

    public Plants(byte[] plants, long zeroOffset) {
      this.plants = plants;
      this.zeroOffset = zeroOffset;
    }

    /**
     * Advances this row of plants one generation according to the rules, returning a new Plants.
     *
     * @param rules List of rules that govern which pots contain plants in the next generation.
     * @return New plants which represents the new generation.
     */
    public Plants tick(Rules rules) {
      // Figure out if the plants array needs to be expanded / contracted based on the new edge values.
      byte leftExpand = 0;
      if ((plants[0] & 0xF0) != 0) {
        leftExpand = rules.apply((byte) 0, (byte) 0, getByte(0));
      }

      byte left = rules.apply((byte) 0, getByte(0), getByte(1));
      byte right = rules.apply(getByte(plants.length - 2), getByte(plants.length - 1), (byte) 0);

      byte rightExpand = 0;
      if ((plants[plants.length-1] & 0x01) != 0) {
        rightExpand = rules.apply(getByte(plants.length - 1), (byte) 0, (byte) 0);
      }

      // Amount to shift the left / right sides by.  Positive is expand, negative is contract.
      int leftShift = shiftAmount(leftExpand, left);
      int rightShift = shiftAmount(rightExpand, right);

      byte[] newPlants = new byte[plants.length + leftShift + rightShift];

      if (leftShift == 0) {
        newPlants[0] = left;
      } else if (leftShift == 1) {
        newPlants[0] = leftExpand;
        newPlants[1] = left;
      }

      if (rightShift == 0) {
        newPlants[newPlants.length - 1] = right;
      } else if (rightShift == 1) {
        newPlants[newPlants.length - 1] = rightExpand;
        newPlants[newPlants.length - 2] = right;
      }

      for (int i = 1; i < plants.length - 1; i ++) {
        newPlants[i + leftShift] = rules.apply(getByte(i - 1), getByte(i), getByte(i + 1));
      }

      return new Plants(newPlants, zeroOffset + leftShift * 8);
    }

    private byte getByte(int index) {
      if (index < 0 || index >= plants.length) {
        return 0;
      }

      return plants[index];
    }

    private int shiftAmount(int expand, int current) {
      if (expand != 0) {
        return 1;
      }

      if (current == 0) {
        return -1;
      }

      return 0;
    }

    /**
     * Adds up the numbers of pots that contain a plant.
     *
     * @return Sum of the number of plant containing pots.
     */
    public long count() {
      long count = 0;

      for (int i = 0; i < plants.length; i ++) {
        for (int bit = 0; bit < 8; bit ++) {
          if ((plants[i] & 1 << (7-bit)) > 0) {
            count += i * 8 + bit - zeroOffset;
          }
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

      byte[] plants = new byte[(pots.length + 7) / 8];

      byte currentByte = 0;
      for (int bit = 0; bit < pots.length; bit ++) {
        int shift = (7 - bit % 8);

        currentByte |= (pots[bit] == '#' ? 1 : 0) << shift;

        if (shift == 0) {
          plants[bit / 8] = currentByte;
          currentByte = 0;
        }
      }

      plants[plants.length - 1] = currentByte;

      return new Plants(plants, 0);
    }

    @Override
    public String toString() {
      StringBuilder plantsString = new StringBuilder();

      for (int i = 0; i < plants.length; i ++) {
        for (int bit = 7; bit >= 0; bit --) {
          plantsString.append((plants[i] & 1 << bit) == 0 ? '.' : '#');
        }
      }

      return MoreObjects.toStringHelper(this)
          .add("plants", plantsString)
          .add("zeroOffset", zeroOffset)
          .toString();
    }
  }

  public static class Rules {
    // Index: 0b0000PPBYTEPP, Value: resulting byte - PP is padding, BYTE is 8 bits of plant data.
    private final byte[] rules;

    public Rules(byte[] rules) {
      this.rules = rules;
    }

    public byte apply(byte left, byte center, byte right) {
      int ruleIndex = ((left & 0x03) << 10)
          | ((center & 0xFF) << 2)
          | ((right >> 6) & 0x03);

      return rules[ruleIndex];
    }

    public static Rules parse(ImmutableList<String> lines) {
      // Index: 5-digit rule encoded in binary, Value: 0 or 1 for the result of the rule
      byte[] plainRules = new byte[32];

      for (String line : lines) {
        int encodedRule = 0;

        for (int i = 0; i < 5; i ++) {
          if (line.charAt(i) == '#') {
            encodedRule |= (1 << (4 - i));
          }
        }

        plainRules[encodedRule] = (byte) (line.charAt(9) == '#' ? 1 : 0);
      }

      // Expand the plain rules into all of the possible 12-digit values
      byte[] rules = new byte[4096];
      for (int rule = 0; rule < rules.length; rule ++) {
        byte result = 0;

        for (int shift = 0; shift < 8; shift ++) {
          // Mask out 5 bits of the rule, shift it into the result spot.
          result |= plainRules[(rule >>> shift) & 0x1F] << shift;
        }

        rules[rule] = result;
      }

      return new Rules(rules);
    }
  }

  private static class PlantOffsetGeneration {
    public final long zeroOffset;
    public final long generation;

    public PlantOffsetGeneration(long zeroOffset, long generation) {
      this.zeroOffset = zeroOffset;
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
  public static long count(Plants initialPlants, Rules rules, long generations) {
    Plants plants = initialPlants;
    long generation = 0;

    PlantOffsetGeneration cycleStart = null;

    // Map of plant positions to the generation where those positions happened.
    Map<BitSet, PlantOffsetGeneration> previousGenerations = new HashMap<>();

    // Find a cycle where the plants aligned.  The plants can shift together - zeroOffset doesn't have to match.
    while (generation < generations && cycleStart == null) {
      plants = plants.tick(rules);
      generation ++;

      BitSet plantsBitSet = BitSet.valueOf(plants.plants);

      PlantOffsetGeneration currentGeneration = new PlantOffsetGeneration(plants.zeroOffset, generation);
      PlantOffsetGeneration previousGeneration = previousGenerations.get(plantsBitSet);

      if (previousGeneration == null) {
        previousGenerations.put(plantsBitSet, currentGeneration);
      } else {
        cycleStart = previousGeneration;
      }
    }

    if (cycleStart != null) {
      long cycleLength = generation - cycleStart.generation;
      long numCycles = (generations - generation) / cycleLength;
      long cycleZeroOffset = plants.zeroOffset - cycleStart.zeroOffset;

      // Plant positions stay the same, but the zero offset shifts by the number of cycles.
      plants = new Plants(plants.plants, plants.zeroOffset + (cycleZeroOffset * numCycles));

      // Finish off the generations.
      generation += cycleLength * numCycles;
      while (generation < generations) {
        plants = plants.tick(rules);
        generation++;
      }
    }

    return plants.count();
  }

  public static void main(String[] args) throws IOException {
    File file = new File(Day12.class.getResource("/day12.txt").getFile());
    List<String> lines = Files.readLines(file, Charsets.UTF_8);

    Plants initialPlants = Plants.parse(lines.get(0));
    Rules rules = Rules.parse(ImmutableList.copyOf(lines.subList(2, lines.size())));

    // Part 1: after 20 generations, what is the sum of the numbers of all pots that contain a plant?
    System.out.println("Part 1: " + count(initialPlants, rules, 20));

    // Part 2: after 50 billion generations, what is the sum of numbers of pots that contain a plant?
    System.out.println("Part 2: " + count(initialPlants, rules, 50000000000L));
  }
}
