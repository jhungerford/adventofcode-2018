package dev.jh.adventofcode;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class Day1Test {

  @Test
  public void parseFrequency() {
    assertThat(Day1.parseFrequency("+1")).isEqualTo(1);
    assertThat(Day1.parseFrequency("-2")).isEqualTo(-2);
  }

  @Test
  public void frequencyAfterChanges() {
    assertThat(Day1.frequencyAfter(ImmutableList.of("+1", "-2", "+3", "+1"))).isEqualTo(3);
    assertThat(Day1.frequencyAfter(ImmutableList.of("+1", "+1", "+1"))).isEqualTo(3);
    assertThat(Day1.frequencyAfter(ImmutableList.of("+1", "+1", "-2"))).isEqualTo(0);
    assertThat(Day1.frequencyAfter(ImmutableList.of("-1", "-2", "-3"))).isEqualTo(-6);
  }

  @Test
  public void firstFrequencyTwice() {
    assertThat(Day1.firstFrequencyTwice(ImmutableList.of("+1", "-1"))).isEqualTo(0);
    assertThat(Day1.firstFrequencyTwice(ImmutableList.of("+3", "+3", "+4", "-2", "-4"))).isEqualTo(10);
    assertThat(Day1.firstFrequencyTwice(ImmutableList.of("-6", "+3", "+8", "+5", "-6"))).isEqualTo(5);
    assertThat(Day1.firstFrequencyTwice(ImmutableList.of("+7", "+7", "-2", "-7", "-4"))).isEqualTo(14);
  }
}