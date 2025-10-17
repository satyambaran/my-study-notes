// https://claude.ai/chat/4c51a5f5-bda5-4489-b9bc-af6d571aea7f
fn main() {
    println!("=== OWNERSHIP ===\n");
    ownership_demo();
    
    println!("\n=== BORROWING ===\n");
    borrowing_demo();
    
    println!("\n=== LIFETIMES ===\n");
    lifetime_demo();
}

// 1. OWNERSHIP
fn ownership_demo() {
    // Move semantics
    let s1 = String::from("hello");
    let s2 = s1;  // s1 is moved to s2
    
    println!("s2 owns: {}", s2);
    // println!("{}", s1);  // Uncomment to see error!
    
    // Passing to function (moves ownership)
    let s3 = String::from("world");
    takes_ownership(s3);
    // println!("{}", s3);  // Uncomment to see error!
    
    // Copy types don't move
    let x = 5;
    makes_copy(x);
    println!("x still valid: {}", x);  // Works!
}

fn takes_ownership(s: String) {
    println!("Function owns: {}", s);
}  // s is dropped here

fn makes_copy(n: i32) {
    println!("Copy of: {}", n);
}

// 2. BORROWING
fn borrowing_demo() {
    let mut s = String::from("hello");
    
    // Immutable borrow
    let len = calculate_length(&s);
    println!("Length of '{}': {}", s, len);
    
    // Mutable borrow
    append_world(&mut s);
    println!("After mutation: {}", s);
    
    // Multiple immutable borrows
    let r1 = &s;
    let r2 = &s;
    println!("r1: {}, r2: {}", r1, r2);
    
    // Mutable borrow (after immutable borrows are done)
    let r3 = &mut s;
    r3.push_str("!!!");
    println!("After r3: {}", r3);
}

fn calculate_length(s: &String) -> usize {
    s.len()
}  // s goes out of scope, but doesn't drop the data (not owner)

fn append_world(s: &mut String) {
    s.push_str(", world");
}

// 3. LIFETIMES
fn lifetime_demo() {
    let string1 = String::from("long string is long");
    
    {
        let string2 = String::from("xyz");
        let result = longest(string1.as_str(), string2.as_str());
        println!("Longest string: {}", result);
    }  // string2 dropped here
    
    // Struct with lifetime
    let novel = String::from("Call me Ishmael. Some years ago...");
    let first = novel.split('.').next().expect("No sentence");
    let excerpt = Excerpt { part: first };
    println!("Excerpt: {}", excerpt.part);
}

// Function with lifetime annotation
fn longest<'a>(x: &'a str, y: &'a str) -> &'a str {
    if x.len() > y.len() {
        x
    } else {
        y
    }
}

// Struct with lifetime annotation
struct Excerpt<'a> {
    part: &'a str,
}

// Common patterns with lifetimes
impl<'a> Excerpt<'a> {
    fn announce_and_return(&self) -> &str {
        println!("Attention! {}", self.part);
        self.part
    }
}