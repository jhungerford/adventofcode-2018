use std::cmp::Ordering;
use std::fs::File;
use std::io::{BufRead, BufReader};
use std::str::FromStr;

use itertools::Itertools;

use std::collections::{HashMap, HashSet, VecDeque};
use std::fmt::{Debug, Formatter, Write};

// Wall #
// Cavern .
// Goblin G
// Elf E

// Round: each alive unit takes a turn, resolving its actions before next unit
// Turn: Move into range of enemy, then attack
// Ties: resolved in reading order - top-to-bottom, left-to-right
// Combat ends when no targets remain
// Range: adjacent (up, down, left, or right)
// Move: fewest steps to adjacent square of enemy, resolving ties in reading order
//       units can't move through walls or other units, including allies.
//       unit takes a _single_ step towards destination, resolving ties in reading order.
// Attack: deals damage equal to attack power, reducing enemy HP by that amount.
//         if resulting unit's HP is <= 0, it dies and square becomes .
//         targets the unit with the lowest HP, resolving ties in reading order
// Units (Goblins and Elves) have 3 attack power and start with 200 HP
// Output: battle outcome - # of rounds completed (not counting the round where combat ends)
// multiplied by the sum of the HP of all remaining units at the moment combat ends - when a unit
// finds no targets during its turn).

const INITIAL_HP: i32 = 200;
const DEFAULT_ATTACK: i32 = 3;

#[derive(Debug, Eq, PartialEq)]
struct ParseErr {}

#[derive(Eq, PartialEq, Copy, Clone, Hash)]
enum Square {
    Open,
    Wall,
    Goblin,
    Elf,
}

impl FromStr for Square {
    type Err = ParseErr;

    fn from_str(s: &str) -> Result<Self, Self::Err> {
        match s {
            "#" => Ok(Square::Wall),
            "." => Ok(Square::Open),
            "G" => Ok(Square::Goblin),
            "E" => Ok(Square::Elf),
            _ => Err(ParseErr {}),
        }
    }
}

impl Debug for Square {
    fn fmt(&self, f: &mut Formatter<'_>) -> core::fmt::Result {
        let c = match self {
            Square::Open => '.',
            Square::Wall => '#',
            Square::Goblin => 'G',
            Square::Elf => 'E',
        };

        f.write_char(c)
    }
}

#[cfg(test)]
mod square_tests {
    use super::*;

    #[test]
    fn parse() {
        assert_eq!("#".parse(), Ok(Square::Wall));
        assert_eq!(".".parse(), Ok(Square::Open));
        assert_eq!("G".parse(), Ok(Square::Goblin));
        assert_eq!("E".parse(), Ok(Square::Elf));
    }
}

#[derive(Eq, PartialEq, PartialOrd, Hash, Copy, Clone)]
struct Position {
    row: usize,
    col: usize,
}

impl Position {
    /// Creates a new Position at the given row and column.
    fn new(row: usize, col: usize) -> Position {
        Position { row, col }
    }
}

impl Ord for Position {
    fn cmp(&self, other: &Self) -> Ordering {
        self.row.cmp(&other.row)
            .then(self.col.cmp(&other.col))
    }
}

impl Debug for Position {
    fn fmt(&self, f: &mut Formatter<'_>) -> core::fmt::Result {
        f.debug_tuple("Position")
            .field(&self.row)
            .field(&self.col)
            .finish()
    }
}

#[derive(Eq, PartialEq, Clone)]
pub struct Map {
    hp: HashMap<Position, i32>,
    squares: Vec<Vec<Square>>,
    pub num_elves: usize,
    pub num_goblins: usize,
}

impl Debug for Map {
    fn fmt(&self, f: &mut Formatter<'_>) -> core::fmt::Result {
        write!(f, "Elves: {}, Goblins: {}, HP: {:?}\n", self.num_elves, self.num_goblins, self.hp)?;

        for row in 0..self.squares.len() {
            let mut units = vec![];

            for col in 0..self.squares[row].len() {
                let square = self.squares[row][col];
                write!(f, "{:?}", square)?;

                if square == Square::Goblin {
                    units.push(format!("G({})", self.hp[&Position::new(row, col)]));
                } else if square == Square::Elf {
                    units.push(format!("E({})", self.hp[&Position::new(row, col)]));
                }
            }

            write!(f, "    {}\n", units.iter().join(", "))?;
        }

        Ok(())
    }
}

