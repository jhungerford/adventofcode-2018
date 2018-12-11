package dev.jh.adventofcode;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class Day11Test {

  @Test
  public void hundredsDigit() {
    assertThat(Day11.hundredsDigit(12345)).isEqualTo(3);
    assertThat(Day11.hundredsDigit(1111)).isEqualTo(1);
    assertThat(Day11.hundredsDigit(949)).isEqualTo(9);
    assertThat(Day11.hundredsDigit(99)).isEqualTo(0);
    assertThat(Day11.hundredsDigit(1000)).isEqualTo(0);
  }

  @Test
  public void largestPower18() {
    assertThat(Day11.largestPower(Day11.grid(18))).isEqualTo(new Day11.Point(33, 45, 29));
  }

  @Test
  public void largestPower42() {
    assertThat(Day11.largestPower(Day11.grid(42))).isEqualTo(new Day11.Point(21, 61, 30));
  }
}