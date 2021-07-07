// Immune System + Infection Armies, composed of groups, composed of 1+ units
// Armies fight until one army has units remaining

// Units have the same HP, attack damage, an attack type, initiative (attack first, win ties),
// and weaknesses / immunities

// Example group: 18 units each with 729 hit points (weak to fire; immune to cold, slashing)
//  with an attack that does 8 radiation damage at initiative 10

// Group has effective power: # of units in the group * attack damage

// Fight consists of target selection and attacking.

// Target selection: in decreasing order of effective power (initiative resolves ties),
// groups chose target - group that will receive the most damage (after accounting for weaknesses
// and immunities, but not accounting for whether the group has enough units to receive the damage.
// Ties resolved by picking the defending group with the largest effective power, then highest initiative.
// Defending groups can only be chosen as a target by one attacking group.

// Attacking: in decreasing initiative order, groups deal damage to the selected target.
// Damage: attacking group does damage equal to the effective power of the defending group.
// Immune means no damage, Weak means double damage.
// Defending group only loses whole units, damage that doesn't kill a unit is ignored.

#[macro_use]
extern crate lazy_static;
extern crate regex;

use std::collections::{HashSet, HashMap};
use std::fs::File;
use std::io::{BufRead, BufReader};
use std::num::ParseIntError;
use std::str::FromStr;

use regex::Regex;
use itertools::Itertools;

#[derive(Debug, Eq, PartialEq, Hash, Clone)]
pub enum DamageType {
    Fire,
    Cold,
    Slashing,
    Radiation,
    Bludgeoning,
}

impl FromStr for DamageType {
    type Err = ParseErr;

    fn from_str(s: &str) -> Result<Self, Self::Err> {
        match s {
            "fire" => Ok(DamageType::Fire),
            "cold" => Ok(DamageType::Cold),
            "slashing" => Ok(DamageType::Slashing),
            "radiation" => Ok(DamageType::Radiation),
            "bludgeoning" => Ok(DamageType::Bludgeoning),

            _ => Err(ParseErr{ message: format!("{} is not a valid damage type", s)}),
        }
    }
}

#[derive(Debug, Eq, PartialEq, Clone)]
pub struct Group {
    units: i32,
    hp: i32,
    immunity: HashSet<DamageType>,
    weakness: HashSet<DamageType>,
    attack: i32,
    attack_type: DamageType,
    initiative: i32,
}

impl Group {
    /// Returns the effective power of this group, with no weaknesses or immunities applied.
    fn raw_power(&self) -> i32 {
        self.units * self.attack
    }

    /// Returns the effective power of this group, accounting for the target's weaknesses and immunities.
    fn power(&self, target: &Group) -> i32 {
        // Immunities take 0 damage.
        if target.immunity.contains(&self.attack_type) {
            return 0;
        }

        // Weakness does double damage.
        if target.weakness.contains(&self.attack_type) {
            return self.raw_power() * 2;
        }

        // No weakness or immunity has the standard power.
        self.raw_power()
    }
}

#[derive(Debug, Eq, PartialEq, Hash, Clone)]
pub enum GroupType {
    ImmuneSystem, Infection,
}

impl GroupType {
    fn enemy(&self) -> Self {
        match self {
            GroupType::ImmuneSystem => GroupType::Infection,
            GroupType::Infection => GroupType::ImmuneSystem,
        }
    }
}

impl FromStr for Group {
    type Err = ParseErr;

    // 4485 units each with 2961 hit points (immune to radiation; weak to fire, cold) with an attack that does 12 slashing damage at initiative 4
    // immune / weak can be in any order, or the parenthesis section can be missing.
    fn from_str(s: &str) -> Result<Self, Self::Err> {
        lazy_static! {
            static ref RE: Regex = Regex::new(concat!(
                r"^(?P<units>\d+) units each with (?P<hp>\d+) hit points ",
                r"(?:[(](?:(?:immune to (?P<immunity>[^);]+))|(?:; )|(?:weak to (?P<weakness>[^);]+)))+[)])?",
                r"\s?with an attack that does (?P<attack>\d+) (?P<attack_type>\w+) damage ",
                r"at initiative (?P<initiative>\d+)$"
            )).unwrap();
        }

        if let Some(captures) = RE.captures(s) {
            let units: i32 = captures.name("units")
                .expect(&format!("No units in {}", s)).as_str().parse()?;

            let hp: i32 = captures.name("hp")
                .expect(&format!("No hp in {}", s)).as_str().parse()?;

            let immunity: HashSet<DamageType> = captures.name("immunity")
                .map(|capture| capture.as_str()
                    .split(", ")
                    .map(|element| element.parse().unwrap())
                    .collect())
                .unwrap_or_default();

            let weakness: HashSet<DamageType> = captures.name("weakness")
                .map(|capture| capture.as_str()
                    .split(", ")
                    .map(|element| element.parse().unwrap())
                    .collect())
                .unwrap_or_default();

            let attack: i32 = captures.name("attack")
                .expect(&format!("No attack in {}", s)).as_str().parse()?;

            let attack_type: DamageType = captures.name("attack_type")
                .expect(&format!("No attack type in {}", s)).as_str().parse()?;

            let initiative: i32 = captures.name("initiative")
                .expect(&format!("No initiative in {}", s)).as_str().parse()?;

            Ok(Group{units, hp, immunity, weakness, attack, attack_type, initiative})
        } else {
            Err(ParseErr {
                message: format!("{} did not match", s)
            })
        }
    }
}

