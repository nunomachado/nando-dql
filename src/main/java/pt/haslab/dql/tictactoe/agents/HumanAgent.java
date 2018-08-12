package pt.haslab.dql.tictactoe.agents;

import pt.haslab.dql.tictactoe.game.Board;

import java.util.Scanner;

/**
 * Implements an agent that receives the next move from
 * the console, thus enabling human players.
 * Created by nunomachado on 06/08/17.
 */
public class HumanAgent extends Agent {
    private static Scanner in;

    public HumanAgent(){
        in = new Scanner(System.in);
    }

    //Read next move from the console (i.e. from human input)
    public String act(Board state) {
        int row = in.nextInt();
        int col = in.nextInt();
        String action = row + " " + col;

        return action;
    }

    @Override
    public String toString()
    {
        return AgentType.HUMAN.toString();
    }
}
