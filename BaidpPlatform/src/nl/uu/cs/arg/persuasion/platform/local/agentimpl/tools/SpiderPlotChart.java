package nl.uu.cs.arg.persuasion.platform.local.agentimpl.tools;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.SpiderWebPlot;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.statistics.DefaultMultiValueCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleEdge;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SpiderPlotChart
{
    public SpiderPlotChart(String path, List<Map<String, Double>> pvs, String[] names)
    {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        int i = 0;
        for (Map<String, Double> pv : pvs) {
            for (Map.Entry<String, Double> entry : pv.entrySet()) {
                String s = entry.getKey();
                dataset.addValue(entry.getValue(), names[i], s.substring(0, 1).toUpperCase() + s.substring(1));
            }
            ++i;
        }

        SpiderWebPlot plot = new SpiderWebPlot(dataset);
        //plot.setStartAngle(3);
        plot.setInteriorGap(0.40);
        plot.setToolTipGenerator(new StandardCategoryToolTipGenerator());
        JFreeChart chart = new JFreeChart("Personality Vector", TextTitle.DEFAULT_FONT, plot, false);
        LegendTitle legend = new LegendTitle(plot);
        legend.setPosition(RectangleEdge.BOTTOM);
        chart.addSubtitle(legend);

        /*JFreeChart barChart = ChartFactory.createBarChart(
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
        }*/

        int width = 800;
        int height = 480;
        try {
            ChartUtilities.saveChartAsJPEG(new File(path), chart, width, height);
        } catch (IOException ex) {
            System.out.println("Achtung, ein fehler!");
        }
    }
}