#[derive(Debug, Eq, PartialEq, Clone)]
pub struct Game {
    groups: HashMap<GroupType, Vec<Group>>
}

#[derive(Debug)]
struct Attack {
    attacker_type: GroupType,
    attacker_init: i32,
    defender_init: i32,
}

impl Game {
    fn new(immune_system: Vec<Group>, infection: Vec<Group>) -> Game {
        let groups = vec![
            (GroupType::ImmuneSystem, immune_system),
            (GroupType::Infection, infection)
        ].into_iter().collect();

        Game { groups }
    }

    /// Loads a game from the input in the given file.
    pub fn load(filename: &str) -> Game {
        // Game is 'Immune System:' groups, a newline, then 'Infection:' groups
        let f = File::open(filename).unwrap();
        let f = BufReader::new(f);

        let mut immune_system: Vec<Group> = Vec::new();
        let mut infection: Vec<Group> = Vec::new();
        let mut group= &mut Vec::new();

        for line in f.lines() {
            match line.unwrap().as_str() {
                "Immune System:" => group = &mut immune_system,
                "Infection:" => group = &mut infection,
                group_str if !group_str.is_empty() => group.push(group_str.parse().unwrap()),
                _ => {},

            }
        }

        Game::new(immune_system, infection)
    }

    /// Runs combat in the game, returning the number of units that the winning army has.
    /// Modifies the game in place.
    pub fn run(&mut self) -> i32 {
        while !self.groups[&GroupType::ImmuneSystem].is_empty() && !self.groups[&GroupType::Infection].is_empty() {
            self.step()
        }

        let winner;
        if self.groups[&GroupType::ImmuneSystem].is_empty() {
            winner = &self.groups[&GroupType::Infection];
        } else {
            winner = &self.groups[&GroupType::ImmuneSystem];
        }

        winner.iter().map(|group| group.units).sum()
    }

    /// Performs one round of combat, modifying this game.
    fn step(&mut self) {
        // sorts:
        // target: raw attack power, then initiative (both descending)
        // selection: damage (unique per group with weaknesses / immunities)
        // attack: initiative, descending

        // Immune System:
        // 17 units each with 5390 hit points (weak to radiation, bludgeoning) with an attack that does 4507 fire damage at initiative 2
        // 989 units each with 1274 hit points (immune to fire; weak to bludgeoning, slashing) with an attack that does 25 slashing damage at initiative 3
        //
        // Infection:
        // 801 units each with 4706 hit points (weak to radiation) with an attack that does 116 bludgeoning damage at initiative 1
        // 4485 units each with 2961 hit points (immune to radiation; weak to fire, cold) with an attack that does 12 slashing damage at initiative 4

        // Immune 1: 17 units, 4507 fire damage = 76619
        // Immune 2: 989 units, 25 slashing damage = 24725
        // Infection 1: 801 units, 116 bludgeoning damage = 92916
        // Infection 2: 4485 units, 12 slashing damage = 53820
        // Selection order: Infection 1, Immune 1, Infection 2, Immune 2


        // Target selection: in order of power then initiative, groups choose the target
        // that they'll do the most damage to.
        let attacks = self.select_targets();

        // Attack: in descending initiative order, attacking groups deal damage to defending groups.
        for attack in attacks {
            self.attack(attack);
        }
    }

