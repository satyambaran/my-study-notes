# LLD Interview Approach Notes

## What Interviewers Expect
- Correct class identification and relationships
- SOLID principles applied naturally
- Design patterns only where they fit — never forced
- Clean API design with clear boundaries
- **Trade-off reasoning** matters more than the "right" answer
- A well-justified "wrong" choice beats an unjustified "right" one

---

## Mental Model: Instance = Server, Main = Client

- Treat your top-level class (e.g., `TicTacToeInstance`) as the **service layer / API boundary**
- Treat `main()` as a **thin client** (UI/controller) that simulates external callers
- `main()` should only work with **primitive identifiers** (gameId, playerId) — never hold references to internal domain objects like `Game`, `Board`, `Cell`
- This mirrors real systems: REST controller → Service → Domain Objects

```java
// main() = thin client, only talks to the service
String gameId = instance.createGame("Alice", "Bob");
instance.makeMove(gameId, "Alice", 0, 0);
```

---

## Where Does a Method Belong?

**Heuristic:** "Does this operation need data from multiple entities or external coordination?"

| Answer | Place it on |
|--------|-------------|
| No — operates on one entity's state | Domain object (e.g., `Game.makeMove()`) |
| Yes — cross-entity coordination, lifecycle | Service/Instance (e.g., `Instance.makeMove()` as facade) |

- **Domain objects own logic** (validation, state mutation, rules)
- **Service/Instance owns coordination** (lookup, lifecycle transitions, cleanup, observers)
- The facade pattern: `Instance.makeMove()` calls `Game.makeMove()` internally — external callers never skip the service

---

## Return IDs, Not Objects

When a service creates something, return an **ID** (or lightweight DTO), not the domain object.

- Returning the object leaks internals — callers can bypass your service
- Returning an ID forces all operations to flow through the single entry point
- Preserves encapsulation and control

```java
// Good: caller must route through service
String gameId = instance.createGame(p1, p2);
instance.makeMove(gameId, ...);

// Bad: caller bypasses service entirely
Game game = instance.createGame(p1, p2);
game.makeMove(...);  // service is unaware
```

---

## Resist Unnecessary Abstractions

- Don't introduce new classes/layers until you have a concrete reason (YAGNI)
- If `TicTacToeInstance` already manages games, you don't need a separate `GameManager`
- Every class should justify its existence with a distinct responsibility

---

## Managers Are Classes, Not Microservices

In LLD, managers (`GameManager`, `PaymentManager`, etc.) live in the **same process**. Communication is direct method calls — no HTTP, no queues, no serialization. Just dependency injection.

```java
class RideManager {
    private PaymentManager paymentManager; // injected dependency

    public void completeRide(String rideId) {
        Ride ride = activeRides.get(rideId);
        ride.complete();
        paymentManager.processPayment(ride.getFare(), ride.getRiderId());
    }
}
```

| Concern | LLD (your scope) | HLD (not your scope) |
|---|---|---|
| Communication | Direct method calls | REST, gRPC, message queues |
| Data | Shared memory, objects | Separate DBs, APIs |
| Failure handling | Exceptions | Retries, circuit breakers |
| Boundaries | Class responsibility | Service boundary |

When the interviewer cares about service boundaries, they'll explicitly ask HLD.

---

## Breaking Circular Dependencies Between Managers

If `ManagerA → ManagerB` AND `ManagerB → ManagerA`, you have a circular dependency.  
This is the **LLD equivalent of inter-service communication** — and the place where design patterns earn their keep.

### 1. Interface Inversion (one-to-one, cleanest)
The depended-on manager accepts an **interface**, not a concrete class. The caller implements it.

```java
public interface PaymentCallback {
    void onPaymentSuccess(String entityId, double amount);
    void onPaymentFailure(String entityId, String reason);
}

// RideManager implements the callback
class RideManager implements PaymentCallback {
    private PaymentManager paymentManager;

    public void completeRide(String rideId) {
        Ride ride = activeRides.get(rideId);
        ride.complete();
        paymentManager.processPayment(ride.getId(), ride.getFare(), this);
    }

    @Override
    public void onPaymentSuccess(String entityId, double amount) {
        activeRides.get(entityId).markPaid();
    }
}

// PaymentManager depends on interface, not RideManager
class PaymentManager {
    public void processPayment(String entityId, double amount, PaymentCallback callback) {
        // process...
        callback.onPaymentSuccess(entityId, amount);
    }
}
```

`PaymentManager` is now reusable — works with rides, orders, subscriptions — whoever implements `PaymentCallback`.

### 2. Observer/Event Pattern (one-to-many broadcast)
Manager emits events, multiple interested managers subscribe. Use when multiple managers react to the same event.

