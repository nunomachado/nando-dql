package pt.haslab.dql.tictactoe.agents.learning;

public class QLearningConfig {
    /* a number of games we want the agent to play */
    private int epochs;

    /* decay or discount rate, to calculate the future discounted reward */
    private double gamma;

    /* exploration rate -> the rate in which an agent randomly decides its action rather than prediction */
    private double epsilon;

    /* we want to decrease the number of explorations as it gets good at playing games */
    private double epsilonDecay;

    /* we want the agent to explore at least this amount */
    private double epsilonMin;

    /* determines how much the neural net learns in each iteration */
    private double learningRate;

    /* the number of memories used during replay */
    private int batchSize;

    public QLearningConfig()
    {
        //default configuration
        epochs = 2500;
        gamma = 0.95;
        epsilon = 1;
        epsilonDecay = 0.995;
        epsilonMin = 0.01;
        learningRate = 0.1;
        batchSize = 128;
    }

    public QLearningConfig(int epochs, double gamma, double epsilon, double epsilonDecay, double epsilonMin, double learningRate, int batchSize) {
        this.epochs = epochs;
        this.gamma = gamma;
        this.epsilon = epsilon;
        this.epsilonDecay = epsilonDecay;
        this.epsilonMin = epsilonMin;
        this.learningRate = learningRate;
        this.batchSize = batchSize;
    }

    public int getEpochs() {
        return epochs;
    }

    public void setEpochs(int epochs) {
        this.epochs = epochs;
    }

    public double getGamma() {
        return gamma;
    }

    public void setGamma(double gamma) {
        this.gamma = gamma;
    }

    public double getEpsilon() {
        return epsilon;
    }

    public void setEpsilon(double epsilon) {
        this.epsilon = epsilon;
    }

    public double getEpsilonDecay() {
        return epsilonDecay;
    }

    public void setEpsilonDecay(double epsilonDecay) {
        this.epsilonDecay = epsilonDecay;
    }

    public double getEpsilonMin() {
        return epsilonMin;
    }

    public void setEpsilonMin(double epsilonMin) {
        this.epsilonMin = epsilonMin;
    }

    public double getLearningRate() {
        return learningRate;
    }

    public void setLearningRate(double learningRate) {
        this.learningRate = learningRate;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }
}
