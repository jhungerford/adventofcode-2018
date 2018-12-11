package dev.jh.adventofcode;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

public class Day11 {

  public static class Point {
    public final int x;
    public final int y;
    public final int size;
    public final int power;

    public Point(int x, int y, int size, int power) {
      this.x = x;
      this.y = y;
      this.size = size;
      this.power = power;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Point point = (Point) o;
      return x == point.x &&
          y == point.y &&
          size == point.size &&
          power == point.power;
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(x, y, size, power);
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("x", x)
          .add("y", y)
          .add("size", size)
          .add("power", power)
          .toString();
    }
  }

  /**
   * Returns the digit in the hundreds place in the given number, or 0 if the number is less than one hundred.
   *
   * @param number Number
   * @return Digit in the hundreds place in the number
   */
  public static int hundredsDigit(int number) {
    return (number / 100) % 10;
  }

  /**
   * Calculates the values of a 300x300 grid with the given serial number.
   *
   * @param serialNumber Serial number of the grid
   * @return 300x300 grid.
   */
  public static int[][] grid(int serialNumber) {
    int[][] grid = new int[300][300];

    for (int y = 1; y <= grid.length; y ++) {
      for (int x = 1; x <= grid[y - 1].length; x ++) {
        int rackId = (x + 10);
        int power = rackId * (rackId * y + serialNumber);

        grid[y - 1][x - 1] = hundredsDigit(power) - 5;
      }
    }

    return grid;
  }

  /**
   * Returns the top left corner of the size x size square on the grid with the largest total power.
   *
   * @param grid Populated grid
   * @param size Size of the square
   * @return Top left corner of the square with the largest power in the grid.
   */
  public static Point largestPower(int[][] grid, int size) {
    Point maxPoint = null;

    for (int y = 0; y < grid.length - size; y ++) {
      for (int x = 0; x < grid[y].length - size; x ++) {

        int power = 0;
        for (int ySquare = 0; ySquare < size; ySquare ++) {
          for (int xSquare = 0; xSquare < size; xSquare ++) {
            power += grid[y + ySquare][x + xSquare];
          }
        }

        Point point = new Point(x + 1, y + 1, size, power);
        if (maxPoint == null || point.power > maxPoint.power) {
          maxPoint = point;
        }
      }
    }

    return maxPoint;
  }

  /**
   * Tries all cells from size 1x1 to 300x300, and returns the square with the largest total power.
   *
   * @param grid Populated grid
   * @return Top left corner of the square of any size with the largest power in the grid
   */
  public static Point largestTotalPower(int[][] grid) {
    Point maxPoint = null;

    for (int size = 1; size < grid.length; size ++) {
      Point point = largestPower(grid, size);
      if (maxPoint == null || point.power > maxPoint.power) {
        maxPoint = point;
      }
    }

    return maxPoint;
  }

  public static void main(String[] args) {
    int serialNumber = 7403;
    int[][] grid = grid(serialNumber);

    // Part 1: what is the x,y coordinate of the top-left cell of the 3x3 cell with the largest power?
    Point largestPower = largestPower(grid, 3);
    System.out.println("Part 1: " + largestPower.x + "," + largestPower.y);

    // Part 2: square size is adjustable - what's the x,y,size of the square with the largest total power?
    Point largestTotalPower = largestTotalPower(grid);
    System.out.println("Part 2: " + largestTotalPower.x + "," + largestTotalPower.y + "," + largestTotalPower.size);
  }
}
