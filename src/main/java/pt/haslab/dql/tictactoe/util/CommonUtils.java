package pt.haslab.dql.tictactoe.util;

import org.apache.commons.math3.util.Pair;
import org.encog.ml.data.MLData;

/**
 * Created by nunomachado on 08/08/17.
 */
public class CommonUtils {

    /**
     * Pretty prints the predicted reward given by the neural network
     * @param data
     */
    public static void printPrediction(double[] data){
        System.out.print("[");
        for(int i = 0; i < data.length; i++){
                System.out.print(data[i]+" ");
        }
        System.out.println("]");
    }

    /**
     * Counts the number of occurrences of an element in an array
     * @param data
     * @param key
     * @return
     */
    public static int countKey(double[] data, int key){
        int count = 0;
        for(int i = 0; i < data.length; i++){
            if(data[i] == key)
                count++;
        }
        return count;
    }


    public Pair<Integer,Double> getMax(double[] data){
        double maxVal = -1;
        int maxPos = -1;

        for (int i = 0; i < data.length; i++) {
            if (data[i] > maxVal) {
                maxVal = data[i];
                maxPos = i;
            }
        }
        return new Pair<Integer, Double>(maxPos,maxVal);
    }

    public static int indexOf(double[] data, int key){
        int index = -1;
        for(int i = 0; i < data.length; i++){
            if(data[i] == key) {
                index = i;
                break;
            }
        }
        return index;
    }
}
