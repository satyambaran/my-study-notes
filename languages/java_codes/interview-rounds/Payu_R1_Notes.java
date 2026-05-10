/**
 * PAYU ROUND 1 — Interview Notes
 * =================================
 *
 * TOPICS COVERED:
 *
 * === 1. FIND REPEATED NUMBER (LeetCode 287) ===
 *
 * Given array of n+1 integers in range [1, n], find the one repeated number.
 * Constraint: O(1) extra space, don't modify the array.
 *
 * Approaches:
 * a) Binary search on value range [1, n] — O(n log n) time, O(1) space
 *    - For mid = (1+n)/2, count numbers <= mid.
 *    - If count > mid, the duplicate is in [1, mid].
 *    - Else duplicate is in [mid+1, n].
 *
 * b) Floyd's cycle detection — O(n) time, O(1) space
 *    - Treat array as linked list: index -> nums[index].
 *    - Duplicate creates a cycle. Find cycle start = duplicate.
 *
 *
 * === 2. FUNCTION OVERLOADING vs OVERRIDING ===
 *
 * Overloading (compile-time polymorphism):
 *   - Same method name, different parameter types/count.
 *   - Resolved at compile time based on method signature.
 *   - Example: add(int a, int b) vs add(int a, int b, int c)
 *
 * Overriding (runtime polymorphism):
 *   - Subclass provides its own implementation of a parent method.
 *   - Same method signature in parent and child.
 *   - Resolved at runtime based on actual object type.
 *   - Example:
 *       class Animal { void speak() { "..." } }
 *       class Dog extends Animal { void speak() { "Woof" } }
 *       Animal a = new Dog();
 *       a.speak(); // "Woof" — runtime dispatch
 *
 * Covariant return type:
 *   - Overriding method can return a subtype of the parent's return type.
 *   - Example: Parent returns Animal, Child returns Dog.
 *
 *
 * === 3. SQL PROBLEM — Student Pass/Fail ===
 *
 * Tables: Student(id, name), Subject(id, name), Marks(student_id, subject_id, marks)
 * Requirement: List students with Pass (>=35 in ALL subjects) or Fail (<35 in ANY).
 *
 * Solution using GROUP BY + CASE:
 *   SELECT s.id, s.name,
 *     CASE WHEN MIN(m.marks) < 35 THEN 'Fail' ELSE 'Pass' END AS status
 *   FROM Student s
 *   LEFT JOIN Marks m ON s.id = m.student_id
 *   GROUP BY s.id, s.name;
 *
 * KEY INSIGHT: MIN(marks) < 35 means at least one subject is failing.
 *
 * Alternative using UNION:
 *   SELECT id, name, 'Fail' FROM Student WHERE id IN
 *     (SELECT student_id FROM Marks WHERE marks < 35)
 *   UNION
 *   SELECT id, name, 'Pass' FROM Student WHERE id NOT IN
 *     (SELECT student_id FROM Marks WHERE marks < 35);
 */
public class Payu_R1_Notes {
    // This file contains interview discussion notes, not executable code.
    // See dsa/ folder for implementations of the algorithms discussed.
}
