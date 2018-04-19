package pt.haslab.dql.tictactoe.game;

/**
 * Created by nunomachado on 05/08/17.
 */
public class Cell {

    public Seed content; // content of this cell of type Seed
    int row, col; // row and column of this cell, not used in this program

    public Cell(int row, int col) {
        this.row = row;
        this.col = col;
        clear();  // clear content
    }

    public void clear() {
        content = Seed.EMPTY;
    }

    /** Paint itself */
    public void paint() {
        switch (content) {
            case CROSS:  System.out.print("'X'"); break;
            case NOUGHT: System.out.print("'O'"); break;
            case EMPTY:  System.out.print("' '"); break;
        }
    }
}
