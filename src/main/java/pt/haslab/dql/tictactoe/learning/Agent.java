package pt.haslab.dql.tictactoe.learning;

import pt.haslab.dql.tictactoe.game.Board;

/**
 * Created by nunomachado on 06/08/17.
 */
public abstract class Agent {
    public abstract String act(Board state);
}
