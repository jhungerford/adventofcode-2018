package dev.jh.adventofcode;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class Day7Test {

  private static final ImmutableList<String> LINES = ImmutableList.of(
      "Step C must be finished before step A can begin.",
      "Step C must be finished before step F can begin.",
      "Step A must be finished before step B can begin.",
      "Step A must be finished before step D can begin.",
      "Step B must be finished before step E can begin.",
      "Step D must be finished before step E can begin.",
      "Step F must be finished before step E can begin.",
      "Step A must be finished before step E can begin."
  );

  @Test
  public void order() {
    assertThat(Day7.order(Day7.parseLines(LINES))).isEqualTo("CABDFE");
  }

  @Test
  public void time() {
    assertThat(Day7.time(Day7.parseLines(LINES), 2)).isEqualTo(258);
  }
}