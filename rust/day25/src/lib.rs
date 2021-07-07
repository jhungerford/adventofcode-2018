// Coordinates are 4-dimensional
// Find the number of constellations of points in the list
// Two points are in the same constellation if their manhatten distance is no more than 3
// or if they can form a chain a points no more than 3 from the last.

use std::collections::HashSet;
use std::fs::File;
use std::io::{BufRead, BufReader};
use std::num::ParseIntError;
use std::str::FromStr;

use kdtree::KdTree;

#[derive(Debug)]
pub struct ParseErr {
    message: String
}

impl From<ParseIntError> for ParseErr {
    fn from(err: ParseIntError) -> Self {
        ParseErr {
            message: err.to_string()
        }
    }
}

#[derive(Debug, Eq, PartialEq, Hash, Clone)]
pub struct Coordinate {
    x: i32,
    y: i32,
    z: i32,
    t: i32,
}

impl FromStr for Coordinate {
    type Err = ParseErr;

    fn from_str(s: &str) -> Result<Self, Self::Err> {
        let nums: Vec<i32> = s.split(",")
            .map(|segment| segment.parse().unwrap())
            .collect();
        if nums.len() != 4 {
            return Err(ParseErr{message: format!("Invalid coordinate: '{}'", s)});
        }

        Ok(Coordinate {
            x: nums[0],
            y: nums[1],
            z: nums[2],
            t: nums[3],
        })
    }
}

impl Coordinate {
    fn to_arr(&self) -> [f32; 4] {
        [self.x as f32, self.y as f32, self.z as f32, self.t as f32]
    }
}

fn manhatten_distance(a: &[f32], b: &[f32]) -> f32 {
    a.iter().zip(b.iter())
        .map(|(x, y)| (x - y).abs())
        .sum()
}

pub fn load(filename: &str) -> Vec<Coordinate> {
    let f = File::open(filename).unwrap();
    let f = BufReader::new(f);

    let mut coords = Vec::new();
    for line in f.lines() {
        coords.push(line.unwrap().parse().unwrap());
    }

    coords
}

pub fn num_constellations(coords: Vec<Coordinate>) -> usize {
    let mut kdtree = KdTree::with_capacity(4, coords.len());
    let mut visited = HashSet::new();

    // Push points into the tree.
    for coord in &coords {
        kdtree.add(coord.to_arr(), coord).unwrap();
    }

    // Pick a coordinate, explore all of its constellation neighbors.
    let mut num_constellations = 0;
    for coord in &coords {
        if visited.contains(&coord) {
            continue;
        }

        num_constellations += 1;

        let mut neighbors = vec![coord];
        while let Some(neighbor) = neighbors.pop() {
            for (_, c) in kdtree.within(&neighbor.to_arr(), 3.0, &manhatten_distance).unwrap() {
                if !visited.contains(c) {
                    visited.insert(c);
                    neighbors.push(c);
                }
            }
        }
    }

    num_constellations
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn parse_coordinate() {
        let expected = Coordinate {
            x: -1,
            y: 2,
            z: 2,
            t: 0
        };

        let actual = "-1,2,2,0".parse().unwrap();

        assert_eq!(expected, actual);
    }

    #[test]
    fn dist() {
        let a: Coordinate = "8,-4,-1,-6".parse().unwrap();
        let b: Coordinate = "-8,5,0,-7".parse().unwrap();

        assert_eq!(27.0, manhatten_distance(&a.to_arr(), &b.to_arr()))
    }

    #[test]
    fn sample() {
        assert_eq!(2, num_constellations(load("sample.txt")));
    }

    #[test]
    fn sample2() {
        assert_eq!(4, num_constellations(load("sample2.txt")));
    }

    #[test]
    fn sample3() {
        assert_eq!(3, num_constellations(load("sample3.txt")));
    }

    #[test]
    fn sample4() {
        assert_eq!(8, num_constellations(load("sample4.txt")));
    }
}