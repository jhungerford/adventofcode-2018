use rayon::iter::IntoParallelIterator;
use rayon::iter::ParallelIterator;

use day24::{Game, GroupType};

fn main() {
    let game = Game::load("input.txt");

    println!("Part 1: {}", game.clone().run().unwrap().1);

    let result = (0..100).into_par_iter()
        .flat_map(|boost| game.clone().boost_immune(boost).run())
        .find_first(|(winner, _)| *winner == GroupType::ImmuneSystem)
        .map(|(_, units)| units);

    // 1005 is too high
    if let Some(units) = result {
        println!("Part 2: {}", units);
    } else {
        println!("Part 2 - not enough boost");
    }
}
