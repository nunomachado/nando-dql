import os
import random
from collections import deque
from dataclasses import dataclass
from pprint import pprint
from typing import Deque, List, Tuple

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
    states: List[np.ndarray]
    moves: List[int]
    rewards: List[float]
    next_states: List[np.ndarray]
    dones: List[bool]
    max_size: int = 10000

    def __post_init__(self):
        # Initialize deque with maximum size
        self.memory = deque(maxlen=self.max_size)

    def add(self, state: np.ndarray, move: int, reward: float, next_state: np.ndarray, done: bool) -> None:
        """Add a new experience to memory."""
        self.memory.append((state, move, reward, next_state, done))

    def sample(self, batch_size: int) -> Tuple[np.ndarray, np.ndarray, np.ndarray, np.ndarray, np.ndarray]:
        """Sample a batch of experiences from memory."""
        batch = random.sample(self.memory, batch_size)
        states, moves, rewards, next_states, dones = zip(*batch)
        return (np.array(states), np.array(moves), np.array(rewards),
                np.array(next_states), np.array(dones))

    def __len__(self) -> int:
        """Return the current size of internal memory."""
        return len(self.memory)


@dataclass
class QLearningConfig:
    """Configuration parameters for the Q-learning algorithm."""
    epochs: int = 2500
    gamma: float = 0.95  # Discount factor
    epsilon_start: float = 1.0  # Starting value of epsilon
    epsilon_min: float = 0.01  # Minimum value of epsilon
    epsilon_decay: float = 0.995  # Decay rate of epsilon
    learning_rate: float = 0.001
    batch_size: int = 32
    target_update_frequency: int = 100  # How often to update target network
    memory_size: int = 10000  # Size of replay memory


class NandoDQLAgent(nn.Module):

    def __init__(self, state_size: int, move_size: int, config: QLearningConfig):
        """Initialize the Deep Q-learning agent with neural network architecture."""
        super(NandoDQLAgent, self).__init__()
        self.state_size = state_size
        self.move_size = move_size
        self.config = config
        self.memory = Memory([], [], [], [], [], max_size=config.memory_size)
        self.epsilon = config.epsilon_start

        # Define the neural network model
        # The network consists of:
        # - An input layer with size equal to the state size (3x3=9 in this case)
        # - Two hidden layers with 64 and 32 neurons and ReLU activation function
        # - An output layer with size equal to the move size (9 possible moves in this case)
        # The hidden layers are chosen to balance complexity and performance.
        # ReLU activation is used to introduce non-linearity, allowing the network to learn more complex patterns.
        self.model = nn.Sequential(
            nn.Linear(state_size, 64),
            nn.ReLU(),
            nn.Linear(64, 32),
            nn.ReLU(),
            nn.Linear(32, move_size)
        )
        self.target_model = nn.Sequential(
            nn.Linear(state_size, 64),
            nn.ReLU(),
            nn.Linear(64, 32),
            nn.ReLU(),
            nn.Linear(32, move_size)
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
        move: int,
        reward: float,
        next_state: np.ndarray,
        done: bool,
    ) -> None:
        """Store experiences in memory for replay."""
        self.memory.add(state, move, reward, next_state, done)

    def make_move(self, state: Board) -> str:
        """Select a move based on the current state using epsilon-greedy policy."""
        if np.random.rand() <= self.epsilon:
            move_index = random.randrange(self.move_size)
        else:
            state_tensor = torch.FloatTensor(state.to_numpy_arr()).unsqueeze(0)
            with torch.no_grad():
                act_values = self.model(state_tensor)
            move_index = torch.argmax(act_values[0]).item()

        return Moves.MOVES[move_index]

    def replay(self) -> float:
        """Train the model using randomly sampled experiences from memory."""
        if len(self.memory) < self.config.batch_size:
            return 0.0
        
        # Sample batch of experiences
        states, moves, rewards, next_states, dones = self.memory.sample(self.config.batch_size)

        # Convert to PyTorch tensors
        states = torch.FloatTensor(states)
        moves = torch.LongTensor(moves)
        rewards = torch.FloatTensor(rewards)
        next_states = torch.FloatTensor(next_states)
        dones = torch.FloatTensor(dones)

        # Compute current Q-values
        current_q = self.model(states).gather(1, moves.unsqueeze(1)).squeeze(1)

        # Compute max Q-values for next states using target network
        max_next_q = self.target_model(next_states).max(1)[0]

        # Compute expected Q-values
        expected_q = rewards + self.config.gamma * max_next_q * (1 - dones)

        # Compute loss
        loss = self.loss_fn(current_q, expected_q.detach())

        # Backward pass and update model
        self.optimizer.zero_grad()
        loss.backward()
        self.optimizer.step()

        # Decay the epsilon value to reduce exploration over time
        if self.epsilon > self.config.epsilon_min:
            self.epsilon *= self.config.epsilon_decay

        return loss.item()

    def train(self, adversary: 'Agent', output_file: str) -> List[float]:
        """Train the DQLAgent against another agent for a given number of epochs."""
        print(f"Training for {self.config.epochs} epochs")
        total_reward = 0
        reward_log = []
        loss_log = []

        update_target_every = 10  # Update the target network every 10 epochs

        for e in range(self.config.epochs):
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
                        Moves.get_move_index(move),
                        reward,
                        next_state.to_numpy_arr(),
                        done,
                    )

                board_state = next_state

                if done:  # Check if the game is over
                    print(
                        f"Epoch: {e + 1}/{self.config.epochs}, Reward: {reward}, e: {self.epsilon:.2}\n"
                    )
                    total_reward += reward
                    reward_log.append(total_reward)
                    break

                # Switch to the other agent
                current_agent_index = 1 - current_agent_index

            # Train the agent with experience replay
            loss = self.replay()
            loss_log.append(loss) 

            # Update target network periodically
            if e % self.config.target_update_frequency == 0:
                self.update_target_network()

        print(
            f"Training complete (Total reward: {total_reward}). Saving model to {output_file}"
        )
        self.save(output_file)

        return reward_log, loss_log

    def load(self, name: str) -> None:
        """Load the model parameters from a file."""
        self.model.load_state_dict(torch.load(name))
        self.update_target_network()

    def save(self, output_file: str) -> None:
        """Save the model parameters to a file."""
        os.makedirs(os.path.dirname(output_file), exist_ok=True)
        torch.save(self.model.state_dict(), output_file)

    @classmethod
    def create_agent(cls, model_file: str) -> "NandoDQLAgent":
        """Create and load a pre-trained agent."""
        print(f"Loading NandoDQLAgent from {model_file}")
        agent = cls(9, 9, QLearningConfig())
        agent.load(model_file)
        return agent
