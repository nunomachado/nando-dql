package pt.haslab.dql.tictactoe.agents;

import pt.haslab.dql.tictactoe.game.Board;

/**
 * The abstract class of an Agent. An agent implementation
 * must provide a game strategy, i.e. to state
 * what move should be made by the agent depending on
 * the board state.
 * Created by nunomachado on 06/08/17.
 */
public abstract class Agent {
    public abstract String act(Board state);
}
