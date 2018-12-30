package dev.jh.adventofcode;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class Day18Test {

  @Test
  public void exampleValue() {
    Day18.Yard yard = Day18.Yard.parse(ImmutableList.of(
        ".#.#...|#.",
        ".....#|##|",
        ".|..|...#.",
        "..|#.....#",
        "#.#|||#|#|",
        "...#.||...",
        ".|....|...",
        "||...#|.#|",
        "|.||||..|.",
        "...#.|..|."
    ));

    assertThat(Day18.tickMinutes(yard, 10).value()).isEqualTo(1147);
    assertThat(Day18.tickMinutes(yard, 1000000000).value()).isEqualTo(0);
  }
}