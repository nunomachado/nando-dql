import random

from tictactoe.agents.agent import Agent
from tictactoe.game.board import Board
from tictactoe.game.moves import Moves


class RandomAgent(Agent):

    def make_move(self, state: Board) -> str:
        return random.choice(list(Moves.MOVES))

    def __str__(self):
        return "RANDOM"
