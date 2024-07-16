import matplotlib.pyplot as plt


class DrawPlot:

    def __init__(self, title):
        self.title = title

    def draw_reward_plot(self, reward_log):
        plt.plot(reward_log)
        plt.title(self.title)
        plt.xlabel("Games")
        plt.xlim(left=0)
        plt.legend(["Reward X"])
        plt.show()
