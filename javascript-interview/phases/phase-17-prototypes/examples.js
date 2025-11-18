/**
 * ============================================================================
 * PHASE 17: PROTOTYPES & INHERITANCE
 * ============================================================================
 */

// ============================================================================
// SECTION 1: PROTOTYPE BASICS
// ============================================================================

/**
 * Every object has an internal [[Prototype]] link to another object
 * This creates a chain that ends at null
 *
 * __proto__: The actual prototype reference (getter/setter)
 * prototype: Property on functions used when creating new instances
 */

// Object literal
const obj = { a: 1 };
console.log(Object.getPrototypeOf(obj) === Object.prototype); // true
console.log(obj.__proto__ === Object.prototype); // true

// Prototype chain lookup
const parent = { x: 10 };
const child = Object.create(parent);
child.y = 20;

console.log(child.x); // 10 (found on parent)
console.log(child.y); // 20 (found on child)
console.log(child.z); // undefined (not found anywhere)

/**
 * HOW PROPERTY LOOKUP WORKS:
 * 1. Check object itself
 * 2. Check object's [[Prototype]]
 * 3. Continue up the chain
 * 4. Reach null -> undefined
 */

// ============================================================================
// SECTION 2: CONSTRUCTOR FUNCTIONS
// ============================================================================

/**
 * Functions have a 'prototype' property
 * When used with 'new', instances link to this prototype
 */

function Person(name, age) {
    // Instance properties
    this.name = name;
    this.age = age;
}

// Shared methods on prototype (memory efficient)
Person.prototype.greet = function() {
    return `Hello, I'm ${this.name}`;
};

Person.prototype.getAge = function() {
    return this.age;
};

const alice = new Person("Alice", 25);
const bob = new Person("Bob", 30);

// Both share the same greet function
console.log(alice.greet === bob.greet); // true

/**
 * WHY PUT METHODS ON PROTOTYPE:
 * - Memory efficiency (shared, not duplicated)
 * - Can be changed/extended dynamically
 * - Instance properties remain on instance
 */

// ============================================================================
// SECTION 3: HOW 'new' WORKS
// ============================================================================

/**
 * new Constructor():
 * 1. Create empty object
 * 2. Link object's [[Prototype]] to Constructor.prototype
 * 3. Execute Constructor with this = new object
 * 4. Return object (unless Constructor returns different object)
 */

// Manual implementation
function myNew(Constructor, ...args) {
    // Step 1 & 2: Create and link prototype
    const obj = Object.create(Constructor.prototype);

    // Step 3: Execute with this = obj
    const result = Constructor.apply(obj, args);

    // Step 4: Return
    return result instanceof Object ? result : obj;
}

const charlie = myNew(Person, "Charlie", 35);
console.log(charlie.greet()); // "Hello, I'm Charlie"

// ============================================================================
// SECTION 4: PROTOTYPAL INHERITANCE
// ============================================================================

// Parent constructor
function Animal(name) {
    this.name = name;
}

Animal.prototype.speak = function() {
    return `${this.name} makes a sound`;
};

// Child constructor
function Dog(name, breed) {
    Animal.call(this, name);  // Call parent constructor
    this.breed = breed;
}

// Set up prototype chain
Dog.prototype = Object.create(Animal.prototype);
Dog.prototype.constructor = Dog;  // Fix constructor reference

// Add Dog-specific method
Dog.prototype.bark = function() {
    return `${this.name} barks!`;
};

// Override parent method
Dog.prototype.speak = function() {
    return `${this.name} woofs`;
};

const dog = new Dog("Rex", "German Shepherd");
console.log(dog.speak()); // "Rex woofs"
console.log(dog.bark());  // "Rex barks!"
console.log(dog instanceof Dog);    // true
console.log(dog instanceof Animal); // true

// ============================================================================
// SECTION 5: ES6 CLASSES (Syntactic Sugar)
// ============================================================================

class AnimalClass {
    constructor(name) {
        this.name = name;
    }

    speak() {
        return `${this.name} makes a sound`;
    }

    // Static method
    static isAnimal(obj) {
        return obj instanceof AnimalClass;
    }
}

class DogClass extends AnimalClass {
    constructor(name, breed) {
        super(name);  // Must call super() before using this
        this.breed = breed;
    }

    speak() {
        return `${this.name} woofs`;
    }

    bark() {
        return `${this.name} barks!`;
    }
}

const dogClass = new DogClass("Max", "Labrador");
console.log(dogClass.speak()); // "Max woofs"
console.log(DogClass.isAnimal(dogClass)); // true

