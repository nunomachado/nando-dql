package pt.haslab.dql.tictactoe.agents.learning;

public class QLearningConfig {
    /* a number of games we want the agent to play */
    private int epochs;

    /* decay or discount rate, to calculate the future discounted reward */
    private double gamma;

    /* exploration rate -> the rate in which an agent randomly decides its action rather than prediction */
    private double epsilon;

    /* we want to decrease the number of explorations as it gets good at playing games */
    private double epsilon_decay;

    /* we want the agent to explore at least this amount */
    private double epsilon_min;

    /* determines how much the neural net learns in each iteration */
    private double learning_rate;

    /* the number of memories used during replay */
    private int batchsize;

    public QLearningConfig()
    {
        //default configuration
        epochs = 2500;
        gamma = 0.95;
        epsilon = 0.1;
        epsilon_decay = 0.995;
        epsilon_min = 0.01;
        learning_rate = 0.001;
        batchsize = 128;
    }

    public QLearningConfig(int epochs, double gamma, double epsilon, double epsilon_decay, double epsilon_min, double learning_rate, int batchsize) {
        this.epochs = epochs;
        this.gamma = gamma;
        this.epsilon = epsilon;
        this.epsilon_decay = epsilon_decay;
        this.epsilon_min = epsilon_min;
        this.learning_rate = learning_rate;
        this.batchsize = batchsize;
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

    public double getEpsilon_decay() {
        return epsilon_decay;
    }

    public void setEpsilon_decay(double epsilon_decay) {
        this.epsilon_decay = epsilon_decay;
    }

    public double getEpsilon_min() {
        return epsilon_min;
    }

    public void setEpsilon_min(double epsilon_min) {
        this.epsilon_min = epsilon_min;
    }

    public double getLearning_rate() {
        return learning_rate;
    }

    public void setLearning_rate(double learning_rate) {
        this.learning_rate = learning_rate;
    }

    public int getBatchsize() {
        return batchsize;
    }

    public void setBatchsize(int batchsize) {
        this.batchsize = batchsize;
    }
}
