package pt.haslab.dql.tictactoe.agents;

import pt.haslab.dql.tictactoe.game.Actions;
import pt.haslab.dql.tictactoe.game.Board;
import pt.haslab.dql.tictactoe.game.Seed;
import pt.haslab.dql.tictactoe.util.CommonUtils;

import java.util.Random;

/**
 * Implements a master agent, dubbed MrMiyagi, which follows a hardcoded
 * strategy that makes the best move available at each time.
 * Created by nunomachado on 09/08/17.
 * Adapted from https://github.com/rfeinman/tictactoe-reinforcement-learning/blob/master/agent.py
 */
public class MrMiyagiAgent extends Agent {
    private double proLevel; //probability of following the optimal strategy instead of a random one
    private int mySymbol;

    public MrMiyagiAgent(Seed key, double level)
    {
        proLevel = level;
        mySymbol = key == Seed.CROSS ? 1 : -1;
    }

    /**
     * If we have two in a row and the 3rd is available, take it.
     * @param board
     */
    private String win(int[][] board, int symbol){
        //Check for diagonal wins
        double[] a = {board[0][0], board[1][1], board[2][2]};
        double[] b = {board[0][2], board[1][1], board[2][0]};

        if(CommonUtils.countKey(a,0) == 1 && CommonUtils.countKey(a, symbol) == 2){
            int index = CommonUtils.indexOf(a, 0);
            return Actions.ACTIONS[(3*index+index)];
        }
        else if(CommonUtils.countKey(b,0) == 1 && CommonUtils.countKey(b, symbol) == 2){
            int index = CommonUtils.indexOf(b, 0);
            if(index == 0)
                return Actions.ACTIONS[2];
            else if(index == 1)
                return Actions.ACTIONS[4];
            else
            return Actions.ACTIONS[6];
        }
        //Now check for 2 in a row/column + empty 3rd
        for(int i = 0; i < 3; i++){
            double[] c = {board[0][i], board[1][i], board[2][i]};
            double[] d = {board[i][0], board[i][1], board[i][2]};
            if(CommonUtils.countKey(c,0) == 1 && CommonUtils.countKey(c, symbol) == 2){
                int index = CommonUtils.indexOf(c, 0);
                return Actions.ACTIONS[3*index+i];
            }
            else if(CommonUtils.countKey(d,0) == 1 && CommonUtils.countKey(d, symbol) == 2){
                int index = CommonUtils.indexOf(d, 0);
                return Actions.ACTIONS[3*i+index];
            }
        }
        return null;
    }

    //return the move that will make our opponent win
    private String blockWin(int[][] board){
        return win(board, mySymbol*-1);
    }

    //Create a fork opportunity such that we have 2 threats to win.
    private String fork(int[][] board){
        //Check all adjacent side middles
        if(board[1][0] == mySymbol && board[0][1] == mySymbol){
            if(board[0][0] == 0 && board[2][0] == 0 && board[0][2] == 0)
                return Actions.ACTIONS[0];
            else if(board[1][1] == 0 && board[2][1] == 0 && board[1][2] == 0)
                return Actions.ACTIONS[4];
        }
        else if(board[1][0] == mySymbol && board[2][1] == mySymbol) {
            if (board[2][0] == 0 && board[0][0] == 0 && board[2][2] == 0)
                return Actions.ACTIONS[6];
            else if (board[1][1] == 0 && board[0][1] == 0 && board[1][2] == 0)
                return Actions.ACTIONS[4];
        }
        else if(board[2][1] == mySymbol && board[1][2] == mySymbol) {
            if (board[2][2] == 0 && board[2][0] == 0 && board[0][2] == 0)
                return Actions.ACTIONS[8];
            else if (board[1][1] == 0 && board[1][0] == 0 && board[0][1] == 0)
                return Actions.ACTIONS[4];
        }
        else if(board[1][2] == mySymbol && board[0][1] == mySymbol) {
            if (board[0][2] == 0 && board[0][0] == 0 && board[2][2] == 0)
                return Actions.ACTIONS[2];
            else if (board[1][1] == 0 && board[1][0] == 0 && board[2][1] == 0)
                return Actions.ACTIONS[4];
        }
        //Check all cross corners
        else if(board[0][0] == mySymbol && board[2][2] == mySymbol) {
            if (board[1][0] == 0 && board[2][1] == 0 && board[2][0] == 0)
                return Actions.ACTIONS[6];
            else if (board[0][1] == 0 && board[1][2] == 0 && board[0][2] == 0)
                return Actions.ACTIONS[2];
        }
        else if(board[2][0] == mySymbol && board[0][2] == mySymbol) {
            if (board[2][1] == 0 && board[1][2] == 0 && board[2][2] == 0)
                return Actions.ACTIONS[8];
            else if (board[1][0] == 0 && board[0][1] == 0 && board[0][0] == 0)
                return Actions.ACTIONS[0];
        }
        return null;
    }

