package dev.jh.adventofcode;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class Day6Test {

  public static final ImmutableList<Day6.Coordinate> EXAMPLE_COORDINATES = ImmutableList.of(
      new Day6.Coordinate('a', 1, 1),
      new Day6.Coordinate('b', 1, 6),
      new Day6.Coordinate('c', 8, 3),
      new Day6.Coordinate('d', 3, 4),
      new Day6.Coordinate('e', 5, 5),
      new Day6.Coordinate('f', 8, 9)
  );

  public static final char[][] EXAMPLE_GRID = new char[][]{
      new char[]{'a', 'a', 'a', 'a', 'a', '.', 'c', 'c', 'c'},
      new char[]{'a', 'a', 'a', 'a', 'a', '.', 'c', 'c', 'c'},
      new char[]{'a', 'a', 'a', 'd', 'd', 'e', 'c', 'c', 'c'},
      new char[]{'a', 'a', 'd', 'd', 'd', 'e', 'c', 'c', 'c'},
      new char[]{'.', '.', 'd', 'd', 'd', 'e', 'e', 'c', 'c'},
      new char[]{'b', 'b', '.', 'd', 'e', 'e', 'e', 'e', 'c'},
      new char[]{'b', 'b', 'b', '.', 'e', 'e', 'e', 'e', '.'},
      new char[]{'b', 'b', 'b', '.', 'e', 'e', 'e', 'f', 'f'},
      new char[]{'b', 'b', 'b', '.', 'e', 'e', 'f', 'f', 'f'},
      new char[]{'b', 'b', 'b', '.', 'f', 'f', 'f', 'f', 'f'}
  };

  @Test
  public void parseLines() {
    ImmutableList<String> lines = ImmutableList.of(
        "1, 1",
        "1, 6",
        "8, 3",
        "3, 4",
        "5, 5",
        "8, 9"
    );

    assertThat(Day6.parseLines(lines)).isEqualTo(EXAMPLE_COORDINATES);
  }

  @Test
  public void fillGrid() {
    assertThat(Day6.fillGrid(EXAMPLE_COORDINATES)).isEqualTo(EXAMPLE_GRID);
  }

  @Test
  public void coordinateAreas() {
    ImmutableMap<Character, Integer> expected = ImmutableMap.<Character, Integer>builder()
        .put('a', Day6.INFINITE_AREA)
        .put('b', Day6.INFINITE_AREA)
        .put('c', Day6.INFINITE_AREA)
        .put('d', 9)
        .put('e', 17)
        .put('f', Day6.INFINITE_AREA)
        .build();

    assertThat(Day6.coordinateAreas(EXAMPLE_GRID)).isEqualTo(expected);
  }

  @Test
  public void distanceAway() {
    Day6.Coordinate coordinate = new Day6.Coordinate('d', 2, 3);

    assertThat(Day6.distanceAway(coordinate, 0, 8, 9)).containsExactly(new Day6.Point(coordinate.x, coordinate.y));

    assertThat(Day6.distanceAway(coordinate, 1, 8, 9)).containsExactlyInAnyOrder(
        new Day6.Point(1, 3),
        new Day6.Point(2, 2),
        new Day6.Point(3, 3),
        new Day6.Point(2, 4)
    );

    assertThat(Day6.distanceAway(coordinate, 4, 8, 9)).containsExactlyInAnyOrder(
        new Day6.Point(0, 1),
        new Day6.Point(1, 0),
        new Day6.Point(3, 0),
        new Day6.Point(4, 1),
        new Day6.Point(5, 2),
        new Day6.Point(6, 3),
        new Day6.Point(5, 4),
        new Day6.Point(4, 5),
        new Day6.Point(3, 6),
        new Day6.Point(2, 7),
        new Day6.Point(1, 6),
        new Day6.Point(0, 5)
    );
  }

  @Test
  public void largestArea() {
    assertThat(Day6.largestArea(EXAMPLE_COORDINATES)).isEqualTo(17);
  }

  @Test
  public void totalDistance() {
    assertThat(Day6.totalDistance(new Day6.Point(4, 3), EXAMPLE_COORDINATES)).isEqualTo(30);
  }

  @Test
  public void totalDistanceRegionSize() {
    assertThat(Day6.totalDistanceRegionSize(EXAMPLE_COORDINATES, 32)).isEqualTo(16);
  }
}