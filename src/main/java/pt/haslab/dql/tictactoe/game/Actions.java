package pt.haslab.dql.tictactoe.game;

import org.apache.commons.math3.util.Pair;

/**
 * Created by nunomachado on 05/08/17.
 */
public class Actions {
    public static String[] ACTIONS = {
            "1 1",      //0
            "1 2",      //1
            "1 3",      //2
            "2 1",      //3
            "2 2",      //4
            "2 3",      //5
            "3 1",      //6
            "3 2",      //7
            "3 3"};     //8

    public static int getActionIndex(String action){
        for(int i = 0; i < ACTIONS.length; i++){
            if(ACTIONS[i] == action)
                return i;
        }
        return -1; //action does not exist
    }
}
