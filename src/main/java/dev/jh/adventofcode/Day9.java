package dev.jh.adventofcode;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import java.util.Arrays;

public class Day9 {

  private static class Marble {
    public final int num;

    private Marble next;
    private Marble previous;

    private Marble(int num) {
      this.num = num;
    }

    /**
     * Inserts a new marble with the given number after this marble, returning the new marble.
     *
     * @param marble Number of the marble to insert
     * @return New marble.
     */
    public Marble insertAfter(int marble) {
      Marble newMarble = new Marble(marble);
      newMarble.previous = this;
      newMarble.next = this.next;

      this.next = newMarble;
      newMarble.next.previous = newMarble;

      return newMarble;
    }

    /**
     * Removes this marble from the ring, returning the marble that was removed.
     *
     * @return This marble, which is no longer a part of the ring.
     */
    public Marble remove() {
      this.next.previous = this.previous;
      this.previous.next = this.next;

      this.next = null;
      this.previous = null;

      return this;
    }

    /**
     * Returns the initial marble (0) that points to itself.
     *
     * @return Initial marble.
     */
    public static Marble initial() {
      Marble marble = new Marble(0);
      marble.next = marble;
      marble.previous = marble;

      return marble;
    }

    @Override
    public String toString() {
      return "Marble: " + num;
    }
  }

  public static int highScore(int numPlayers, int lastMarble) {
    // Scores: removed + held marbles, index per player
    int[] scores = new int[numPlayers];
    Marble current = Marble.initial();

    for (int marble = 1; marble <= lastMarble; marble ++) {
      if (marble % 23 == 0) {
        // Remove the marble 7 counter-clockwise from the current marble, and add the held marble's score and the
        // removed marble's score to the player's score
        for (int i = 0; i < 6; i ++) {
          current = current.previous;
        }

        Marble removed = current.previous.remove();

        int player = (marble - 1) % numPlayers;
        scores[player] += marble + removed.num;

      } else {
        // Insert a marble between 1 and 2 marbles clockwise of the current marble.
        current = current.next.insertAfter(marble);
      }
    }

    return Arrays.stream(scores)
        .max()
        .orElseThrow(() -> new IllegalArgumentException("No players."));
  }

  public static void main(String[] args) {
    // Part 1: what is the winning elf's score?
    System.out.println("Part 1: " + highScore(458, 72019));
  }
}
