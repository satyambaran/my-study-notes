#include <iostream>
#include <memory>
#include <mutex>
#include <stdexcept>
#include <string>
#include <unordered_map>
#include <vector>

// ─── Forward Declarations ────────────────────────────────────────────────────

class Board;
class Game;

// ─── Enums ───────────────────────────────────────────────────────────────────

enum class Symbol { X,
                    O,
                    EMPTY };
enum class GameStatus { IN_PROGRESS,
                        DRAW,
                        WINNER };

static char symbolToChar(Symbol s) {
    switch (s) {
        case Symbol::X:
            return 'X';
        case Symbol::O:
            return 'O';
        default:
            return '_';
    }
}

// ─── Exceptions ──────────────────────────────────────────────────────────────

class InvalidMoveException : public std::runtime_error {
public:
    explicit InvalidMoveException(const std::string& msg) : std::runtime_error(msg) {}
};

class GameNotFoundException : public std::runtime_error {
public:
    explicit GameNotFoundException(int id)
        : std::runtime_error("Game with ID " + std::to_string(id) + " not found.") {}
};

// ─── Interfaces ──────────────────────────────────────────────────────────────

/** Strategy interface for win-condition checking. */
class WinningStrategy {
public:
    virtual bool checkWin(Board& board, int row, int col, Symbol symbol) = 0;
    virtual ~WinningStrategy()                                           = default;
};

/** Observer interface notified on game state changes. */
class Observer {
public:
    virtual void notify(Game* game) = 0;
    virtual ~Observer()             = default;
};

// ─── Utils ───────────────────────────────────────────────────────────────────

class AppLogger {
    static AppLogger* instance;
    static std::mutex mtx;
    AppLogger() = default;

public:
    static AppLogger& getInstance() {
        if (!instance) {
            std::lock_guard<std::mutex> lock(mtx);
            if (!instance) instance = new AppLogger();
        }
        return *instance;
    }
    void info(const std::string& msg) { std::cout << "[INFO] " << msg << "\n"; }
    void warning(const std::string& msg) { std::cout << "[WARNING] " << msg << "\n"; }
};
AppLogger* AppLogger::instance = nullptr;
std::mutex AppLogger::mtx;

// ─── Entities ────────────────────────────────────────────────────────────────

/** Immutable player with a display name. */
class Player {
    const std::string name;

public:
    explicit Player(const std::string& name) : name(name) {}
    std::string getName() const { return name; }
};

/**
 * A single cell on the board.
 * Carries its own (row, col) index so callers don't need to track position separately.
 */
class Cell {
    const int row;
    const int col;
    Symbol symbol;

public:
    Cell(int row, int col) : row(row), col(col), symbol(Symbol::EMPTY) {}
    int getRow() const { return row; }
    int getCol() const { return col; }
    Symbol getSymbol() const { return symbol; }
    void setSymbol(Symbol s) { symbol = s; }
    bool isEmpty() const { return symbol == Symbol::EMPTY; }
};

class Board {
    int size;
    std::vector<std::vector<Cell>> cells;

public:
    explicit Board(int size) : size(size) {
        cells.resize(size);
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++)
                cells[i].emplace_back(i, j);
        }
    }

    int getSize() const { return size; }
    Cell& getCell(int row, int col) { return cells[row][col]; }

    void printBoard() const {
        std::string out = "\n";
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                out += symbolToChar(cells[i][j].getSymbol());
                if (j < size - 1) out += " | ";
            }
            out += "\n";
            if (i < size - 1) out += std::string(size * 4 - 3, '-') + "\n";
        }
        AppLogger::getInstance().info(out);
    }

    bool isFull() const {
        for (int i = 0; i < size; i++)
            for (int j = 0; j < size; j++)
                if (cells[i][j].isEmpty()) return false;
        return true;
    }

    void setCell(int row, int col, Symbol s) { cells[row][col].setSymbol(s); }
    bool isCellEmpty(int row, int col) const { return cells[row][col].isEmpty(); }
};

