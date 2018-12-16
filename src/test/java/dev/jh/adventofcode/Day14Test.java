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

  @Test
  public void recipesBefore51589() {
    assertThat(Day14.recipesBefore("51589")).isEqualTo(9);
  }

  @Test
  public void recipesBefore01245() {
    assertThat(Day14.recipesBefore("01245")).isEqualTo(5);
  }

  @Test
  public void recipesBefore92510() {
    assertThat(Day14.recipesBefore("92510")).isEqualTo(18);
  }

  @Test
  public void recipesBefore59414() {
    assertThat(Day14.recipesBefore("59414")).isEqualTo(2018);
  }
}