/**
 * CLASSES ARE STILL PROTOTYPES:
 * - class is syntactic sugar over constructor functions
 * - Methods go on prototype
 * - extends sets up prototype chain
 */

// ============================================================================
// SECTION 6: Object.create()
// ============================================================================

/**
 * Creates object with specified prototype
 * Cleaner than constructor manipulation
 */

const personProto = {
    greet() {
        return `Hello, I'm ${this.name}`;
    },
    init(name, age) {
        this.name = name;
        this.age = age;
        return this;
    }
};

const person1 = Object.create(personProto).init("Alice", 25);
console.log(person1.greet()); // "Hello, I'm Alice"

// Create object with no prototype
const plainObj = Object.create(null);
console.log(plainObj.toString); // undefined (no Object.prototype)

// ============================================================================
// SECTION 7: PROPERTY CHECKING
// ============================================================================

const parent2 = { inherited: true };
const child2 = Object.create(parent2);
child2.own = true;

// in operator - checks entire prototype chain
console.log("own" in child2);       // true
console.log("inherited" in child2); // true

// hasOwnProperty - only checks own properties
console.log(child2.hasOwnProperty("own"));       // true
console.log(child2.hasOwnProperty("inherited")); // false

// Safe version (if hasOwnProperty could be overwritten)
console.log(Object.prototype.hasOwnProperty.call(child2, "own")); // true

// for...in iterates all enumerable (own + inherited)
for (const key in child2) {
    if (child2.hasOwnProperty(key)) {
        console.log("Own:", key);
    } else {
        console.log("Inherited:", key);
    }
}

// Object.keys() - only own enumerable
console.log(Object.keys(child2)); // ["own"]

// ============================================================================
// SECTION 8: MIXINS AND COMPOSITION
// ============================================================================

/**
 * MIXINS: Add functionality from multiple sources
 * Alternative to single inheritance
 */

const canWalk = {
    walk() {
        return `${this.name} is walking`;
    }
};

const canSwim = {
    swim() {
        return `${this.name} is swimming`;
    }
};

const canFly = {
    fly() {
        return `${this.name} is flying`;
    }
};

class Bird {
    constructor(name) {
        this.name = name;
    }
}

// Mix in capabilities
Object.assign(Bird.prototype, canWalk, canFly);

const bird = new Bird("Sparrow");
console.log(bird.walk()); // "Sparrow is walking"
console.log(bird.fly());  // "Sparrow is flying"

// Composition over inheritance
function createDuck(name) {
    return {
        name,
        ...canWalk,
        ...canSwim,
        ...canFly
    };
}

// ============================================================================
// SECTION 9: INTERVIEW QUESTIONS
// ============================================================================

/**
 * Q1: What's the difference between __proto__ and prototype?
 *
 * __proto__: Every object has this, points to its prototype
 * prototype: Only functions have this, used as prototype for new instances
 */

function Foo() {}
const foo = new Foo();
console.log(foo.__proto__ === Foo.prototype); // true
console.log(Foo.__proto__ === Function.prototype); // true

/**
 * Q2: Implement instanceof
 */
function myInstanceof(obj, Constructor) {
    let proto = Object.getPrototypeOf(obj);
    while (proto !== null) {
        if (proto === Constructor.prototype) return true;
        proto = Object.getPrototypeOf(proto);
    }
    return false;
}

/**
 * Q3: What's the output?
 */
function A() {}
A.prototype.x = 1;
const a = new A();
console.log(a.x); // 1

A.prototype = { x: 2 };
console.log(a.x); // 1 (still linked to OLD prototype)

const b = new A();
console.log(b.x); // 2 (linked to NEW prototype)

/**
 * Q4: Create inheritance without class keyword
 */
function Parent(name) {
    this.name = name;
}
Parent.prototype.sayHi = function() {
    return `Hi from ${this.name}`;
};

function Child(name, age) {
    Parent.call(this, name);
    this.age = age;
}
Child.prototype = Object.create(Parent.prototype);
Child.prototype.constructor = Child;

/**
 * Q5: What happens when you modify Object.prototype?
 */
Object.prototype.custom = "danger!";
const testObj = {};
console.log(testObj.custom); // "danger!" - affects ALL objects!

// Why it's dangerous: prototype pollution
// Always use hasOwnProperty checks

/**
 * Q6: Property shadowing
 */
const proto = { value: 1 };
const instance = Object.create(proto);
instance.value = 2;  // Creates OWN property, doesn't modify proto
console.log(instance.value); // 2 (own)
console.log(proto.value);    // 1 (unchanged)
delete instance.value;
console.log(instance.value); // 1 (proto shows through)