```java
class PaymentManager {
    private List<PaymentListener> listeners = new ArrayList<>();

    public void processPayment(String entityId, double amount) {
        // process...
        listeners.forEach(l -> l.onPaymentComplete(entityId, amount));
    }
}
```

### 3. Mediator (complex multi-manager orchestration)
A third class coordinates both managers — neither knows the other. Use when orchestration logic is complex and doesn't belong in either manager.

### Which to use?
- **One-to-one dependency** → Interface inversion
- **One-to-many broadcast** → Observer/events
- **Complex multi-manager orchestration** → Mediator

---

## Design Patterns Reference

### 1. Creational Patterns

| Pattern | Intent | Example |
|---------|--------|---------|
| **Singleton** | One instance, global access point | Configuration manager, DB connection |
| **Factory Method** | Subclasses decide which object to create | Document creation (PDF, Word) |
| **Abstract Factory** | Factory of factories — families of related objects | GUI libraries with different themes |
| **Builder** | Step-by-step construction of a complex object | Building a computer with various components |
| **Prototype** | Clone an existing object | Cloning objects in a game |

### 2. Structural Patterns

| Pattern | Intent | Example |
|---------|--------|---------|
| **Adapter** | Convert one interface to another | Integrating a legacy system |
| **Bridge** | Separate abstraction from implementation | Drawing shapes with different colors |
| **Composite** | Tree structures for part-whole hierarchies | File systems (folders and files) |
| **Decorator** | Dynamically add responsibilities to an object | Adding features to a UI component |
| **Facade** | Simplified interface to a complex subsystem | Simplified API for a complex library |
| **Flyweight** | Share common parts among many similar objects | Text rendering with shared glyphs |
| **Proxy** | Surrogate to control access to another object | Virtual proxy for image loading |

### 3. Behavioral Patterns

| Pattern | Intent | Example |
|---------|--------|---------|
| **Chain of Responsibility** | Pass a request along a chain of handlers | Logging frameworks with log levels |
| **Command** | Encapsulate a request as an object | Undo functionality in text editors |
| **Interpreter** | Define a grammar and interpret sentences | Parsing expressions in a language |
| **Iterator** | Sequential access without exposing internals | Iterating Java collections |
| **Mediator** | Encapsulate how objects interact (peer-to-peer → hub) | Chat room |
| **Memento** | Capture and restore state without exposing internals | Saving game state |
| **Observer** | Notify dependents automatically on state change | Event handling systems |
| **State** | Object changes behavior when internal state changes | Vending machine |
| **Strategy** | Interchangeable family of algorithms | Different sorting algorithms |
| **Template Method** | Skeleton algorithm with deferred steps | Cooking recipe subclasses |
| **Visitor** | Operation on elements without changing their classes | Calculating taxes on a collection |
| **Null Object** | Default do-nothing object to avoid null checks | — |

> Proxy flow: `controller → service → repository`

---

## Pattern Notes

- **Singleton** — use `volatile` + double-checked locking to be thread-safe; `volatile` prevents CPU reordering (allocate → assign → init risk)
    - Multiple threads can create multiple instances due to instruction reordering and CPU cache behavior.
        Two unsafe orderings the JVM/CPU can produce:
        1. Allocate memory → Initialize → Assign pointer *(safe but may stall)*
        2. Allocate memory → **Assign pointer** → Initialize *(unsafe: another thread sees a non-null but uninitialized object)*
    - `volatile` keyword
        - Forces reads/writes to go directly to main memory (bypasses L1 per-core cache)
        - Adds a memory barrier: all instructions before the assignment must complete before the pointer is published
        - Restores ordering guarantees

    ```java
    class DBConnection {
        private volatile static DBConnection dbConnection;
        private DBConnection() {}

        public static DBConnection getDBConnection() {
            if (dbConnection == null) {          // first check (no lock)
                synchronized (DBConnection.class) {
                    if (dbConnection == null) {  // second check (inside lock)
                        dbConnection = new DBConnection();
                    }
                }
            }
            return dbConnection;
        }
    }
    ```

- **Factory** — centralises `if/else` object creation; avoids duplicating switch logic at every call site

    ```java
    class ShapeFactory {
        Shape create(ShapeType type) {
            switch (type) {
                case CIRCLE:    return new Circle();
                case RECTANGLE: return new Rectangle();
                default:        return null;
            }
        }
    }
    ```
- **Abstract Factory** — factory of factories; `WindowsFactory` / `MacFactory` each produce a family of related objects

    ```
    AbstractFactory
    ├── WindowsFactory  → WindowsButton, WindowsCheckbox
    └── MacFactory      → MacButton, MacCheckbox
    ```
