package dev.jh.adventofcode;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class Day7Test {

  private static final Day7.Step EXAMPLE_STEPS;
  static {
    Day7.Step stepE = new Day7.Step('E', ImmutableSet.of('A', 'B', 'D', 'F'), ImmutableSet.of());

    EXAMPLE_STEPS = new Day7.Step('C', ImmutableSet.of(), ImmutableSet.of(
        new Day7.Step('A', ImmutableSet.of('C'), ImmutableSet.of(
            new Day7.Step('B', ImmutableSet.of('A'), ImmutableSet.of(stepE)),
            new Day7.Step('D', ImmutableSet.of('A'), ImmutableSet.of(stepE)),
            stepE
        )),
        new Day7.Step('F', ImmutableSet.of('C'), ImmutableSet.of(stepE))
    ));
  }

  @Test
  public void parseLines() {
    ImmutableList<String> lines = ImmutableList.of(
        "Step C must be finished before step A can begin.",
        "Step C must be finished before step F can begin.",
        "Step A must be finished before step B can begin.",
        "Step A must be finished before step D can begin.",
        "Step B must be finished before step E can begin.",
        "Step D must be finished before step E can begin.",
        "Step F must be finished before step E can begin.",
        "Step A must be finished before step E can begin."
    );

    assertThat(Day7.parseLines(lines)).isEqualTo(EXAMPLE_STEPS);
  }

  @Test
  public void order() {
    assertThat(Day7.order(EXAMPLE_STEPS)).isEqualTo("CABDFE");
  }

}