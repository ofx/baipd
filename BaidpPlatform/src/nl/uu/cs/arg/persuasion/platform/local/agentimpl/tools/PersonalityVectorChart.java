package nl.uu.cs.arg.persuasion.platform.local.agentimpl.tools;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.statistics.DefaultMultiValueCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PersonalityVectorChart
{
    public PersonalityVectorChart(String path, List<Map<String, Double>> pvs, String[] names)
    {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        int i = 0;
        for (Map<String, Double> pv : pvs) {
            for (Map.Entry<String, Double> entry : pv.entrySet()) {
                dataset.addValue(entry.getValue(), names[i], entry.getKey());
            }
            ++i;
        }

        JFreeChart barChart = ChartFactory.createBarChart(
                "Personality Configuration",
                "Personality Vector",
                "Strength",
                dataset,
                PlotOrientation.VERTICAL,
                true, false, false);

        CategoryPlot plot = (CategoryPlot) barChart.getPlot();
        plot.setBackgroundPaint(new Color(0xf1f1f1));
        plot.setDomainGridlinePaint(Color.white);
        plot.setDomainGridlinesVisible(true);
        plot.setRangeGridlinePaint(Color.white);

        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);

        // Beautiful
        Color[] colors = {
                new Color(0x0071b1),
                new Color(0x09BE9E),
                new Color(0x091EBE),
                new Color(0xE45017),
                new Color(0xFDF80D)
        };

        for (i = 0 ; i < pvs.size() ; ++i) {
            plot.getRenderer().setSeriesPaint(i, colors[i]);
        }

        int width = 800;
        int height = 480;
        try {
            ChartUtilities.saveChartAsJPEG(new File(path), barChart, width, height);
        } catch (IOException ex) {
            System.out.println("Achtung, ein fehler!");
        }
    }
}