- **Decorator** — wraps object in nested subclasses to add features incrementally; avoids class explosion (one class per combination)

    ```
    Component (interface)
    └── BaseComponent
            └── DecoratorA(Component)      // adds feature A
                └── DecoratorB(Component) // adds feature A + B
    ```
- **Observer** — Defines a `one-to-many dependency`: when one object (Subject) changes state, all registered dependents (Observers) are notified and updated automatically.
    ```
    Subject → notifyAll() → [Observer1, Observer2, Observer3]
    ```
- **Chain of Responsibility** — each handler has a `successor`; handles the request or forwards it (e.g. logger chain: INFO → WARNING → ERROR → FATAL)
    ```java
    // Logger chain: INFO → WARNING → ERROR → FATAL
    class Logger {
        Level currentLevel;
        Logger successor;

        void log(Level level) {
            if (level == currentLevel) {
                // handle it
            } else {
                successor.log(level);
            }
        }
    }
    ```

- **Command** — encapsulates a request as an object, decoupling the **invoker** (who triggers) from the **receiver** (who does the work). The invoker calls `command.execute()` without knowing what happens inside. Enables queuing, scheduling, and undo.

    ```
    Client → creates → ConcreteCommand(receiver) → handed to → Invoker
    Invoker → calls → command.execute() → delegates to → Receiver.action()
    ```

    ```java
    // Command interface
    interface Task { void execute(); }

    // Concrete command — holds its own receiver data
    class BackupTask implements Task {
        private String src, dest;
        BackupTask(String src, String dest) { this.src = src; this.dest = dest; }
        public void execute() { /* backup src → dest */ }
    }

    // Invoker — doesn't know what execute() does
    class Scheduler {
        void run(Task task) { task.execute(); }
    }
    ```

    Key insight: the command **owns everything it needs** to run. The invoker never touches the receiver's internals. This is why you can queue commands, serialize them, or undo them — they're self-contained.

- **Strategy** — defines a family of interchangeable algorithms, encapsulated behind a common interface. The client picks the strategy; the context (object using it) doesn't care which one.

    ```
    Context has-a Strategy
    Context.doWork() → strategy.algorithm()

    Swap strategy at runtime → behavior changes, no code changes
    ```

    ```java
    interface SchedulingStrategy {
        Optional<LocalDateTime> getNextExecutionTime(LocalDateTime last);
    }

    class OneTimeStrategy implements SchedulingStrategy {
        public Optional<LocalDateTime> getNextExecutionTime(LocalDateTime last) {
            return last == null ? Optional.of(fixedTime) : Optional.empty();
        }
    }

    class RecurringStrategy implements SchedulingStrategy {
        public Optional<LocalDateTime> getNextExecutionTime(LocalDateTime last) {
            return Optional.of((last == null ? LocalDateTime.now() : last).plus(interval));
        }
    }
    ```

    **Strategy vs Factory:** Factory decides **which object** to create. Strategy decides **which algorithm** to use. Factory is about construction; Strategy is about behavior.

- **Producer-Consumer** — decouples producers (who create work) from consumers (who process it) via a shared buffer. Producers add to the buffer; consumers take from it. Synchronization (locks, conditions) ensures no data races, no busy-waiting, and optional backpressure.

    ```
    Producer → [Bounded Buffer / Queue] → Consumer
                     ↑                         ↑
               notFull.await()          notEmpty.await()
               notEmpty.signal()        notFull.signal()
    ```

    ```java
    // Producer
    lock.lock();
    while (queue.size() >= capacity) notFull.await(); // backpressure
    queue.offer(item);
    notEmpty.signalAll(); // wake consumers
    lock.unlock();

    // Consumer
    lock.lock();
    while (queue.isEmpty()) notEmpty.await(); // sleep until work exists
    Item item = queue.poll();
    notFull.signal(); // unblock producers
    lock.unlock();
    process(item);
    ```

    Production systems use `BlockingQueue` which hides this. In interviews, building it manually shows you understand lock-based synchronization and condition variables.

---

## System Design Examples

### Parking Lot
- Multiple entrances/exits, floors, spot types (2/3/4-wheeler)
- Payment strategies: per-minute, one-time, mixed
- Assign nearest available spot to entry gate

| Object | Key fields |
|--------|-----------|
| `Vehicle` | plateNumber, type |
| `Spot` | id, type, isEmpty, vehicleId, price |
| `Ticket` | entryTime, spot |
| `EntranceGate` | findSpot(), updateSpot(), generateTicket() |
| `ExitGate` | calculateCost(), processPayment(), freeSpot() |

---

### Snake & Ladder
- Use `Deque<Player>` for turn management
- Board holds cell map: landing position → snake/ladder destination