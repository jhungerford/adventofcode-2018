package dev.jh.adventofcode;

import com.google.common.base.Charsets;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Day15 {

  public enum Square {
    WALL('#'),
    OPEN('.'),
    GOBLIN('G'),
    ELF('E');

    public final char name;

    Square(char name) {
      this.name = name;
    }

    public static Square valueOf(char name) {
      for (Square square : values()) {
        if (square.name == name) {
          return square;
        }
      }

      throw new IllegalArgumentException(name + " is not a valid square.");
    }
  }

  public static class Position implements Comparable<Position> {
    public final int x;
    public final int y;

    public Position(int x, int y) {
      this.x = x;
      this.y = y;
    }

    @Override
    public int compareTo(Position other) {
      int compare = Integer.compare(this.y, other.y);
      if (compare != 0) {
        return compare;
      }

      return Integer.compare(this.x, other.x);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Position position = (Position) o;
      return x == position.x && y == position.y;
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(x, y);
    }

    @Override
    public String toString() {
      return x + "," + y;
    }
  }

  public enum Direction {
    UP(position -> new Position(position.x, position.y - 1)),
    DOWN(position -> new Position(position.x, position.y + 1)),
    LEFT(position -> new Position(position.x - 1, position.y)),
    RIGHT(position -> new Position(position.x + 1, position.y));

    public final UnaryOperator<Position> move;

    Direction(UnaryOperator<Position> move) {
      this.move = move;
    }

    public static Stream<Direction> stream() {
      return Arrays.stream(Direction.values());
    }
  }

  public static class Unit {
    public final Square type;
    public final Position position;
    public final int hp;
    public final int attack;

    public Unit(Square type, Position position) {
      this(type, position, 200, 3);
    }

    public Unit(Square type, Position position, int hp, int attack) {
      this.type = type;
      this.position = position;
      this.hp = hp;
      this.attack = attack;
    }

    /**
     * Returns whether this unit is adjacent to the given position.
     *
     * @param position Position to check
     * @return Whether this unit is immediately above, below, left, or right of the given position.
     */
    public boolean isAdjacent(Position position) {
      return new Position(position.x - 1, position.y).equals(this.position)
          || new Position(position.x + 1, position.y).equals(this.position)
          || new Position(position.x, position.y - 1).equals(this.position)
          || new Position(position.x, position.y + 1).equals(this.position);
    }

    /**
     * Returns a new Unit with the same type, attack, and position as this unit, but with the given HP.
     *
     * @param newHP New HP value for the new unit
     * @return New unit with the given HP.
     */
    public Unit withHP(int newHP) {
      return new Unit(type, position, newHP, attack);
    }

    /**
     * Returns a new Unit with the same type, attack, and HP as this unit, but with the given position.
     *
     * @param newPosition Position the new unit will have
     * @return New unit with the given position.
     */
    public Unit withPosition(Position newPosition) {
      return new Unit(type, newPosition, hp, attack);
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("type", type)
          .add("position", position)
          .add("hp", hp)
          .add("attack", attack)
          .toString();
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Unit unit = (Unit) o;
      return hp == unit.hp &&
          attack == unit.attack &&
          type == unit.type &&
          Objects.equal(position, unit.position);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(type, position, hp, attack);
    }
  }

  public static class Round {
    public final Board board;
    public final boolean completed;

    public Round(Board board, boolean completed) {
      this.board = board;
      this.completed = completed;
    }
  }

  public static class Board {
    public final Square[][] squares;
    public final ImmutableSet<Unit> units;

    public Board(Square[][] squares, ImmutableSet<Unit> units) {
      this.squares = squares;
      this.units = units;
    }

    /**
     * Returns the position where the unit should move to be one square closer to the closest enemy,
     * or the unit's current position if it can't move or it's already next to an enemy.
     *
     * @param newSquares Board with the in-round position of all of the units.
     * @param unit       Unit moved along the board.
     * @param enemies    Set of enemy units.
     * @return Position where the unit should move on this turn.
     */
    private Position nextPosition(Square[][] newSquares, Unit unit, ImmutableSet<Unit> enemies) {
      // Bail if the unit is already adjacent to an enemy.
      ImmutableSet<Position> enemyPositions = enemies.stream()
          .map(enemy -> enemy.position)
          .collect(ImmutableSet.toImmutableSet());

      boolean alreadyAdjacentToEnemy = adjacent(unit.position).anyMatch(enemyPositions::contains);
      if (alreadyAdjacentToEnemy) {
        return unit.position;
      }

      // Identify squares adjacent to enemies
      ImmutableSet<Position> adjacentToEnemies = enemyPositions.stream()
          .flatMap(this::adjacent)
          .filter(position -> newSquares[position.y][position.x] == Square.OPEN)
          .collect(ImmutableSet.toImmutableSet());

      // Determine which squares can be reached in the fewest steps
      ImmutableSet<Position> closestToEnemies = closest(newSquares, ImmutableSet.of(unit.position), adjacentToEnemies);

      if (closestToEnemies.isEmpty()) {
        return unit.position;
      }

      // Determine where the unit can move this turn to get to one of the closest squares.
      ImmutableSet<Position> adjacentToUnit = adjacent(unit.position)
          .filter(position -> newSquares[position.y][position.x] == Square.OPEN)
          .collect(ImmutableSet.toImmutableSet());

      ImmutableSet<Position> moves = closest(newSquares, closestToEnemies, adjacentToUnit);

      // Move is the first move in reading order, or the unit's current position if there are no valid moves.
      return moves.stream()
          .min(Comparator.naturalOrder())
          .orElse(unit.position);
    }

    /**
     * Returns an unfiltered stream of all positions adjacent to the given position.
     *
     * @param position Position the returned values will be adjacent to.
     * @return Stream of adjacent positions.
     */
    private Stream<Position> adjacent(Position position) {
      return Direction.stream().map(direction -> direction.move.apply(position));
    }

    /**
     * Returns the set of end positions that are closest to the given position, starting at any of the start positions.
     *
     * @param newSquares Board with the in-turn position of all units.
     * @param start Starting positions.
     * @param end Positions to find the closest path to.
     * @return End positions filtered to the ones that can be reached in the fewest steps.
     */
    private ImmutableSet<Position> closest(Square[][] newSquares, ImmutableSet<Position> start, ImmutableSet<Position> end) {
      Set<Position> visited = new HashSet<>();
      Set<Position> visitEdge = new HashSet<>();
      visitEdge.addAll(start);

      while (!visitEdge.isEmpty() && Sets.intersection(visited, end).isEmpty()) {
        visited.addAll(visitEdge);
        visitEdge = visitEdge.stream()
            .flatMap(this::adjacent)
            .filter(position -> newSquares[position.y][position.x] == Square.OPEN && !visited.contains(position))
            .collect(ImmutableSet.toImmutableSet());
      }

      return ImmutableSet.copyOf(Sets.intersection(visited, end));
    }

    /**
     * Returns a stream of enemy units based on the latest unit positions in the middle of a round.
     *
     * @param unit      Unit to find enemies of
     * @param newUnits  Set of units that have moved this round.
     * @param turnOrder Queue of units that haven't moved this round.
     * @return Stream of enemy units.
     */
    private Stream<Unit> enemies(Unit unit, Set<Unit> newUnits, PriorityQueue<Unit> turnOrder) {
      return Stream.concat(newUnits.stream(), turnOrder.stream())
          .filter(u -> u.type != unit.type);
    }

    /**
     * Simulates a round of combat on this board, returning a new Board that contains the resulting state.
     *
     * @return New board with the outcome of this round.
     */
    public Round round() {
      Square[][] newSquares = new Square[squares.length][squares[0].length];
      Set<Unit> newUnits = new HashSet<>();

      for (int y = 0; y < newSquares.length; y ++) {
        for (int x = 0; x < newSquares[y].length; x ++) {
          newSquares[y][x] = squares[y][x];
        }
      }

      PriorityQueue<Unit> turnOrder = new PriorityQueue<>(Comparator.comparing(unit -> unit.position));
      turnOrder.addAll(units);

      while (!turnOrder.isEmpty()) {
        Unit unit = turnOrder.remove();

        // Units move by considering squares in range, which of the squares it can reach in the fewest steps,
        // and moving a single step along the shortest path to the square.
        ImmutableSet<Unit> enemies = enemies(unit, newUnits, turnOrder).collect(ImmutableSet.toImmutableSet());
        Position newPosition = nextPosition(newSquares, unit, enemies);

        // Break when the first unit doesn't see any enemies, not when an entire round goes.

        newSquares[unit.position.y][unit.position.x] = Square.OPEN;
        newSquares[newPosition.y][newPosition.x] = unit.type;
        newUnits.add(unit.withPosition(newPosition));

        // Units attack the unit immediately adjacent with the fewest hit points
        Optional<Unit> attackUnit = enemies(unit, newUnits, turnOrder)
            .filter(u -> u.isAdjacent(newPosition))
            .min(Comparator.comparing((Unit u) -> u.hp).thenComparing(u -> u.position));

        if (attackUnit.isPresent()) {
          Unit afterAttackUnit = attackUnit.get().withHP(attackUnit.get().hp - unit.attack);

          if (afterAttackUnit.hp > 0) {
            // Attack unit still alive - update it's HP in the turn order.
            if (turnOrder.remove(attackUnit.get())) {
              turnOrder.add(afterAttackUnit);
            } else if (newUnits.remove(attackUnit.get())) {
              newUnits.add(afterAttackUnit);
            } else {
              throw new IllegalStateException("Attacked unit wasn't in the turn order or new units.");
            }

          } else {
            // Attacked unit fainted - remove it from the board entirely.
            newSquares[afterAttackUnit.position.y][afterAttackUnit.position.x] = Square.OPEN;
            turnOrder.remove(attackUnit.get());
            newUnits.remove(attackUnit.get());

            // If it's the last unit, combat is over.  The round counts if this is the last move of the turn.
            if (enemies(unit, newUnits, turnOrder).count() == 0) {
              boolean complete = turnOrder.isEmpty();
              newUnits.add(unit);
              newUnits.addAll(turnOrder);
              return new Round(new Board(newSquares, ImmutableSet.copyOf(newUnits)), complete);
            }
          }
        }
      }

      return new Round(new Board(newSquares, ImmutableSet.copyOf(newUnits)), true);
    }

    /**
     * Returns whether all of the units of one type have been defeated.
     *
     * @return Whether combat is over.
     */
    public boolean isOver() {
      return units.stream()
          .map(unit -> unit.type)
          .distinct()
          .count() == 1;
    }

    public static Board parseLines(ImmutableList<String> lines) {
      Square[][] squares = new Square[lines.size()][lines.get(0).length()];
      ImmutableSet.Builder<Unit> units = ImmutableSet.builder();

      for (int y = 0; y < squares.length; y ++) {
        String line = lines.get(y);
        // Line is made up by squares, optionally followed by spaces and a comma-separated list of unit HPs.
        // Example: #...EG#   E(197), G(197)

        List<Unit> lineUnits = new ArrayList<>();
        for (int x = 0; x < squares[y].length; x ++) {
          Square square = Square.valueOf(line.charAt(x));

          squares[y][x] = square;

          if (square == Square.GOBLIN || square == Square.ELF) {
            lineUnits.add(new Unit(square, new Position(x, y)));
          }
        }

        int unitNum = 0;
        Matcher matcher = Pattern.compile("[EG]\\((\\d+)\\)").matcher(line);
        while (matcher.find()) {
          Unit unit = lineUnits.get(unitNum);
          lineUnits.set(unitNum, unit.withHP(Integer.parseInt(matcher.group(1))));
          unitNum++;
        }

        units.addAll(lineUnits);
      }

      return new Board(squares, units.build());
    }

    @Override
    public String toString() {
      StringBuilder bldr = new StringBuilder();

      for (Square[] line : squares) {
        for (Square cell : line) {
          bldr.append(cell.name);
        }
        bldr.append('\n');
      }

      return bldr.toString();
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Board board = (Board) o;
      return Objects.equal(toString(), board.toString()) && Objects.equal(units, board.units);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(squares, units);
    }
  }

  /**
   * Returns the outcome of the battle on the given board, which is the number of complete rounds that combat took
   * multiplied by the sum of HP of the remaining units.
   *
   * @param board Board
   * @return
   */
  public static int outcome(Board board) {
    int numRounds = 0;

    while (!board.isOver()) {
      Round round = board.round();
      if (round.completed) {
        numRounds ++;
      }

      board = round.board;
    }

    return numRounds * board.units.stream().mapToInt(unit -> unit.hp).sum();
  }

  public static void main(String[] args) throws IOException {
    File file = new File(Day15.class.getResource("/day15.txt").getFile());
    ImmutableList<String> lines = ImmutableList.copyOf(Files.readLines(file, Charsets.UTF_8));
    Board board = Board.parseLines(lines);

    // Part 1: what is the outcome (# of full rounds * sum of remaining units hit points) of the input?
    // TODO: 344375 is too low, 346750 is also incorrect (which is one more round).  Bug in the target selection logic?
    System.out.println("Part 1: " + outcome(board));
  }
}
