from typing import Tuple


class Moves:
    MOVES = ["1 1", "1 2", "1 3", "2 1", "2 2", "2 3", "3 1", "3 2", "3 3"]

    @staticmethod
    def get_move_index(action: str) -> int:
        if action in Moves.MOVES:
            return Moves.MOVES.index(action)
        return -1  # action does not exist

    @staticmethod
    def move_to_tuple(move: str) -> Tuple[int, int]:
        if move not in Moves.MOVES:
            raise ValueError(f"Invalid move: {move}")
        return tuple(map(int, move.split()))
    