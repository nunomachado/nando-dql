package pt.haslab.dql.tictactoe.learning;

import org.apache.commons.math3.util.Pair;
import org.encog.engine.network.activation.*;
import org.encog.ml.data.MLData;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;
import org.encog.persist.EncogDirectoryPersistence;
import pt.haslab.dql.tictactoe.game.*;
import pt.haslab.dql.tictactoe.util.CommonUtils;
import pt.haslab.dql.tictactoe.util.DrawPlot;
import sun.rmi.log.ReliableLog;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 * Created by nunomachado on 05/08/17.
 */
public class DQLAgent extends Agent {
    public String LOGFILE;
    public int epochs; // a number of games we want the agent to play
    public double gamma; //decay or discount rate, to calculate the future discounted reward
    public double epsilon;    //exploration rate, this is the rate in which an agent randomly decides its action rather than prediction
    public double epsilon_decay; // we want to decrease the number of explorations as it gets good at playing games.
    public double epsilon_min; //we want the agent to explore at least this amount
    public double learning_rate; // Determines how much neural net learns in each iteration
    public int batchsize;   //the number of memories used in replay
    public ArrayList<Memory> memoryList;   //list of memories of last game plays
    public BasicNetwork network;
    public ResilientPropagation train;

    public DQLAgent(String filename){
        LOGFILE = filename;
        epochs = 2500;
        gamma = 0.95;
        epsilon = 0.1;
        epsilon_decay = 0.995;
        epsilon_min = 0.01;
        learning_rate = 0.001;
        batchsize = 128;
        memoryList = new ArrayList<Memory>(2000);
        initNN();
    }

    public DQLAgent(int epochs, double gamma, double epsilon, double epsilon_min, double epsilon_decay, double learning_rate, String filename){
        LOGFILE = filename;
        this.epochs = epochs;
        this.gamma = gamma;
        this.epsilon = epsilon;
        this.epsilon_decay = epsilon_decay;
        this.epsilon_min = epsilon_min;
        this.learning_rate = learning_rate;
        this.memoryList = new ArrayList<Memory>(2000);
        initNN();
    }

    private void initNN(){
        if(!loadNNFromFile()) {
            //build NN with 9-neuron input layer, 9-neuron hidden layer, 9-neuron output layer
            network = new BasicNetwork();
            network.addLayer(new BasicLayer(null, true, 9));
            network.addLayer(new BasicLayer(new ActivationTANH(), true, 27));
            network.addLayer(new BasicLayer(new ActivationSigmoid(), true, 9));
            network.addLayer(new BasicLayer(new ActivationLinear(), false, 9));
            network.getStructure().finalizeStructure();
            network.reset();
        }
    }

    public void remember(Memory m){
        if(!memoryList.contains(m))
            memoryList.add(m);
    }

    public String act(Board state){
        Random r = new Random();
        if(r.nextDouble() <= epsilon){
            //agent acts randomly
            int a = r.nextInt(Actions.ACTIONS.length);
            System.out.println("(picks randomly)");
            return Actions.ACTIONS[a];
        }

        //Predict the reward value based on the given state
        MLData prediction = network.compute(state.toMLData());

        int invalidTries = 0;
        double[] data = prediction.getData().clone();
        int row = -1;
        int col = -1;
        Pair<Integer, Double> maxReward = null;
        do {
            if(invalidTries > 0){
                System.out.println("("+Actions.ACTIONS[maxReward.getKey()]+" is invalid)");
            }
            maxReward = getMaxRewardInvalid(prediction, invalidTries);
            col = maxReward.getKey()%3;
            row = (maxReward.getKey()-col)/3;
            invalidTries++;
            if(col == -1 || row == -1)
                System.out.println("WRONG");
        }while (state.cells[row][col].content != Seed.EMPTY);


        //Pick the action based on the predicted reward
        String nextAction = Actions.ACTIONS[maxReward.getKey()];
        System.out.println("(picks from NN | "+(invalidTries-1)+" invalid tries)");
        CommonUtils.printPrediction(prediction.getData());


        return nextAction;
    }


