package dev.jh.adventofcode;

import com.google.common.collect.ImmutableList;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import static dev.jh.adventofcode.Day16.Opcode.*;
import static org.assertj.core.api.Assertions.assertThat;

public class Day16Test {

  public static final Day16.Sample SAMPLE = new Day16.Sample(
      new Day16.Registers(new int[]{3, 2, 1, 1}),
      new Day16.NumericInstruction(new int[]{9, 2, 1, 2}),
      new Day16.Registers(new int[]{3, 2, 2, 1})
  );

  @Test
  public void parseSample() {
    ImmutableList<String> lines = ImmutableList.of(
        "Before: [3, 2, 1, 1]",
        "9 2 1 2",
        "After:  [3, 2, 2, 1]"
    );

    assertThat(Day16.Sample.parse(lines)).isEqualTo(SAMPLE);
  }

  @Test
  public void sampleMatches() {
    assertThat(SAMPLE.matchingOpcodes()).containsExactlyInAnyOrder(MULR, ADDI, SETI);
  }
}