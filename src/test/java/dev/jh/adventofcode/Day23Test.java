package dev.jh.adventofcode;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class Day23Test {

  @Test
  public void exampleInRange() {
    assertThat(Day23.mostBotsInRange(ImmutableList.of(
        Day23.Nanobot.parse("pos=<0,0,0>, r=4"),
        Day23.Nanobot.parse("pos=<1,0,0>, r=1"),
        Day23.Nanobot.parse("pos=<4,0,0>, r=3"),
        Day23.Nanobot.parse("pos=<0,2,0>, r=1"),
        Day23.Nanobot.parse("pos=<0,5,0>, r=3"),
        Day23.Nanobot.parse("pos=<0,0,3>, r=1"),
        Day23.Nanobot.parse("pos=<1,1,1>, r=1"),
        Day23.Nanobot.parse("pos=<1,1,2>, r=1"),
        Day23.Nanobot.parse("pos=<1,3,1>, r=1")
    ))).isEqualTo(7);
  }

  @Test
  public void exampleMostDistance() {
    assertThat(Day23.mostNanobotsDistance(ImmutableList.of(
        Day23.Nanobot.parse("pos=<10,12,12>, r=2"),
        Day23.Nanobot.parse("pos=<12,14,12>, r=2"),
        Day23.Nanobot.parse("pos=<16,12,12>, r=4"),
        Day23.Nanobot.parse("pos=<14,14,14>, r=6"),
        Day23.Nanobot.parse("pos=<50,50,50>, r=200"),
        Day23.Nanobot.parse("pos=<10,10,10>, r=5")
    ))).isEqualTo(36);
  }
}