package dev.jh.adventofcode;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class Day22Test {

  private static final Day22.Cave EXAMPLE_CAVE = new Day22.Cave(new Day22.Point(10, 10), 510);

  @Test
  public void exampleCave() {
    ImmutableList<String> expected = ImmutableList.of(
        "M=.|=.|.|=.|=|=.",
        ".|=|=|||..|.=...",
        ".==|....||=..|==",
        "=.|....|.==.|==.",
        "=|..==...=.|==..",
        "=||.=.=||=|=..|=",
        "|.=.===|||..=..|",
        "|..==||=.|==|===",
        ".=..===..=|.|||.",
        ".======|||=|=.|=",
        ".===|=|===T===||",
        "=|||...|==..|=.|",
        "=.=|=.=..=.||==|",
        "||=|=...|==.=|==",
        "|=.=||===.|||===",
        "||.|==.|.|.||=||"
    );

    for (int y = 0; y < expected.size(); y ++) {
      assertThat(EXAMPLE_CAVE.erosion[y]).startsWith(parseErosion(expected.get(y)));
    }
  }

  @Test
  public void exampleRisk() {
    assertThat(EXAMPLE_CAVE.risk()).isEqualTo(114);
  }

  @Test
  public void exampleFastestMinutes() {
    assertThat(EXAMPLE_CAVE.fastestMinutes()).isEqualTo(45);
  }

  private static Day22.Erosion[] parseErosion(String line) {
    return line.chars()
        .mapToObj(c -> Day22.Erosion.fromName((char) c))
        .toArray(Day22.Erosion[]::new);
  }
}