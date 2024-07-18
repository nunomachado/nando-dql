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

    def draw_train_plot(self, reward_log, loss_log):
        fig, ax1 = plt.subplots()

        # Plot reward
        ax1.set_xlabel("Games")
        ax1.set_ylabel("Reward", color="tab:blue")
        ax1.plot(reward_log, color="tab:blue")
        ax1.tick_params(axis="y", labelcolor="tab:blue")

        # Create a second y-axis for loss
        ax2 = ax1.twinx()
        ax2.set_ylabel("Loss", color="tab:red")
        ax2.plot(loss_log, color="tab:red")
        ax2.tick_params(axis="y", labelcolor="tab:red")

        # Set title and display the plot
        plt.title(self.title)
        fig.tight_layout()
        plt.show()
