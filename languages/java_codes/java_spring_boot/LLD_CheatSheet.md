# LLD Interview — Last Minute Cheat Sheet

## Structure
Each section follows: concept → when to use → minimal implementation skeleton or key decision.
Design patterns show: name → trigger phrase → participants → 2-line skeleton.
Common questions show: problem → core objects → key pattern(s) → critical design decision.

## Purpose
Rapid revision for Low-Level Design interviews. Assumes OOP fluency. Covers SOLID, patterns, and popular question approaches.

---

## 1. SOLID — One-Line Triggers

```
S — Single Responsibility    : "If you need to change this class for 2 different reasons, split it"
O — Open/Closed              : "Add behavior via new class/strategy, don't edit existing code"
L — Liskov Substitution      : "Subclass must honor parent's contract — no surprise exceptions, no narrowed inputs"
I — Interface Segregation    : "Client shouldn't depend on methods it doesn't use — split fat interfaces"
D — Dependency Inversion     : "High-level modules depend on abstractions, not concretions — inject interfaces"
```

---

## 2. Design Patterns

### Creational

**Singleton** — one global instance (config, cache, thread pool)
```
private static instance; private constructor;
static getInstance() { if null → create; return instance; }
// Thread-safe: enum singleton OR static holder class (Bill Pugh)
// Interview: prefer enum — serialization-safe, reflection-safe
```

**Factory Method** — caller doesn't know concrete class, decides at runtime
```
Creator defines: abstract Product createProduct();
ConcreteCreatorA returns ProductA; ConcreteCreatorB returns ProductB;
// Trigger: "if/else or switch on type to create objects" → refactor to factory
```

**Abstract Factory** — family of related objects (UI toolkit: Button+Checkbox per OS)
```
AbstractFactory { createButton(); createCheckbox(); }
WinFactory implements → WinButton, WinCheckbox
MacFactory implements → MacButton, MacCheckbox
// Trigger: "multiple related objects that must be consistent"
```

**Builder** — complex object with many optional params
```
Product.builder().setX(x).setY(y).build();
// Builder holds same fields, validates in build()
// Trigger: constructor has 4+ params, some optional
```

**Prototype** — clone existing object, avoid costly creation
```
Prototype { clone(); }
// Use when: object creation is expensive, slight variations needed
// Java: implement Cloneable, override clone() — or copy constructor (preferred)
```

---

### Structural

**Adapter** — make incompatible interface work with existing code
```
Target { request(); }
Adapter implements Target { wraps Adaptee; request() → adaptee.specificRequest(); }
// Trigger: "integrate third-party/legacy class with different interface"
```

**Decorator** — add behavior dynamically without subclassing (I/O streams, pizza toppings)
```
Component { operation(); }
Decorator implements Component { wraps Component; operation() → add behavior + delegate; }
// Trigger: "combinations of features would cause subclass explosion"
// Key: decorator IS-A and HAS-A Component
```

**Facade** — simplified interface over complex subsystem
```
Facade { simpleMethod() → orchestrates subsystemA.x(), subsystemB.y(), subsystemC.z(); }
// Trigger: "client doesn't need to know internal wiring"
// LLD interviews: your Service layer IS a facade over domain objects
```

**Composite** — tree structure, treat leaf and group uniformly (file system, org chart)
```
Component { operation(); }
Leaf implements Component;
Composite implements Component { List<Component> children; operation() → iterate children; }
// Trigger: "part-whole hierarchy, recursive structure"
```

**Proxy** — control access to real object (lazy init, access control, logging, caching)
```
Subject { request(); }
Proxy implements Subject { wraps RealSubject; request() → check/log + delegate; }
```

---

### Behavioral

**Strategy** — swap algorithm at runtime (payment method, pricing, sorting)
```
Strategy { execute(); }
Context { setStrategy(s); doWork() → strategy.execute(); }
// Trigger: "multiple ways to do same thing, chosen at runtime"
// Most used pattern in LLD interviews
```

**Observer** — pub-sub, notify dependents on state change (event system, notifications)
```
Subject { register(Observer); remove(Observer); notifyAll(); }
Observer { update(data); }
// Trigger: "when X changes, Y and Z should react"
// Java: avoid java.util.Observable (deprecated) — roll your own or use listeners
```

**State** — object changes behavior when internal state changes (vending machine, order FSM)
```
State { handle(Context); }
Context { setState(State); request() → currentState.handle(this); }
// Trigger: "if/else on status field controlling behavior" → extract to State classes
// Each state knows valid transitions
```

**Command** — encapsulate request as object (undo/redo, queue operations, macro recording)
```
Command { execute(); undo(); }
Invoker stores command history; Receiver does actual work;
// Trigger: "undo", "queue of operations", "log and replay"
```