    /// Picks out targets, returning attacks in the order they should be carried out.
    fn select_targets(&self) -> Vec<Attack> {
        let target_selection: Vec<(&GroupType, &Group)> = self.groups.keys()
            // Vec of (group type, group)
            .flat_map(|group_type| self.groups[group_type].iter().map(move |group| (group_type, group)))
            // Sorted in the order that they should pick targets.
            .sorted_by(|(_, a_group), (_, b_group)|
                b_group.raw_power().cmp(&a_group.raw_power())
                    .then(b_group.initiative.cmp(&a_group.initiative)))
            .collect();

        let mut defending_initiatives = HashSet::new();
        let mut attacks = Vec::new();

        for (attacker_type, attacker_group) in target_selection {
            let maybe_target = self.groups[&attacker_type.enemy()].iter()
                // Defender can only be attacked by a single attacker.
                .filter(|enemy| !defending_initiatives.contains(&enemy.initiative))
                // Attackers pick the group that it will deal the most damage to,
                // then the group with the largest effective power,
                // then the group with the highest initiative.
                .sorted_by(|a, b| attacker_group.power(b).cmp(&attacker_group.power(a))
                    .then(b.raw_power().cmp(&a.raw_power()))
                    .then(b.initiative.cmp(&a.initiative)))
                .next();

            if let Some(target_group) = maybe_target {
                defending_initiatives.insert(target_group.initiative);
                attacks.push(Attack {
                    attacker_type: attacker_type.clone(),
                    attacker_init: attacker_group.initiative,
                    defender_init: target_group.initiative,
                });
            }
        }

        attacks.sort_by(|a, b| b.attacker_init.cmp(&a.attacker_init));

        attacks
    }

    /// Carries out the given attack if the attacker and defender are still alive.
    fn attack(&mut self, attack: Attack) {
        let defender_type = attack.attacker_type.enemy();
        let maybe_attacker = self.groups[&attack.attacker_type].iter().find(|group| group.initiative == attack.attacker_init);
        let maybe_defender = self.groups[&defender_type].iter().find_position(|group| group.initiative == attack.defender_init);

        if maybe_attacker.is_none() || maybe_defender.is_none() {
            return;
        }

        let attacker = maybe_attacker.unwrap();
        let (defender_index, defender) = maybe_defender.unwrap();

        // Defending group only loses whole units from damage.
        let defeated_units = attacker.power(defender) / defender.hp;

        if defeated_units >= defender.units {
            self.groups.get_mut(&defender_type).unwrap().remove(defender_index);
        } else {
            self.groups.get_mut(&defender_type).unwrap()[defender_index].units -= defeated_units;
        }
    }
}

#[derive(Debug, Eq, PartialEq)]
pub struct ParseErr {
    message: String,
}

impl From<regex::Error> for ParseErr {
    fn from(err: regex::Error) -> Self {
        ParseErr {
            message: err.to_string()
        }
    }
}

