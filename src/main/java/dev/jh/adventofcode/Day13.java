package dev.jh.adventofcode;

import com.google.common.base.Charsets;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class Day13 {

  public static class Position {
    public final int x;
    public final int y;

    public Position(int x, int y) {
      this.x = x;
      this.y = y;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Position position = (Position) o;
      return x == position.x &&
          y == position.y;
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(x, y);
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("x", x)
          .add("y", y)
          .toString();
    }
  }

  public enum CartTurn {
    LEFT, RIGHT, STRAIGHT
  }

  public enum CartDirection {
    UP('^', position -> new Position(position.x, position.y - 1)),
    DOWN('v', position -> new Position(position.x, position.y + 1)),
    LEFT('<', position -> new Position(position.x - 1, position.y)),
    RIGHT('>', position -> new Position(position.x + 1, position.y));

    public final char name;
    public final UnaryOperator<Position> move;

    CartDirection(char name, UnaryOperator<Position> move) {
      this.name = name;
      this.move = move;
    }

    public static Optional<CartDirection> valueOf(char c) {
      for (CartDirection direction : values()) {
        if (direction.name == c) {
          return Optional.of(direction);
        }
      }

      return Optional.empty();
    }
  }

  public static class CartTurnDirection {
    public final CartTurn turn;
    public final CartDirection direction;

    public CartTurnDirection(CartTurn turn, CartDirection direction) {
      this.turn = turn;
      this.direction = direction;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      CartTurnDirection that = (CartTurnDirection) o;
      return turn == that.turn &&
          direction == that.direction;
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(turn, direction);
    }
  }

  public static class Cart {
    public final Position position;
    public final CartDirection direction;
    public final CartTurn nextTurn;

    public Cart(Position position, CartDirection direction, CartTurn nextTurn) {
      this.position = position;
      this.direction = direction;
      this.nextTurn = nextTurn;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Cart cart = (Cart) o;
      return Objects.equal(position, cart.position) &&
          direction == cart.direction &&
          nextTurn == cart.nextTurn;
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(position, direction, nextTurn);
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
          .add("position", position)
          .add("direction", direction)
          .add("nextTurn", nextTurn)
          .toString();
    }
  }

  private static final ImmutableMap<CartDirection, CartDirection> RIGHT_DIRECTIONS = ImmutableMap.of(
      CartDirection.UP, CartDirection.RIGHT,
      CartDirection.DOWN, CartDirection.LEFT,
      CartDirection.LEFT, CartDirection.DOWN,
      CartDirection.RIGHT, CartDirection.UP
  );

  private static final ImmutableMap<CartDirection, CartDirection> LEFT_DIRECTIONS = ImmutableMap.of(
      CartDirection.UP, CartDirection.LEFT,
      CartDirection.DOWN, CartDirection.RIGHT,
      CartDirection.LEFT, CartDirection.UP,
      CartDirection.RIGHT, CartDirection.DOWN
  );

  private static final ImmutableMap<CartTurnDirection, CartDirection> INTERSECTION_DIRECTIONS = ImmutableMap.<CartTurnDirection, CartDirection>builder()
      .put(new CartTurnDirection(CartTurn.LEFT, CartDirection.UP), CartDirection.LEFT)
      .put(new CartTurnDirection(CartTurn.LEFT, CartDirection.DOWN), CartDirection.RIGHT)
      .put(new CartTurnDirection(CartTurn.LEFT, CartDirection.LEFT), CartDirection.DOWN)
      .put(new CartTurnDirection(CartTurn.LEFT, CartDirection.RIGHT), CartDirection.UP)

      .put(new CartTurnDirection(CartTurn.STRAIGHT, CartDirection.UP), CartDirection.UP)
      .put(new CartTurnDirection(CartTurn.STRAIGHT, CartDirection.DOWN), CartDirection.DOWN)
      .put(new CartTurnDirection(CartTurn.STRAIGHT, CartDirection.LEFT), CartDirection.LEFT)
      .put(new CartTurnDirection(CartTurn.STRAIGHT, CartDirection.RIGHT), CartDirection.RIGHT)

      .put(new CartTurnDirection(CartTurn.RIGHT, CartDirection.UP), CartDirection.RIGHT)
      .put(new CartTurnDirection(CartTurn.RIGHT, CartDirection.DOWN), CartDirection.LEFT)
      .put(new CartTurnDirection(CartTurn.RIGHT, CartDirection.LEFT), CartDirection.UP)
      .put(new CartTurnDirection(CartTurn.RIGHT, CartDirection.RIGHT), CartDirection.DOWN)
      .build();

  private static final ImmutableMap<CartTurn, CartTurn> NEXT_TURN = ImmutableMap.of(
      CartTurn.LEFT, CartTurn.STRAIGHT,
      CartTurn.STRAIGHT, CartTurn.RIGHT,
      CartTurn.RIGHT, CartTurn.LEFT
  );

  public enum TrackSegment {
    EMPTY(' ', (CartTurnDirection turnDirection) -> {
      throw new IllegalStateException("Cart cannot be in empty space.");
    }),

    VERTICAL('|', turnDirection -> {
      if (turnDirection.direction != CartDirection.UP && turnDirection.direction != CartDirection.DOWN) {
        throw new IllegalStateException("Direction can only be up and down for vertical segments.");
      }

      return turnDirection;
    }),

    HORIZONTAL('-', turnDirection -> {
      if (turnDirection.direction != CartDirection.LEFT && turnDirection.direction != CartDirection.RIGHT) {
        throw new IllegalStateException("Direction can only be left and right for horizontal segments.");
      }

      return turnDirection;
    }),

    CURVE_RIGHT('/', turnDirection -> {
      return new CartTurnDirection(turnDirection.turn, RIGHT_DIRECTIONS.get(turnDirection.direction));
    }),

    CURVE_LEFT('\\', turnDirection -> {
      return new CartTurnDirection(turnDirection.turn, LEFT_DIRECTIONS.get(turnDirection.direction));
    }),

    INTERSECTION('+', turnDirection -> {
      return new CartTurnDirection(
          NEXT_TURN.get(turnDirection.turn),
          INTERSECTION_DIRECTIONS.get(turnDirection)
      );
    });

    public final char name;
    public final UnaryOperator<CartTurnDirection> turn;

    TrackSegment(char name, UnaryOperator<CartTurnDirection> turn) {
      this.name = name;
      this.turn = turn;
    }

    public static Optional<TrackSegment> valueOf(char c) {
      for (TrackSegment segment : values()) {
        if (segment.name == c) {
          return Optional.of(segment);
        }
      }

      return Optional.empty();
    }
  }

  public static class Track {
    public final TrackSegment[][] track;
    public final ImmutableSet<Cart> carts;

    public Track(TrackSegment[][] track, ImmutableSet<Cart> carts) {
      this.track = track;
      this.carts = carts;
    }

    public Track tick() {
      // Carts move one at a time, sorted by row then column.  Can a crash happen in the middle of a tick?
      Set<Cart> newCarts = new HashSet<>();

      PriorityQueue<Cart> orderedCarts = new PriorityQueue<>(Comparator
          .comparing((Cart cart) -> cart.position.y)
          .thenComparing(cart -> cart.position.x));

      orderedCarts.addAll(carts);

      while (!orderedCarts.isEmpty()) {
        Cart cart = orderedCarts.remove();
        Position newPosition = cart.direction.move.apply(cart.position);
        CartTurnDirection newTurnDirection = track[newPosition.y][newPosition.x]
            .turn.apply(new CartTurnDirection(cart.nextTurn, cart.direction));


        boolean collision = orderedCarts.stream().anyMatch(c -> c.position.equals(newPosition))
            || newCarts.stream().anyMatch(c -> c.position.equals(newPosition));

        if (collision) {
          System.out.println("Mid-tick collision at " + newPosition);
        }

        newCarts.add(new Cart(newPosition, newTurnDirection.direction, newTurnDirection.turn));
      }

      return new Track(track, ImmutableSet.copyOf(newCarts));
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Track track1 = (Track) o;
      return Arrays.deepEquals(track, track1.track) &&
          Objects.equal(carts, track1.carts);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(track, carts);
    }

    @Override
    public String toString() {
      StringBuilder bldr = new StringBuilder();

      Map<Position, List<Cart>> cartPositions = carts.stream()
          .collect(Collectors.groupingBy(cart -> cart.position));

      for (int y = 0; y < track.length; y ++) {
        for (int x = 0; x < track[y].length; x ++) {
          Position position = new Position(x, y);

          List<Cart> positionCarts = cartPositions.getOrDefault(position, ImmutableList.of());

          if (positionCarts.size() == 0) {
            bldr.append(track[y][x].name);
          } else if (positionCarts.size() == 1) {
            bldr.append(positionCarts.iterator().next().direction.name);
          } else {
            bldr.append('X');
          }
        }
        bldr.append('\n');
      }

      return bldr.toString();
    }
  }

  public static Track parseLines(ImmutableList<String> lines) {
    TrackSegment[][] segments = new TrackSegment[lines.size()][lines.get(0).length()];
    ImmutableSet.Builder<Cart> carts = ImmutableSet.builder();

    // Process each character in the lines into a grid of segments and a set of carts
    for (int y = 0; y < lines.size(); y ++) {
      char[] line = lines.get(y).toCharArray();

      for (int x = 0; x < line.length; x ++) {
        char c = line[x];
        Optional<TrackSegment> segment = TrackSegment.valueOf(c);
        if (segment.isPresent()) {
          segments[y][x] = segment.get();
        } else {
          Optional<CartDirection> cartDirection = CartDirection.valueOf(c);
          if (cartDirection.isPresent()) {
            CartDirection direction = cartDirection.get();
            carts.add(new Cart(new Position(x, y), direction, CartTurn.LEFT));

            if (direction == CartDirection.LEFT || direction == CartDirection.RIGHT) {
              segments[y][x] = TrackSegment.HORIZONTAL;
            } else {
              segments[y][x] = TrackSegment.VERTICAL;
            }

          } else {
            throw new IllegalStateException("Character " + c + " at + " + x + "," + y + " isn't a track segment or cart direction");
          }
        }
      }
    }

    return new Track(segments, carts.build());
  }

  public static Position firstCollision(Track track) {
    while (true) {
      track = track.tick();
      Map<Position, Long> cartPositionCounts = track.carts.stream()
          .collect(Collectors.groupingBy(cart -> cart.position, Collectors.counting()));

      Optional<Position> collision = cartPositionCounts.entrySet().stream()
          .filter(positionCounts -> positionCounts.getValue() > 1)
          .map(Map.Entry::getKey)
          .findFirst();

      if (collision.isPresent()) {
        return collision.get();
      }
    }
  }

  public static void main(String[] args) throws IOException {
    File file = new File(Day13.class.getResource("/day13.txt").getFile());
    ImmutableList<String> lines = ImmutableList.copyOf(Files.readLines(file, Charsets.UTF_8));
    Track track = parseLines(lines);

    // Part 1: what's the location of the first collision?
    // TODO: it's not 89,53
    Position firstCollision = firstCollision(track);
    System.out.printf("Part 1: %d,%d\n", firstCollision.x, firstCollision.y);
  }
}