    /**
     * Return the maximum reward (expressed as a pair with the action and its reward)
     * for a given prediction given by the NN
     * @param prediction
     * @return
     */
    public Pair<Integer,Double> getMaxReward(MLData prediction){
        double maxVal = -1;
        int maxPos = -1;
        double[] data = prediction.getData().clone();

        for (int i = 0; i < prediction.size(); i++) {
            if (data[i] > maxVal) {
                maxVal = data[i];
                maxPos = i;
            }
        }
        return new Pair<Integer, Double>(maxPos,maxVal);
    }

    public Pair<Integer,Double> getMaxRewardInvalid(MLData prediction, int invalidTries){
        double maxVal = -1;
        int maxPos = -1;
        double[] data = prediction.getData().clone();

        do{
            if(maxPos != -1){
                data[maxPos] = -1;
                maxVal = -1;
                invalidTries--;
            }

            for (int i = 0; i < prediction.size(); i++) {
                if (data[i] > maxVal) {
                    maxVal = data[i];
                    maxPos = i;
                }
            }
        } while(invalidTries > 0);

        return new Pair<Integer, Double>(maxPos,maxVal);
    }

    public void replay(int batchSize){
        Collections.shuffle(memoryList);
        int size = Math.min(memoryList.size(), batchSize);
        double target = 0;

        for(int i = 0; i < size; i++){
            Memory m = memoryList.get(i);

            //if done, make our target reward
            target = m.reward;

            if(!m.done){
                //predict the future discounted reward
                MLData prediction = network.compute(m.nextState.toMLData());
                Pair<Integer,Double> maxReward = getMaxReward(prediction);
                target = m.reward + gamma * maxReward.getValue();
            }

            //make the agent to approximately map
            //the current state to future discounted reward
            MLData targetFuture = network.compute(m.state.toMLData());
            targetFuture.setData(Actions.getActionIndex(m.action), target);
            double[][] targetOutput = {targetFuture.getData()}; //transform targetFuture into double[][] as required by the BasicMLDataSet

            //Train the Neural Net with the state and targetFuture (with the updated reward value)
            MLDataSet trainingSet = new BasicMLDataSet(m.state.toTrainingData(), targetOutput);
            train = new ResilientPropagation(network, trainingSet);
            train.iteration();

        }

        //update epsilon (exploration rate)
        if(epsilon > epsilon_min){
            epsilon *= epsilon_decay;
        }
    }

