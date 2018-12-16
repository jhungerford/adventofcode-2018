package dev.jh.adventofcode;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Day14 {

  /**
   * Returns the scores of the 10 recepes after the given number of recipes.
   *
   * @param number Number of recipes before the returned number
   * @return Score of the 10 recipes after the given number
   */
  public static String scoreAfter(int number) {
    int[] scores = new int[number + 11];
    scores[0] = 3;
    scores[1] = 7;

    int aIndex = 0;
    int bIndex = 1;
    int nextScoreIndex = 2;

    while (nextScoreIndex < number + 10) {
      // New score is the sum of the scores of the recipes that the elves are on.
      int newScore = scores[aIndex] + scores[bIndex];

      // Append the new score to the end of the scores.
      if (newScore < 10) {
        scores[nextScoreIndex] = newScore;
        nextScoreIndex ++;
      } else {
        scores[nextScoreIndex] = 1;
        scores[nextScoreIndex + 1] = newScore % 10;
        nextScoreIndex += 2;
      }

      // Elfs step forward a number of recipes equal to 1 plus the score of their current recipe.
      aIndex = (aIndex + scores[aIndex] + 1) % nextScoreIndex;
      bIndex = (bIndex + scores[bIndex] + 1) % nextScoreIndex;
    }

    return IntStream.range(number, number + 10)
        .mapToObj(i -> Integer.toString(scores[i]))
        .collect(Collectors.joining());
  }

  public static void main(String[] args) {
    int input = 360781;

    // Part 1: what is the score of the 10 recipies after the puzzle input?
    System.out.println("Part 1: " + scoreAfter(input));
  }
}
