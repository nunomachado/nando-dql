# Nando - A Deep Q-Learning Agent for Tic-Tac-Toe

Nando is a pet project with the goal of building a deep Q-learning agent to learn how to play Tic-Tac-Toe. In a nutshell, Q-learning is a reinforcement learning technique that aims at inferring a reward function _Q(s,a)_ (where _s_ is a state and _a_ a possible next action) by increasing experience. Having an accurate reward function _Q_, one can then build an agent that employs the optimal strategy in an environment (i.e. for a given state, the agent picks as next action the one that maximizes the future reward). Deep Q-learning thus consists of approximating _Q_ by means of a neural network.

Additional info regarding (deep) Q-learning can be found in the following references:

* https://rubenfiszel.github.io/posts/rl4j/2016-08-24-Reinforcement-Learning-and-DQN.html

* https://leonardoaraujosantos.gitbooks.io/artificial-inteligence/content/deep_q_learning.html

* https://blog.valohai.com/reinforcement-learning-tutorial-basic-deep-q-learning

## Quick Start

Nando has two execution modes: `train` and `play`. In the former mode, a DQL agent is trained against another agent by playing a given number of Tic-Tac-Toe games. At the end of the training, the program outputs a plot depicting the variation of the reward obtained with the number of games played. In the latter mode, two agents simply play against each other. 

The types of opponent agents currently supported are:
- `human`, which represents a human player. This agent receives the next move from the console.
- `basic`, which implements an agent that attempts to play obvious moves if possible or random ones otherwise.
- `random` which implements an agent that makes random moves.
- `mrmiyagi`, which implements an agent that follows a hardcoded optimal strategy.

Nando DQL agent is implemented with a neural network comprising a 9-neuron input layer followed by two 27-neuron hidden layers (sigmoid activation), and a 9-neuron output layer (sigmoid activation). The 9 neurons of the input layer represent the current board state, where values 1, 0, and -1 respectively indicate a cross, empty, and nought. In turn, the 9 neurons of the output layer give the Q-value of each action   

Nando is implemented in Java using [DL4J](https://deeplearning4j.org).



### Compile

```bash
mvn package
```

### Usage

**1. Training mode:**
```bash
java -jar target/nando-dql-1.0-SNAPSHOT-jar-with-dependencies.jar train [options]
```
Options include:

`-i <path-input-file>` load a neural network previously trained. _(optional)_

`-o <path-output-file>` save the neural network to be trained to a file. _(optional)_

`-p <opponent>` pick the type of agent against which the DQL agent's neural network will be trained. Types of agents include: `human`, `basic`, `random`, and `mrmiyagi`. _(required)_

`-r <num-rounds>` duration of the training in number of games played. _(optional, default = 2500)_

Example: 
    
    java -jar target/nando-dql-1.0-SNAPSHOT-jar-with-dependencies.jar train -p basic -r 100 -o NandoTest.model  

The command above trains a Nando agent by playing 100 games against a basic agent and outputs the resulting neural network to file NandoTest.model.

**2. Play mode:**

To play a game between two agents use the command: 

    java -jar target/nando-dql-1.0-SNAPSHOT-jar-with-dependencies.jar play agentX agentO -r <num-rounds>

For instance, for you to play 5 games against the Nando DQL agent stored in file _NandoTest.model_, run the following:

    java -jar target/nando-dql-1.0-SNAPSHOT-jar-with-dependencies.jar play NandoTest.model human -r 5

Note that, in the example above, you will play as nought (O) and the Nando agent as cross (X). 