impl Map {

    /// Loads a map from the given file.
    pub fn load(filename: &str) -> Map {
        let f = File::open(filename).unwrap();
        let f = BufReader::new(f);

        let squares: Vec<Vec<Square>> = f.lines()
            .map(|line| line.unwrap().chars().map(|c| c.to_string().parse().unwrap()).collect())
            .collect();

        let mut hp = HashMap::new();
        let mut num_elves = 0;
        let mut num_goblins = 0;

        for row in 0..squares.len() {
            for col in 0..squares[row].len() {
                let square = squares[row][col];
                if square == Square::Goblin || square == Square::Elf {
                    hp.insert(Position::new(row, col), INITIAL_HP);
                }

                if square == Square::Goblin {
                    num_goblins += 1;
                }

                if square == Square::Elf {
                    num_elves += 1;
                }
            }
        }

        Map { hp, squares, num_elves, num_goblins }
    }

    /// Runs combat on the map, returning the result.  Modifies the positions of units.
    pub fn run(&mut self, elf_attack: i32) -> Outcome {
        let mut rounds = 0;
        // println!("Initial {}: {:?}", &rounds, &self);

        while self.run_round(elf_attack) {
            rounds += 1;

            // println!("Round {}: {:?}", &rounds, &self);
        }

        let total_hp = self.hp.values().sum();
        // println!("Final - round {} total hp {}: {:?}", &rounds, &total_hp, &self);

        Outcome::new(rounds, total_hp, self.num_goblins, self.num_elves)
    }

    /// Runs a round of combat on this map, modifying units.  Returns whether combat continues.
    fn run_round(&mut self, elf_attack: i32) -> bool {
        // Units take their turns in reading order.
        let all_units = self.units();
        let mut defeated_units = HashSet::new();

        for unit in all_units {
            // Stop the round if there are no more enemies.
            if self.is_over() {
                return false;
            }

            // Ignore units that were defeated before their turn.
            if self.get(&unit) == Square::Open || defeated_units.contains(&unit) {
                continue;
            }

            // Unit type determines attack power.
            let attack;
            if self.squares[unit.row][unit.col] == Square::Elf {
                attack = elf_attack;
            } else {
                attack = DEFAULT_ATTACK;
            }

            // Units move if they aren't in range of an enemy.
            let mut target = self.attack_target(&unit);
            if target == None {
                if let Some(new_unit) = self.move_unit(&unit) {
                    target = self.attack_target(&new_unit);
                }
            }

            // Then attack if they are in range.
            if let Some(target_pos) = target {
                let defeated = self.attack(&target_pos, attack);
                if defeated {
                    defeated_units.insert(target_pos);
                }
            }
        }

        true
    }

    /// Moves the unit in the given position one square closer to its target.
    fn move_unit(&mut self, unit: &Position) -> Option<Position> {
        if let Some(target) = self.move_square(unit) {
            self.squares[target.row][target.col] = self.squares[unit.row][unit.col];
            self.squares[unit.row][unit.col] = Square::Open;

            self.hp.insert(target.clone(), self.hp[unit]);
            self.hp.remove(unit);

            return Some(target);
        }

        None
    }

