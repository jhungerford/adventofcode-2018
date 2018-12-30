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

    for (int minute = 0; minute < 10; minute ++) {
      yard = yard.tick();
    }

    assertThat(yard.value()).isEqualTo(1147);
  }
}