impl From<ParseIntError> for ParseErr {
    fn from(err: ParseIntError) -> Self {
        ParseErr {
            message: err.to_string()
        }
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn parse_group() {
        let group: Group = "17 units each with 5390 hit points (weak to radiation, bludgeoning) with an attack that does 4507 fire damage at initiative 2".parse().unwrap();
        let expected = Group {
            units: 17,
            hp: 5390,
            immunity: vec![].into_iter().collect(),
            weakness: vec![DamageType::Radiation, DamageType::Bludgeoning].into_iter().collect(),
            attack: 4507,
            attack_type: DamageType::Fire,
            initiative: 2,
        };

        assert_eq!(expected, group);
    }

    #[test]
    fn power() {
        let group = Group {
            units: 17,
            hp: 5390,
            immunity: vec![].into_iter().collect(),
            weakness: vec![DamageType::Radiation, DamageType::Bludgeoning].into_iter().collect(),
            attack: 4507,
            attack_type: DamageType::Fire,
            initiative: 2,
        };

        let normal_group = Group {
            units: 801,
            hp: 4706,
            immunity: vec![].into_iter().collect(),
            weakness: vec![DamageType::Radiation].into_iter().collect(),
            attack: 116,
            attack_type: DamageType::Bludgeoning,
            initiative: 1,
        };

        let immune_group = Group {
            units: 989,
            hp: 1274,
            immunity: vec![DamageType::Fire].into_iter().collect(),
            weakness: vec![DamageType::Bludgeoning, DamageType::Slashing].into_iter().collect(),
            attack: 25,
            attack_type: DamageType::Slashing,
            initiative: 3,
        };

        let weak_group = Group {
            units: 4485,
            hp: 2961,
            immunity: vec![DamageType::Radiation].into_iter().collect(),
            weakness: vec![DamageType::Fire, DamageType::Cold].into_iter().collect(),
            attack: 12,
            attack_type: DamageType::Slashing,
            initiative: 4,
        };

        let power = 17 * 4507;
        assert_eq!(power, group.raw_power());

        assert_eq!(power, group.power(&normal_group));
        assert_eq!(0, group.power(&immune_group));
        assert_eq!(power * 2, group.power(&weak_group));
    }

    #[test]
    fn run_sample() {
        let mut game = Game::load("sample.txt");
        assert_eq!(5216, game.run());
    }

    #[test]
    fn step_sample() {
        let mut game = Game::load("sample.txt");

        let immune_group1 = Group {
            units: 17,
            hp: 5390,
            immunity: vec![].into_iter().collect(),
            weakness: vec![DamageType::Radiation, DamageType::Bludgeoning].into_iter().collect(),
            attack: 4507,
            attack_type: DamageType::Fire,
            initiative: 2,
        };

        let mut immune_group2 = Group {
            units: 989,
            hp: 1274,
            immunity: vec![DamageType::Fire].into_iter().collect(),
            weakness: vec![DamageType::Bludgeoning, DamageType::Slashing].into_iter().collect(),
            attack: 25,
            attack_type: DamageType::Slashing,
            initiative: 3,
        };

        let mut infection_group1 = Group {
            units: 801,
            hp: 4706,
            immunity: vec![].into_iter().collect(),
            weakness: vec![DamageType::Radiation].into_iter().collect(),
            attack: 116,
            attack_type: DamageType::Bludgeoning,
            initiative: 1,
        };

        let mut infection_group2 = Group {
            units: 4485,
            hp: 2961,
            immunity: vec![DamageType::Radiation].into_iter().collect(),
            weakness: vec![DamageType::Fire, DamageType::Cold].into_iter().collect(),
            attack: 12,
            attack_type: DamageType::Slashing,
            initiative: 4,
        };

        assert_eq!(game, Game::new(
            vec![immune_group1.clone(), immune_group2.clone()],
            vec![infection_group1.clone(), infection_group2.clone()]
        ));

        // Round 1
        immune_group2 = Group {
            units: 905,
            ..immune_group2
        };

        infection_group1 = Group {
            units: 797,
            ..infection_group1
        };

        infection_group2 = Group {
            units: 4434,
            ..infection_group2
        };

        game.step();

        assert_eq!(game, Game::new(
            vec![immune_group2.clone()],
            vec![infection_group1.clone(), infection_group2.clone()]
        ));

        // Round 2
        immune_group2 = Group {
            units: 761,
            ..immune_group2
        };

        infection_group1 = Group {
            units: 793,
            ..infection_group1
        };

        infection_group2 = Group {
            units: 4434,
            ..infection_group2
        };

        game.step();

        assert_eq!(game, Game::new(
            vec![immune_group2.clone()],
            vec![infection_group1.clone(), infection_group2.clone()]
        ));

        // Round 3
        immune_group2 = Group {
            units: 618,
            ..immune_group2
        };

        infection_group1 = Group {
            units: 789,
            ..infection_group1
        };

        infection_group2 = Group {
            units: 4434,
            ..infection_group2
        };

        game.step();

        assert_eq!(game, Game::new(
            vec![immune_group2.clone()],
            vec![infection_group1.clone(), infection_group2.clone()]
        ));

        // Round 4
        immune_group2 = Group {
            units: 475,
            ..immune_group2
        };

        infection_group1 = Group {
            units: 786,
            ..infection_group1
        };

        infection_group2 = Group {
            units: 4434,
            ..infection_group2
        };

        game.step();

        assert_eq!(game, Game::new(
            vec![immune_group2.clone()],
            vec![infection_group1.clone(), infection_group2.clone()]
        ));

        // Round 5
        immune_group2 = Group {
            units: 333,
            ..immune_group2
        };

        infection_group1 = Group {
            units: 784,
            ..infection_group1
        };

        infection_group2 = Group {
            units: 4434,
            ..infection_group2
        };

        game.step();

        assert_eq!(game, Game::new(
            vec![immune_group2.clone()],
            vec![infection_group1.clone(), infection_group2.clone()]
        ));

        // Round 6
        immune_group2 = Group {
            units: 191,
            ..immune_group2
        };

        infection_group1 = Group {
            units: 783,
            ..infection_group1
        };

        infection_group2 = Group {
            units: 4434,
            ..infection_group2
        };

        game.step();

        assert_eq!(game, Game::new(
            vec![immune_group2.clone()],
            vec![infection_group1.clone(), infection_group2.clone()]
        ));

        // Round 7
        immune_group2 = Group {
            units: 49,
            ..immune_group2
        };

        infection_group1 = Group {
            units: 782,
            ..infection_group1
        };

        infection_group2 = Group {
            units: 4434,
            ..infection_group2
        };

        game.step();

        assert_eq!(game, Game::new(
            vec![immune_group2.clone()],
            vec![infection_group1.clone(), infection_group2.clone()]
        ));

        // Round 8
        infection_group1 = Group {
            units: 782,
            ..infection_group1
        };

        infection_group2 = Group {
            units: 4434,
            ..infection_group2
        };

        game.step();

        assert_eq!(game, Game::new(
            vec![],
            vec![infection_group1.clone(), infection_group2.clone()]
        ));
    }
}