package pt.haslab.dql.tictactoe.learning;

import pt.haslab.dql.tictactoe.game.Board;

import java.util.Scanner;

/**
 * Created by nunomachado on 06/08/17.
 */
public class HumanAgent extends Agent {
    private static Scanner in;

    public HumanAgent(){
        in = new Scanner(System.in);
    }

    //agent reads input from console (i.e. human input)
    public String act(Board state) {
        int row = in.nextInt();
        int col = in.nextInt();
        String action = row + " " + col;

        return action;
    }
}
