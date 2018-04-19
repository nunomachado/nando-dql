package pt.haslab.dql.tictactoe.game;

import org.encog.ml.data.MLData;
import pt.haslab.dql.tictactoe.learning.*;
import pt.haslab.dql.tictactoe.util.CommonUtils;

import java.util.Scanner;

/**
 * Created by nunomachado on 05/08/17.
 */
public class GameMain {
    public Board board;            // the game board
    public GameState currentState; // the current state of the game (of enum GameState)
    public Seed currentPlayer;     // the current player (of enum Seed)

    private static Scanner in = new Scanner(System.in);  // input Scanner

    /** Constructor to setup the game */
    public GameMain() {
        board = new Board();  // allocate game-board

        // Initialize the game-board and current status
        initGame();
    }

    /** Initialize the game-board contents and the current states */
    public void initGame() {
        board.init();  // clear the board contents
        currentPlayer = Seed.CROSS;       // CROSS plays first
        currentState = GameState.PLAYING; // ready to play
    }

    public void playGame(Agent aX, Agent aO) {
        Agent agent;
        boolean validAction = false;

        do {
            if(currentPlayer == Seed.CROSS){
                System.out.println("\nPlayer 'X', enter your move (row[1-3] column[1-3]): ");
                agent = aX;
            }
            else{
                System.out.println("\nPlayer 'O', enter your move (row[1-3] column[1-3]): ");
                agent = aO;
            }

            //execute move by agent
            do {
                String a = agent.act(board);
                String[] actions = a.split(" ");
                System.out.println(">> "+a);
                validAction = playerMove(currentPlayer, Integer.valueOf(actions[0]), Integer.valueOf(actions[1]));
            } while (!validAction);

            //update board and currentState
            board.paint();
            updateGame(currentPlayer);

            // Print message if game-over
            if (currentState == GameState.CROSS_WON) {
                System.out.println("'X' won! Bye!");
            } else if (currentState == GameState.NOUGHT_WON) {
                System.out.println("'O' won! Bye!");
            } else if (currentState == GameState.DRAW) {
                System.out.println("It's Draw! Bye!");
            }

            // Switch player
            currentPlayer = (currentPlayer == Seed.CROSS) ? Seed.NOUGHT : Seed.CROSS;
        } while (currentState == GameState.PLAYING);  // repeat until game-over
    }

    /** The player with "theSeed" makes one move, with input validation.
     Update Cell's content, Board's currentRow and currentCol. */
    public boolean playerMove(Seed theSeed, int r, int c) {
        int row = r - 1;
        int col = c - 1;
        if (row >= 0 && row < Board.ROWS && col >= 0 && col < Board.COLS
                && board.cells[row][col].content == Seed.EMPTY) {
            board.cells[row][col].content = theSeed;
            board.currentRow = row;
            board.currentCol = col;
            return true; // input okay, exit loop
        } else {
            System.out.println("This move at (" + (row + 1) + "," + (col + 1)
                    + ") is not valid. Try again...");
        }
        return false;
    }

    /**
     * Update the currentState after the player with "theSeed" has moved.
     * Return corresponding reward (1 : cross won, -1 : nought won, 0.5 : draw, 0 : otherwise)
     * */
    public double updateGame(Seed theSeed) {
        if (board.hasWon(theSeed)) {  // check for win
            currentState = (theSeed == Seed.CROSS) ? GameState.CROSS_WON : GameState.NOUGHT_WON;
            if(currentState == GameState.CROSS_WON)
                return 1;
            else
                return  -1;
        } else if (board.isDraw()) {  // check for draw
            currentState = GameState.DRAW;
            return 0.5;
        }
        // Otherwise, no change to current state (still GameState.PLAYING).
        return 0;
    }

    /** The entry main() method */
    public static void main(String[] args) {
        int N_GAMES = 10;
        DQLAgent aX = new DQLAgent("Nando.eg");
        aX.epsilon = 1;
        aX.trainAgent(new MrMiagiAgent(Seed.NOUGHT, 1));

        //DQLAgent aX = new DQLAgent("DQLAgentTOP.eg");


        for(int i = 0; i < N_GAMES; i++) {
            GameMain game = new GameMain();  // Let the constructor do the job

            //Agent aX = new MrMiagiAgent(Seed.CROSS,1);

            /*System.out.println("Prediction for an empty board: ");
            Board b = new Board();
            b.init();
            b.paint();
            MLData prediction = ((DQLAgent) aX).network.compute(b.toMLData());
            CommonUtils.printPrediction(prediction.getData());*/

            Agent aO = new HumanAgent();
            game.playGame(aX, aO);
        }
    }
}
