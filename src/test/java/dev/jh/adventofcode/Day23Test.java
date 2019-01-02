package dev.jh.adventofcode;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class Day23Test {
  private static final ImmutableList<Day23.Nanobot> EXAMPLE = ImmutableList.of(
      Day23.Nanobot.parse("pos=<0,0,0>, r=4"),
      Day23.Nanobot.parse("pos=<1,0,0>, r=1"),
      Day23.Nanobot.parse("pos=<4,0,0>, r=3"),
      Day23.Nanobot.parse("pos=<0,2,0>, r=1"),
      Day23.Nanobot.parse("pos=<0,5,0>, r=3"),
      Day23.Nanobot.parse("pos=<0,0,3>, r=1"),
      Day23.Nanobot.parse("pos=<1,1,1>, r=1"),
      Day23.Nanobot.parse("pos=<1,1,2>, r=1"),
      Day23.Nanobot.parse("pos=<1,3,1>, r=1")
  );

  @Test
  public void exampleInRange() {
    assertThat(Day23.mostBotsInRange(EXAMPLE)).isEqualTo(7);
  }
}