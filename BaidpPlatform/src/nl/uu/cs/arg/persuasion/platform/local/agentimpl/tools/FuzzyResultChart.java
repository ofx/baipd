package nl.uu.cs.arg.persuasion.platform.local.agentimpl.tools;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class FuzzyResultChart
{
    private Map<String, Double> personalityVector;

    private Map<String, Double> fuzzyResult;

    public FuzzyResultChart(int i, Map<String, Double> PV, Map<String, Double> fuzzyResult)
    {
        this.personalityVector = PV;
        this.fuzzyResult = fuzzyResult;

        JFreeChart barChart1 = ChartFactory.createBarChart(
                "Personality Configuration",
                "Personality Vector",
                "Configuration",
                createDataset(this.personalityVector),
                PlotOrientation.VERTICAL,
                false, false, false);

        JFreeChart barChart2 = ChartFactory.createBarChart(
                "Fuzzy Result Action Revision",
                "Fuzzy Outcome",
                "Preference",
                createDataset(this.fuzzyResult),
                PlotOrientation.VERTICAL,
                false, false, false);

        int width = 800;
        int height = 480;
        try {
            ChartUtilities.saveChartAsJPEG(new File("char_" + i + "_pv.jpeg"), barChart1, width, height);
            ChartUtilities.saveChartAsJPEG(new File("char_" + i + "_result.jpeg"), barChart2, width, height);
        } catch (IOException ex) {
            System.out.println("Achtung, ein fehler!");
        }
    }

    private CategoryDataset createDataset(Map<String, Double> set)
    {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (Map.Entry<String, Double> entry : set.entrySet()) {
            dataset.addValue(entry.getValue(), "Value",entry.getKey());
        }

        return dataset;
    }
}