class Move {
    const int playerIdx;
    const int row;
    const int col;
    long timestamp = 0;
    Symbol symbol  = Symbol::EMPTY;

public:
    Move(int playerIdx, int row, int col) : playerIdx(playerIdx), row(row), col(col) {}
    int getPlayerIdx() const { return playerIdx; }
    int getRow() const { return row; }
    int getCol() const { return col; }
    long getTimestamp() const { return timestamp; }
    void setTimestamp(long t) { timestamp = t; }
    Symbol getSymbol() const { return symbol; }
    void setSymbol(Symbol s) { symbol = s; }
};

// ─── Winning Strategies ──────────────────────────────────────────────────────

class RowWinningStrategy : public WinningStrategy {
public:
    bool checkWin(Board& board, int row, int /*col*/, Symbol symbol) override {
        for (int c = 0; c < board.getSize(); c++)
            if (board.getCell(row, c).getSymbol() != symbol) return false;
        return true;
    }
};

class ColumnWinningStrategy : public WinningStrategy {
public:
    bool checkWin(Board& board, int /*row*/, int col, Symbol symbol) override {
        for (int r = 0; r < board.getSize(); r++)
            if (board.getCell(r, col).getSymbol() != symbol) return false;
        return true;
    }
};

class DiagonalWinningStrategy : public WinningStrategy {
public:
    bool checkWin(Board& board, int /*row*/, int /*col*/, Symbol symbol) override {
        int size      = board.getSize();
        bool mainDiag = true;
        for (int i = 0; i < size; i++)
            if (board.getCell(i, i).getSymbol() != symbol) {
                mainDiag = false;
                break;
            }
        if (mainDiag) return true;
        for (int i = 0; i < size; i++)
            if (board.getCell(i, size - 1 - i).getSymbol() != symbol) return false;
        return true;
    }
};

// ─── Game ────────────────────────────────────────────────────────────────────

/**
 * Manages a single game session: turn order, move history, win/draw detection,
 * and observer notifications.
 */
class Game {
    static int idCounter;
    const int id;
    Board board;
    std::vector<Player*> players;
    std::vector<Symbol> symbols;
    int currentPlayerIndex = 0;
    GameStatus status      = GameStatus::IN_PROGRESS;
    Player* winner         = nullptr;
    std::vector<Move> moves;
    std::vector<std::unique_ptr<WinningStrategy>> winningStrategies;
    std::vector<Observer*> observers;
    std::mutex mtx;

    // Symbols assigned in player-addition order: first → X, second → O
    static constexpr Symbol PLAYER_SYMBOLS[] = {Symbol::X, Symbol::O};

    void initWinningStrategies() {
        winningStrategies.push_back(std::make_unique<RowWinningStrategy>());
        winningStrategies.push_back(std::make_unique<ColumnWinningStrategy>());
        winningStrategies.push_back(std::make_unique<DiagonalWinningStrategy>());
    }

    bool checkWin(int row, int col) {
        for (auto& s : winningStrategies)
            if (s->checkWin(board, row, col, symbols[currentPlayerIndex])) return true;
        return false;
    }

    void notifyObservers() {
        for (auto* obs : observers) obs->notify(this);
    }

    /** Pure validation — throws InvalidMoveException on any rule violation. */
    void validateMove(const Move& move) {
        if (status != GameStatus::IN_PROGRESS)
            throw InvalidMoveException("Game is already over. No more moves allowed.");
        if (move.getPlayerIdx() != currentPlayerIndex)
            throw InvalidMoveException("It's not the current player's turn.");
        if (!board.isCellEmpty(move.getRow(), move.getCol()))
            throw InvalidMoveException("Cell is already occupied. Try a different move.");
    }

public:
    explicit Game(int boardSize) : id(idCounter++), board(boardSize) {
        initWinningStrategies();
    }

