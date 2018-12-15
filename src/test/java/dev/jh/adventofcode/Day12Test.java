package dev.jh.adventofcode;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class Day12Test {

  private final Day12.Plants EXAMPLE_PLANTS = Day12.Plants.parse("initial state: #..#.#..##......###...###");
  private final Day12.Rules EXAMPLE_RULES = Day12.Rules.parse(ImmutableList.of(
      "...## => #",
      "..#.. => #",
      ".#... => #",
      ".#.#. => #",
      ".#.## => #",
      ".##.. => #",
      ".#### => #",
      "#.#.# => #",
      "#.### => #",
      "##.#. => #",
      "##.## => #",
      "###.. => #",
      "###.# => #",
      "####. => #"
  ));

  @Test
  public void plantsParse() {
    assertThat(EXAMPLE_PLANTS.toString()).isEqualTo("Plants{plants=#..#.#..##......###...###......., zeroOffset=0}");
  }

  @Test
  public void tickExample() {
    Day12.Plants tick = EXAMPLE_PLANTS.tick(EXAMPLE_RULES);
    assertThat(tick.toString()).isEqualTo("Plants{plants=#...#....#.....#..#..#..#......., zeroOffset=0}");
  }

  @Test
  public void exampleCount() {
    assertThat(EXAMPLE_PLANTS.count()).isEqualTo(145);
  }

  @Test
  public void exampleCount20Generations() {
    Day12.Plants plants = EXAMPLE_PLANTS;
    for (int generation = 0; generation < 20; generation ++) {
      plants = plants.tick(EXAMPLE_RULES);
    }

    assertThat(plants.toString()).isEqualTo("Plants{plants=......#....##....#####...#######....#.#..##....., zeroOffset=8}");
    assertThat(plants.count()).isEqualTo(325);

    assertThat(Day12.count(EXAMPLE_PLANTS, EXAMPLE_RULES, 20)).isEqualTo(325);
  }

  @Test
  public void exampleCountFiveBillionGenerations() {
    assertThat(Day12.count(EXAMPLE_PLANTS, EXAMPLE_RULES, 5000000000L)).isEqualTo(99999999374L);
  }
}