# LLD Design Notes — Tic Tac Toe

## Overview

A multiplayer Tic-Tac-Toe engine that supports:

- Variable board sizes (N×N)
- Multiple concurrent games (each identified by a numeric `gameId`)
- Move validation, undo, and win/draw detection
- A persistent scoreboard across games

---

## Package Structure

```
com.tictactoe
├── TicTacToe.java                        ← Entry point (main); demo driver
├── TicTacToeInstance.java                ← Facade + Singleton; public API
├── GameRegistry.java                     ← Manages active / completed game maps
├── models/
│   ├── entities/
│   │   ├── Game.java                     ← Core game logic (moves, win check, observers)
│   │   ├── Board.java                    ← N×N grid of Cells
│   │   ├── Cell.java                     ← Single cell with its Symbol
│   │   ├── Player.java                   ← Holds player name (immutable)
│   │   └── Move.java                     ← Value object: playerIdx + (row, col) + symbol
│   ├── enums/
│   │   ├── GameStatus.java               ← IN_PROGRESS | DRAW | WINNER
│   │   └── Symbol.java                   ← X | O | EMPTY (each carries a display char)
│   ├── interfaces/
│   │   ├── Observer.java                 ← notify(Game) — receives game-over events
│   │   └── WinningStrategy.java          ← checkWin(Board, row, col, Symbol)
│   └── implementations/
│       ├── observers/
│       │   └── Scoreboard.java           ← Observer impl; tallies wins per player
│       └── winningstrategies/
│           ├── RowLevelWinningStrategy.java
│           ├── ColumnLevelWinningStrategy.java
│           └── DiagonalLevelWinningStrategy.java
├── exceptions/
│   ├── InvalidMoveException.java         ← Unchecked; bad move or undo-on-empty
│   └── GameNotFoundException.java        ← Unchecked; unknown/completed gameId
└── utils/
    └── AppLogger.java                    ← Singleton wrapper over java.util.logging.Logger
```

---

## Design Patterns Applied

### 1. Facade — `TicTacToeInstance`

- Exposes four clean methods: `createGame()`, `makeMove()`, `undoMove()`, `printScoreboard()`
- Callers never touch `Game`, `Board`, or winning strategy internals directly.
- **Why:** keeps the public API stable even if internals change.

### 2. Singleton — `TicTacToeInstance`, `AppLogger`

- Both use double-checked locking (DCL) inside a `synchronized` block.
- **Why:** `TicTacToeInstance` holds shared state (registry + scoreboard), so two instances would split game sessions. `AppLogger` wraps a global JVM logger, so one instance is sufficient.

### 3. Observer — `Game` → `Scoreboard`

- `Game` holds a `List<Observer>`; on win or draw it calls `notifyObservers()`.
- Any future listener (analytics, notifications, replay recorder) just implements `Observer` and registers itself.
- Uses `CopyOnWriteArrayList` for the observer list so notifications during concurrent writes don't throw `ConcurrentModificationException`.
- **Why:** `Game` doesn't import or know about `Scoreboard` — zero coupling between game logic and score tracking.

### 4. Strategy — `WinningStrategy`

- Three concrete strategies: `RowLevelWinningStrategy`, `ColumnLevelWinningStrategy`, `DiagonalLevelWinningStrategy`.
- `Game` keeps a `List<WinningStrategy>` and iterates through all of them after every move.
- `checkWin()` receives the last-played `(row, col)` so each strategy only checks the affected row/column/diagonal — not the full board.
- **Why:** Open/Closed Principle — adding a new win condition (e.g., "5-in-a-row") requires no changes to `Game`.

### 5. Command / Memento — Move list as history

- Every `Move` is a value object: `playerIdx`, `row`, `col`, `symbol` (set by `Game` after validation).
- `Game` stores all moves in an ordered `List<Move>` acting as a history stack.
- `undoMove()` pops the last move, clears that cell, rewinds `currentPlayerIndex`, and resets game status if the game was already over.
- **Why:** storing full `Move` objects (not just coordinates) makes the history self-describing — useful for replay or audit.

---

## Responsibility Split

| Class | Owns |
|---|---|
| `TicTacToeInstance` | Session orchestration — create, delegate, log, lifecycle |
| `GameRegistry` | Game lookup (active vs completed maps); SRP: facade doesn't mix lifecycle with lookup. Uses `ConcurrentHashMap`. |
| `Game` | Turn management, move validation, win detection, undo, observer notification. `synchronized` on `makeMove`/`undoMove`. |
| `Board` | Grid state; prints itself, checks fullness. |
| `Cell` | Single grid position. Package-private constructor — only `Board` creates `Cell`s. |
| `Move` | Pure data. `symbol` is set by `Game` after validation; caller never specifies it. |
| `Player` | Immutable name holder; identity-by-reference in maps. |
| `Scoreboard` | Per-player win counter. Thread-safe via `ConcurrentHashMap` + `merge()` for atomic increment. |

---

## Key Design Decisions

### Symbol Assignment
Symbols are auto-assigned by join order via `PLAYER_SYMBOLS[] = { X, O }`.  
Player 0 → `X`, Player 1 → `O`.  
**Trade-off:** simplifies setup but hardcodes max 2 players (enforced in `addPlayer()`).

### Turn Management
`currentPlayerIndex` cycles as `(idx + 1) % players.size()`.  
`validateMove()` checks `move.getPlayerIdx() == currentPlayerIndex`, so out-of-turn moves are rejected and logged as warnings — the game continues.

### Thread Safety

| What | Mechanism |
|---|---|
| `Game.makeMove` / `undoMove` | `synchronized` — one move at a time per game |
| `GameRegistry` maps | `ConcurrentHashMap` — multiple games active concurrently |
| `Scoreboard` scores | `ConcurrentHashMap` + `merge()` — atomic win updates |
| Observer list | `CopyOnWriteArrayList` — safe iteration during notification |
| Singleton init | DCL `synchronized` block — safe one-time initialization |

### Exception Design
Both custom exceptions extend `RuntimeException` (unchecked).  
The Facade catches `InvalidMoveException` and logs a warning, so invalid moves degrade gracefully rather than crashing the session.

---

## Intentional Omissions

| Missing | Why / How to add later |
|---|---|
| AI / bot player | `Player` is just a name; subclass it to add bot logic |
| Persistence | Games live in-memory; adding a DB only touches `GameRegistry` |
| HTTP layer | All logic is behind `TicTacToeInstance`; wrap it in a REST controller without touching game code |
| Per-turn timer | `Move` has a `timestamp` field placeholder but it is currently unused |
