package Grown.Version_2;

import java.awt.Font;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.swing.*;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

public class Test {


    /**
     * 创建一个数据集合
     *
     * @return
     */


    public static void showHist(HashMap<Integer,Integer> histMap)
            throws  IOException {


        DefaultCategoryDataset dataset = new DefaultCategoryDataset();// 取得数据集合
        for (Map.Entry<Integer,Integer> entry:histMap.entrySet()
             ) {
            dataset.addValue(entry.getValue(),"",entry.getKey());
        }
        JFreeChart chart2 = ChartFactory.createBarChart3D("距离分布图", // 图表标题
                "距离", // 目录轴的显示标签
                "数量", // 数值轴的显示标签
                dataset, // 数据集
                PlotOrientation.VERTICAL, // HORIZONTAL,// 图表方向：水平、垂直
                false, // 是否显示图例(对于简单的柱状图是false,因为只有一种，没有必要显示)
                false, // 是否生成工具
                false // 是否生成URL链接
        );
        Font titleFont = new Font("黑体", Font.BOLD, 20);
        Font plotFont = new Font("宋体", Font.PLAIN, 16);

        TextTitle textTitle2 = chart2.getTitle();
        textTitle2.setFont(titleFont);// 为标题设置上字体

        CategoryPlot categoryPlot = chart2.getCategoryPlot();
        categoryPlot.getRangeAxis().setLabelFont(plotFont);// 设置Y轴标识字体
        categoryPlot.getDomainAxis().setLabelFont(plotFont);// 设置X轴标识字体
        categoryPlot.getDomainAxis().setTickLabelFont(plotFont);// 设置轴标记的坐标的标记字体


        ChartFrame frame = new ChartFrame("aaa",chart2 );
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        ChartUtilities.writeChartAsJPEG(new FileOutputStream("b.jpg"), 1.0f, chart2,
                800, 450, null);// 输出图表
    }
}