package pt.haslab.dql.tictactoe.learning;

import pt.haslab.dql.tictactoe.game.Actions;
import pt.haslab.dql.tictactoe.game.Board;
import pt.haslab.dql.tictactoe.game.Seed;

import javax.swing.*;
import java.util.Random;

/**
 * Created by nunomachado on 07/08/17.
 */
public class IntelliAgent extends Agent {

    public IntelliAgent(){}

    //agent acts with most common winning moves
    public String act(Board state) {

        boolean middeEmpty = state.cells[1][1].content == Seed.EMPTY;
        boolean middleNought = state.cells[1][1].content == Seed.NOUGHT;
        boolean topRightEmpty = state.cells[0][2].content == Seed.EMPTY;
        boolean topRightNought = state.cells[0][2].content == Seed.NOUGHT;
        boolean topLeftEmpty = state.cells[0][0].content == Seed.EMPTY;
        boolean topLeftNought = state.cells[0][0].content == Seed.NOUGHT;
        boolean bottomLeftEmpty = state.cells[2][0].content == Seed.EMPTY;
        boolean bottomRightEmpty = state.cells[2][2].content == Seed.EMPTY;


        if(middeEmpty)
            return Actions.ACTIONS[4]; //"2 2"
        if(middleNought && topRightEmpty)
            return Actions.ACTIONS[2]; //"1 3"
        if(middleNought && topRightNought && bottomLeftEmpty)
            return Actions.ACTIONS[6]; //"3 1"
        if(middleNought && topLeftEmpty)
            return Actions.ACTIONS[0]; //"1 1"
        if(middleNought && topLeftNought && bottomRightEmpty)
            return Actions.ACTIONS[8]; //"3 3"

        //play randomly otherwise
        Random r = new Random();
        int a = r.nextInt(Actions.ACTIONS.length);
        return Actions.ACTIONS[a];
    }
}
