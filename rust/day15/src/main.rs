use day15::Map;

fn main() {
    let mut map = Map::load("input.txt");
    let outcome = map.run();

    println!("Part 1: {}", outcome.total);
}
