package pt.haslab.dql.tictactoe.agents;

import org.apache.commons.math3.util.Pair;
import org.encog.engine.network.activation.ActivationLinear;
import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.engine.network.activation.ActivationTANH;
import org.encog.ml.data.MLData;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;
import org.encog.persist.EncogDirectoryPersistence;
import pt.haslab.dql.tictactoe.agents.learning.Memory;
import pt.haslab.dql.tictactoe.agents.learning.QLearningConfig;
import pt.haslab.dql.tictactoe.game.Actions;
import pt.haslab.dql.tictactoe.game.Board;
import pt.haslab.dql.tictactoe.game.GameMain;
import pt.haslab.dql.tictactoe.game.GameState;
import pt.haslab.dql.tictactoe.game.Seed;
import pt.haslab.dql.tictactoe.util.CommonUtils;
import pt.haslab.dql.tictactoe.util.DrawPlot;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

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
    private BasicNetwork network;

    /* backpropagation for trainign the NN*/
    private ResilientPropagation train;

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
            network = new BasicNetwork();
            network.addLayer( new BasicLayer( null, true, 9 ) );
            network.addLayer( new BasicLayer( new ActivationTANH(), true, 27 ) );
            network.addLayer( new BasicLayer( new ActivationTANH(), true, 27 ) );
            network.addLayer( new BasicLayer( new ActivationTANH(), true, 27 ) );
            network.addLayer( new BasicLayer( new ActivationSigmoid(), false, 9 ) );
            network.getStructure().finalizeStructure();
            network.reset();
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
        MLData prediction = network.compute( state.toMLData() );
        double[] rewardPrediction = prediction.getData().clone();
        Arrays.sort( rewardPrediction ); //sort the rewards in ascending order

        //2) Pick as next move the next available one with highest reward
        int row = -1;
        int col = -1;
        int invalidMoves = 0;
        Pair<Integer, Double> maxReward = null;
        do
        {
            if ( invalidMoves > 0 )
            {
                System.out.println( "(" + Actions.ACTIONS[maxReward.getKey()] + " is invalid)" );
            }

            maxReward = getMaxNthReward( rewardPrediction, invalidMoves );
            col = maxReward.getKey() % 3;
            row = ( maxReward.getKey() - col ) / 3;
            invalidMoves++;

            //something wrong happened...
            if ( col == -1 || row == -1 )
                System.out.println( "WRONG" );

        }
        while ( state.cells[row][col].content != Seed.EMPTY );

        //Pick the action based on the predicted reward
        String nextAction = Actions.ACTIONS[maxReward.getKey()];
        System.out.println( "(picks from NN | " + ( invalidMoves - 1 ) + " invalid tries)" );
        CommonUtils.printPrediction( prediction.getData() );

        return nextAction;
    }

    /**
     * Return the maximum reward (expressed as a pair with the action and its reward)
     * for a given prediction given by the NN
     *
     * @param prediction
     * @return
     */
    public Pair<Integer, Double> getMaxReward( MLData prediction )
    {
        double maxVal = -1;
        int maxPos = -1;
        double[] data = prediction.getData().clone();

        for ( int i = 0; i < prediction.size(); i++ )
        {
            if ( data[i] > maxVal )
            {
                maxVal = data[i];
                maxPos = i;
            }
        }
        return new Pair<Integer, Double>( maxPos, maxVal );
    }

    /**
     * Returns the n-th action with highest reward, where n indicates the
     * number of previous invalid attempts.
     *
     * @param data
     * @param invalidMoves
     * @return
     */
    public Pair<Integer, Double> getMaxNthReward( double[] data, int invalidMoves )
    {

        double maxVal = -1;
        int maxPos = -1;
        //we want the N-th max value from the sorted array
        //where N is given by the invalid moves so far
        maxPos = ( data.length - 1 ) - invalidMoves;
        maxVal = data[maxPos];

        return new Pair<Integer, Double>( maxPos, maxVal );
    }

    public void replay( int batchSize )
    {
        Collections.shuffle( memoryList );
        int size = Math.min( memoryList.size(), batchSize );
        double target = 0;

        for ( int i = 0; i < size; i++ )
        {
            Memory m = memoryList.get( i );

            //if done, make our target reward
            target = m.reward;

            if ( !m.isDone() )
            {
                //predict the future discounted reward
                MLData prediction = network.compute( m.nextState.toMLData() );
                Pair<Integer, Double> maxReward = getMaxReward( prediction );
                target = m.reward + learnConfig.getGamma() * maxReward.getValue();
            }

            //make the agent to approximately map
            //the current state to future discounted reward
            MLData targetFuture = network.compute( m.state.toMLData() );
            targetFuture.setData( Actions.getActionIndex( m.action ), target );
            double[][] targetOutput =
                            { targetFuture.getData() }; //transform targetFuture into double[][] as required by the BasicMLDataSet

            //Train the Neural Net with the state and targetFuture (with the updated reward value)
            MLDataSet trainingSet = new BasicMLDataSet( m.state.toTrainingData(), targetOutput );
            train = new ResilientPropagation( network, trainingSet );
            train.iteration();

        }

        //update epsilon (exploration rate)
        double epsilon = learnConfig.getEpsilon();
        if ( epsilon > learnConfig.getEpsilon_min() )
        {
            epsilon *= learnConfig.getEpsilon_decay();
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
                        validAction = game.playerMove( game.currentPlayer, Integer.valueOf( actions[0] ),
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

            this.replay( learnConfig.getBatchsize() );
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
            BasicNetwork nn = (BasicNetwork) EncogDirectoryPersistence.loadObject( file );
            this.network = nn;
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
            EncogDirectoryPersistence.saveObject( new File( outfile ), this.network );
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

        NandoDQLAgent agent = new NandoDQLAgent( "NandoDQLAgent.eg" );

        //agent.trainAgent(agent2);
        //agent.trainAgent(new MrMiyagiAgent(Seed.NOUGHT, 1));
        //agent.epsilon = 1;
        //agent.trainAgent(new RandomAgent());

        System.out.println( "Prediction for an empty board: " );
        Board b = new Board();
        b.init();
        b.paint();
        MLData prediction = agent.network.compute( b.toMLData() );
        CommonUtils.printPrediction( prediction.getData() );//*/

        agent.trainAgent( new HumanAgent() );

    }
}