    //Block the opponents fork if she has one available.
    private String blockFork(int[][] board){
        double[] corners = {board[0][0], board[2][0], board[0][2], board[2][2]};
        int opponent = mySymbol*-1;
        //Check all adjacent side middles
        if( board[1][0] == opponent && board[0][1] == opponent) {
            if (board[0][0] == 0 && board[2][0] == 0 && board[0][2] == 0)
                return Actions.ACTIONS[0];
            else if (board[1][1] == 0 && board[2][1] == 0 && board[1][2] == 0)
                return Actions.ACTIONS[4];
        }
        else if( board[1][0] == opponent && board[2][1] == opponent) {
            if (board[2][0] == 0 && board[0][0] == 0 && board[2][2] == 0)
                return Actions.ACTIONS[6];
            else if (board[1][1] == 0 && board[0][1] == 0 && board[1][2] == 0)
                return Actions.ACTIONS[4];
        }
        else if( board[2][1] == opponent && board[1][2] == opponent)
            if( board[2][2] == 0 && board[2][0] == 0 && board[0][2] == 0)
                return Actions.ACTIONS[8];
            else if( board[1][1] == 0 && board[1][0] == 0 && board[0][1] == 0)
            return Actions.ACTIONS[4];
        else if( board[1][2] == opponent && board[0][1] == opponent)
            if( board[0][2] == 0 && board[0][0] == 0 && board[2][2] == 0)
                return Actions.ACTIONS[2];
            else if( board[1][1] == 0 && board[1][0] == 0 && board[2][1] == 0)
            return Actions.ACTIONS[4];

        //Check all cross corners (first check for double fork opp using the corners array)
        else if( CommonUtils.countKey(corners,0) == 1 && CommonUtils.countKey(corners,opponent) == 2)
            return Actions.ACTIONS[5];
        else if( board[0][0] == opponent && board[2][2] == opponent)
            if( board[1][0] == 0 && board[2][1] == 0 && board[2][0] == 0)
                return Actions.ACTIONS[6];
            else if( board[0][1] == 0 && board[1][2] == 0 && board[0][2] == 0)
            return Actions.ACTIONS[2];
        else if( board[2][0] == opponent && board[0][2] == opponent)
            if( board[2][1] == 0 && board[1][2] == 0 && board[2][2] == 0)
                return Actions.ACTIONS[8];
            else if( board[1][0] == 0 && board[0][1] == 0 && board[0][0] == 0)
            return Actions.ACTIONS[0];
        return null;
    }

    //Pick the center if it is available.
    public String center(int[][] board){
        if(board[1][1] == 0)
            return Actions.ACTIONS[4];
        return null;
    }

    //Pick a corner move
    public String corner(int[][] board){
        int opponent = mySymbol*-1;
        //Pick opposite corner of opponent if available
        if( board[0][0] == opponent && board[2][2] == 0)
            return Actions.ACTIONS[8];
        else if( board[2][0] == opponent && board[0][2] == 0)
            return Actions.ACTIONS[2];
        else if( board[0][2] == opponent && board[2][0] == 0)
            return Actions.ACTIONS[6];
        else if( board[2][2] == opponent && board[0][0] == 0)
            return Actions.ACTIONS[0];
        //Pick any corner if( no opposites are available
        else if( board[0][0] == 0)
            return Actions.ACTIONS[0];
        else if( board[2][0] == 0)
            return Actions.ACTIONS[6];
        else if( board[0][2] == 0)
            return Actions.ACTIONS[2];
        else if( board[2][2] == 0)
            return Actions.ACTIONS[8];
        return null;
    }

    //Pick an empty side.
    public String sideEmpty(int[][] board){
        if( board[1][0] == 0)
            return Actions.ACTIONS[3];
        else if( board[2][1] == 0)
            return Actions.ACTIONS[7];
        else if( board[1][2] == 0)
            return Actions.ACTIONS[5];
        else if( board[0][1] == 0)
            return Actions.ACTIONS[1];
        return null;
    }

    //agent acts randomly
    public String randomMove(int[][] board) {
        Random r = new Random();
        int a = r.nextInt(Actions.ACTIONS.length);
        return Actions.ACTIONS[a];
    }

    /**
     *  Agent goes through a hierarchy of moves, making the best move that
        is currently available each time.
     * @param state
     * @return
     */
    public String act(Board state) {
        Random r = new Random();
        int[][] board = state.toMatrix();

        //Choose randomly with some probability so that the agent does not always win
        if(r.nextDouble() > proLevel){
            return randomMove(board);
        }
        //Follow optimal strategy
        String a = win(board, mySymbol);
        if(a!=null)
            return a;
        a = blockWin(board);
        if(a!=null)
            return a;
        a = fork(board);
        if(a!=null)
            return a;
        a = blockFork(board);
        if(a!=null)
            return a;
        a = center(board);
        if(a!=null)
            return a;
        a = corner(board);
        if(a!=null)
            return a;
        a = sideEmpty(board);
        if(a!=null)
            return a;
        return randomMove(board);
    }
}
