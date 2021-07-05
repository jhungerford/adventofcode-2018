use rayon::iter::IntoParallelIterator;
use rayon::iter::ParallelIterator;

use day15::Map;

fn main() {
    let map = Map::load("input.txt");

    println!("Part 1: {}", map.clone().run(3).total);

    let (elf_attack, outcome) = (4..100).into_par_iter()
        .map(|attack| (attack, map.clone().run(attack)))
        .find_first(|(_, outcome)| outcome.num_elves == map.num_elves)
        .unwrap();

    println!("Part 2: attack {} - outcome {}", elf_attack, outcome.total);
}
