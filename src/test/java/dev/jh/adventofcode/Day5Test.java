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

  @Test
  public void allUnits() {
    assertThat(Day5.allUnits("aabAAB")).containsExactlyInAnyOrder('a', 'b');
    assertThat(Day5.allUnits("dabAcCaCBAcCcaDA")).containsExactlyInAnyOrder('a', 'b', 'c', 'd');
  }

  @Test
  public void removeUnit() {
    assertThat(Day5.removeUnit("dabAcCaCBAcCcaDA", 'a')).isEqualTo("dbcCCBcCcD");
    assertThat(Day5.removeUnit("dabAcCaCBAcCcaDA", 'b')).isEqualTo("daAcCaCAcCcaDA");
    assertThat(Day5.removeUnit("dabAcCaCBAcCcaDA", 'c')).isEqualTo("dabAaBAaDA");
    assertThat(Day5.removeUnit("dabAcCaCBAcCcaDA", 'd')).isEqualTo("abAcCaCBAcCcaA");
  }

  @Test
  public void reactWithUnitRemoved() {
    assertThat(Day5.react(Day5.removeUnit("dabAcCaCBAcCcaDA", 'a'))).isEqualTo("dbCBcD");
    assertThat(Day5.react(Day5.removeUnit("dabAcCaCBAcCcaDA", 'b'))).isEqualTo("daCAcaDA");
    assertThat(Day5.react(Day5.removeUnit("dabAcCaCBAcCcaDA", 'c'))).isEqualTo("daDA");
    assertThat(Day5.react(Day5.removeUnit("dabAcCaCBAcCcaDA", 'd'))).isEqualTo("abCBAc");
  }

  @Test
  public void shortestPolymerWithUnitRemoved() {
    assertThat(Day5.shortestWithUnitRemoved("dabAcCaCBAcCcaDA")).isEqualTo(new Day5.ShortestPolymer("daDA", 'c'));
  }
}