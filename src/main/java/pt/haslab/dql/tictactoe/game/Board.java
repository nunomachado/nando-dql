package pt.haslab.dql.tictactoe.game;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

/**
 * Created by nunomachado on 05/08/17.
 */
public class Board {
    public static final int ROWS = 3;
    public static final int COLUMNS = 3;
    public Cell[][] cells;  // a board composes of ROWS-by-COLUMNS Cell instances
    int currentRow, currentColumn;  // the current seed's row and column

    /** Constructor to initialize the game board */
    public Board() {
        cells = new Cell[ROWS][COLUMNS];  // allocate the array
        for (int row = 0; row < ROWS; ++row) {
            for (int col = 0; col < COLUMNS; ++col) {
                cells[row][col] = new Cell(row, col); // allocate element of the array
            }
        }
    }

    /** Initialize (or re-initialize) the contents of the game board */
    public void init() {
        for (int row = 0; row < ROWS; ++row) {
            for (int col = 0; col < COLUMNS; ++col) {
                cells[row][col].clear();  // clear the cell content
            }
        }
    }

    /** Return true if it is a draw (i.e., no more EMPTY cell) */
    public boolean isDraw() {
        for (int row = 0; row < ROWS; ++row) {
            for (int col = 0; col < COLUMNS; ++col) {
                if (cells[row][col].content == Seed.EMPTY) {
                    return false; // an empty seed found, not a draw, exit
                }
            }
        }
        return true; // no empty cell, it's a draw
    }

    /** Return true if the player with "theSeed" has won after placing at
     (currentRow, currentColumn) */
    public boolean hasWon(Seed theSeed) {
        return (cells[currentRow][0].content == theSeed         // 3-in-the-row
                && cells[currentRow][1].content == theSeed
                && cells[currentRow][2].content == theSeed
                || cells[0][currentColumn].content == theSeed      // 3-in-the-column
                && cells[1][currentColumn].content == theSeed
                && cells[2][currentColumn].content == theSeed
                || currentRow == currentColumn            // 3-in-the-diagonal
                && cells[0][0].content == theSeed
                && cells[1][1].content == theSeed
                && cells[2][2].content == theSeed
                || currentRow + currentColumn == 2    // 3-in-the-opposite-diagonal
                && cells[0][2].content == theSeed
                && cells[1][1].content == theSeed
                && cells[2][0].content == theSeed);
    }

    /** Paint itself */
    public void paint() {
        for (int row = 0; row < ROWS; ++row) {
            for (int col = 0; col < COLUMNS; ++col) {
                cells[row][col].paint();   // each cell paints itself
                if (col < COLUMNS - 1) System.out.print("|");
            }
            System.out.println();
            if (row < ROWS - 1) {
                //System.out.println("-----------");
            }
        }
    }

    @Override
    public Board clone(){
        Board ret = new Board();
        for (int row = 0; row < ROWS; ++row) {
            for (int col = 0; col < COLUMNS; ++col) {
                ret.cells[row][col].content = this.cells[row][col].content;
            }
        }
        return ret;
    }

    public INDArray toINDArray() {
        double[][] data = new double[1][9];
        int pos = 0;
        for (int row = 0; row < ROWS; ++row) {
            for (int col = 0; col < COLUMNS; ++col) {
                switch(cells[row][col].content) {
                    case CROSS:
                        data[0][pos] = 1;
                        break;
                    case EMPTY:
                        data[0][pos] = 0;
                        break;
                    case NOUGHT:
                        data[0][pos] = -1;
                        break;
                }
                pos++;
            }
        }

        return Nd4j.create(data);
    }

    public double[][] toTrainingData(){
        double[][] data = new double[1][9];
        int pos = 0;
        for (int row = 0; row < ROWS; ++row) {
            for (int col = 0; col < COLUMNS; ++col) {
                switch(cells[row][col].content) {
                    case CROSS:
                        data[0][pos] = 1;
                        break;
                    case EMPTY:
                        data[0][pos] = 0;
                        break;
                    case NOUGHT:
                        data[0][pos] = -1;
                        break;
                }
                pos++;
            }
        }
        return data;
    }

    public int[][] toMatrix(){
        int[][] data = new int[ROWS][COLUMNS];
        for (int row = 0; row < ROWS; ++row) {
            for (int col = 0; col < COLUMNS; ++col) {
                switch(cells[row][col].content) {
                    case CROSS:
                        data[row][col] = 1;
                        break;
                    case EMPTY:
                        data[row][col] = 0;
                        break;
                    case NOUGHT:
                        data[row][col] = -1;
                        break;
                }
            }
        }
        return data;
    }

}
