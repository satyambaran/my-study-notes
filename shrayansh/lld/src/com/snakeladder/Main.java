package src.com.snakeladder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Main {

    public static void main(String[] args) {
        Game game1 = new Game();
        game1.initialize(4, 5, 6, 10, 10, 4, 6);
        game1.run();
    }
}

class Game {
    Integer winnerId, numOfSnakes, numOfLadders, numOfFaces, requiredFirstRoll, playerIndex;
    Board board;
    List<Player> players;
    Dice dice;

    Game() {
    }

    public void initialize(int numOfSnakes, int numOfLadders, int numOfFaces, int boardWidth, int boardHeight,
            int numOfPlayers, int requiredFirstRoll) {
        winnerId = null;
        this.numOfLadders = numOfLadders;
        this.numOfSnakes = numOfSnakes;
        this.numOfFaces = numOfFaces;
        playerIndex = 0;
        // rule
        this.requiredFirstRoll = requiredFirstRoll;

        // create a dice
        dice = new Dice(numOfFaces);

        // create board
        int maxPosition = boardHeight * boardWidth - 1;
        // create snakes
        List<Shift> shifts = new ArrayList<>();
        if (numOfSnakes >= dice.numOfFaces) {
            System.out.println("Warning: There might be a dead end before winning");
        }
        for (int i = 0; i < numOfSnakes; i++) {
            int startPosition = ThreadLocalRandom.current().nextInt(maxPosition + 1);
            int endPosition = ThreadLocalRandom.current().nextInt(maxPosition + 1);
            while (startPosition >= endPosition) {
                startPosition = ThreadLocalRandom.current().nextInt(maxPosition + 1);
                endPosition = ThreadLocalRandom.current().nextInt(maxPosition + 1);
            }
            Shift curShift = new Shift(startPosition, endPosition);

            shifts.add(curShift);
        }
        // create ladders
        for (int i = 0; i < numOfLadders; i++) {
            int startPosition = ThreadLocalRandom.current().nextInt(maxPosition + 1);
            int endPosition = ThreadLocalRandom.current().nextInt(maxPosition + 1);
            while (startPosition <= endPosition) {
                startPosition = ThreadLocalRandom.current().nextInt(maxPosition + 1);
                endPosition = ThreadLocalRandom.current().nextInt(maxPosition + 1);
            }
            Shift curShift = new Shift(startPosition, endPosition);
            shifts.add(curShift);
        }
        board = new Board(boardWidth, boardHeight, shifts);
        // create players
        players = new ArrayList<>();
        for (int i = 0; i < numOfPlayers; i++) {
            Player curPlayer = new Player();
            players.add(curPlayer);
        }
    }

    public void run() {
        while (winnerId == null) {
            int curRoll = dice.rollDice();
            Player curPlayer = players.get(playerIndex);
            int newPosition = board.getEndPosition(curPlayer.getPosition(), curRoll);
            if (board.isPositionWinning(newPosition)) {
                winnerId = playerIndex;
            }
            System.out.println(
                    "Player " + playerIndex + " has moved from " + curPlayer.getPosition() + " to " + newPosition);
            curPlayer.setPosition(newPosition);
            playerIndex = (playerIndex + 1) % players.size();
        }
    }
}

class Board {
    int width, height;
    HashMap<Integer, Shift> shifts;

    Board(int width, int height, List<Shift> _shifts) {
        this.width = width;
        this.height = height;
        shifts = new HashMap<>();
        for (Shift curShift : _shifts) {
            shifts.put(curShift.startPosition, curShift);
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Shift getShiftAtCurPoisition(int position) {
        if (shifts.containsKey(position)) {
            shifts.get(position);
        }
        return null;
    }

    public int getEndPosition(int position, int curRoll) {
        if (position + curRoll <= width * height - 1) {
            position += curRoll;
            if (shifts.containsKey(position)) {
                position = shifts.get(position).getEndPosition();
            }
        }
        return position;
    }

    public boolean isPositionWinning(int position) {
        return position == width * height - 1;
    }

}

class Shift {
    int startPosition, endPosition;

    Shift(int startPosition, int endPosition) {
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        System.out.println("A shift has been created from " + startPosition + " to " + endPosition);
    }

    public int getStartPosition() {
        return startPosition;
    }

    public int getEndPosition() {
        return endPosition;
    }
}

class Player {
    static int idGenerator = 1;
    int id;
    String name;
    int position;

    Player() {
        id = idGenerator++;
        position = 0;
    }

    Player(String name) {
        id = idGenerator++;
        this.name = name;
        position = 0;
    }

    public int getPosition() {
        return position;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}

class Dice {
    int numOfFaces;

    Dice(int numOfFaces) {
        this.numOfFaces = numOfFaces;
    }

    public int rollDice() {
        int curRoll = 1 + ThreadLocalRandom.current().nextInt(numOfFaces);
        System.out.println("Dice has been rolled with output: " + curRoll);
        return curRoll;
    }
}

/*
 * 
 * Game:
 * initialize(), run(), print each moves,
 * winning condition:
 * exactly n*n
 * n*row+col
 * no snakes/ladders have follow up snakes/ladders:
 * k snakes, k ladders each
 * 
 * Player:
 * m players
 * 
 * Dice:
 * 1
 * 
 */
/*
 * 100, 99, ..., 95, 94
 * Objects:
 * 
 * Game:
 * initialize(), run(), winnerId = null, board, players, dice,
 * rule(){requiredFirstRoll}
 * 
 * Board:
 * shifts, sizes
 * Shift(ladder/snake):
 * 
 * Player:
 * id, name, co-ordinates
 * 
 * Dice:
 * d faces
 * 
 */