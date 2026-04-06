const Deque = require("collections/deque");
const { PriorityQueue } = require("ds.js");
const _ = require("lodash");

const deque = new Deque([1, 2, 3]);
deque.push(4);
console.log(deque.pop()); // Output: 4

const hashSet = new Set();
hashSet.add(1);
hashSet.add(2);
hashSet.add(1); // Duplicate, will be ignored

console.log(hashSet); // Output: Set { 1, 2 }

const treeSet = new Set([3, 1, 4, 2]);
const sortedArray = Array.from(treeSet).sort((a, b) => a - b);

console.log(sortedArray); // Output: [1, 2, 3, 4]

const hashMap = new Map();
hashMap.set("key1", "value1");
hashMap.set("key2", "value2");
hashMap.set("key3", "value3");

console.log(hashMap.get("key2")); // Output: value2

const treeMap = new Map([
	["b", 2],
	["a", 1],
	["c", 3],
]);
const sortedTreeMap = new Map([...treeMap.entries()].sort());

console.log(sortedTreeMap); // Output: Map { 'a' => 1, 'b' => 2, 'c' => 3 }

const queue = [];
queue.push(1); // Enqueue
queue.push(2);
queue.shift(); // Dequeue

console.log(queue); // Output: [2]

const stack = [];
stack.push(1); // Push
stack.push(2);
stack.pop(); // Pop

console.log(stack); // Output: [1]

class PriorityQueue {
	constructor() {
		this.queue = [];
	}

	enqueue(element, priority) {
		this.queue.push({ element, priority });
		this.queue.sort((a, b) => a.priority - b.priority); // Sort by priority
	}

	dequeue() {
		return this.queue.shift(); // Remove the highest priority element
	}
}

const pq = new PriorityQueue();
pq.enqueue("task1", 2);
pq.enqueue("task2", 1);

console.log(pq.dequeue()); // Output: { element: 'task2', priority: 1 }

const matrix = [
	[1, 2, 3],
	[4, 5, 6],
	[7, 8, 9],
];

console.log(matrix[1][2]); // Output: 6

const people = [
	{ name: "Alice", age: 30 },
	{ name: "Bob", age: 25 },
	{ name: "Charlie", age: 35 },
];

people.sort((a, b) => a.age - b.age);

console.log(people);
// Output: [{ name: 'Bob', age: 25 }, { name: 'Alice', age: 30 }, { name: 'Charlie', age: 35 }]
