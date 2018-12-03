package dev.jh.adventofcode;

import com.google.common.base.Charsets;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day3 {

  public static class Claim {
    private static final Pattern CLAIM_PATTERN = Pattern.compile("(#\\d+) @ (\\d+),(\\d+): (\\d+)x(\\d+)");

    public final String id;
    public final int leftOffset;
    public final int topOffset;
    public final int width;
    public final int height;

    public Claim(String id, int leftOffset, int topOffset, int width, int height) {
      this.id = id;
      this.leftOffset = leftOffset;
      this.topOffset = topOffset;
      this.width = width;
      this.height = height;
    }

    public static Claim parse(String claim) {
      Matcher matcher = CLAIM_PATTERN.matcher(claim);
      if (!matcher.matches()) {
        throw new IllegalArgumentException(claim + " is not a valid claim.");
      }

      return new Claim(
          matcher.group(1),
          Integer.parseInt(matcher.group(2)),
          Integer.parseInt(matcher.group(3)),
          Integer.parseInt(matcher.group(4)),
          Integer.parseInt(matcher.group(5))
      );
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Claim claim = (Claim) o;
      return leftOffset == claim.leftOffset &&
          topOffset == claim.topOffset &&
          width == claim.width &&
          height == claim.height &&
          Objects.equal(id, claim.id);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(id, leftOffset, topOffset, width, height);
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("id", id)
          .add("leftOffset", leftOffset)
          .add("topOffset", topOffset)
          .add("width", width)
          .add("height", height)
          .toString();
    }
  }

  public static int numOverlappingSquares(ImmutableList<Claim> claims) {
    int[][] fabric = new int[1000][1000];

    for (Claim claim : claims) {
      for (int y = claim.topOffset; y < claim.topOffset + claim.height; y ++) {
        for (int x = claim.leftOffset; x < claim.leftOffset + claim.width; x ++) {
          fabric[y][x]++;
        }
      }
    }

    int numOverlapping = 0;
    for (int y = 0; y < fabric.length; y ++) {
      for (int x = 0; x < fabric[y].length; x ++) {
        if (fabric[y][x] > 1) {
          numOverlapping++;
        }
      }
    }

    return numOverlapping;
  }

  public static void main(String[] args) throws Exception {
    File file = new File(Day1.class.getResource("/day3.txt").getFile());
    ImmutableList<String> lines = ImmutableList.copyOf(Files.readLines(file, Charsets.UTF_8));

    ImmutableList<Claim> claims = lines.stream().map(Day3.Claim::parse).collect(ImmutableList.toImmutableList());

    // Part 1: Number of squares where one or more claims overlap
    System.out.println("Part 1: " + numOverlappingSquares(claims));
  }
}
