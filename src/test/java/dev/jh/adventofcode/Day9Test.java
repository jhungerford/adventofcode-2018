package dev.jh.adventofcode;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class Day9Test {

  @Test
  public void highScoreExample() {
    assertThat(Day9.highScore(9, 25)).isEqualTo(32);
    assertThat(Day9.highScore(10, 1618)).isEqualTo(8317);
    assertThat(Day9.highScore(13, 7999)).isEqualTo(146373);
    assertThat(Day9.highScore(17, 1104)).isEqualTo(2764);
    assertThat(Day9.highScore(21, 6111)).isEqualTo(54718);
    assertThat(Day9.highScore(30, 5807)).isEqualTo(37305);
  }
}