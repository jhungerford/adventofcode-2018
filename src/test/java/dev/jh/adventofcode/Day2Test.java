package dev.jh.adventofcode;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class Day2Test {

  @Test
  public void letterCounts() {
    assertThat(Day2.letterCounts("abcdef")).isEqualTo(ImmutableMap.<Character, Integer>builder()
        .put('a', 1)
        .put('b', 1)
        .put('c', 1)
        .put('d', 1)
        .put('e', 1)
        .put('f', 1)
        .build());

    assertThat(Day2.letterCounts("bababc")).isEqualTo(ImmutableMap.<Character, Integer>builder()
        .put('a', 2)
        .put('b', 3)
        .put('c', 1)
        .build());

    assertThat(Day2.letterCounts("abbcde")).isEqualTo(ImmutableMap.<Character, Integer>builder()
        .put('a', 1)
        .put('b', 2)
        .put('c', 1)
        .put('d', 1)
        .put('e', 1)
        .build());

    assertThat(Day2.letterCounts("abcccd")).isEqualTo(ImmutableMap.<Character, Integer>builder()
        .put('a', 1)
        .put('b', 1)
        .put('c', 3)
        .put('d', 1)
        .build());

    assertThat(Day2.letterCounts("aabcdd")).isEqualTo(ImmutableMap.<Character, Integer>builder()
        .put('a', 2)
        .put('b', 1)
        .put('c', 1)
        .put('d', 2)
        .build());

    assertThat(Day2.letterCounts("abcdee")).isEqualTo(ImmutableMap.<Character, Integer>builder()
        .put('a', 1)
        .put('b', 1)
        .put('c', 1)
        .put('d', 1)
        .put('e', 2)
        .build());

    assertThat(Day2.letterCounts("ababab")).isEqualTo(ImmutableMap.<Character, Integer>builder()
        .put('a', 3)
        .put('b', 3)
        .build());
  }

  @Test
  public void uniqueLetterRepeats() {
    assertThat(Day2.uniqueLetterRepeats(Day2.letterCounts("abcdef"))).isEqualTo(new Day2.RepeatedLetters(0, 0));
    assertThat(Day2.uniqueLetterRepeats(Day2.letterCounts("bababc"))).isEqualTo(new Day2.RepeatedLetters(1, 1));
    assertThat(Day2.uniqueLetterRepeats(Day2.letterCounts("abbcde"))).isEqualTo(new Day2.RepeatedLetters(1, 0));
    assertThat(Day2.uniqueLetterRepeats(Day2.letterCounts("abcccd"))).isEqualTo(new Day2.RepeatedLetters(0, 1));
    assertThat(Day2.uniqueLetterRepeats(Day2.letterCounts("aabcdd"))).isEqualTo(new Day2.RepeatedLetters(1, 0));
    assertThat(Day2.uniqueLetterRepeats(Day2.letterCounts("abcdee"))).isEqualTo(new Day2.RepeatedLetters(1, 0));
    assertThat(Day2.uniqueLetterRepeats(Day2.letterCounts("ababab"))).isEqualTo(new Day2.RepeatedLetters(0, 1));
  }

  @Test
  public void computeChecksum() {
    int checksum = Day2.computeChecksum(Stream.of(
        "abcdef",
        "bababc",
        "abbcde",
        "abcccd",
        "aabcdd",
        "abcdee",
        "ababab"
    )
        .map(Day2::letterCounts)
        .map(Day2::uniqueLetterRepeats)
        .collect(ImmutableList.toImmutableList()));

    assertThat(checksum).isEqualTo(12);
  }

  @Test
  public void closeBoxIds() {
    ImmutableList<String> boxIds = ImmutableList.of(
        "abcde",
        "fghij",
        "klmno",
        "pqrst",
        "fguij",
        "axcye",
        "wvxyz"
    );

    assertThat(Day2.closeBoxIds(boxIds)).isEqualTo(new Day2.CloseBoxIds("fghij", "fguij"));
  }

  @Test
  public void commonLetters() {
    assertThat(Day2.commonLetters(new Day2.CloseBoxIds("fghij", "fguij"))).isEqualTo("fgij");
  }
}