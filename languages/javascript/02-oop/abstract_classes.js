class Shape {
	constructor() {
	  if (this.constructor === Shape) {
		throw new Error("Cannot instantiate abstract class");
	  }
	}
  
	area() {
	  throw new Error("Method 'area()' must be implemented.");
	}
  }
  
  class Circle extends Shape {
	constructor(radius) {
	  super();
	  this.radius = radius;
	}
  
	area() {
	  return Math.PI * this.radius ** 2;
	}
  }
  
  const circle = new Circle(10);
  console.log(circle.area()); // Output: 314.159...