from abc import ABC, abstractmethod

from tictactoe.game.board import Board


class Agent(ABC):
    """The abstract class of an Agent. An agent implementation
    must provide a game strategy, i.e. to state
    what move should be made by the agent depending on
    the board state.
    """

    @abstractmethod
    def make_move(self, state: Board) -> str:
        pass
