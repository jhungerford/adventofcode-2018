package dev.jh.adventofcode;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class Day7Test {

  @Test
  public void order() {
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

    assertThat(Day7.order(lines)).isEqualTo("CABDFE");
  }
}