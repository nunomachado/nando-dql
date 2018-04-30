package pt.haslab.dql.tictactoe.util;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import java.util.ArrayList;

/**
 * Created by nunomachado on 07/08/17.
 */
public class DrawPlot extends ApplicationFrame {
    String title;

    /**
     * Draws the reward plot with the variation of the reward with the epoch
     * @param rewardLog
     * @param seriesTitle
     */
    public void drawRewardPlot(ArrayList<Integer> rewardLog, String seriesTitle){
        final XYSeries series = new XYSeries(seriesTitle);
        for(int i = 0; i < rewardLog.size(); i++){
            series.add(i, rewardLog.get(i));
        }
        final XYSeriesCollection data = new XYSeriesCollection(series);
        final JFreeChart chart = ChartFactory.createXYLineChart(
                title,
                "Epoch",
                "Reward",
                data,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        setContentPane(chartPanel);
        this.pack();
        RefineryUtilities.centerFrameOnScreen(this);
        this.setVisible(true);

    }

    public DrawPlot(String title){
        super(title);
        this.title = title;
    }

    public static void main(final String[] args) {

        final DrawPlot demo = new DrawPlot("My Demo");
        ArrayList<Integer> log = new ArrayList<Integer>();
        log.add(1);
        log.add(2);
        log.add(1);
        log.add(3);
        log.add(4);
        demo.drawRewardPlot(log,"reward plot");
    }
}
