package dev.jh.adventofcode;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class Day12Test {

  private final Day12.Plants EXAMPLE_PLANTS = Day12.Plants.parse("initial state: #..#.#..##......###...###");
  private final ImmutableList<Day12.Rule> EXAMPLE_RULES = Stream.of(
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
  ).map(Day12.Rule::parse).collect(ImmutableList.toImmutableList());

  @Test
  public void plantsParse() {
    Day12.Plants plants = Day12.Plants.parse("initial state: #..#.#..##......###...###");
    assertThat(plants.zeroOffset).isEqualTo(0);
    assertThat(plants.plants).isEqualTo(new boolean[]{
        true, false, false, true, false, true, false, false, true, true, false, false, false,
        false, false, false, true, true, true, false, false, false, true, true, true
    });
  }

  @Test
  public void ruleParse() {
    Day12.Rule rule = Day12.Rule.parse("...## => #");
    assertThat(rule.pattern).isEqualTo(new boolean[]{false, false, false, true, true});
    assertThat(rule.result).isEqualTo(true);

    Day12.Rule rule2 = Day12.Rule.parse("..#.. => #");
    assertThat(rule2.pattern).isEqualTo(new boolean[]{false, false, true, false, false});
    assertThat(rule2.result).isEqualTo(true);
  }

  @Test
  public void patternMatches() {
    Day12.Rule rule1 = Day12.Rule.parse("...## => #");
    Day12.Rule rule2 = Day12.Rule.parse("..#.. => #");
    Day12.Rule rule3 = Day12.Rule.parse("###.. => #");

    assertThat(rule1.matches(EXAMPLE_PLANTS.plants, 0)).isFalse();
    assertThat(rule2.matches(EXAMPLE_PLANTS.plants, 0)).isTrue();

    assertThat(rule1.matches(EXAMPLE_PLANTS.plants, 15)).isTrue();

    assertThat(rule1.matches(EXAMPLE_PLANTS.plants, 24)).isFalse();
    assertThat(rule3.matches(EXAMPLE_PLANTS.plants, 24)).isTrue();
  }

  @Test
  public void toStringExample() {
    assertThat(EXAMPLE_PLANTS.toString()).isEqualTo("#..#.#..##......###...###");
  }

  @Test
  public void tickExample() {
    Day12.Plants tick = EXAMPLE_PLANTS.tick(EXAMPLE_RULES);
    assertThat(tick.toString()).isEqualTo(".#...#....#.....#..#..#..#.");
    assertThat(tick.zeroOffset).isEqualTo(1);
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

    assertThat(plants.count()).isEqualTo(325);
  }
}