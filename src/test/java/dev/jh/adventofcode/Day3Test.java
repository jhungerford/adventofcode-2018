package dev.jh.adventofcode;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class Day3Test {
  @Test
  public void parseClaim() {
    assertThat(Day3.Claim.parse("#123 @ 3,2: 5x4")).isEqualTo(new Day3.Claim("#123", 3, 2, 5, 4));
  }

  @Test
  public void numOverlappingSquares() {
    ImmutableList<Day3.Claim> claims = ImmutableList.of(
        Day3.Claim.parse("#1 @ 1,3: 4x4"),
        Day3.Claim.parse("#2 @ 3,1: 4x4"),
        Day3.Claim.parse("#3 @ 5,5: 2x2")
    );

    assertThat(Day3.numOverlappingSquares(claims)).isEqualTo(4);
  }

  @Test
  public void nonOverlappingClaim() {
    ImmutableList<Day3.Claim> claims = ImmutableList.of(
        Day3.Claim.parse("#1 @ 1,3: 4x4"),
        Day3.Claim.parse("#2 @ 3,1: 4x4"),
        Day3.Claim.parse("#3 @ 5,5: 2x2")
    );

    assertThat(Day3.nonOverlappingClaim(claims)).isEqualTo("#3");
  }
}