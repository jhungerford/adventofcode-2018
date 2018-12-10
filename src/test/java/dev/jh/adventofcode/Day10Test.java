package dev.jh.adventofcode;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class Day10Test {

  private static final ImmutableList<String> EXAMPLE_LINES = ImmutableList.of(
      "position=< 9,  1> velocity=< 0,  2>",
      "position=< 7,  0> velocity=<-1,  0>",
      "position=< 3, -2> velocity=<-1,  1>",
      "position=< 6, 10> velocity=<-2, -1>",
      "position=< 2, -4> velocity=< 2,  2>",
      "position=<-6, 10> velocity=< 2, -2>",
      "position=< 1,  8> velocity=< 1, -1>",
      "position=< 1,  7> velocity=< 1,  0>",
      "position=<-3, 11> velocity=< 1, -2>",
      "position=< 7,  6> velocity=<-1, -1>",
      "position=<-2,  3> velocity=< 1,  0>",
      "position=<-4,  3> velocity=< 2,  0>",
      "position=<10, -3> velocity=<-1,  1>",
      "position=< 5, 11> velocity=< 1, -2>",
      "position=< 4,  7> velocity=< 0, -1>",
      "position=< 8, -2> velocity=< 0,  1>",
      "position=<15,  0> velocity=<-2,  0>",
      "position=< 1,  6> velocity=< 1,  0>",
      "position=< 8,  9> velocity=< 0, -1>",
      "position=< 3,  3> velocity=<-1,  1>",
      "position=< 0,  5> velocity=< 0, -1>",
      "position=<-2,  2> velocity=< 2,  0>",
      "position=< 5, -2> velocity=< 1,  2>",
      "position=< 1,  4> velocity=< 2,  1>",
      "position=<-2,  7> velocity=< 2, -2>",
      "position=< 3,  6> velocity=<-1, -1>",
      "position=< 5,  0> velocity=< 1,  0>",
      "position=<-6,  0> velocity=< 2,  0>",
      "position=< 5,  9> velocity=< 1, -2>",
      "position=<14,  7> velocity=<-2,  0>",
      "position=<-3,  6> velocity=< 2, -1>"
  );

  private static final ImmutableList<Day10.Star> EXAMPLE_STARS = ImmutableList.of(
      new Day10.Star(new Day10.Point(9, 1), new Day10.Point(0, 2)),
      new Day10.Star(new Day10.Point(7, 0), new Day10.Point(-1, 0)),
      new Day10.Star(new Day10.Point(3, -2), new Day10.Point(-1, 1)),
      new Day10.Star(new Day10.Point(6, 10), new Day10.Point(-2, -1)),
      new Day10.Star(new Day10.Point(2, -4), new Day10.Point(2, 2)),
      new Day10.Star(new Day10.Point(-6, 10), new Day10.Point(2, -2)),
      new Day10.Star(new Day10.Point(1, 8), new Day10.Point(1, -1)),
      new Day10.Star(new Day10.Point(1, 7), new Day10.Point(1, 0)),
      new Day10.Star(new Day10.Point(-3, 11), new Day10.Point(1, -2)),
      new Day10.Star(new Day10.Point(7, 6), new Day10.Point(-1, -1)),
      new Day10.Star(new Day10.Point(-2, 3), new Day10.Point(1, 0)),
      new Day10.Star(new Day10.Point(-4, 3), new Day10.Point(2, 0)),
      new Day10.Star(new Day10.Point(10, -3), new Day10.Point(-1, 1)),
      new Day10.Star(new Day10.Point(5, 11), new Day10.Point(1, -2)),
      new Day10.Star(new Day10.Point(4, 7), new Day10.Point(0, -1)),
      new Day10.Star(new Day10.Point(8, -2), new Day10.Point(0, 1)),
      new Day10.Star(new Day10.Point(15, 0), new Day10.Point(-2, 0)),
      new Day10.Star(new Day10.Point(1, 6), new Day10.Point(1, 0)),
      new Day10.Star(new Day10.Point(8, 9), new Day10.Point(0, -1)),
      new Day10.Star(new Day10.Point(3, 3), new Day10.Point(-1, 1)),
      new Day10.Star(new Day10.Point(0, 5), new Day10.Point(0, -1)),
      new Day10.Star(new Day10.Point(-2, 2), new Day10.Point(2, 0)),
      new Day10.Star(new Day10.Point(5, -2), new Day10.Point(1, 2)),
      new Day10.Star(new Day10.Point(1, 4), new Day10.Point(2, 1)),
      new Day10.Star(new Day10.Point(-2, 7), new Day10.Point(2, -2)),
      new Day10.Star(new Day10.Point(3, 6), new Day10.Point(-1, -1)),
      new Day10.Star(new Day10.Point(5, 0), new Day10.Point(1, 0)),
      new Day10.Star(new Day10.Point(-6, 0), new Day10.Point(2, 0)),
      new Day10.Star(new Day10.Point(5, 9), new Day10.Point(1, -2)),
      new Day10.Star(new Day10.Point(14, 7), new Day10.Point(-2, 0)),
      new Day10.Star(new Day10.Point(-3, 6), new Day10.Point(2, -1))
  );

  @Test
  public void parse() {
    for (int i = 0; i < EXAMPLE_LINES.size(); i ++) {
      assertThat(Day10.Star.parse(EXAMPLE_LINES.get(i))).isEqualTo(EXAMPLE_STARS.get(i));
    }
  }

  @Test
  public void renderPart1Message() {
    assertThat(Day10.renderMessage(Day10.smallestBounds(EXAMPLE_STARS))).isEqualTo(
        "#...#..###\n" +
        "#...#...#.\n" +
        "#...#...#.\n" +
        "#####...#.\n" +
        "#...#...#.\n" +
        "#...#...#.\n" +
        "#...#...#.\n" +
        "#...#..###\n");
  }
}