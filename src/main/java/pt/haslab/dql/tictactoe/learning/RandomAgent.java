package pt.haslab.dql.tictactoe.learning;

import pt.haslab.dql.tictactoe.game.Actions;
import pt.haslab.dql.tictactoe.game.Board;

import java.util.Random;

/**
 * Created by nunomachado on 06/08/17.
 */
public class RandomAgent extends Agent {

    public RandomAgent(){}

    //agent acts randomly
    public String act(Board state) {
        Random r = new Random();
        int a = r.nextInt(Actions.ACTIONS.length);
        return Actions.ACTIONS[a];
    }
}
