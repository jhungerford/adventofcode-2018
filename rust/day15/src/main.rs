use day15::Map;

fn main() {
    let map = Map::load("input.txt");

    let mut map_copy = map.clone();
    let mut elf_attack = 3;
    let mut outcome = map.clone().run(elf_attack);

    println!("Part 1: {}", outcome.total);
    println!("Attack {} - outcome: {:?}\n{:?}", elf_attack, &map_copy, &outcome);

    while outcome.num_elves < map.num_elves {
        map_copy = map.clone();
        elf_attack += 1;
        outcome = map_copy.run(elf_attack);

        println!("Attack {} - outcome: {:?}\n{:?}", elf_attack, &map_copy, &outcome);
    }

    println!("Part 2: attack {} - outcome {}", elf_attack, outcome.total);
    // 59708 is too low
}
