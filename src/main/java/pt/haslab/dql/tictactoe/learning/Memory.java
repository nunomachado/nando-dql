package pt.haslab.dql.tictactoe.learning;

import org.apache.commons.math3.util.Pair;
import pt.haslab.dql.tictactoe.game.Board;
import pt.haslab.dql.tictactoe.game.Seed;

/**
 * Created by nunomachado on 05/08/17.
 */
public class Memory {
    private int hash; //used to speed up equals
    public Board state;  //origin state
    public String action; //action consisting of a string of format "r c" meaning put an X in row r and column c
    public double reward;    //reward obtained by applying action to state
    public Board nextState; //destination state after applying action to origin state
    boolean done; //indicates if state is the final state

    public Memory(Board s, String a, double r, Board ns, boolean isFinalState){
        state = s;
        action = a;
        reward = r;
        nextState = ns;
        done = isFinalState;

        //compute hash
        computeHash();

    }

    public void setState(Board newState){
        this.state = newState;
    }

    public void setNextState(Board newState){
        this.nextState = newState;

        //update the hash after setting the new state
        computeHash();
    }

    /**
     * Two memories are the same if their action is identical and yields the same next state
     */
    private void computeHash(){
        String str = action;
        for(int i = 0; i < state.ROWS; i++) {
            for (int j = 0; j < state.COLS; j++) {
                switch (nextState.cells[i][j].content) {
                    case CROSS: str += "1"; break;
                    case EMPTY: str += "0"; break;
                    case NOUGHT: str += "-1"; break;
                }
            }
        }
        hash = str.hashCode();
    }

    @Override
    public boolean equals(Object o){
        boolean res = false;
        if (this==o){
            res = true;
        }
        else{
            if(o instanceof Memory){
                Memory tmp = (Memory) o;
                res =  (tmp.hashCode() == this.hashCode());
            }
        }
        return res;
    }

    @Override
    public int hashCode(){
        return hash;
    }

    //test equals and hashcode
    public static void main(String args[]){
        Board b1 = new Board();
        b1.cells[0][0].content = Seed.CROSS;
        b1.cells[1][1].content = Seed.CROSS;
        b1.cells[2][2].content = Seed.CROSS;

        Board b2 = new Board();
        b2.cells[0][0].content = Seed.CROSS;
        b2.cells[1][1].content = Seed.CROSS;
        b2.cells[2][2].content = Seed.CROSS;

        Board b3 = new Board();
        b3.cells[0][0].content = Seed.NOUGHT;
        b3.cells[1][1].content = Seed.NOUGHT;
        b3.cells[2][2].content = Seed.NOUGHT;

        Board b4 = new Board();

        Memory m1 = new Memory(b1, "1 2", 0, b1, false);
        Memory m2 = new Memory(b1, "1 2", 0, b1, false);
        Memory m3 = new Memory(b2, "1 2", 0, b1, false);
        Memory m4 = new Memory(b1, "1 3", 0, b1, false);
        Memory m5 = new Memory(b3, "1 2", 0, b1, false);

        System.out.println("m1 == m2 (true) ? "+(m1.equals(m2)));
        System.out.println("m1 == m3 (true) ? "+(m1.equals(m3)));
        System.out.println("m1 == m4 (false) ? "+(m1.equals(m4)));
        System.out.println("m1 == m5 (false) ? "+(m1.equals(m5)));
        System.out.println("m1 hash: "+m1.hashCode());
        System.out.println("m2 hash: "+m2.hashCode());
        System.out.println("m3 hash: "+m3.hashCode());
        System.out.println("m4 hash: "+m4.hashCode());
        System.out.println("m5 hash: "+m5.hashCode());

    }
}
