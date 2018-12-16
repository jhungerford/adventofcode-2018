package dev.jh.adventofcode;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Day14 {

  /**
   * Returns length scores starting at the given index as a String.
   *
   * @param scores Array of scores
   * @param index Index to start the number at.
   * @param length Length of the returned number
   * @return String representation of the scores.
   */
  private static String number(int[] scores, int index, int length) {
    return IntStream.range(index, index + length)
        .mapToObj(i -> Integer.toString(scores[i]))
        .collect(Collectors.joining());
  }

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

    return number(scores, number, 10);
  }

  /**
   * Returns the number of recipes that appear before the given number.
   *
   * @param number Number of the recipe to look up
   * @return Number of recipes on the scoreboard to the left of the recipe.
   */
  public static long recipesBefore(String number) {
    int[] scores = new int[10];
    scores[0] = 3;
    scores[1] = 7;

    int aIndex = 0;
    int bIndex = 1;
    int nextScoreIndex = 2;

    while (true) {
      // New score is the sum of the scores of the recipes that the elves are on, expanding the scores if necessary.
      int newScore = scores[aIndex] + scores[bIndex];

      // Append the new score to the end of the scores.
      if (newScore < 10) {
        scores[nextScoreIndex] = newScore;
        nextScoreIndex ++;

        if (nextScoreIndex >= number.length() && number(scores, nextScoreIndex - number.length(), number.length()).equals(number)) {
          return nextScoreIndex - number.length();
        }

      } else {
        scores[nextScoreIndex] = 1;
        scores[nextScoreIndex + 1] = newScore % 10;
        nextScoreIndex += 2;

        if (nextScoreIndex > number.length() && number(scores, nextScoreIndex - number.length() - 1, number.length()).equals(number)) {
          return nextScoreIndex - number.length() - 1;
        }

        if (nextScoreIndex >= number.length() && number(scores, nextScoreIndex - number.length(), number.length()).equals(number)) {
          return nextScoreIndex - number.length();
        }
      }

      if (nextScoreIndex > scores.length - 2) {
        scores = Arrays.copyOf(scores, scores.length * 2);
      }

      // Elfs step forward a number of recipes equal to 1 plus the score of their current recipe.
      aIndex = (aIndex + scores[aIndex] + 1) % nextScoreIndex;
      bIndex = (bIndex + scores[bIndex] + 1) % nextScoreIndex;
    }
  }

  public static void main(String[] args) {
    int input = 360781;

    // Part 1: what is the score of the 10 recipies after the puzzle input?
    System.out.println("Part 1: " + scoreAfter(input));

    // Part 2: how many recipes appear on the scoreboard to the left of the score sequence?
    System.out.println("Part 2: " + recipesBefore(Integer.toString(input)));
  }
}