    /**
     *  Play an 'epoch' number of games to train the agent
     */
    public void trainAgent(Agent adversary){
        //to measure improvement via training
        int totalReward = 0;
        ArrayList<Integer> rewardLog = new ArrayList<Integer>(epochs);

        for(int i = 0; i < epochs; i++) {
            System.out.println("\n ====== EPOCH "+i+" ======");
            //initialize game-board and current status
            GameMain game = new GameMain();
            Random r = new Random();
            game.currentPlayer = r.nextBoolean() ? Seed.CROSS : Seed.NOUGHT; //randomly pick first player
            int moves = 1;

            do{
                boolean validAction = false;
                double reward = 0;
                boolean isDone = false;
                Board oldBoard = game.board.clone();
                String a;

                //this agent's turn to play
                if(game.currentPlayer == Seed.CROSS) {
                    //////// don't penalizing invalid actions
                    do{
                        a = this.act(game.board);
                        String[] actions = a.split(" ");
                        System.out.println(">> X in " + a);
                        validAction = game.playerMove(game.currentPlayer, Integer.valueOf(actions[0]), Integer.valueOf(actions[1]));
                    } while (!validAction);

                    //update board and currentState
                    reward = game.updateGame(game.currentPlayer);
                    isDone = (game.currentState != GameState.PLAYING);
                    //*//////////////////////////////////////

                    ////////// trying to learn invalid actions by penalizing them
                   /* a = this.act(game.board);
                    String[] actions = a.split(" ");
                    System.out.println(">> X in " + a);
                    validAction = game.playerMove(game.currentPlayer, Integer.valueOf(actions[0]), Integer.valueOf(actions[1]));

                    if(!validAction){
                        //if the action isn't valid, the reward is 0
                        reward = 0;
                        isDone = false;
                    }
                    else{
                        //update board and currentState
                        reward = game.updateGame(game.currentPlayer);
                        isDone = (game.currentState != GameState.PLAYING);
                    }
                    //*//////////////////////////////////////
                }
                else{ //the other agent's turn
                    do{
                        a = adversary.act(game.board);
                        String[] actions = a.split(" ");
                        System.out.println(">> O in " + a);
                        validAction = game.playerMove(game.currentPlayer, Integer.valueOf(actions[0]), Integer.valueOf(actions[1]));
                    } while (!validAction);

                    //update board and currentState
                    reward = game.updateGame(game.currentPlayer);
                    isDone = (game.currentState != GameState.PLAYING);
                }

                //Remember the previous state, action, reward, and done (after both agents have played)
                if(game.currentPlayer == Seed.CROSS || isDone) {
                    if(game.currentPlayer == Seed.CROSS) {
                        Memory m = new Memory(oldBoard, a, reward, game.board.clone(), isDone);
                        this.remember(m);
                    }
                    else{//means that we just lost/tied the game due to a play by the other player
                        //change the reward of our last move to -1 or 0.5
                        this.memoryList.get(memoryList.size()-1).reward = reward;
                        this.memoryList.get(memoryList.size()-1).setNextState(game.board);
                    }
                }else if(moves > 1){
                    //update the board of previously stored memory with the adversary's play
                    this.memoryList.get(memoryList.size()-1).setNextState(game.board);
                }

                game.board.paint();

                // Print message and reward if game ended
                if(isDone) {
                    if (game.currentState == GameState.CROSS_WON) {
                        System.out.print("'X' WON! =)");
                    } else if (game.currentState == GameState.NOUGHT_WON) {
                        System.out.print("'O' won! =(");
                    } else if (game.currentState == GameState.DRAW) {
                        System.out.print("It's Draw! =/");
                    }
                    totalReward += reward;
                    rewardLog.add(totalReward);
                    System.out.println(" REWARD: "+reward + " ("+moves+" moves)");
                }

                // Switch player if previous action was valid
                if(validAction) {
                    moves++;
                    game.currentPlayer = (game.currentPlayer == Seed.CROSS) ? Seed.NOUGHT : Seed.CROSS;
                }

            } while (game.currentState == GameState.PLAYING);  // repeat until game-over

            this.replay(batchsize);
            if(adversary instanceof HumanAgent){
                saveNNToFile();
            }
        }
        System.out.println("TOTAL REWARD "+totalReward);
        DrawPlot plot = new DrawPlot("Reward for "+epochs+" epochs");
        plot.drawRewardPlot(rewardLog, "reward");
        saveNNToFile();
    }

    public boolean loadNNFromFile(){
        try{
            File file = new File(LOGFILE);
            System.out.println(">> Loading network from "+LOGFILE);
            BasicNetwork nn = (BasicNetwork) EncogDirectoryPersistence.loadObject(file);
            this.network = nn;
            return true;
        }
        catch (Exception e){
            System.err.println("File "+LOGFILE+" does not exist. Create a new NN.");
            //e.printStackTrace();
        }
        finally {
            return false;
        }
    }

    public void saveNNToFile(){
        try {
            System.out.println(">> Saving network to "+LOGFILE);
            EncogDirectoryPersistence.saveObject(new File(LOGFILE), this.network);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }


    public static void main(String args[]){

        DQLAgent agent = new DQLAgent("DQLAgent.eg");

        //agent.trainAgent(agent2);
        agent.epsilon = 1;
        agent.trainAgent(new MrMiagiAgent(Seed.NOUGHT, 1));
        //agent.epsilon = 1;
        //agent.trainAgent(new RandomAgent());

        System.out.println("Prediction for an empty board: ");
        Board b = new Board();
        b.init();
        b.paint();
        MLData prediction = agent.network.compute(b.toMLData());
        CommonUtils.printPrediction(prediction.getData());//*/

        agent.trainAgent(new HumanAgent());

    }
}
