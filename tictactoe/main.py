import sys
from agents.basic_agent import BasicAgent
from agents.human_agent import HumanAgent
from agents.random_agent import RandomAgent
from agents.nando_dql_agent import NandoDQLAgent
from game.game_main import GameMain
from util.draw_plot import DrawPlot

ROUNDS = 2500
input_file = None
output_file = None

def main(args):
    mode = args[0]

    try:
        if mode == "train":
            parse_params(args)
            print_input()

            train_config = {"epochs": ROUNDS}
            agent_x = NandoDQLAgent(train_config, input_file)

            if agent_o is None:
                raise Exception()

            agent_x.train_agent(agent_o)

            if output_file:
                agent_x.save_nn_to_file(output_file)

        elif mode == "play":
            agent_x = parse_agent(args[1])
            agent_o = parse_agent(args[2])

            if agent_x is None or agent_o is None:
                raise Exception()

            parse_params(args)

            total_reward = 0
            reward_log = []
            for _ in range(ROUNDS):
                game = GameMain()
                total_reward += game.play_game(agent_x, agent_o)
                reward_log.append(total_reward)

            plot = DrawPlot(f"{agent_x} (X) vs {agent_o} (O) for {ROUNDS} rounds\n(X win = 1; X loss = -1; draw = 0)")
            plot.draw_reward_plot(reward_log, f"Reward for {agent_x}")

    except Exception as e:
        print(f"Wrong input: {' '.join(args)}")
        print_usage()

def parse_params(args):
    global input_file, output_file, ROUNDS, agent_o
    for i in range(1, len(args), 2):
        param = args[i]
        next_param = args[i + 1]
        if param == "-i":
            input_file = next_param
        elif param == "-o":
            output_file = next_param
        elif param == "-p":
            agent_o = parse_agent(next_param)
        elif param == "-r":
            ROUNDS = int(next_param)

def parse_agent(parameter):
    if parameter == "basic":
        return BasicAgent()
    elif parameter == "human":
        return HumanAgent()
    elif parameter == "random":
        return RandomAgent()
    elif parameter == "mrmiyagi":
        return MrMiyagiAgent()
    else:
        return NandoDQLAgent(parameter) if os.path.exists(parameter) else None

def print_input():
    print(f"ROUNDS: {ROUNDS}")
    if input_file:
        print(f"INPUT FILE: {input_file}")
    if output_file:
        print(f"OUTPUT FILE: {output_file}")

def print_usage():
    print("\n-- DQL AGENT TRAINING MODE --")
    print("Usage: train [options]")
    print("Options description:")
    print("-i <path-input-file>\tLoad a neural network previously trained. (optional)")
    print("-o <path-output-file>\tSave the neural network to be trained to a file. (optional)")
    print("-p <opponent>\t\t\tPick the type of agent against which the DQL agent's neural network will be trained. (required)")
    print("-r <num-rounds>\t\t\tDuration of the training in number of games played. (optional)")

    print("\n-- PLAY MODE --")
    print("Usage: play agentX agentO -r <num-rounds>")
    print("Types of agents include: 'human', 'basic', 'random', and 'mrmiyagi' and 'path-to-nando-agent-file'.")

if __name__ == "__main__":
    main(sys.argv[1:])
