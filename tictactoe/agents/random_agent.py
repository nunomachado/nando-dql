import random

class RandomAgent:
    def __init__(self):
        pass

    def make_move(self, game_state):
        # Implement random agent logic
        return random.choice(game_state.get_available_moves())