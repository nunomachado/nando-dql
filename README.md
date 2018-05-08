# Nando - A Deep Q-Learning Agent for Tic-Tac-Toe

Nando is a pet project with the goal of building a deep Q-learning agent to learn how to play Tic-Tac-Toe. In a nutshell, Q-learning is a reinforcement learning technique that aims at inferring a reward function _Q(s,a)_ (where _s_ is a state and _a_ a possible next action) by increasing experience. Having an accurate reward function _Q_, one can then build an agent that employs the optimal strategy in an environment (i.e. for a given state, the agent picks as next action the one that maximizes the future reward). Deep Q-learning thus consists of approximating _Q_ by means of a neural network.

Additional info regarding Q-learning can be found in the following references:

* https://ai.intel.com/demystifying-deep-reinforcement-learning/

* https://rubenfiszel.github.io/posts/rl4j/2016-08-24-Reinforcement-Learning-and-DQN.html

* https://leonardoaraujosantos.gitbooks.io/artificial-inteligence/content/deep_q_learning.html


## Quick Start

Nando has two execution modes: `train` and `play`. In the former mode, a DQL agent is trained against another agent by playing a given number of Tic-Tac-Toe games. At the end of the training, the program outputs a plot depicting the variation of the reward obtained with the number of games played. In the latter mode, two agents simply play against each other. 

The types of opponent agents currently supported are:
- `human`, which represents a human player. This agent receives the next move from the console.
- `basic`, which implements an agent that attempts to play obvious moves if possible or random ones otherwise.
- `mrmiyagi`, which implements an agent that follows a hardcoded optimal strategy.

Nando DQL agent is implemented with a neural network comprising a 9-neuron input layer followed by a 27-neuron hidden layer (sigmoid activation), a 9-neuron hidden layer (sigmoid activation), and a 9-neuron output layer (linear activation). The 9 neurons of the input and output layers refer to the nine cells of the tic-tac-toe board. 

Nando is implemented in Java, using the [Encog Machine Learning framework](http://www.heatonresearch.com/encog/).



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

`-p <opponent>` pick the type of agent against which the DQL agent's neural network will be trained. Types of agents include: `human`, `basic`, and `mrmiyagi`. _(required)_

`-r <num-rounds>` duration of the training in number of games played. _(optional, default = 2500)_

Example: `java -jar target/nando-dql-1.0-SNAPSHOT-jar-with-dependencies.jar train -i NandoTest.eg -p basic -r 100`  (loads a Nando DQL agent from file _NandoTest.eg_ and trains it (i.e. updates its neural network) by playing 100 games against a basic agent)

**2. Play mode:**
```bash
java -jar target/nando-dql-1.0-SNAPSHOT-jar-with-dependencies.jar play agentX agentO -r <num-rounds>
```
Example: `java -jar target/nando-dql-1.0-SNAPSHOT-jar-with-dependencies.jar play NandoTest.eg human -r 5`  (allow a human to play 5 tic-tac-toe games against the Nando DQL agent stored in file _NandoTest.eg_)


