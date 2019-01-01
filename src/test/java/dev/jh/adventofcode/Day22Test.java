package dev.jh.adventofcode;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class Day22Test {

  @Test
  public void exampleCave() {
    Day22.Cave cave = new Day22.Cave(10, 10, 510);
    assertThat(cave.erosion).startsWith(
        parseErosion("M=.|=.|.|=."),
        parseErosion(".|=|=|||..|"),
        parseErosion(".==|....||="),
        parseErosion("=.|....|.=="),
        parseErosion("=|..==...=."),
        parseErosion("=||.=.=||=|"),
        parseErosion("|.=.===|||."),
        parseErosion("|..==||=.|="),
        parseErosion(".=..===..=|"),
        parseErosion(".======|||="),
        parseErosion(".===|=|===T"),
        parseErosion("=|||...|==."),
        parseErosion("=.=|=.=..=."),
        parseErosion("||=|=...|=="),
        parseErosion("|=.=||===.|"),
        parseErosion("||.|==.|.|.")
    );
  }

  @Test
  public void exampleRisk() {
    assertThat(new Day22.Cave(10, 10, 510).risk()).isEqualTo(114);
  }

  private static Day22.Erosion[] parseErosion(String line) {
    return line.chars()
        .mapToObj(c -> Day22.Erosion.fromName((char) c))
        .toArray(Day22.Erosion[]::new);
  }
}