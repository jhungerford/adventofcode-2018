package dev.jh.adventofcode;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class Day18 {
  public enum Acre {
    OPEN('.'),
    TREES('|'),
    LUMBERYARD('#');

    public final char name;

    Acre(char name) {
      this.name = name;
    }

    public static Acre valueOf(char c) {
      for (Acre acre : values()) {
        if (acre.name == c) {
          return acre;
        }
      }

      throw new IllegalArgumentException(c + " is not a valid acre");
    }
  }

  public static final ImmutableMap<Acre, Function<ImmutableList<Acre>, Acre>> NEXT_ACRES = ImmutableMap.<Acre, Function<ImmutableList<Acre>, Acre>>builder()
      // Open becomes filled with trees if 3+ adjacent acres contain trees.  Otherwise, stays open.
      .put(Acre.OPEN, adjacent -> adjacent.stream()
          .filter(acre -> acre == Acre.TREES)
          .count() >= 3
          ? Acre.TREES
          : Acre.OPEN)

      // Trees becomes lumberyard if 3+ adjacent acres contain lumberyards.  Otherwise, stays trees.
      .put(Acre.TREES, adjacent -> adjacent.stream()
          .filter(acre -> acre == Acre.LUMBERYARD)
          .count() >= 3
          ? Acre.LUMBERYARD
          : Acre.TREES)

      // Lumberyard stays lumberyard if it's adjacent to at least one other lumberyard and at least
      // one acre containing trees.  Otherwise, becomes open.
      .put(Acre.LUMBERYARD, adjacent -> adjacent.contains(Acre.LUMBERYARD) && adjacent.contains(Acre.TREES)
          ? Acre.LUMBERYARD
          : Acre.OPEN)

      .build();

  public static class Yard {
    public final Acre[][] acres;

    public Yard(Acre[][] acres) {
      this.acres = acres;
    }

    public Yard tick() {
      Acre[][] newAcres = new Acre[acres.length][acres[0].length];

      for (int y = 0; y < acres.length; y ++) {
        for (int x = 0; x < acres[y].length; x ++) {
          newAcres[y][x] = NEXT_ACRES.get(acres[y][x]).apply(adjacent(x, y));
        }
      }

      return new Yard(newAcres);
    }

    private ImmutableList<Acre> adjacent(int toX, int toY) {
      ImmutableList.Builder<Acre> adjacent = ImmutableList.builder();

      for (int y = toY - 1; y <= toY + 1; y ++) {
        for (int x = toX - 1; x <= toX + 1; x++) {
          if (y >= 0 && y < acres.length && x >= 0 && x < acres[y].length && (y != toY || x != toX)) {
            adjacent.add(acres[y][x]);
          }
        }
      }

      return adjacent.build();
    }

    /**
     * Returns the value of this yard, which is the number of wooded acres multiplied by the number of lumberyards.
     *
     * @return Total value of this yard.
     */
    public int value() {
      int wooded = 0;
      int lumberyards = 0;

      for (int y = 0; y < acres.length; y ++) {
        for (int x = 0; x < acres[y].length; x ++) {
          switch (acres[y][x]) {
            case TREES:
              wooded ++;
              break;
            case LUMBERYARD:
              lumberyards++;
              break;
          }
        }
      }

      return wooded * lumberyards;
    }

    /**
     * Parses the given list of lines into a yard.
     *
     * @param lines List of lines to parse
     * @return Yard in the same state as the input.
     */
    public static Yard parse(ImmutableList<String> lines) {
      Acre[][] acres = new Acre[lines.size()][lines.get(0).length()];

      for (int y = 0; y < acres.length; y ++) {
        for (int x = 0; x < acres[y].length; x ++) {
          acres[y][x] = Acre.valueOf(lines.get(y).charAt(x));
        }
      }

      return new Yard(acres);
    }

    @Override
    public String toString() {
      StringBuilder bldr = new StringBuilder();

      for (int y = 0; y < acres.length; y ++) {
        for (int x = 0; x < acres[y].length; x++) {
          bldr.append(acres[y][x].name);
        }
        bldr.append('\n');
      }

      return bldr.toString();
    }
  }

  public static Yard tickMinutes(Yard yard, int minutes) {
    Map<String, Integer> yardToMinute = new HashMap<>();

    int minute = 1;
    while (minute <= minutes && !yardToMinute.containsKey(yard.toString())) {
      yardToMinute.put(yard.toString(), minute);
      yard = yard.tick();
      minute ++;
    }

    if (minute <= minutes) {
      int cycleStart = yardToMinute.get(yard.toString());
      int cycleLength = minute - cycleStart;
      int numCycles = (minutes - minute) / cycleLength;

      minute += numCycles * cycleLength;

      while (minute <= minutes) {
        yard = yard.tick();
        minute++;
      }
    }

    return yard;
  }

  public static void main(String[] args) throws IOException {
    File file = new File(Day18.class.getResource("/day18.txt").getFile());
    ImmutableList<String> lines = ImmutableList.copyOf(Files.readLines(file, Charsets.UTF_8));

    Yard yard = Yard.parse(lines);

    // Part 1: what is the total resource value of the yard after 10 minutes?
    System.out.println("Part 1: " + tickMinutes(yard, 10).value());

    // Part 2: what is the total value after 1000000000 minutes?
    System.out.println("Part 2: " + tickMinutes(yard, 1000000000).value());
  }
}