    // Non-copyable/movable due to std::mutex member
    Game(const Game&)            = delete;
    Game& operator=(const Game&) = delete;

    /** Add a player before the game starts. Max 2 players supported. */
    void addPlayer(Player* player) {
        if (players.size() >= 2)
            throw std::logic_error("Game already has the maximum number of players.");
        symbols.push_back(PLAYER_SYMBOLS[players.size()]);
        players.push_back(player);
    }

    /** Execute a move. Validates first, then applies. */
    void makeMove(Move move) {
        std::lock_guard<std::mutex> lock(mtx);
        validateMove(move);

        int row = move.getRow(), col = move.getCol();
        Symbol currentSymbol = symbols[currentPlayerIndex];
        move.setSymbol(currentSymbol);
        moves.push_back(move);
        board.setCell(row, col, currentSymbol);

        if (checkWin(row, col)) {
            status = GameStatus::WINNER;
            winner = players[currentPlayerIndex];
            notifyObservers();
            return;
        }
        if (board.isFull()) {
            status = GameStatus::DRAW;
            notifyObservers();
            return;
        }
        currentPlayerIndex = (currentPlayerIndex + 1) % static_cast<int>(players.size());
    }

    /** Undo the last move. Reverts board, turn, and game status. */
    void undoMove() {
        std::lock_guard<std::mutex> lock(mtx);
        if (moves.empty())
            throw InvalidMoveException("No moves to undo.");

        const Move& last = moves.back();
        board.setCell(last.getRow(), last.getCol(), Symbol::EMPTY);

        // Revert status before switching player
        if (status != GameStatus::IN_PROGRESS) {
            status = GameStatus::IN_PROGRESS;
            winner = nullptr;
        }

        // Revert turn to the player who made the last move
        currentPlayerIndex = (currentPlayerIndex - 1 + static_cast<int>(players.size())) % static_cast<int>(players.size());
        moves.pop_back();
    }

    int getId() const { return id; }
    Player* getWinner() const { return winner; }
    GameStatus getStatus() const { return status; }
    bool isOver() const { return status != GameStatus::IN_PROGRESS; }
    Board& getBoard() { return board; }
    Player* getCurrentPlayer() const { return players[currentPlayerIndex]; }
    void printBoard() { board.printBoard(); }
    void addObserver(Observer* observer) { observers.push_back(observer); }
};

int Game::idCounter = 1;
constexpr Symbol Game::PLAYER_SYMBOLS[];

// ─── Observers ───────────────────────────────────────────────────────────────

class Scoreboard : public Observer {
    std::unordered_map<std::string, int> playerScores;

public:
    void notify(Game* game) override {
        if (game->getStatus() == GameStatus::WINNER) {
            Player* w = game->getWinner();
            AppLogger::getInstance().info("Scoreboard updated: " + w->getName() + " wins!");
            playerScores[w->getName()]++;
        }
    }

    int getScore(const std::string& playerName) const {
        auto it = playerScores.find(playerName);
        return it != playerScores.end() ? it->second : 0;
    }

    void printScoreboard() const {
        std::string sb = "\n===== SCOREBOARD =====\n";
        if (playerScores.empty()) {
            sb += "No games played yet.\n";
        } else {
            for (auto& [name, score] : playerScores)
                sb += name + ": " + std::to_string(score) + " wins\n";
        }
        sb += "======================";
        AppLogger::getInstance().info(sb);
    }
};

// ─── Game Registry (session management, separated from facade) ───────────────

/**
 * Owns the active/completed game maps.
 * Separated from TicTacToeInstance so the facade doesn't mix lookup logic
 * with game-lifecycle concerns.
 */
class GameRegistry {
    std::unordered_map<int, std::shared_ptr<Game>> activeGames;
    std::unordered_map<int, std::shared_ptr<Game>> completedGames;

public:
    void registerGame(std::shared_ptr<Game> game) {
        activeGames[game->getId()] = game;
    }

