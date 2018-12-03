package dev.jh.adventofcode;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class Day3Test {
  @Test
  public void parseClaim() {
    assertThat(Day3.Claim.parse("#123 @ 3,2: 5x4")).isEqualTo(new Day3.Claim("#123", 3, 2, 5, 4));
  }

  /*
  @Test
  public void claimOverlapsExample() {
    Day3.Claim claim1 = Day3.Claim.parse("#1 @ 1,3: 4x4");
    Day3.Claim claim2 = Day3.Claim.parse("#2 @ 3,1: 4x4");
    Day3.Claim claim3 = Day3.Claim.parse("#3 @ 5,5: 2x2");

    assertThat(claim1.overlaps(claim2)).isTrue();
    assertThat(claim2.overlaps(claim3)).isTrue();

    assertThat(claim1.overlaps(claim3)).isFalse();
    assertThat(claim2.overlaps(claim3)).isFalse();
  }

  @Test
  public void claimOverlapsAround() {
    Day3.Claim claim1 = Day3.Claim.parse("#1 @ 3,3: 4x4");

    // Same origin
    Day3.Claim claim2 = Day3.Claim.parse("#2 @ 3,3: 2x2");
    assertThat(claim1.overlaps(claim2)).isTrue();
    assertThat(claim2.overlaps(claim1)).isTrue();

    // Origin to the left - overlaps
    assertThat(claim1.overlaps(Day3.Claim.parse("#2 @ 1,3: 3x3"))).isTrue();
    // Origin to the left - claim isn't wide enough to overlap
    assertThat(claim1.overlaps(Day3.Claim.parse("#2 @ 1,3: 2x2"))).isFalse();

    // Origin above - overlaps
    assertThat(claim1.overlaps(Day3.Claim.parse("#2 @ 3,1: 3x3"))).isTrue();
    // Origin above - claim isn't wide enough to overlap
    assertThat(claim1.overlaps(Day3.Claim.parse("#2 @ 3,1: 2x2"))).isFalse();

    // Origin to the right - overlaps
    assertThat(claim1.overlaps(Day3.Claim.parse("#2 @ 6,3: 2x2"))).isTrue();

    // Origin to the right - #1 isn't wide enough to overlap
    assertThat(claim1.overlaps(Day3.Claim.parse("#2 @ 7,3: 2x2"))).isFalse();

    // Origin below - overlaps
    assertThat(claim1.overlaps(Day3.Claim.parse("#2 @ 3,6: 2x2"))).isTrue();

    // Origin below - #1 isn't wide enough to overlap
    assertThat(claim1.overlaps(Day3.Claim.parse("#2 @ 3,7: 2x2"))).isFalse();
  }
  */

  @Test
  public void numOverlappingSquares() {
    ImmutableList<Day3.Claim> claims = ImmutableList.of(
        Day3.Claim.parse("#1 @ 1,3: 4x4"),
        Day3.Claim.parse("#2 @ 3,1: 4x4"),
        Day3.Claim.parse("#3 @ 5,5: 2x2")
    );

    assertThat(Day3.numOverlappingSquares(claims)).isEqualTo(4);
  }
}