    /// Returns the square that the given unit should move to, or empty if the unit shouldn't move.
    fn move_square(&self, unit: &Position) -> Option<Position> {
        #[derive(Debug, Clone)]
        struct ToVisit {
            position: Position,
            direction: Position,
            steps: usize,
        }

        // Track visited positions, and positions to visit in order.
        let enemy_square = self.enemy(unit);
        let mut visited = HashSet::new();
        let mut to_visit = VecDeque::new();

        // Visit the unit's neighbors first, unless it's already next to an enemy.
        for (neighbor_pos, neighbor_square) in self.neighbors(unit) {
            if neighbor_square == enemy_square {
                return None;
            } else if neighbor_square == Square::Open {
                to_visit.push_back(ToVisit {
                    position: neighbor_pos.clone(),
                    direction: neighbor_pos,
                    steps: 1
                });
            }
        }

        // Keep exploring positions until we find the level with an enemy neighbor.
        // The first time we see a position is the shortest path since to_visit is in reading order.
        let mut level = 1;
        let mut targets = Vec::new();

        while let Some(pos) = to_visit.pop_front() {
            // Explore until we find all of the targets on a level.
            if pos.steps > level {
                if !targets.is_empty() {
                    break;
                } else {
                    level = pos.steps;
                }
            }

            // Explore the open neighboring squares.
            let neighbors = self.neighbors(&pos.position);

            for (neighbor_pos, neighbor_square) in neighbors {
                if neighbor_square == enemy_square {
                    targets.push(pos.clone());
                } else if neighbor_square == Square::Open && ! visited.contains(&neighbor_pos) {
                    visited.insert(neighbor_pos);
                    to_visit.push_back(ToVisit {
                        position: neighbor_pos.clone(),
                        direction: pos.direction,
                        steps: pos.steps + 1,
                    })
                }
            }
        }

        // Pick the target in reading order.
        targets.into_iter()
            .sorted_by_key(|target| target.position)
            .map(|target| target.direction)
            .next()
    }

    /// Returns the position of the target that the given unit will attack, or empty if no
    /// enemies are in range.  Units will attack the adjacent enemy with the fewest HP,
    /// resolving ties in reading order.
    fn attack_target(&self, pos: &Position) -> Option<Position> {
        let enemy_square = self.enemy(pos);
        self.neighbors(pos).iter()
            .filter(|(_, square)| *square == enemy_square)
            .map(|(pos, _)| pos)
            .sorted_by(|&a, &b| self.hp[a].cmp(&self.hp[b]).then(a.cmp(b)))
            .cloned()
            .next()
    }

    /// Attacks the enemy at the given position, returning whether the enemy was defeated.
    fn attack(&mut self, pos: &Position, attack: i32) -> bool {
        let new_hp = self.hp[pos] - attack;
        let defeated = new_hp < 0;

        if defeated {
            let unit_type = self.squares[pos.row][pos.col];
            if unit_type == Square::Elf {
                self.num_elves -= 1;
            } else {
                self.num_goblins -= 1;
            }

            self.hp.remove(pos);
            self.squares[pos.row][pos.col] = Square::Open;
        } else {
            self.hp.insert(pos.clone(), new_hp);
        }

        defeated
    }

    /// Returns neighboring squares around the given position, in reading order.
    fn neighbors(&self, position: &Position) -> Vec<(Position, Square)> {
        let adds: [(i32, i32); 4] = [(-1, 0), (0, -1), (0, 1), (1, 0)];

        adds.iter().flat_map(|(row_add, col_add)| {
            let out_of_bounds = (position.row == 0 && *row_add == -1)
                || (position.col == 0 && *col_add == -1)
                || (position.row == self.squares.len() - 1 && *row_add == 1)
                || (position.col == self.squares[position.row].len() - 1 && *col_add == 1);

            if out_of_bounds {
                None
            } else {
                let new_pos = Position::new(
                    (position.row as i32 + row_add) as usize,
                    (position.col as i32 + col_add) as usize);
                let new_square = self.get(&new_pos);

                Some((new_pos, new_square))
            }
        }).collect()
    }

    /// Returns the square at the given position.
    fn get(&self, position: &Position) -> Square {
        self.squares[position.row][position.col]
    }

    /// Returns a list of unit positions, sorted in reading order.
    fn units(&self) -> Vec<Position> {
        self.hp.keys().sorted().cloned().collect()
    }

