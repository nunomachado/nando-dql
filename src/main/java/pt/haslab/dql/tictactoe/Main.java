package pt.haslab.dql.tictactoe;

import pt.haslab.dql.tictactoe.agents.*;
import pt.haslab.dql.tictactoe.agents.learning.QLearningConfig;
import pt.haslab.dql.tictactoe.game.GameMain;
import pt.haslab.dql.tictactoe.game.Seed;
import pt.haslab.dql.tictactoe.util.DrawPlot;

import java.io.File;
import java.util.ArrayList;

public class Main {

    public static Agent agentX;       //first player (DQL agent in training mode)
    public static Agent agentO;       //second player
    public static int ROUNDS = 2500;  //default number of games to play

    //variables for training mode
    public static String inputFile = null;  //load trained agent from file
    public static String outputFile = null; //save trained agent to file



    /** The entry main() method */
    public static void main(String[] args) {
        String mode = args[0];

        try {
            //train mode
            if (mode.equals("train")) {
                parseParams(args);
                printInput();

                QLearningConfig trainConfig = new QLearningConfig(); //create default configuration
                trainConfig.setEpochs(ROUNDS);
                agentX = new NandoDQLAgent(trainConfig, inputFile);

                //check that the type of agent is valid
                if(agentO == null){
                    throw new Exception();
                }

                //start training
                ((NandoDQLAgent) agentX).trainAgent(agentO);

                //store agent to file if required
                if (outputFile != null) {
                    ((NandoDQLAgent) agentX).saveNNToFile(outputFile);
                }
            }
            //play mode
            else if (mode.equals("play")) {
                agentX = parseAgent(args[1]);
                agentO = parseAgent(args[2]);

                //check that the type of agent is valid
                if (agentX == null || agentO == null) {
                    throw new Exception();
                }

                //parse number of rounds
                parseParams(args);

                //start game
                int totalReward = 0;
                ArrayList<Integer> rewardLog = new ArrayList<Integer>( ROUNDS );
                for(int i = 0; i < ROUNDS; i++) {
                    GameMain game = new GameMain();
                    totalReward += game.playGame(agentX, agentO);
                    rewardLog.add( totalReward );
                }
                DrawPlot plot = new DrawPlot( agentX.toString() + "(X) vs " + agentO.toString() + "(O) for " + ROUNDS + " rounds\n(X win = 1; X loss = -1; draw = 0)" );
                plot.drawRewardPlot( rewardLog, "Reward for " + agentX.toString() );


            }
        }
        catch(Exception e){
            System.err.print("Wrong input: ");
            for(String s : args){
                System.err.print(s+" ");
            }
            System.err.println("\n\n-- DQL AGENT TRAINING MODE --");
            System.err.println("Usage: train [options]");
            System.err.println("Options description:");
            System.err.println("-i <path-input-file>\tLoad a neural network previously trained. (optional)");
            System.err.println("-o <path-output-file>\tSave the neural network to be trained to a file. (optional)");
            System.err.println("-p <opponent>\t\t\tPick the type of agent against which the DQL agent's neural network will " +
                    "be trained. Types of agents include: 'human', 'basic', 'random', and 'mrmiyagi'. (required)");
            System.err.println("-r <num-rounds>\t\t\tDuration of the training in number of games played. (optional)");

            System.err.println("\n-- PLAY MODE --");
            System.err.println("Usage: play agentX agentO -r <num-rounds>");
            System.err.println("Types of agents include: 'human', 'basic', 'random', and 'mrmiyagi' and 'path-to-nando-agent-file'.");
        }


    }

    public static void parseParams(String args[]){
        for(int i = 1; i < args.length; i++){
            String param = args[i];
            String nextParam = args[++i];
            if(param.equals("-i")){ //load a previous DQL agent
                inputFile = nextParam;
            }
            else if(param.equals("-o")){ //set file to store agent after training
                outputFile = nextParam;
            }
            else if(param.equals("-p")){ //read opponent player
                agentO = parseAgent(nextParam);
            }
            else if(param.equals("-r")){ //number of rounds/games
                ROUNDS = Integer.valueOf(nextParam);
            }
        }
    }

    public static Agent parseAgent(String parameter){
        if(parameter.equals(AgentType.BASIC.toString())){
            return new BasicAgent();
        }
        if(parameter.equals(AgentType.HUMAN.toString())){
            return new HumanAgent();
        }
        if(parameter.equals(AgentType.RANDOM.toString())){
            return new RandomAgent();
        }
        if(parameter.equals(AgentType.MYIAGI.toString())){
            return new MrMiyagiAgent(Seed.NOUGHT, 1);
        }
        else{
            File file = new File(parameter);
            if(file.exists()){
                return new NandoDQLAgent(parameter);
            }
        }
        return null;
    }

    public static void printInput(){
        System.out.println("ROUNDS: "+ROUNDS);
        if(inputFile!=null)
            System.out.println("INPUT FILE: "+inputFile);
        if(outputFile!=null)
            System.out.println("OUTPUT FILE: "+outputFile);

    }
}
