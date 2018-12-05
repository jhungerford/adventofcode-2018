package dev.jh.adventofcode;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class Day5Test {

  @Test
  public void reactaA() {
    assertThat(Day5.react("aA")).isEqualTo("");
  }

  @Test
  public void reactabBA() {
    assertThat(Day5.react("abBA")).isEqualTo("");
  }

  @Test
  public void reactabAB() {
    assertThat(Day5.react("abAB")).isEqualTo("abAB");
  }

  @Test
  public void reactaabAAB() {
    assertThat(Day5.react("aabAAB")).isEqualTo("aabAAB");
  }

  @Test
  public void reactdabAcCaCBAcCcaDA() {
    assertThat(Day5.react("dabAcCaCBAcCcaDA")).isEqualTo("dabCBAcaDA");
  }
}