    /// Returns the enemy type of the unit in the given position.
    fn enemy(&self, pos: &Position) -> Square {
        match self.get(pos) {
            Square::Goblin => Square::Elf,
            Square::Elf => Square::Goblin,
            _ => panic!("No unit at position {:?}", pos),
        }
    }

    fn is_over(&self) -> bool {
        self.num_elves == 0 || self.num_goblins == 0
    }
}

#[cfg(test)]
mod map_tests {
    use super::*;

    #[test]
    fn parse() {
        let map = Map::load("sample.txt");

        let expected_hp: HashMap<Position, i32> = vec![
            (Position::new(1, 2), INITIAL_HP),
            (Position::new(2, 5), INITIAL_HP),
            (Position::new(3, 5), INITIAL_HP),
            (Position::new(4, 3), INITIAL_HP),
            (Position::new(2, 4), INITIAL_HP),
            (Position::new(4, 5), INITIAL_HP),
        ].iter().cloned().collect();

        let expected = Map {
            hp: expected_hp,
            squares: vec![
                vec![Square::Wall, Square::Wall, Square::Wall, Square::Wall, Square::Wall, Square::Wall, Square::Wall],
                vec![Square::Wall, Square::Open, Square::Goblin, Square::Open, Square::Open, Square::Open, Square::Wall],
                vec![Square::Wall, Square::Open, Square::Open, Square::Open, Square::Elf, Square::Goblin, Square::Wall],
                vec![Square::Wall, Square::Open, Square::Wall, Square::Open, Square::Wall, Square::Goblin, Square::Wall],
                vec![Square::Wall, Square::Open, Square::Open, Square::Goblin, Square::Wall, Square::Elf, Square::Wall],
                vec![Square::Wall, Square::Open, Square::Open, Square::Open, Square::Open, Square::Open, Square::Wall],
                vec![Square::Wall, Square::Wall, Square::Wall, Square::Wall, Square::Wall, Square::Wall, Square::Wall],
            ],
            num_elves: 2,
            num_goblins: 4,
        };

        assert_eq!(expected, map);
    }

    #[test]
    fn run_sample() {
        let map = Map::load("sample.txt");
        let outcome = map.clone().run(DEFAULT_ATTACK);
        assert_eq!(27730, outcome.total);
        assert_eq!(Outcome::new(47, 590, 4, 0), outcome);

        assert_eq!(Outcome::new(29, 172, 0, 2), map.clone().run(15));
    }

    #[test]
    fn run_sample2() {
        let map = Map::load("sample2.txt");
        assert_eq!(Outcome::new(37, 982, 0, 5), map.clone().run(DEFAULT_ATTACK));
    }

    #[test]
    fn run_sample3() {
        let map = Map::load("sample3.txt");
        assert_eq!(Outcome::new(46, 859, 0, 5), map.clone().run(DEFAULT_ATTACK));
        assert_eq!(Outcome::new(33, 948, 0, 6), map.clone().run(4));
    }

    #[test]
    fn run_sample4() {
        let map = Map::load("sample4.txt");
        assert_eq!(Outcome::new(35, 793, 5, 0), map.clone().run(DEFAULT_ATTACK));
        assert_eq!(Outcome::new(37, 94, 0, 2), map.clone().run(15));
    }

    #[test]
    fn run_sample5() {
        let map = Map::load("sample5.txt");
        assert_eq!(Outcome::new(54, 536, 4, 0), map.clone().run(DEFAULT_ATTACK));
        assert_eq!(Outcome::new(39, 166, 0, 2), map.clone().run(12));
    }

    #[test]
    fn run_sample6() {
        let map = Map::load("sample6.txt");
        assert_eq!(Outcome::new(20, 937, 5, 0), map.clone().run(DEFAULT_ATTACK));
        assert_eq!(Outcome::new(30, 38, 0, 1), map.clone().run(34));
    }

