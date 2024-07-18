import os
import sys
import traceback

sys.path.append(".")

from tictactoe.agents.basic_agent import BasicAgent
from tictactoe.agents.human_agent import HumanAgent
from tictactoe.agents.mr_miyagi_agent import MrMiyagiAgent
from tictactoe.agents.nando_dql_agent import NandoDQLAgent, QLearningConfig
from tictactoe.agents.random_agent import RandomAgent
from tictactoe.game.board import Seed
from tictactoe.game.game_main import GameMain
from tictactoe.util.draw_plot import DrawPlot

output_file = None
opponent = None
num_games = 2500


def main(args):
    if len(args) < 1:
        print_usage()
        return

    mode = args[0]

    try:
        if mode == "train":
            train_mode(args[1:])
        elif mode == "play":
            play_mode(args[1:])
        else:
            raise ValueError("Invalid mode. Use 'train' or 'play'.")
    except Exception as e:
        print(f"Error: {str(e)}")
        traceback.print_exc()
        print_usage()


def train_mode(args):
    global output_file, opponent, num_games
    parse_train_params(args)

    if not opponent:
        raise ValueError("Opponent (-p) is required for training mode.")

    if not output_file:
        output_file = f"models/nando_dql_agent_o={opponent}_g={num_games}.pth"

    print(f"Training for {num_games} games against {opponent}")
    print(f"Output file: {output_file}")

    config = QLearningConfig(epochs=num_games)
    agent_x = NandoDQLAgent(9, 9, config)
    agent_o = parse_agent(opponent, Seed.NOUGHT)

    reward_log, loss_log = agent_x.train(agent_o, output_file)

    plot = DrawPlot(
        f"Nando DQL Agent Training against {opponent} for {num_games} games"
    )
    plot.draw_train_plot(reward_log, loss_log)


def play_mode(args):
    if len(args) < 2:
        raise ValueError(
            "Play mode requires at least two arguments: <player-X> <player-O>"
        )

    agent_x = parse_agent(args[0], Seed.CROSS)
    agent_o = parse_agent(args[1], Seed.NOUGHT)

    if len(args) > 2 and args[2] == "-g":
        if len(args) < 4:
            raise ValueError("Number of games must be specified after -g")
        num_games = int(args[3])

    print(
        f"Playing {num_games} games: {agent_x.__class__.__name__} (X) vs {agent_o.__class__.__name__} (O)"
    )

    total_reward = 0
    reward_log = []
    for _ in range(num_games):
        game = GameMain(agent_x, agent_o)
        total_reward += game.play_game()
        reward_log.append(total_reward)

    plot = DrawPlot(
        f"{agent_x.__class__.__name__} (X) vs {agent_o.__class__.__name__} (O) for {num_games} games\n(X win = 1; X loss = -1; draw = 0)"
    )
    plot.draw_reward_plot(reward_log)


def parse_train_params(args):
    global output_file, opponent, num_games
    i = 0
    while i < len(args):
        if args[i] == "-o":
            i += 1
            if i < len(args):
                output_file = args[i]
        elif args[i] == "-p":
            i += 1
            if i < len(args):
                opponent = args[i]
        elif args[i] == "-g":
            i += 1
            if i < len(args):
                num_games = int(args[i])
        i += 1


def parse_agent(parameter, seed: Seed):
    if parameter == "basic":
        return BasicAgent()
    elif parameter == "human":
        return HumanAgent()
    elif parameter == "random":
        return RandomAgent()
    elif parameter == "mrmiyagi":
        return MrMiyagiAgent(seed, 1)
    elif os.path.exists(parameter):
        return NandoDQLAgent.create_agent(parameter)
    else:
        raise ValueError(f"Invalid agent type or file not found: {parameter}")


def print_usage():
    print("\n-- DQL AGENT TRAINING MODE --")
    print("Usage: python tictactoe/main.py train [options]")
    print("Options:")
    print(
        "-o <path-output-file>\tSave the neural network to be trained to a file. (optional)"
    )
    print(
        "-p <opponent>\t\tPick the type of agent against which the DQL agent's neural network will be trained. (required)"
    )
    print("\t\t\tTypes: human, basic, random, mrmiyagi")
    print(
        "-g <num-games>\t\tDuration of the training in number of games played. (optional, default = 2500)"
    )

    print("\n-- PLAY MODE --")
    print("Usage: python tictactoe/main.py play <player-X> <player-O> [-g <num-games>]")
    print(
        "Types of agents include: 'human', 'basic', 'random', 'mrmiyagi', and '<path-to-nando-model.pth>'"
    )


if __name__ == "__main__":
    main(sys.argv[1:])
