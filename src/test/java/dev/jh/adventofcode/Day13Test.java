package dev.jh.adventofcode;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.junit.Test;

import static dev.jh.adventofcode.Day13.TrackSegment.*;
import static org.assertj.core.api.Assertions.assertThat;

public class Day13Test {

  public static Day13.Track EXAMPLE_TRACK = new Day13.Track(
      new Day13.TrackSegment[][]{
          new Day13.TrackSegment[] {CURVE_RIGHT, HORIZONTAL, HORIZONTAL, HORIZONTAL, CURVE_LEFT, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY, EMPTY},
          new Day13.TrackSegment[] {VERTICAL, EMPTY, EMPTY, EMPTY, VERTICAL, EMPTY, EMPTY, CURVE_RIGHT, HORIZONTAL, HORIZONTAL, HORIZONTAL, HORIZONTAL, CURVE_LEFT},
          new Day13.TrackSegment[] {VERTICAL, EMPTY, CURVE_RIGHT, HORIZONTAL, INTERSECTION, HORIZONTAL, HORIZONTAL, INTERSECTION, HORIZONTAL, CURVE_LEFT, EMPTY, EMPTY, VERTICAL},
          new Day13.TrackSegment[] {VERTICAL, EMPTY, VERTICAL, EMPTY, VERTICAL, EMPTY, EMPTY, VERTICAL, EMPTY, VERTICAL, EMPTY, EMPTY, VERTICAL},
          new Day13.TrackSegment[] {CURVE_LEFT, HORIZONTAL, INTERSECTION, HORIZONTAL, CURVE_RIGHT, EMPTY, EMPTY, CURVE_LEFT, HORIZONTAL, INTERSECTION, HORIZONTAL, HORIZONTAL, CURVE_RIGHT},
          new Day13.TrackSegment[] {EMPTY, EMPTY, CURVE_LEFT, HORIZONTAL, HORIZONTAL, HORIZONTAL, HORIZONTAL, HORIZONTAL, HORIZONTAL, CURVE_RIGHT, EMPTY, EMPTY, EMPTY}
      },
      ImmutableSet.of(
          new Day13.Cart(new Day13.Position(2, 0), Day13.CartDirection.RIGHT, Day13.CartTurn.LEFT),
          new Day13.Cart(new Day13.Position(9, 3), Day13.CartDirection.DOWN, Day13.CartTurn.LEFT)
      )
  );

  @Test
  public void parseExample() {
    ImmutableList<String> lines = ImmutableList.of(
        "/->-\\        ",
        "|   |  /----\\",
        "| /-+--+-\\  |",
        "| | |  | v  |",
        "\\-+-/  \\-+--/",
        "  \\------/   "
    );

    Day13.Track parsed = Day13.parseLines(lines);
    assertThat(parsed).isEqualTo(EXAMPLE_TRACK);
  }

  @Test
  public void firstCollision() {
    assertThat(Day13.firstCollision(EXAMPLE_TRACK)).isEqualTo(new Day13.Position(7, 3));
  }

  @Test
  public void lastCart() {
    assertThat(Day13.lastCart(Day13.parseLines(ImmutableList.of(
        "/>-<\\  ",
        "|   |  ",
        "| /<+-\\",
        "| | | v",
        "\\>+</ |",
        "  |   ^",
        "  \\<->/"
    )))).isEqualTo(new Day13.Position(6, 4));
  }

  @Test
  public void lastCartLoop() {
    assertThat(Day13.lastCart(Day13.parseLines(ImmutableList.of(
        "/->--->>--\\   /-----<->----\\",
        "|         |   |            |",
        "\\---------/   \\------------/"
    )))).isEqualTo(new Day13.Position(6, 2));
  }
}