package dev.jh.adventofcode;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class Day8Test {

  private static final String EXAMPLE_LINE = "2 3 0 3 10 11 12 1 1 0 1 99 2 1 1 2";
  private static final Day8.Node EXAMPLE_ROOT = new Day8.Node(
      ImmutableList.of(
          new Day8.Node(ImmutableList.of(), ImmutableList.of(10, 11, 12)),
          new Day8.Node(
              ImmutableList.of(
                  new Day8.Node(ImmutableList.of(), ImmutableList.of(99))
              ),
              ImmutableList.of(2)
          )
      ),
      ImmutableList.of(1, 1, 2)
  );


  @Test
  public void parse() {
    assertThat(Day8.parse(EXAMPLE_LINE)).isEqualTo(EXAMPLE_ROOT);
  }

  @Test
  public void sumMetadata() {
    assertThat(Day8.sumMetadata(EXAMPLE_ROOT)).isEqualTo(138);
  }
}