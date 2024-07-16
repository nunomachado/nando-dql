import itertools
from enum import Enum
from typing import List

import numpy as np


class Seed(Enum):
    EMPTY = " "
    CROSS = "X"
    NOUGHT = "O"


class Cell:
    def __init__(self, row, col):
        self.row = row
        self.col = col
        self.content = Seed.EMPTY

    def clear(self):
        self.content = Seed.EMPTY

    def paint(self):
        print(self.content.value, end="")


class Board:
    ROWS = 3
    COLS = 3

    def __init__(self):
        self.cells = [
            [Cell(row, col) for col in range(self.COLS)] for row in range(self.ROWS)
        ]
        self.current_row = 0
        self.current_col = 0

    def init(self):
        for cell in itertools.chain(*self.cells):
            cell.clear()

    def is_draw(self) -> bool:
        return all(cell.content != Seed.EMPTY for cell in itertools.chain(*self.cells))

    def has_won(self, seed: Seed) -> bool:
        return (
            all(
                self.cells[self.current_row][col].content == seed
                for col in range(Board.COLS)
            )
            or all(
                self.cells[row][self.current_col].content == seed
                for row in range(Board.ROWS)
            )
            or all(self.cells[i][i].content == seed for i in range(Board.ROWS))
            or all(
                self.cells[i][Board.COLS - 1 - i].content == seed
                for i in range(Board.ROWS)
            )
        )

    def paint(self):
        for row in range(Board.ROWS):
            for col in range(Board.COLS):
                self.cells[row][col].paint()
                if col < Board.COLS - 1:
                    print("|", end="")
            print()
            if row < Board.ROWS - 1:
                print("-" * (Board.COLS * 2 - 1))

    def to_numpy_arr(self) -> np.ndarray:
        return np.array(
            [self._seed_to_value(cell.content) for cell in itertools.chain(*self.cells)]
        )

    def to_matrix(self) -> List[List[int]]:
        return [
            [self._seed_to_value(cell.content) for cell in row] for row in self.cells
        ]

    @staticmethod
    def _seed_to_value(seed: Seed) -> int:
        return 1 if seed == Seed.CROSS else 0 if seed == Seed.EMPTY else -1
