import os
import random
from collections import deque
from dataclasses import dataclass
from pprint import pprint
from typing import Deque, List

import numpy as np
import torch
import torch.nn as nn
import torch.optim as optim

from tictactoe.agents.agent import Agent
from tictactoe.game.board import Board
from tictactoe.game.game_main import GameMain, GameState, Seed
from tictactoe.game.moves import Moves


@dataclass
class Memory:
    """Initialize memory to store experience replay data."""

    state: np.ndarray
    move: str
    reward: float
    next_state: np.ndarray
    done: bool


@dataclass
class QLearningConfig:
    """Configuration parameters for the Q-learning algorithm."""

    epochs: int = 2500
    gamma: float = 0.95
    epsilon: float = 1.0
    epsilon_decay: float = 0.99
    epsilon_min: float = 0.01
    learning_rate: float = 0.001
    batch_size: int = 128


class NandoDQLAgent(nn.Module):

    def __init__(self, state_size: int, move_size: int, config: QLearningConfig):
        """Initialize the Deep Q-learning agent with neural network architecture."""
        super(NandoDQLAgent, self).__init__()
        self.state_size = state_size
        self.move_size = move_size
        self.q_learn_config = config
        self.memory: Deque[Memory] = deque(maxlen=2000)

        # Define the neural network model
        # The network consists of:
        # - An input layer with size equal to the state size (3x3=9 in this case)
        # - A hidden layer with 18 neurons and ReLU activation function
        # - An output layer with size equal to the move size (9 possible moves in this case)
        # The hidden layer with 18 neurons is chosen to balance complexity and performance.
        # ReLU activation is used to introduce non-linearity, allowing the network to learn more complex patterns.
        self.model = nn.Sequential(
            nn.Linear(state_size, 18), nn.ReLU(), nn.Linear(18, move_size)
        )
        self.target_model = nn.Sequential(
            nn.Linear(state_size, 18), nn.ReLU(), nn.Linear(18, move_size)
        )
        self.optimizer = optim.Adam(self.model.parameters(), lr=config.learning_rate)
        self.loss_fn = nn.MSELoss()

        self.update_target_network()
        print(
            f"Model initialized with state size: {state_size}, move size: {move_size}"
        )
        print("Q Learning Config:")
        pprint(config.__dict__)

    def update_target_network(self):
        """Copy weights from the model to the target model."""
        self.target_model.load_state_dict(self.model.state_dict())

    def remember(
        self,
        state: np.ndarray,
        move: str,
        reward: float,
        next_state: np.ndarray,
        done: bool,
    ) -> None:
        """Store experiences in memory for replay."""
        self.memory.append(Memory(state, move, reward, next_state, done))

    def make_move(self, state: Board) -> str:
        """Select a move based on the current state using epsilon-greedy policy."""
        if np.random.rand() <= self.q_learn_config.epsilon:
            move_index = random.randrange(self.move_size)
        else:
            state_tensor = torch.FloatTensor(state.to_numpy_arr()).unsqueeze(0)
            with torch.no_grad():
                act_values = self.model(state_tensor)
            move_index = torch.argmax(act_values[0]).item()

        return Moves.MOVES[move_index]

    def replay(self) -> None:
        """Train the model using randomly sampled experiences from memory."""
        # Sample a mini-batch of experiences from memory
        minibatch = random.sample(
            self.memory, min(len(self.memory), self.q_learn_config.batch_size)
        )

        for memory in minibatch:
            target = memory.reward

            if not memory.done:
                # Compute the discounted future reward if the episode is not done
                next_state_tensor = torch.FloatTensor(memory.next_state).unsqueeze(0)
                with torch.no_grad():
                    target += self.q_learn_config.gamma * torch.max(
                        self.target_model(next_state_tensor)[0]
                    )

            # Get the current Q-values for the state
            target_f = self.model(torch.FloatTensor(memory.state).unsqueeze(0))
            # Update the Q-value for the selected move with the target value
            target_f[0][Moves.get_move_index(memory.move)] = target

            # Zero the gradients, compute the loss, perform backpropagation, and update the model
            self.optimizer.zero_grad()
            loss = self.loss_fn(
                self.model(torch.FloatTensor(memory.state).unsqueeze(0)), target_f
            )
            loss.backward()
            self.optimizer.step()

        # Decay the epsilon value to reduce exploration over time
        if self.q_learn_config.epsilon > self.q_learn_config.epsilon_min:
            self.q_learn_config.epsilon *= self.q_learn_config.epsilon_decay

    def load(self, name: str) -> None:
        """Load the model parameters from a file."""
        self.model.load_state_dict(torch.load(name))
        self.update_target_network()

    def save(self, output_file: str) -> None:
        """Save the model parameters to a file."""
        os.makedirs(os.path.dirname(output_file), exist_ok=True)
        torch.save(self.model.state_dict(), output_file)

    def train(self, adversary: Agent, output_file: str) -> List[float]:
        """Train the DQLAgent against another agent for a given number of epochs."""
        print(f"Training for {self.q_learn_config.epochs} epochs")
        total_reward = 0
        reward_log = []

        update_target_every = 10  # Update the target network every 10 epochs

        for e in range(self.q_learn_config.epochs):
            print(f"\n===== EPOCH {e} =====")
            agents = [self, adversary]
            random.shuffle(agents)
            # set target_seed to the seed of NandoDQLAgent
            target_seed = Seed.CROSS if self == agents[0] else Seed.NOUGHT
            print(
                f"'X': {agents[0].__class__.__name__}, 'O': {agents[1].__class__.__name__}"
            )

            game = GameMain(*agents)  # Create a new game with the shuffled agents
            board_state = game.board  # Get the initial state
            current_agent_index = 0  # Start with the first agent in the shuffled list

            while game.current_state == GameState.PLAYING:
                current_agent = agents[current_agent_index]
                print(
                    f"\nPlayer '{game.current_player.value}' ({current_agent.__class__.__name__}), enter your move (row[1-3] column[1-3]): "
                )

                # get valid move from agent
                valid_move = False
                while not valid_move:
                    move = current_agent.make_move(board_state)
                    print(f">> {move}")
                    try:
                        r, c = Moves.move_to_tuple(move)
                        next_state, reward, done = game.apply_move(r, c, target_seed)
                        valid_move = True
                    except ValueError:
                        print(f"Move {move} is not valid. Try again...")

                # update board and current state
                game.board.paint()

                if current_agent == self:
                    self.remember(
                        board_state.to_numpy_arr(),
                        move,
                        reward,
                        next_state.to_numpy_arr(),
                        done,
                    )

                board_state = next_state

                if done:  # Check if the game is over
                    print(
                        f"Epoch: {e + 1}/{self.q_learn_config.epochs}, Reward: {reward}, e: {self.q_learn_config.epsilon:.2}\n"
                    )
                    total_reward += reward
                    reward_log.append(total_reward)
                    break

                # Switch to the other agent
                current_agent_index = 1 - current_agent_index

            self.replay()  # Train the agent with experience replay
            if e % update_target_every == 0:
                self.update_target_network()

        print(
            f"Training complete (Total reward: {total_reward}). Saving model to {output_file}"
        )
        self.save(output_file)

        return reward_log

    @classmethod
    def create_agent(cls, model_file: str) -> "NandoDQLAgent":
        print(f"Loading NandoDQLAgent from {model_file}")
        agent = cls(9, 9, QLearningConfig())
        agent.load(model_file)
        return agent
