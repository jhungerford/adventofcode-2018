package dev.jh.adventofcode;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class Day14Test {

  @Test
  public void scoreAfter5() {
    assertThat(Day14.scoreAfter(5)).isEqualTo("0124515891");
  }

  @Test
  public void scoreAfter9() {
    assertThat(Day14.scoreAfter(9)).isEqualTo("5158916779");
  }

  @Test
  public void scoreAfter18() {
    assertThat(Day14.scoreAfter(18)).isEqualTo("9251071085");
  }

  @Test
  public void scoreAfter2018() {
    assertThat(Day14.scoreAfter(2018)).isEqualTo("5941429882");
  }
}