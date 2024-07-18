# Nando - A Deep Q-Learning Agent for Tic-Tac-Toe

Nando is a pet project with the goal of building a deep Q-learning agent to learn how to play Tic-Tac-Toe. In a nutshell, Q-learning is a reinforcement learning technique that aims at inferring a reward function _Q(s,a)_ (where _s_ is a state and _a_ a possible next action) by increasing experience. Having an accurate reward function _Q_, one can then build an agent that employs the optimal strategy in an environment (i.e. for a given state, the agent picks as next action the one that maximizes the future reward). Deep Q-learning thus consists of approximating _Q_ by means of a neural network.

Additional info regarding Q-learning can be found in the following references:

* https://ai.intel.com/demystifying-deep-reinforcement-learning/

* https://rubenfiszel.github.io/posts/rl4j/2016-08-24-Reinforcement-Learning-and-DQN.html

* https://leonardoaraujosantos.gitbooks.io/artificial-inteligence/content/deep_q_learning.html


## Setup

1. Create a virtual environment:
```bash
python -m venv venv
```

2. Activate the virtual environment:
```bash
source venv/bin/activate
```

3. Install the required dependencies:
```bash
pip install -r requirements.txt
```

## Usage

Nando has two execution modes: `train` and `play`. In the former mode, a DQL agent is trained against another agent by playing a given number of Tic-Tac-Toe games. At the end of the training, the program outputs a plot depicting the variation of the reward obtained with the number of games played. In the latter mode, two agents simply play against each other. 

The types of opponent agents currently supported are:
- `human`, which represents a human player. This agent receives the next move from the console.
- `basic`, which implements an agent that attempts to play obvious moves if possible or random ones otherwise.
- `random` which implements an agent that makes random moves.
- `mrmiyagi`, which implements an agent that follows a hardcoded optimal strategy.
- `<path-to-nando-model.pth>`, which loads a NandoDQL trained model from a file (`play` mode only).

Nando DQL agent is implemented with a neural network comprising a 9-neuron input layer, followed by two hidden layers (the first with 64 neurons and the second with 32 neurons, both using ReLU activation), and a 9-neuron output layer with linear activation. The 9 neurons of the input layer represent the state of the nine cells of the tic-tac-toe board, while the 9 neurons of the output layer correspond to the nine possible actions a player can take. 

Nando is implemented in Python, using PyTorch.

### Train Mode

```bash
python tictactoe/main.py train [options]
```

Options include:

-o <path-output-file> save the neural network to be trained to a file. (optional)

-p <opponent> pick the type of agent against which the DQL agent's neural network will be trained. Types of agents include: human, basic, random, and mrmiyagi. (required)

-g <num-games> duration of the training in number of games played. (optional, default = 2500)

Example: `python tictactoe/main.py train -p basic -g 100` (trains a Nando DQL agent by playing 100 games against a basic agent)

### Play Mode

```bash
python tictactoe/main.py play <player-X> <player-O> -g <num-games>
```
Example: `python tictactoe/main.py play models/nando_dql_agent.pth human -r 5 (allow a human to play 5 tic-tac-toe games against the Nando DQL agent stored in file 'models/nando_dql_agent.pth')`

## Development

The project uses [black](https://github.com/psf/black), [isort](https://github.com/timothycrosley/isort) and [flake8](https://flake8.pycqa.org/en/latest/) for linting and formatting. You can run these tools with the following commands:

```bash
# Check for formatting issues
black .

# Check for import order issues
isort .

# Check for linting issues
flake8 .
```