from enum import Enum
from typing import Tuple

from tictactoe.agents.agent import Agent
from tictactoe.game.board import Board, Seed
from tictactoe.game.moves import Moves


class GameState(Enum):
    PLAYING = 1
    DRAW = 2
    CROSS_WON = 3
    NOUGHT_WON = 4


class GameMain:
    def __init__(self, agent_X: Agent, agent_O: Agent) -> None:
        """Constructor to setup the game."""
        self.board = Board()
        self.current_state = GameState.PLAYING
        self.current_player = Seed.CROSS
        self.agent_X = agent_X
        self.agent_O = agent_O

        self.board.init()

    def play_game(self) -> int:
        """Implements the logic of a tic-tac-toe game played by two agents."""
        print(f"\n===== GAME =====")
        while self.current_state == GameState.PLAYING:
            agent = self.agent_X if self.current_player == Seed.CROSS else self.agent_O
            print(
                f"\nPlayer '{self.current_player.value}' ({agent.__class__.__name__}), enter your move (row[1-3] column[1-3]): "
            )

            # get valid move from agent
            valid_move = False
            while not valid_move:
                move = agent.make_move(self.board)
                print(f">> {move}")
                try:
                    r, c = Moves.move_to_tuple(move)
                    _, reward, _ = self.apply_move(r, c, Seed.CROSS) # assume target_seed is CROSS
                    valid_move = True
                except ValueError:
                    print(f"Move {move} is not valid. Try again...")

            # update board and current state
            self.board.paint()
    
        return reward
    
    def apply_move(self, row: int, col: int, target_seed: Seed) -> Tuple[Board, float, bool]:
        """Take an move, update the game state, and return the next state, reward, and done flag."""
        valid_move = self.validate_move(self.current_player, row, col)
        
        if not valid_move:
            raise ValueError(f"Invalid move: {row} {col}")

        reward = self.update_game(self.current_player, target_seed)
        done = self.current_state != GameState.PLAYING

        # Switch player if the game is not done
        if not done:
            self.current_player = (
                Seed.NOUGHT if self.current_player == Seed.CROSS else Seed.CROSS
            )

        return self.board, reward, done

    def validate_move(self, seed: Seed, r: int, c: int) -> bool:
        """The player with 'seed' makes one move with input validation."""
        row = r - 1
        col = c - 1
        if (
            0 <= row < Board.ROWS
            and 0 <= col < Board.COLS
            and self.board.cells[row][col].content == Seed.EMPTY
        ):
            self.board.cells[row][col].content = seed
            self.board.current_row = row
            self.board.current_col = col
            return True
        else:
            return False

    def update_game(self, player_seed: Seed, target_seed: Seed ) -> float:
        """Update the currentState after the player with 'player_seed' has moved.
        Return corresponding reward according to the target_seed (1 if target_seed won, -1 if target_seed lost, 0 if draw or otherwise)
        """
        if self.board.has_won(player_seed):
            self.current_state = (
                GameState.CROSS_WON if player_seed == Seed.CROSS else GameState.NOUGHT_WON
            )
            if self.current_state == GameState.CROSS_WON:
                print("'X' won!")
                return 1 if target_seed == Seed.CROSS else -1
            else:
                print("'O' won!")
                return 1 if target_seed == Seed.NOUGHT else -1
        elif self.board.is_draw():
            self.current_state = GameState.DRAW
            print("It's a Draw!")
            return 0
        
        # Small negative reward for each move to encourage faster wins
        return -0.01