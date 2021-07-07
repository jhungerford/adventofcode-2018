use day24::Game;

fn main() {
    let mut game = Game::load("input.txt");

    println!("Part 1: {}", game.run());
}
