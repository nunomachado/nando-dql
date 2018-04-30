package pt.haslab.dql.tictactoe.agents;

import pt.haslab.dql.tictactoe.game.Actions;
import pt.haslab.dql.tictactoe.game.Board;
import pt.haslab.dql.tictactoe.game.Seed;

import java.util.Random;

/**
 * Implements an agent that attempts to play obvious and
 * typical moves if possible or random ones otherwise.
 * Created by nunomachado on 07/08/17.
 */
public class BasicAgent extends Agent {

    public BasicAgent(){}

    //agent acts with most common winning moves
    public String act(Board state) {

        boolean middleEmpty = state.cells[1][1].content == Seed.EMPTY;
        boolean middleNought = state.cells[1][1].content == Seed.NOUGHT;
        boolean topRightEmpty = state.cells[0][2].content == Seed.EMPTY;
        boolean topRightNought = state.cells[0][2].content == Seed.NOUGHT;
        boolean topLeftEmpty = state.cells[0][0].content == Seed.EMPTY;
        boolean topLeftNought = state.cells[0][0].content == Seed.NOUGHT;
        boolean bottomLeftEmpty = state.cells[2][0].content == Seed.EMPTY;
        boolean bottomRightEmpty = state.cells[2][2].content == Seed.EMPTY;

        //mark the middle first - "2 2"
        if(middleEmpty)
            return Actions.ACTIONS[4];
        //mark the top right - "1 3"
        if(middleNought && topRightEmpty)
            return Actions.ACTIONS[2];
        //mark the top right - "3 1"
        if(middleNought && topRightNought && bottomLeftEmpty)
            return Actions.ACTIONS[6];
        //mark the top left - "1 1"
        if(middleNought && topLeftEmpty)
            return Actions.ACTIONS[0];
        //mark the bottom right - "3 3"
        if(middleNought && topLeftNought && bottomRightEmpty)
            return Actions.ACTIONS[8];

        //play randomly otherwise
        Random r = new Random();
        int a = r.nextInt(Actions.ACTIONS.length);
        return Actions.ACTIONS[a];
    }
}
