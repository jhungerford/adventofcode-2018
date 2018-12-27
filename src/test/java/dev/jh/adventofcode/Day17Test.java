package dev.jh.adventofcode;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class Day17Test {

  @Test
  public void exampleSquares() {
    ImmutableList<String> lines = ImmutableList.of(
        "x=495, y=2..7",
        "y=7, x=495..501",
        "x=501, y=3..7",
        "x=498, y=2..4",
        "x=506, y=1..2",
        "x=498, y=10..13",
        "x=504, y=10..13",
        "y=13, x=498..504"
    );

    Day17.Grid grid = Day17.Grid.parseLines(lines);

    assertThat(grid.squares()).isEqualTo(57);
  }

  @Test
  public void containedWell() {
    /*
    ...........+........
    .#......#..|........
    .#......#..|......#.
    .#......#..|......#.
    .#......#..|......#.
    .#.........|......#.
    .#...|||||||||||||#.
    .#...|#~#~~~~~~~~~#.
    .#...|#~#~~~~~~~~~#.
    .#~~~~###~~~~~~~~~#.
    .#~~~~~~~~~~~~~~~~#.
    .#~~~~~~~~~~~~~~~~#.
    .#~~~~~~~~~~~~~~~~#.
    .#~~~~~~~~~~~~~~~~#.
    .##################.
     */

    ImmutableList<String> lines = ImmutableList.of(
        "x=490, y=1..13",
        "x=507, y=2..13",
        "y=14, x=490..507",
        "x=495, y=7..8",
        "x=497, y=7..8",
        "y=9, x=495..497",
        "x=497, y=1..4"
    );

    Day17.Grid grid = Day17.Grid.parseLines(lines);

    assertThat(grid.squares()).isEqualTo(188);
  }

  @Test
  public void notWalledBySpill() {
    /*
    .....+.....
    ...........
    ...#.#.....
    ...###.....
    .#.........
    .#.......#.
    .#.......#.
    .#########.
     */

    ImmutableList<String> lines = ImmutableList.of(
        "x=498, y=2..2",
        "x=500, y=2..2",
        "y=3, x=498..500",
        "x=496, y=4..6",
        "x=504, y=5..6",
        "y=7, x=496..504"
    );

    Day17.Grid grid = Day17.Grid.parseLines(lines);

    assertThat(grid.squares()).isEqualTo(0);
  }
}