import random
from tictactoe.agents.agent import Agent
from tictactoe.game.moves import Moves
from tictactoe.game.board import Board, Seed

class BasicAgent(Agent):

    def make_move(self, state: Board) -> str:
        middle_empty = state.cells[1][1].content == Seed.EMPTY
        middle_nought = state.cells[1][1].content == Seed.NOUGHT
        top_right_empty = state.cells[0][2].content == Seed.EMPTY
        top_right_nought = state.cells[0][2].content == Seed.NOUGHT
        top_left_empty = state.cells[0][0].content == Seed.EMPTY
        top_left_nought = state.cells[0][0].content == Seed.NOUGHT
        bottom_left_empty = state.cells[2][0].content == Seed.EMPTY
        bottom_right_empty = state.cells[2][2].content == Seed.EMPTY
        if middle_empty:
            return Moves.MOVES[4]
        if middle_nought and top_right_empty:
            return Moves.MOVES[2]
        if middle_nought and top_right_nought and bottom_left_empty:
            return Moves.MOVES[6]
        if middle_nought and top_left_empty:
            return Moves.MOVES[0]
        if middle_nought and top_left_nought and bottom_right_empty:
            return Moves.MOVES[8]
        return random.choice(Moves.MOVES)

    def __str__(self):
        return 'BASIC'