package dev.jh.adventofcode;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class Day19Test {

  @Test
  public void exampleProgram() {
    Day19.Program program = Day19.Program.parse(ImmutableList.of(
        "#ip 0",
        "seti 5 0 1",
        "seti 6 0 2",
        "addi 0 1 0",
        "addr 1 2 3",
        "setr 1 0 0",
        "seti 8 0 4",
        "seti 9 0 5"
    ));

    assertThat(program.run(Day19.State.INITIAL)).isEqualTo(new Day19.State(new int[]{6, 5, 6, 0, 0, 9}));
  }
}