    #[test]
    fn run_round_sample() {
        let mut map = Map::load("sample.txt");

        map.run_round(DEFAULT_ATTACK);

        let expected_hp = vec![
            (Position::new(1, 3), 200),
            (Position::new(2, 5), 197),
            (Position::new(3, 3), 200),
            (Position::new(3, 5), 197),
            (Position::new(2, 4), 197),
            (Position::new(4, 5), 197),
        ].iter().cloned().collect();

        let expected = Map {
            hp: expected_hp,
            squares: vec![
                vec![Square::Wall, Square::Wall, Square::Wall, Square::Wall, Square::Wall, Square::Wall, Square::Wall],
                vec![Square::Wall, Square::Open, Square::Open, Square::Goblin, Square::Open, Square::Open, Square::Wall],
                vec![Square::Wall, Square::Open, Square::Open, Square::Open, Square::Elf, Square::Goblin, Square::Wall],
                vec![Square::Wall, Square::Open, Square::Wall, Square::Goblin, Square::Wall, Square::Goblin, Square::Wall],
                vec![Square::Wall, Square::Open, Square::Open, Square::Open, Square::Wall, Square::Elf, Square::Wall],
                vec![Square::Wall, Square::Open, Square::Open, Square::Open, Square::Open, Square::Open, Square::Wall],
                vec![Square::Wall, Square::Wall, Square::Wall, Square::Wall, Square::Wall, Square::Wall, Square::Wall],
            ],
            num_elves: 2,
            num_goblins: 4,
        };

        assert_eq!(expected, map);
    }

    #[test]
    fn move_square() {
        let map = Map::load("sample.txt");

        assert_eq!(Some(Position::new(1, 3)), map.move_square(&Position::new(1, 2)));
        assert_eq!(None, map.move_square(&Position::new(2, 4)));
        assert_eq!(None, map.move_square(&Position::new(2, 5)));
        assert_eq!(None, map.move_square(&Position::new(3, 5)));
        assert_eq!(Some(Position::new(3, 3)), map.move_square(&Position::new(4, 3)));
        assert_eq!(None, map.move_square(&Position::new(4, 5)));
    }

    #[test]
    fn move_square_movement1() {
        let map = Map::load("movement1.txt");

        assert_eq!(Some(Position::new(3, 4)), map.move_square(&Position::new(3, 3)));
    }

    #[test]
    fn neighbors() {
        let map = Map::load("sample.txt");

        assert_eq!(
            vec![
                (Position::new(0, 2), Square:: Wall),
                (Position::new(1, 1), Square::Open),
                (Position::new(1, 3), Square::Open),
                (Position::new(2, 2), Square::Open),
            ],
            map.neighbors(&Position::new(1, 2)));

        assert_eq!(
            vec![
                (Position::new(1, 5), Square::Open),
                (Position::new(2, 4), Square::Elf),
                (Position::new(2, 6), Square::Wall),
                (Position::new(3, 5), Square::Goblin),
            ],
            map.neighbors(&Position::new(2, 5)));
    }

    #[test]
    fn get() {
        let map = Map::load("sample.txt");

        assert_eq!(Square::Wall, map.get(&Position::new(0, 0)));
        assert_eq!(Square::Open, map.get(&Position::new(1, 1)));
        assert_eq!(Square::Goblin, map.get(&Position::new(1, 2)));
        assert_eq!(Square::Elf, map.get(&Position::new(2, 4)));
    }
}

#[derive(Debug, Eq, PartialEq)]
pub struct Outcome {
    /// Number of full rounds that were completed.
    pub rounds: i32,
    /// Total remaining HP of living units.
    pub hp: i32,
    /// Battle outcome: rounds * hp
    pub total: i32,
    pub num_goblins: usize,
    pub num_elves: usize,
}

impl Outcome {
    fn new(rounds: i32, hp: i32, num_goblins: usize, num_elves: usize) -> Outcome {
        Outcome { rounds, hp, total: rounds * hp, num_goblins, num_elves }
    }
}

#[cfg(test)]
mod outcome_tests {
    use super::*;

    #[test]
    fn compute_total() {
        let outcome = Outcome::new(20, 937, 0, 0);
        assert_eq!(20, outcome.rounds);
        assert_eq!(937, outcome.hp);
        assert_eq!(18740, outcome.total);
    }
}