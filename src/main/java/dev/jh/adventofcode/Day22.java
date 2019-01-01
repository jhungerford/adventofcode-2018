package dev.jh.adventofcode;

import com.google.common.base.MoreObjects;

public class Day22 {

  public enum Erosion {
    ROCKY('.', 0),
    WET('=', 1),
    NARROW('|', 2);

    public final char name;
    public final int risk;

    Erosion(char name, int risk) {
      this.name = name;
      this.risk = risk;
    }

    public static Erosion fromErosionLevel(int erosionLevel) {
      switch (erosionLevel % 3) {
        case 0: return ROCKY;
        case 1: return WET;
        case 2: return NARROW;
        default: throw new IllegalStateException("Geologic index wasn't mod 3.");
      }
    }

    public static Erosion fromName(char name) {
      if (name == 'M' || name == 'T') {
        return ROCKY;
      }

      for (Erosion erosion : values()) {
        if (erosion.name == name) {
          return erosion;
        }
      }

      throw new IllegalArgumentException(name + " is not a valid erosion.");
    }
  }

  public static class Cave {
    public final int targetX;
    public final int targetY;
    public final int depth;
    public final Erosion[][] erosion;

    public Cave(int targetX, int targetY, int depth) {
      this.targetX = targetX;
      this.targetY = targetY;
      this.depth = depth;

      this.erosion = calculateErosion(targetX, targetY, depth);
    }

    private Erosion[][] calculateErosion(int targetX, int targetY, int depth) {
      Erosion[][] erosion = new Erosion[targetY + targetX][targetX + 1];
      int[][] erosionLevels = new int[erosion.length][erosion[0].length];

      for (int diagonalY = 0; diagonalY < erosion.length + erosion[0].length; diagonalY ++) {
        for (int y = diagonalY >= erosion.length ? erosion.length - 1 : diagonalY; y >= 0; y --) {
          int x = diagonalY - y;

          if (x < erosion[y].length) {
            int erosionLevel;
            if ((x == 0 && y == 0) || x == targetX && y == targetY) {
              erosionLevel = depth % 20183;
            } else if (y == 0) {
              erosionLevel = (x * 16807 + depth) % 20183;
            } else if (x == 0) {
              erosionLevel = (y * 48271 + depth) % 20183;
            } else {
              erosionLevel = (erosionLevels[y - 1][x] * erosionLevels[y][x - 1] + depth) % 20183;
            }

            erosionLevels[y][x] = erosionLevel;
            erosion[y][x] = Erosion.fromErosionLevel(erosionLevel);
          }
        }
      }

      return erosion;
    }

    public int risk() {
      int risk = 0;
      for (int y = 0; y <= targetY; y ++) {
        for (int x = 0; x <= targetX; x ++) {
          risk += erosion[y][x].risk;
        }
      }
      return risk;
    }

    @Override
    public String toString() {
      StringBuilder bldr = new StringBuilder();

      for (int y = 0; y < erosion.length; y ++) {
        bldr.append('\n');
        for (int x = 0; x < erosion[y].length; x ++) {
          bldr.append(erosion[y][x].name);
        }
      }

      return MoreObjects.toStringHelper(this)
          .add("targetX", targetX)
          .add("targetY", targetY)
          .add("depth", depth)
          .add("erosion", bldr.toString())
          .toString();
    }
  }

  public static void main(String[] args) {
    int targetX = 8;
    int targetY = 701;
    int caveDepth = 5913;

    Cave cave = new Cave(targetX, targetY, caveDepth);

    // Part 1: what is the total risk level for the rectangle from 0,0 to the target?
    System.out.println("Part 1: " + cave.risk());

  }
}
