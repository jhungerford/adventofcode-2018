package dev.jh.adventofcode;

import com.google.common.base.Charsets;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day17 {
  public static class Grid {
    public final int xOffset;
    public final Square[][] grid;

    public Grid(int xOffset, Square[][] grid) {
      this.xOffset = xOffset;
      this.grid = grid;
    }

    /**
     * Counts the number of squares that the water can reach within the range of y values in the scan.
     *
     * @return Number of squares that water can reach.
     */
    public int squares() {
      // Point: flowing downward, settling, overflowing
      Set<MovingWater> movingWater = new HashSet<>();
      movingWater.add(new MovingWater(MovingWaterMode.FALLING, 500 - xOffset, 0));

      while (!movingWater.isEmpty()) {
        Set<MovingWater> newMovingWater = new HashSet<>();

        for (MovingWater water : movingWater) {
          if (water.mode == MovingWaterMode.FALLING) {
            // Water falling past the edge of the map falls forever - done!
            if (water.y == grid.length - 1) {
              continue;
            }

            // Look at the next square down.  If it's clay, start rising.  If it's sand, keep falling.
            Square squareBelow = grid[water.y + 1][water.x];
            if (squareBelow == Square.CLAY || squareBelow == Square.STILL_WATER) {
              newMovingWater.add(new MovingWater(MovingWaterMode.RISING, water.x, water.y));
            } else if (squareBelow == Square.SAND) {
              grid[water.y + 1][water.x] = Square.MOVING_WATER;
              newMovingWater.add(new MovingWater(MovingWaterMode.FALLING, water.x, water.y + 1));
            }

          } else if (water.mode == MovingWaterMode.RISING) {
            boolean leftSpill = false;
            int leftSide = water.x - 1;
            while (leftSide > 0) {
              if (grid[water.y][leftSide] == Square.CLAY || grid[water.y][leftSide] == Square.MOVING_WATER) {
                leftSpill = false;
                break;
              } else if (grid[water.y + 1][leftSide] == Square.CLAY && (grid[water.y + 1][leftSide - 1] != Square.CLAY || grid[water.y + 1][leftSide + 1] != Square.CLAY)) {
                leftSpill = true;
                break;
              }

              leftSide --;
            }

            boolean rightSpill = false;
            int rightSide = water.x + 1;
            while (rightSide < grid[water.y].length) {
              if (grid[water.y][rightSide] == Square.CLAY || grid[water.y][rightSide] == Square.MOVING_WATER) {
                rightSpill = false;
                break;
              } else if (grid[water.y + 1][rightSide] == Square.CLAY && (grid[water.y + 1][rightSide - 1] != Square.CLAY || grid[water.y + 1][rightSide + 1] != Square.CLAY)) {
                rightSpill = true;
                break;
              }

              rightSide ++;
            }

            Square square = (rightSpill || leftSpill) ? Square.MOVING_WATER : Square.STILL_WATER;
            for (int x = (leftSpill ? leftSide - 1 : leftSide + 1); x <= (rightSpill ? rightSide + 1 : rightSide - 1); x++) {
              grid[water.y][x] = square;
            }

            if (!(leftSpill || rightSpill)) {
              if (grid[water.y - 1][water.x] == Square.MOVING_WATER) {
                newMovingWater.add(new MovingWater(MovingWaterMode.RISING, water.x, water.y - 1));
              } else {
                for (int i = 1; true; i ++) {
                  if (water.x + i < grid[water.y].length && grid[water.y - 1][water.x + i] == Square.MOVING_WATER) {
                    newMovingWater.add(new MovingWater(MovingWaterMode.RISING, water.x + i, water.y - 1));
                    break;
                  } else if (water.x - i >= 0 && grid[water.y - 1][water.x - i] == Square.MOVING_WATER) {
                    newMovingWater.add(new MovingWater(MovingWaterMode.RISING, water.x - i, water.y - 1));
                    break;
                  }
                }
              }
            }

            if (leftSpill) {
              newMovingWater.add(new MovingWater(MovingWaterMode.FALLING, leftSide - 1, water.y));
            }

            if (rightSpill) {
              newMovingWater.add(new MovingWater(MovingWaterMode.FALLING, rightSide + 1, water.y));
            }
          }
        }

        movingWater = newMovingWater;
      }

      int count = 0;
      for (int y = 0; y < grid.length; y ++) {
        for (int x = 0; x < grid[y].length; x ++) {
          if (grid[y][x] == Square.MOVING_WATER || grid[y][x] == Square.STILL_WATER) {
            count++;
          }
        }
      }

      return count;
    }

    /**
     * Parses the given list of scan lines into a grid.
     *
     * @param lines Lines with x and y extends of clay
     * @return Grid
     */
    public static Grid parseLines(ImmutableList<String> lines) {
      ImmutableList<ClayVein> clayVeins = lines.stream()
          .map(ClayVein::parseLine)
          .collect(ImmutableList.toImmutableList());

      ClayVein extent = extent(clayVeins);

      // Spring starts at x=500, y=0.
      int xOffset = extent.lineStart - 1;
      Square[][] grid = new Square[extent.fixed + 1][extent.lineEnd - extent.lineStart + 3];

      for (int y = 0; y < grid.length; y ++) {
        for (int x = 0; x < grid[y].length; x ++) {
          grid[y][x] = Square.SAND;
        }
      }

      grid[0][500 - xOffset] = Square.SPRING;

      for (ClayVein vein : clayVeins) {
        if (vein.direction == VeinDirection.HORIZONTAL) {
          for (int x = vein.lineStart - xOffset; x <= vein.lineEnd - xOffset; x ++) {
            grid[vein.fixed][x] = Square.CLAY;
          }
        } else {
          for (int y = vein.lineStart; y <= vein.lineEnd; y ++) {
            grid[y][vein.fixed - xOffset] = Square.CLAY;
          }
        }
      }

      return new Grid(xOffset, grid);
    }

    private static ClayVein extent(ImmutableList<ClayVein> clayVeins) {
      int minX = 500;
      int maxX = 500;
      int maxY = 0;
      for (ClayVein vein : clayVeins) {
        if (vein.direction == VeinDirection.HORIZONTAL) {
          if (vein.lineEnd < minX) {
            minX = vein.lineEnd;
          }

          if (vein.lineEnd > maxX) {
            maxX = vein.lineEnd;
          }

          if (vein.fixed > maxY) {
            maxY = vein.fixed;
          }
        } else {
          if (vein.lineEnd > maxY) {
            maxY = vein.lineEnd;
          }

          if (vein.fixed < minX) {
            minX = vein.fixed;
          }

          if (vein.fixed > maxX) {
            maxX = vein.fixed;
          }
        }
      }

      return new ClayVein(VeinDirection.VERTICAL, maxY, minX, maxX);
    }

    @Override
    public String toString() {
      StringBuilder bldr = new StringBuilder();

      for (int y = 0; y < grid.length; y ++) {
        for (int x = 0; x < grid[y].length; x ++) {
          bldr.append(grid[y][x].name);
        }
        bldr.append('\n');
      }

      return MoreObjects.toStringHelper(this)
          .add("xOffset", xOffset)
          .add("grid", '\n' + bldr.toString())
          .toString();
    }
  }

  public enum VeinDirection {
    HORIZONTAL, VERTICAL
  }

  public static class ClayVein {
    private static Pattern pattern = Pattern.compile("^([xy])=(\\d+), [xy]=(\\d+)..(\\d+)$");

    public final VeinDirection direction;
    public final int fixed;
    public final int lineStart;
    public final int lineEnd;

    public ClayVein(VeinDirection direction, int fixed, int lineStart, int lineEnd) {
      this.direction = direction;
      this.fixed = fixed;
      this.lineStart = lineStart;
      this.lineEnd = lineEnd;
    }

    public static ClayVein parseLine(String line) {
      Matcher matcher = pattern.matcher(line);
      if (!matcher.matches()) {
        throw new IllegalArgumentException(line + " is not a valid clay vein");
      }

      return new ClayVein(
          "x".equals(matcher.group(1)) ? VeinDirection.VERTICAL : VeinDirection.HORIZONTAL,
          Integer.parseInt(matcher.group(2)),
          Integer.parseInt(matcher.group(3)),
          Integer.parseInt(matcher.group(4))
      );
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("direction", direction)
          .add("fixed", fixed)
          .add("lineStart", lineStart)
          .add("lineEnd", lineEnd)
          .toString();
    }
  }

  public enum Square {
    SAND('.'),
    CLAY('#'),
    SPRING('+'),
    STILL_WATER('~'),
    MOVING_WATER('|');

    public final char name;

    Square(char name) {
      this.name = name;
    }
  }

  public enum MovingWaterMode {
    FALLING, RISING
  }

  public static class MovingWater {
    public final MovingWaterMode mode;
    public final int x;
    public final int y;

    public MovingWater(MovingWaterMode mode, int x, int y) {
      this.mode = mode;
      this.x = x;
      this.y = y;
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("mode", mode)
          .add("x", x)
          .add("y", y)
          .toString();
    }
  }

  public static void main(String[] args) throws IOException {
    File file = new File(Day17.class.getResource("/day17.txt").getFile());
    ImmutableList<String> lines = ImmutableList.copyOf(Files.readLines(file, Charsets.UTF_8));

    // Part 1: how many tiles can the water reach?
    // TODO: 56977 is too high
    Grid grid = Grid.parseLines(lines);
    System.out.println("Part 1: " + grid.squares());

    System.out.println("Here");
  }
}