    /** Returns the active game or throws GameNotFoundException. */
    std::shared_ptr<Game> findActiveGame(int gameId) const {
        auto it = activeGames.find(gameId);
        if (it == activeGames.end()) throw GameNotFoundException(gameId);
        return it->second;
    }

    void completeGame(int gameId) {
        auto it = activeGames.find(gameId);
        if (it != activeGames.end()) {
            completedGames[gameId] = it->second;
            activeGames.erase(it);
        }
    }
};

// ─── TicTacToeInstance (Facade + Singleton) ──────────────────────────────────

/**
 * Facade: single entry-point for the outside world.
 * Delegates session tracking to GameRegistry and score tracking to Scoreboard.
 */
class TicTacToeInstance {
    static TicTacToeInstance* instance;
    static std::mutex mtx;
    Scoreboard scoreboard;
    GameRegistry registry;

    TicTacToeInstance() = default;

public:
    static TicTacToeInstance& getInstance() {
        if (!instance) {
            std::lock_guard<std::mutex> lock(mtx);
            if (!instance) instance = new TicTacToeInstance();
        }
        return *instance;
    }

    /** Creates a game and adds the provided players in order. */
    int createGame(int boardSize, const std::vector<Player*>& players) {
        auto game = std::make_shared<Game>(boardSize);
        for (auto* p : players) game->addPlayer(p);

        std::string names;
        for (size_t i = 0; i < players.size(); i++) {
            if (i) names += " and ";
            names += players[i]->getName();
        }
        AppLogger::getInstance().info(
            "Created new game with board size " + std::to_string(boardSize) +
            " for players: " + names);

        game->addObserver(&scoreboard);
        registry.registerGame(game);
        return game->getId();
    }

    void makeMove(int gameId, int playerIdx, int row, int col) {
        auto game = registry.findActiveGame(gameId);

        try {
            game->makeMove(Move(playerIdx, row, col));
        } catch (const InvalidMoveException& e) {
            AppLogger::getInstance().warning(
                "Invalid move in game " + std::to_string(gameId) + ": " + e.what());
            return;
        }

        if (game->isOver()) registry.completeGame(gameId);
        game->printBoard();
    }

    void undoMove(int gameId) {
        auto game = registry.findActiveGame(gameId);
        try {
            game->undoMove();
        } catch (const InvalidMoveException& e) {
            AppLogger::getInstance().warning(
                "Undo failed in game " + std::to_string(gameId) + ": " + e.what());
        }
        game->printBoard();
    }

    GameStatus getGameStatus(int gameId) {
        return registry.findActiveGame(gameId)->getStatus();
    }

    void printScoreboard() { scoreboard.printScoreboard(); }
};

TicTacToeInstance* TicTacToeInstance::instance = nullptr;
std::mutex TicTacToeInstance::mtx;

// ─── Main ────────────────────────────────────────────────────────────────────

int main() {
    TicTacToeInstance& instance = TicTacToeInstance::getInstance();
    Player player1("Alice");
    Player player2("Bob");

    int gameId = instance.createGame(3, {&player1, &player2});
    instance.makeMove(gameId, 0, 0, 0);  // Alice → (0,0)
    instance.makeMove(gameId, 1, 0, 1);  // Bob   → (0,1)
    instance.makeMove(gameId, 0, 1, 1);  // Alice → (1,1)
    instance.makeMove(gameId, 0, 1, 1);  // duplicate — warned
    instance.makeMove(gameId, 1, 1, 1);  // Bob tries occupied cell — warned
    instance.makeMove(gameId, 1, 1, 0);  // Bob   → (1,0)

    AppLogger::getInstance().info("--- Undoing Bob's last move ---");
    instance.undoMove(gameId);  // undo Bob (1,0)

    instance.makeMove(gameId, 1, 2, 0);  // Bob   → (2,0)
    instance.makeMove(gameId, 0, 2, 2);  // Alice → (2,2) — wins on diagonal
    instance.printScoreboard();
    return 0;
}
