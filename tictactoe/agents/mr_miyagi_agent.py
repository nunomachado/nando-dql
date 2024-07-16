import random

from tictactoe.agents.agent import Agent
from tictactoe.game.board import Board, Seed
from tictactoe.game.moves import Moves


class MrMiyagiAgent(Agent):
    """
    Implements a master agent, dubbed MrMiyagi, which follows a hardcoded
    strategy that makes the best move available at each time.
    Adapted from https://github.com/rfeinman/tictactoe-reinforcement-learning/blob/master/agent.py
    """

    def __init__(self, seed: Seed, level: float):
        self.pro_level = level
        if seed == Seed.CROSS:
            self.seed = 1
        else:
            self.seed = -1

    def find_move(self, board, symbol):
        """Helper function to find winning or blocking move."""
        for i in range(3):
            if board[i].count(symbol) == 2 and board[i].count(0) == 1:
                return Moves.MOVES[i * 3 + board[i].index(0)]
            col = [board[j][i] for j in range(3)]
            if col.count(symbol) == 2 and col.count(0) == 1:
                return Moves.MOVES[col.index(0) * 3 + i]
        else:
            diag1 = [board[i][i] for i in range(3)]
            diag2 = [board[i][2 - i] for i in range(3)]
            if diag1.count(symbol) == 2 and diag1.count(0) == 1:
                index = diag1.index(0)
                return Moves.MOVES[index * 3 + index]
            if diag2.count(symbol) == 2 and diag2.count(0) == 1:
                index = diag2.index(0)
                return Moves.MOVES[index * 3 + (2 - index)]

    def win(self, board):
        """
        Finds a winning move if one exists.
        """
        return self.find_move(board, self.seed)

    def block_win(self, board):
        return self.find_move(board, -self.seed)

    def find_fork(self, board, symbol):
        """Helper function to find fork opportunities."""
        for i, j in [(1, 1), (0, 0), (0, 2), (2, 0), (2, 2)]:
            if board[i][j] == 0:
                board[i][j] = symbol
                moves = sum(
                    (self.find_move(board, symbol) is not None for _ in range(4))
                )
                board[i][j] = 0
                if moves >= 2:
                    return Moves.MOVES[i * 3 + j]
        else:
            return

    def fork(self, board):
        return self.find_fork(board, self.seed)

    def block_fork(self, board):
        return self.find_fork(board, -self.seed)

    def center(self, board):
        if board[1][1] == 0:
            return Moves.MOVES[4]
        return None

    def corner(self, board):
        for i, j in [(0, 0), (0, 2), (2, 0), (2, 2)]:
            if board[i][j] == 0:
                return Moves.MOVES[i * 3 + j]
        else:
            return

    def side_empty(self, board):
        for i, j in [(1, 0), (2, 1), (1, 2), (0, 1)]:
            if board[i][j] == 0:
                return Moves.MOVES[i * 3 + j]
        else:
            return

    def random_move(self):
        return random.choice(Moves.MOVES)

    def make_move(self, state: Board) -> str:
        board = state.to_matrix()
        if random.random() > self.pro_level:
            return self.random_move(board)
        strategies = [
            self.win,
            self.block_win,
            self.fork,
            self.block_fork,
            self.center,
            self.corner,
            self.side_empty,
        ]
        for strategy in strategies:
            action = strategy(board)
            if action:
                return action
        else:
            return self.random_move(board)

    def __str__(self):
        return "MIYAGI"
