package dev.jh.adventofcode;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class Day20Test {

  @Test
  public void simpleExample() {
    Day20.Instruction instruction = Day20.parse("^WNE$");
    ImmutableMap<Day20.Point, ImmutableSet<Day20.Point>> graph = Day20.buildGraph(instruction);
    ImmutableMap<Day20.Point, Integer> distances = Day20.roomDistances(graph);

    assertThat(Day20.furthestRoom(distances)).isEqualTo(3);
  }

  @Test
  public void hallwayExample() {
    Day20.Instruction instruction = Day20.parse("^ENWWW(NEEE|SSE(EE|N))$");
    ImmutableMap<Day20.Point, ImmutableSet<Day20.Point>> graph = Day20.buildGraph(instruction);
    ImmutableMap<Day20.Point, Integer> distances = Day20.roomDistances(graph);

    assertThat(Day20.furthestRoom(distances)).isEqualTo(10);
  }

  @Test
  public void emptyExample() {
    Day20.Instruction instruction = Day20.parse("^ENNWSWW(NEWS|)SSSEEN(WNSE|)EE(SWEN|)NNN$");
    ImmutableMap<Day20.Point, ImmutableSet<Day20.Point>> graph = Day20.buildGraph(instruction);
    ImmutableMap<Day20.Point, Integer> distances = Day20.roomDistances(graph);

    assertThat(Day20.furthestRoom(distances)).isEqualTo(18);
  }

  @Test
  public void example23() {
    Day20.Instruction instruction = Day20.parse("^ESSWWN(E|NNENN(EESS(WNSE|)SSS|WWWSSSSE(SW|NNNE)))$");
    ImmutableMap<Day20.Point, ImmutableSet<Day20.Point>> graph = Day20.buildGraph(instruction);
    ImmutableMap<Day20.Point, Integer> distances = Day20.roomDistances(graph);

    assertThat(Day20.furthestRoom(distances)).isEqualTo(23);
  }

  @Test
  public void example31() {
    Day20.Instruction instruction = Day20.parse("^WSSEESWWWNW(S|NENNEEEENN(ESSSSW(NWSW|SSEN)|WSWWN(E|WWS(E|SS))))$");
    ImmutableMap<Day20.Point, ImmutableSet<Day20.Point>> graph = Day20.buildGraph(instruction);
    ImmutableMap<Day20.Point, Integer> distances = Day20.roomDistances(graph);

    assertThat(Day20.furthestRoom(distances)).isEqualTo(31);
  }
}