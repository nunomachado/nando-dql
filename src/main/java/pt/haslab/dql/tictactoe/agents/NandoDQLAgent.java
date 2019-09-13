package pt.haslab.dql.tictactoe.agents;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import org.apache.commons.math3.util.Pair;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.learning.config.Sgd;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import pt.haslab.dql.tictactoe.agents.learning.Memory;
import pt.haslab.dql.tictactoe.agents.learning.QLearningConfig;
import pt.haslab.dql.tictactoe.game.Actions;
import pt.haslab.dql.tictactoe.game.Board;
import pt.haslab.dql.tictactoe.game.GameMain;
import pt.haslab.dql.tictactoe.game.GameState;
import pt.haslab.dql.tictactoe.game.Seed;
import pt.haslab.dql.tictactoe.util.CommonUtils;
import pt.haslab.dql.tictactoe.util.DrawPlot;

/**
 * Implements a deep Q-learning agent, dubbed Nando.
 * Created by nunomachado on 05/08/17.
 */
public class NandoDQLAgent
                extends Agent
{

    /* list of memories with the last game moves */
    private ArrayList<Memory> memoryList;

    /* neural network (NN) */
    private MultiLayerNetwork network;

    /* Deep Q-learning configuration */
    QLearningConfig learnConfig;

    /* NN load/save file */
    private String NNFILE;

    public NandoDQLAgent( String nnFile )
    {
        NNFILE = nnFile;
        learnConfig = new QLearningConfig();
        memoryList = new ArrayList<Memory>( 2000 );

        initNN();
    }

    public NandoDQLAgent( QLearningConfig qLearningConfig, String nnFile )
    {
        NNFILE = nnFile;
        learnConfig = qLearningConfig;
        this.memoryList = new ArrayList<Memory>( 2000 );
        initNN();
    }

    /**
     * Initializes the neural network by loading it from a file
     * or creating a new one.
     */
    private void initNN()
    {
        if ( !loadNNFromFile() )
        {
            MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                    .updater(new Sgd(learnConfig.getLearningRate()))
                    .activation(Activation.SIGMOID)
                    .weightInit(WeightInit.XAVIER)
                    //.l2(0.0001)
                    .list()
                    // Input is 9 neurons, representing the board state
                    .layer(new DenseLayer.Builder()
                            .nIn(9)
                            .nOut(27)
                            .activation(Activation.SIGMOID)
                            .build())
                    .layer(new DenseLayer.Builder()
                            .nIn(27)
                            .nOut(27)
                            .activation(Activation.SIGMOID)
                            .build())
                    // Output is 9 neurons, each represents the Q-value for an action (i.e. play in a given cell)
                    .layer(new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                            .activation(Activation.SIGMOID)
                            .nIn(27)
                            .nOut(9)
                            .build())
                    .build();

            network = new MultiLayerNetwork(conf);
            network.init();

            System.out.println(network.summary());
        }
    }

    /**
     * Store a move for posterior replay
     *
     * @param m
     */
    public void remember( Memory m )
    {
        if ( !memoryList.contains( m ) )
            memoryList.add( m );
    }

    /**
     * Play the next move by:
     * 1) using the NN to compute the per-action reward based on the current state
     * 2) picking the available action with the highest reward
     *
     * @param state
     * @return
     */
    public String act( Board state )
    {
        Random r = new Random();
        if ( r.nextDouble() <= learnConfig.getEpsilon() )
        {
            //agent acts randomly
            int a = r.nextInt( Actions.ACTIONS.length );
            System.out.println( "(picks randomly)" );

            return Actions.ACTIONS[a];
        }

        //1) Predict the reward value based on the given state
        INDArray rewardPrediction = network.output( state.toINDArray() );

        //2) Pick as next move the next available one with highest reward
        //TODO: pick argmax here from available fields
        int row;
        int col;
        int invalidMoves = 0;
        int maxReward = -1;
        do
        {
            maxReward = rewardPrediction.argMax().getInt();

            if ( invalidMoves > 0 )
            {
                System.out.println( "(" + Actions.ACTIONS[maxReward] + " is invalid)" );
                rewardPrediction.put(0, maxReward, -1000); //TODO: confirm this is a row vector
                maxReward = rewardPrediction.argMax().getInt();

            }

            col = maxReward % 3;
            row = ( maxReward - col ) / 3;
            invalidMoves++;
        }
        while ( state.cells[row][col].content != Seed.EMPTY );

        //Pick the action based on the predicted reward
        String nextAction = Actions.ACTIONS[maxReward];
        System.out.println( "(picks from NN | " + ( invalidMoves - 1 ) + " invalid tries)" );
        CommonUtils.printPrediction( rewardPrediction.toDoubleVector() );

        return nextAction;
    }

    /**
     * Return the maximum reward (expressed as a pair with the action and its reward)
     * for a given prediction given by the NN
     *
     * @param prediction
     * @return
     */
    public Pair<Integer, Double> getMaxReward( double[] prediction )
    {
        double maxVal = -1;
        int maxPos = -1;

        for ( int i = 0; i < prediction.length; i++ )
        {
            if ( prediction[i] > maxVal )
            {
                maxVal = prediction[i];
                maxPos = i;
            }
        }
        return new Pair<Integer, Double>( maxPos, maxVal );
    }


    //TODO: this is wrong, we should be obtaining
    public void replay( int batchSize )
    {
        Collections.shuffle( memoryList );
        int size = Math.min( memoryList.size(), batchSize );
        INDArray oldStateQValues;
        INDArray newStateQValues;

        for ( int i = 0; i < size; i++ )
        {
            Memory m = memoryList.get( i );

            // Ask the model for the Q values of the old state (inference)
            oldStateQValues = network.output( m.state.toINDArray() );

            // Ask the model for the Q values of the new state (inference)
            newStateQValues = network.output( m.nextState.toINDArray() );

            // Real Q value for the action we took. This is what we will train towards.
            double targetQ = m.reward + learnConfig.getGamma() * newStateQValues.max().getDouble();
            oldStateQValues.putScalar(Actions.getActionIndex( m.action ), targetQ);

            //Train the Neural Net with the state and targetFuture (with the updated reward value)
            DataSet trainDataSet = new DataSet(m.state.toINDArray(), oldStateQValues);
            network.fit(trainDataSet);
        }

        //update epsilon (exploration rate)
        double epsilon = learnConfig.getEpsilon();
        if ( epsilon > learnConfig.getEpsilonMin() )
        {
            epsilon *= learnConfig.getEpsilonDecay();
            learnConfig.setEpsilon( epsilon );
        }
    }

    /**
     * Play an 'epoch' number of games to train the agent
     */
    public void trainAgent( Agent adversary )
    {
        //to measure improvement via training
        int totalReward = 0;
        int epochs = learnConfig.getEpochs();
        ArrayList<Integer> rewardLog = new ArrayList<Integer>( epochs );

        for ( int i = 0; i < epochs; i++ )
        {
            System.out.println( "\n ====== EPOCH " + i + " ======" );
            //initialize game-board and current status
            GameMain game = new GameMain();
            Random r = new Random();
            game.currentPlayer = r.nextBoolean() ? Seed.CROSS : Seed.NOUGHT; //randomly pick first player
            int moves = 1;

            do
            {
                boolean validAction = false;
                double reward = 0;
                boolean isDone = false;
                Board oldBoard = game.board.clone();
                String a;

                //this agent's turn to play
                if ( game.currentPlayer == Seed.CROSS )
                {
                    //////// don't penalizing invalid actions
                    do
                    {
                        a = this.act( game.board );
                        String[] actions = a.split( " " );
                        System.out.println( ">> X in " + a );
                        validAction = game.playerMove(
                                game.currentPlayer,
                                Integer.valueOf( actions[0] ),
                                Integer.valueOf( actions[1] ) );
                    }
                    while ( !validAction );

                    //update board and currentState
                    reward = game.updateGame( game.currentPlayer );
                    isDone = ( game.currentState != GameState.PLAYING );
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
                else
                { //the other agent's turn
                    do
                    {
                        a = adversary.act( game.board );
                        String[] actions = a.split( " " );
                        System.out.println( ">> O in " + a );
                        validAction = game.playerMove( game.currentPlayer, Integer.valueOf( actions[0] ),
                                                       Integer.valueOf( actions[1] ) );
                    }
                    while ( !validAction );

                    //update board and currentState
                    reward = game.updateGame( game.currentPlayer );
                    isDone = ( game.currentState != GameState.PLAYING );
                }

                //Remember the previous state, action, reward, and done (after both agents have played)
                if ( game.currentPlayer == Seed.CROSS || isDone )
                {
                    if ( game.currentPlayer == Seed.CROSS )
                    {
                        Memory m = new Memory( oldBoard, a, reward, game.board.clone(), isDone );
                        this.remember( m );
                    }
                    else
                    {//means that we just lost/tied the game due to a play by the other player
                        //change the reward of our last move to -1 or 0.5
                        this.memoryList.get( memoryList.size() - 1 ).reward = reward;
                        this.memoryList.get( memoryList.size() - 1 ).setNextState( game.board );
                    }
                }
                else if ( moves > 1 )
                {
                    //update the board of previously stored memory with the adversary's play
                    this.memoryList.get( memoryList.size() - 1 ).setNextState( game.board );
                }

                game.board.paint();

                // Print message and reward if game ended
                if ( isDone )
                {
                    if ( game.currentState == GameState.CROSS_WON )
                    {
                        System.out.print( "'X' WON! =)" );
                    }
                    else if ( game.currentState == GameState.NOUGHT_WON )
                    {
                        System.out.print( "'O' won! =(" );
                    }
                    else if ( game.currentState == GameState.DRAW )
                    {
                        System.out.print( "It's Draw! =/" );
                    }
                    totalReward += reward;
                    rewardLog.add( totalReward );
                    System.out.println( " REWARD: " + reward + " (" + moves + " moves)" );
                }

                // Switch player if previous action was valid
                if ( validAction )
                {
                    moves++;
                    game.currentPlayer = ( game.currentPlayer == Seed.CROSS ) ? Seed.NOUGHT : Seed.CROSS;
                }

            }
            while ( game.currentState == GameState.PLAYING );  // repeat until game-over

            this.replay( learnConfig.getBatchSize() );
        }
        System.out.println( "TOTAL REWARD " + totalReward );
        DrawPlot plot = new DrawPlot( this.toString() + " vs " + adversary.toString() + " for " + epochs + " rounds" );
        plot.drawRewardPlot( rewardLog, "Reward" );
    }

    /**
     * Load a neural network from a file.
     *
     * @return
     */
    public boolean loadNNFromFile()
    {
        try
        {
            File file = new File( NNFILE );
            System.out.println( ">> Loading network from " + NNFILE );
            this.network = ModelSerializer.restoreMultiLayerNetwork(file);

            return true;
        }
        catch ( Exception e )
        {
            System.err.println( "File " + NNFILE + " does not exist. Create a new NN." );
            //e.printStackTrace();
        }
        finally
        {
            return false;
        }
    }

    /**
     * Save a neural network to a file.
     */
    public void saveNNToFile( String outfile )
    {
        try
        {
            System.out.println( ">> Saving network to " + outfile );
            ModelSerializer.writeModel(network, new File(outfile), true);
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }
    }

    @Override
    public String toString()
    {
        if(NNFILE == null)
            return "NandoDQLAgent";

        return NNFILE;

    }

    public static void main( String args[] )
    {

        NandoDQLAgent agent = new NandoDQLAgent( "NandoDQLAgent.model" );

        //agent.trainAgent(agent2);
        //agent.trainAgent(new MrMiyagiAgent(Seed.NOUGHT, 1));
        //agent.epsilon = 1;
        //agent.trainAgent(new RandomAgent());

        System.out.println( "Prediction for an empty board: " );
        Board board = new Board();
        board.init();
        board.paint();
        double[] prediction = agent.network.output( board.toINDArray() ).toDoubleVector();
        CommonUtils.printPrediction( prediction );//*/

        agent.trainAgent( new HumanAgent() );

    }
}
