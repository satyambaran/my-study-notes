use std::io::{self, BufRead};

fn main() {
    // Method 1: Using stdin with BufRead (Recommended for CP)
    let stdin = io::stdin();
    let mut lines = stdin.lock().lines();

    // Read a single number n
    let n: usize = lines.next().unwrap().unwrap().trim().parse().unwrap();

    println!("n = {}", n);

    // Read a vector of n integers (space-separated on one line)
    let vec: Vec<i32> = lines
        .next()
        .unwrap()
        .unwrap()
        .split_whitespace()
        .map(|x| x.parse().unwrap())
        .collect();

    println!("Vector: {:?}", vec);

    // Read a string
    let s: String = lines.next().unwrap().unwrap();

    println!("String: {}", s);
}

// ============================================
// Method 2: Simple macro-based approach
// ============================================

// Uncomment below to use this simpler method:

/*
macro_rules! input {
    ($($t:ty),*) => {{
        let mut line = String::new();
        io::stdin().read_line(&mut line).unwrap();
        let mut iter = line.split_whitespace();
        ($(iter.next().unwrap().parse::<$t>().unwrap(),)*)
    }};
}

fn main() {
    // Read single number
    let (n,) = input!(usize);
    println!("n = {}", n);

    // Read vector
    let mut line = String::new();
    io::stdin().read_line(&mut line).unwrap();
    let vec: Vec<i32> = line.split_whitespace()
        .map(|x| x.parse().unwrap())
        .collect();
    println!("Vector: {:?}", vec);

    // Read string
    let mut s = String::new();
    io::stdin().read_line(&mut s).unwrap();
    let s = s.trim();
    println!("String: {}", s);
}
*/

// ============================================
// Method 3: Read multiple numbers on same line
// ============================================

/*
fn main() {
    let mut line = String::new();
    io::stdin().read_line(&mut line).unwrap();
    let mut iter = line.split_whitespace();

    let n: usize = iter.next().unwrap().parse().unwrap();
    let m: usize = iter.next().unwrap().parse().unwrap();

    println!("n = {}, m = {}", n, m);

    // Read vector
    let mut line = String::new();
    io::stdin().read_line(&mut line).unwrap();
    let vec: Vec<i32> = line.split_whitespace()
        .map(|x| x.parse().unwrap())
        .collect();

    println!("Vector: {:?}", vec);
}
*/

// ============================================
// Method 4: Competitive Programming Template
// ============================================

/*
use std::io::{self, BufRead};

fn solve() {
    let stdin = io::stdin();
    let mut lines = stdin.lock().lines();

    // Read n
    let n: usize = lines.next().unwrap().unwrap().parse().unwrap();

    // Read vector of size n
    let arr: Vec<i64> = lines.next()
        .unwrap()
        .unwrap()
        .split_whitespace()
        .map(|x| x.parse().unwrap())
        .collect();

    // Read a string
    let s: String = lines.next().unwrap().unwrap();

    // Your solution here
    println!("n = {}", n);
    println!("array = {:?}", arr);
    println!("string = {}", s);
}

fn main() {
    solve();
}
*/