**Chain of Responsibility** — pass request along handler chain (middleware, approval flow, logging)
```
Handler { setNext(Handler); handle(request) → process or pass to next; }
// Trigger: "multiple processors, any can handle or pass along"
```

**Template Method** — skeleton algorithm in base, subclasses override steps
```
AbstractClass { templateMethod() { step1(); step2(); step3(); } // final
  step1() concrete; step2() abstract; step3() hook with default; }
// Trigger: "same algorithm structure, different step implementations"
```

**Iterator** — traverse collection without exposing internals
```
Iterator<T> { hasNext(); next(); }
Iterable<T> { iterator(); } // enables for-each
// Rarely asked to implement, but know when to use
```

**Mediator** — centralize complex communication between objects (chat room, ATC, dialog box)
```
Mediator { notify(sender, event); }
Colleagues talk through mediator, not directly to each other;
// Trigger: "N objects with M×M interactions → centralize to reduce coupling"
```

---

## 3. Pattern Selection Cheat

```
"multiple payment types"          → Strategy
"undo/redo"                       → Command
"notify on change"                → Observer
"object has states with rules"    → State
"recursive tree structure"        → Composite
"add features without subclass"   → Decorator
"complex creation, many params"   → Builder
"family of related objects"       → Abstract Factory
"type-based object creation"      → Factory Method
"wrap legacy/third-party"         → Adapter
"simplify complex subsystem"      → Facade
"chain of processors"             → Chain of Responsibility
"same algo, different steps"      → Template Method
"centralize N×N communication"    → Mediator
"lazy load / access control"      → Proxy
"global single instance"          → Singleton
```

---

## 4. LLD Interview Framework (5-step)

```
1. CLARIFY      → ask 2-3 scoping questions (users? scale? core features?)
2. CORE OBJECTS → identify 4-8 nouns (entities + value objects)
3. RELATIONSHIPS → has-a, is-a, uses — draw quick class diagram mentally
4. PATTERNS     → which 1-3 patterns fit? (Strategy + Observer covers 60% of problems)
5. APIs         → define public methods on your service/facade layer
```

**Key principles during design:**
```
- Start with interfaces, not implementations
- Separate what varies from what stays the same
- Favor composition over inheritance
- One manager/service class orchestrates domain objects (Facade)
- Enums for fixed types (VehicleType, PieceType), Strategy for variable behavior
- Keep domain objects behavior-rich (tell, don't ask)
- Use Factory when object creation has conditional logic
```

---

## 5. Common LLD Questions — Approach Notes

**Parking Lot**
```
Objects: ParkingLot, Floor, ParkingSpot(type), Vehicle(type), Ticket, EntryPanel, ExitPanel
Patterns: Strategy (pricing), Factory (spot allocation), Observer (display boards)
Key: ParkingSpot.canFit(Vehicle) — spot types vs vehicle types
     ParkingLot.findSpot(VehicleType) — scanning strategy
     Ticket tracks: entryTime, spot, vehicle → ExitPanel calculates fee
```

**Elevator System**
```
Objects: Building, Elevator, Request(floor, direction), ElevatorController
Patterns: Strategy (scheduling: LOOK/SCAN/SSTF), Observer (floor arrival notify), State (Moving/Idle/DoorOpen)
Key: ElevatorController picks best elevator for request
     Direction enum, Request queue per elevator
     State machine: Idle → Moving → DoorOpen → Idle
```

**Chess / TicTacToe**
```
Objects: Game, Board, Cell, Piece(type), Player, Move
Patterns: Strategy (move validation per piece type or winning strategy), Command (move + undo)
Key: Board is 2D Cell array; Piece has isValidMove(from, to, board)
     Game controls turn, validates, checks win condition
     Piece hierarchy: abstract Piece → King, Queen, Pawn... each overrides movement rules
```

**BookMyShow / Movie Ticket Booking**
```
Objects: Theater, Screen, Show, Seat(type), Booking, User, Movie, Payment
Patterns: Strategy (pricing by seat/time), Observer (notify waitlist), State (seat: Available→Locked→Booked)
Key: Seat locking with TTL (temp hold during payment)
     Concurrency: synchronized/lock on seat selection
     Search: by movie → city → theater → show → available seats
```

**Splitwise / Expense Sharing**
```
Objects: User, Group, Expense, Split(Equal/Exact/Percent), Balance
Patterns: Strategy (split calculation), Observer (notify on expense add)
Key: Graph of balances: Map<Pair<User,User>, double>
     Simplify debts: min-cash-flow algorithm (net amounts → settle greedily)
     Expense creates splits → updates pairwise balances
```

**Snakes and Ladders**
```
Objects: Game, Board, Player, Cell, Snake, Ladder, Dice
Patterns: Template Method (turn: roll → move → check snake/ladder → check win)
Key: Board has Map<Integer,Integer> for snakes and ladders (from → to)
     Simple loop: roll → newPos = old + dice; if snake/ladder → redirect; if >=100 → win
```

