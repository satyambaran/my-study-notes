const job = {
	position: "cashier",
	type: "hourly",
	isAvailable: true,
	showDetails() {
		const accepting = this.isAvailable ? "is accepting applications" : "is not currently accepting applications";

		console.log(`The ${this.position} position is ${this.type} and ${accepting}.`);
	},
};

// Use Object.create to pass properties
const barista = Object.create(job);

barista.position = "barista";
barista.showDetails();
console.log(Object.keys(barista));
Object.keys(barista).forEach((key) => {
	let value = barista[key];
	console.log(`${key}: ${value}`);
});
console.log(Object.keys(barista).length);
console.log(Object.values(barista));
console.log(Object.entries(barista));
Object.entries(barista).forEach((entry) => {
	let key = entry[0];
	let value = entry[1];

	console.log(`${key}: ${value}`);
});

//!!!

// Initialize an object
const name = {
	firstName: "Philip",
	lastName: "Fry",
};
// Initialize another object
const details = {
	job: "Delivery Boy",
	employer: "Planet Express",
};
// Merge the objects
const character = Object.assign(name, details);
console.log(character);
const character2 = { ...name, ...details };
console.log(character2);

var id = Object.freeze(name);
id.firstName = "ssatyam";
id.work = "SDE";
console.log(id, Object.isFrozen(id));

var id2 = Object.seal(details);
id2.job = "sde";
id2.work = "SDE";
console.log(id2, Object.isSealed(id2));

// ====================================================================
// Closure pattern: inventory list (from sol.js)
// ====================================================================

function inventoryList() {
	let obj = {
		list: [],
		add: function (str) {
			this.list.push(str);
		},
		remove: function (str) {
			let index = this.list.indexOf(str);
			if (index != -1) {
				this.list.splice(index, 1);
			}
		},
		getList: function () {
			return String(this.list);
		},
	};
	return obj;
}

//? testing after here
let ans = inventoryList();

ans.add("satyam");
console.log(ans.getList());
ans.add("kundan");
console.log(ans.getList());
ans.add("chandan");
console.log(ans.getList());
ans.remove("chandan");
console.log(ans.getList());
ans.remove("abhilash");
console.log(ans.getList());
