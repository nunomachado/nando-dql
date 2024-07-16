from tictactoe.agents.agent import Agent
from tictactoe.game.board import Board


class HumanAgent(Agent):

    def __init__(self):
        self.inp = input

    def make_move(self, state: Board) -> str:
        row = int(self.inp("Enter row: "))
        col = int(self.inp("Enter column: "))
        return f"{row} {col}"

    def __str__(self):
        return "HUMAN"