**Library Management**
```
Objects: Library, Book, BookItem(copy), Member, Librarian, Loan, Reservation, Fine
Patterns: Observer (notify on book available), Strategy (fine calculation), State (BookItem: Available→Loaned→Reserved→Lost)
Key: Book vs BookItem (catalog entry vs physical copy)
     Member has borrowing limit; Loan has due date → Fine on overdue
     Search: by title/author/ISBN → Catalog
```

**Vending Machine**
```
Objects: VendingMachine, Product, Inventory, Coin/Money, State
Patterns: State (Idle→CoinInserted→ProductSelected→Dispensing), Strategy (payment type)
Key: Pure state machine problem
     States handle: insertCoin(), selectProduct(), dispense(), cancelAndRefund()
     Each state validates transitions and delegates to next state
```

**Rate Limiter**
```
Objects: RateLimiter, Rule, RequestContext
Patterns: Strategy (algorithm: fixed window / sliding window / token bucket / leaky bucket)
Key: Fixed Window — counter per time window, reset on window boundary
     Sliding Window — weighted count of current + previous window
     Token Bucket — tokens refill at rate R, max burst B, consume on request
     Leaky Bucket — fixed-rate output queue
     Map<clientId, BucketState> with synchronized access
```

**URL Shortener (LLD focus)**
```
Objects: URLService, URLMapping, User, Analytics
Patterns: Strategy (encoding: base62, MD5+truncate, counter-based)
Key: encode(longUrl) → shortCode; decode(shortCode) → longUrl
     Storage: Map<String, String> both directions
     Counter-based: AtomicLong id → base62 encode
     Collision handling if hash-based
```

---

## 6. Common Enums & Interfaces to Pre-decide

```java
// Enums you'll almost always need
enum Status { ACTIVE, INACTIVE, BLOCKED }
enum VehicleType { CAR, TRUCK, BIKE, BUS }
enum PaymentType { CASH, CARD, UPI, WALLET }
enum SeatType { REGULAR, PREMIUM, VIP }

// Interfaces you'll almost always define
interface Searchable<T> { List<T> search(SearchCriteria criteria); }
interface Observable { register(Observer); remove(Observer); notifyObservers(); }
interface Observer { update(Event event); }
interface PricingStrategy { double calculate(Context ctx); }
```

---

## 7. Quick Do's and Don'ts

```
DO:  start with nouns → classes, verbs → methods
DO:  use enums for finite fixed types, not strings
DO:  make classes immutable where possible (Value Objects)
DO:  keep service layer thin — push logic into domain objects
DO:  clarify concurrency needs — "can two users book same seat?"

DON'T: make God classes (if a class has 10+ methods, split it)
DON'T: use inheritance for code reuse — use composition
DON'T: hardcode business rules — extract into Strategy
DON'T: expose internal collections — return unmodifiable copies
DON'T: design DB schema — this is OOP design, not ER modeling
```

**Example — each DO/DON'T illustrated:**

```java
// ✗ DON'T: strings for fixed types         ✓ DO: enums
String type = "PREMIUM";                  // SeatType type = SeatType.PREMIUM;

// ✗ DON'T: mutable, exposed internals      ✓ DO: immutable value object
class Order {                             // class Order {
  public List<Item> items;                //   private final List<Item> items;
}                                         //   public List<Item> getItems() { return List.copyOf(items); }
                                          // }

// ✗ DON'T: inheritance for code reuse      ✓ DO: composition
class Dog extends DBLogger { }            // class Dog { private Logger logger; }

// ✗ DON'T: hardcoded business rules        ✓ DO: Strategy pattern
double getFee(Ticket t) {                 // interface PricingStrategy { double calculate(Ticket t); }
  if (type == "hourly") return hrs * 10;  // class HourlyPricing implements PricingStrategy { ... }
  if (type == "flat") return 50;          // class FlatPricing implements PricingStrategy { ... }
}                                         // feeCalculator.setStrategy(pricingStrategy);

// ✗ DON'T: God class                       ✓ DO: split by responsibility
class ParkingLot {                        // class ParkingLot { ... }         // spot mgmt
  parkVehicle(); removeVehicle();         // class TicketService { ... }      // ticketing
  generateTicket(); calculateFee();       // class PricingService { ... }     // fee calc
  displayAvailability(); sendNotify();    // class DisplayBoard { ... }       // display
}                                         // class NotificationService { ... } // notify

// ✗ DON'T: logic in service layer          ✓ DO: push logic into domain (tell, don't ask)
if (seat.getStatus() == AVAILABLE) {      // seat.book(user);  // seat validates & updates internally
  seat.setStatus(BOOKED);                 //   → throws AlreadyBookedException if not available
  seat.setBookedBy(user);                 //   → encapsulates state transition rules inside Seat
}
```