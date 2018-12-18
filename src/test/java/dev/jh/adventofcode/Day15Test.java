package dev.jh.adventofcode;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class Day15Test {

  @Test
  public void comparePositions() {
    ImmutableList<Day15.Position> positions = Stream.of(
        new Day15.Position(0, 1),
        new Day15.Position(0, 0),
        new Day15.Position(1, 1),
        new Day15.Position(1, 0)
    )
        .sorted()
        .collect(ImmutableList.toImmutableList());

    assertThat(positions).containsExactly(
        new Day15.Position(0, 0),
        new Day15.Position(1, 0),
        new Day15.Position(0, 1),
        new Day15.Position(1, 1)
    );
  }

  @Test
  public void exampleMovement() {

    Day15.Board initial = Day15.Board.parseLines(ImmutableList.of(
        "#########",
        "#G..G..G#",
        "#.......#",
        "#.......#",
        "#G..E..G#",
        "#.......#",
        "#.......#",
        "#G..G..G#",
        "#########"
    ));

    Day15.Board round1 = Day15.Board.parseLines(ImmutableList.of(
        "#########",
        "#.G...G.#",
        "#...G...#",
        "#...E..G#",
        "#.G.....#",
        "#.......#",
        "#G..G..G#",
        "#.......#",
        "#########"
    ));

    Day15.Board round2 = Day15.Board.parseLines(ImmutableList.of(
        "#########",
        "#..G.G..#",
        "#...G...#",
        "#.G.E.G.#",
        "#.......#",
        "#G..G..G#",
        "#.......#",
        "#.......#",
        "#########"
    ));

    Day15.Board round3 = Day15.Board.parseLines(ImmutableList.of(
        "#########",
        "#.......#",
        "#..GGG..#",
        "#..GEG..#",
        "#G..G...#",
        "#......G#",
        "#.......#",
        "#.......#",
        "#########"
    ));

    assertThat(initial.round().board.toString()).isEqualTo(round1.toString());
    assertThat(round1.round().board.toString()).isEqualTo(round2.toString());
    assertThat(round2.round().board.toString()).isEqualTo(round3.toString());
  }

  @Test
  public void exampleCombat() {
    Day15.Board initial = Day15.Board.parseLines(ImmutableList.of(
        "#######",
        "#.G...#",
        "#...EG#",
        "#.#.#G#",
        "#..G#E#",
        "#.....#",
        "#######"
    ));

    Day15.Board round1 = Day15.Board.parseLines(ImmutableList.of(
        "#######",
        "#..G..#   G(200)",
        "#...EG#   E(197), G(197)",
        "#.#G#G#   G(200), G(197)",
        "#...#E#   E(197)",
        "#.....#   ",
        "#######"
    ));

    Day15.Board round2 = Day15.Board.parseLines(ImmutableList.of(
        "#######",
        "#...G.#   G(200)",
        "#..GEG#   G(200), E(188), G(194)",
        "#.#.#G#   G(194)",
        "#...#E#   E(194)",
        "#.....#   ",
        "#######"
    ));

    Day15.Board round23 = Day15.Board.parseLines(ImmutableList.of(
        "#######",
        "#...G.#   G(200)",
        "#..G.G#   G(200), G(131)",
        "#.#.#G#   G(131)",
        "#...#E#   E(131)",
        "#.....#",
        "#######"
    ));

    Day15.Board round24 = Day15.Board.parseLines(ImmutableList.of(
        "#######",
        "#..G..#   G(200)",
        "#...G.#   G(131)",
        "#.#G#G#   G(200), G(128)",
        "#...#E#   E(128)",
        "#.....#",
        "#######"
    ));

    Day15.Board round25 = Day15.Board.parseLines(ImmutableList.of(
        "#######",
        "#.G...#   G(200)",
        "#..G..#   G(131)",
        "#.#.#G#   G(125)",
        "#..G#E#   G(200), E(125)",
        "#.....#",
        "#######"
    ));

    Day15.Board round26 = Day15.Board.parseLines(ImmutableList.of(
        "#######",
        "#G....#   G(200)",
        "#.G...#   G(131)",
        "#.#.#G#   G(122)",
        "#...#E#   E(122)",
        "#..G..#   G(200)",
        "#######"
    ));

    Day15.Board round27 = Day15.Board.parseLines(ImmutableList.of(
        "#######",
        "#G....#   G(200)",
        "#.G...#   G(131)",
        "#.#.#G#   G(119)",
        "#...#E#   E(119)",
        "#...G.#   G(200)",
        "#######"
    ));

    Day15.Board round28 = Day15.Board.parseLines(ImmutableList.of(
        "#######",
        "#G....#   G(200)",
        "#.G...#   G(131)",
        "#.#.#G#   G(116)",
        "#...#E#   E(113)",
        "#....G#   G(200)",
        "#######"
    ));

    Day15.Board round47 = Day15.Board.parseLines(ImmutableList.of(
        "#######",
        "#G....#   G(200)",
        "#.G...#   G(131)",
        "#.#.#G#   G(59)",
        "#...#.#",
        "#....G#   G(200)",
        "#######"
    ));

    assertThat(initial.round().board).isEqualTo(round1);
    assertThat(round1.round().board).isEqualTo(round2);

    Day15.Board actualRound23 = initial;
    for (int i = 0; i < 23; i ++) {
      actualRound23 = actualRound23.round().board;
    }

    assertThat(actualRound23).isEqualTo(round23);

    Day15.Board actualRound24 = actualRound23.round().board;
    assertThat(actualRound24).isEqualTo(round24);

    Day15.Board actualRound25 = actualRound24.round().board;
    assertThat(actualRound25).isEqualTo(round25);

    Day15.Board actualRound26 = actualRound25.round().board;
    assertThat(actualRound26).isEqualTo(round26);

    Day15.Board actualRound27 = actualRound26.round().board;
    assertThat(actualRound27).isEqualTo(round27);

    Day15.Board actualRound28 = actualRound27.round().board;
    assertThat(actualRound28).isEqualTo(round28);

    Day15.Board actualRound47 = initial;
    for (int i = 0; i <= 47; i ++) {
      actualRound47 = actualRound47.round().board;
    }

    assertThat(actualRound47.isOver()).isTrue();
    assertThat(actualRound47).isEqualTo(round47);

    assertThat(Day15.outcome(initial)).isEqualTo(27730);
  }

  @Test
  public void outcome1() {
    Day15.Board board = Day15.Board.parseLines(ImmutableList.of(
        "#######",
        "#G..#E#",
        "#E#E.E#",
        "#G.##.#",
        "#...#E#",
        "#...E.#",
        "#######"
    ));

    Day15.Board outcome = Day15.Board.parseLines(ImmutableList.of(
        "#######",
        "#...#E#   E(200)",
        "#E#...#   E(197)",
        "#.E##.#   E(185)",
        "#E..#E#   E(200), E(200)",
        "#.....#",
        "#######"
    ));

    Day15.Board actual = board;
    for (int i = 0; i <= 37; i ++) {
      actual = actual.round().board;
    }

    assertThat(actual.isOver()).isTrue();
    assertThat(actual).isEqualTo(outcome);

    assertThat(Day15.outcome(board)).isEqualTo(36334);
  }

  @Test
  public void outcome2() {
    Day15.Board board = Day15.Board.parseLines(ImmutableList.of(
        "#######",
        "#E..EG#",
        "#.#G.E#",
        "#E.##E#",
        "#G..#.#",
        "#..E#.#",
        "#######"
    ));

    Day15.Board outcome = Day15.Board.parseLines(ImmutableList.of(
        "#######",
        "#.E.E.#   E(164), E(197)",
        "#.#E..#   E(200)",
        "#E.##.#   E(98)",
        "#.E.#.#   E(200)",
        "#...#.#",
        "#######"
    ));

    Day15.Board actual = board;
    for (int i = 0; i <= 46; i ++) {
      actual = actual.round().board;
    }

    assertThat(actual).isEqualTo(outcome);
    assertThat(Day15.outcome(board)).isEqualTo(39514);
  }

  @Test
  public void outcome3() {
    Day15.Board board = Day15.Board.parseLines(ImmutableList.of(
        "#######",
        "#E.G#.#",
        "#.#G..#",
        "#G.#.G#",
        "#G..#.#",
        "#...E.#",
        "#######"
    ));

    Day15.Board outcome = Day15.Board.parseLines(ImmutableList.of(
        "#######",
        "#G.G#.#   G(200), G(98)",
        "#.#G..#   G(200)",
        "#..#..#",
        "#...#G#   G(95)",
        "#...G.#   G(200)",
        "#######"
    ));

    Day15.Board actual = board;
    for (int i = 0; i <= 35; i++) {
      actual = actual.round().board;
    }

    assertThat(actual).isEqualTo(outcome);
    assertThat(Day15.outcome(board)).isEqualTo(27755);
  }

  @Test
  public void outcome4() {
    Day15.Board board = Day15.Board.parseLines(ImmutableList.of(
        "#######",
        "#.E...#",
        "#.#..G#",
        "#.###.#",
        "#E#G#G#",
        "#...#G#",
        "#######"
    ));

    Day15.Board outcome = Day15.Board.parseLines(ImmutableList.of(
        "#######",
        "#.....#",
        "#.#G..#   G(200)",
        "#.###.#",
        "#.#.#.#",
        "#G.G#G#   G(98), G(38), G(200)",
        "#######"
    ));

    Day15.Board actual = board;
    for (int i = 0; i <= 54; i++) {
      actual = actual.round().board;
    }

    assertThat(actual).isEqualTo(outcome);
    assertThat(Day15.outcome(board)).isEqualTo(28944);
  }

  @Test
  public void outcome5() {
    Day15.Board board = Day15.Board.parseLines(ImmutableList.of(
        "#########",
        "#G......#",
        "#.E.#...#",
        "#..##..G#",
        "#...##..#",
        "#...#...#",
        "#.G...G.#",
        "#.....G.#",
        "#########"
    ));

    Day15.Board outcome = Day15.Board.parseLines(ImmutableList.of(
        "#########",
        "#.G.....#   G(137)",
        "#G.G#...#   G(200), G(200)",
        "#.G##...#   G(200)",
        "#...##..#",
        "#.G.#...#   G(200)",
        "#.......#",
        "#.......#",
        "#########"
    ));

    Day15.Board actual = board;
    for (int i = 0; i <= 20; i++) {
      actual = actual.round().board;
    }

    assertThat(actual).isEqualTo(outcome);
    assertThat(Day15.outcome(board)).isEqualTo(18740);
  }
}