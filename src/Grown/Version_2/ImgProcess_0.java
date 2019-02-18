package Grown.Version_2;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * @作者 ：黄强
 * @时间 ：2018/11/22 23:53
 * @描述 ：灰度->迭代->距离->otsu
 */

public class ImgProcess_0 {
    public static void main(String[] args) {
        //输入图像
        File imgFile=new File("F:\\IdeaProject\\DIP\\Grown\\inputImage\\flower.png");
        int height;
        int width;
        BufferedImage inputImage;
        try {
            inputImage= ImageIO.read(imgFile);
            height=inputImage.getHeight();
            width=inputImage.getWidth();
        } catch (IOException e) {
            e.printStackTrace();
            throw  new RuntimeException("\n文件读取错误！\n");
        }
        //转灰，迭代。
        int [][]img=redefineImg(inputImage,height,width);
        //获得距离矩阵。
        int [][]distanceMatrix=getDistanceMatrix(img,height,width);
        //获得阈值。
        int threshold=getThreshold(distanceMatrix,height,width);
        //标记边缘点。
        drawEdgeLine(inputImage,distanceMatrix,height,width,threshold);
        //标记不同区域，不同区域标记不同颜色。
        divideArea(inputImage,distanceMatrix,height,width,threshold);
    }

    /**

     *@描述 :从转灰，依据周围点更新自身值，增强与周围图像关联。
     *@参数 :[inputImage, height, width]
     *@返回值:int[][]
     *@创建人 : 黄强
     *@创建时间  2018/11/23 13:43
     *@修改人和其它信息：
     *@版本：

     */
    private static int[][] redefineImg(BufferedImage inputImage,int height,int width){
        int i,j,k;
        int [][]garyImg=new  int[height][width];
        int [][]newImg =new int[height][width];
        //图像转灰。
        for (i=0;i<height;++i){
            for (j=0;j<width;++j){
                Color color =new Color(inputImage.getRGB(j,i));
                garyImg[i][j]=(color.getRed()*299+color.getGreen()*587+color.getBlue()*114)/1000;
            }
        }
        //遍历所有像素点的像素八领域，迭代像素，增强关联性。
        byte []drction_x={-1,-1,-1,0,1,1,1,0};
        byte []drction_y={-1,0,1,1,1,0,-1,-1};
        for (i=0;i<height;++i){
            for (j=0;j<width;++j){
                int pixelSum=0;
                int minDistance=Integer.MAX_VALUE;
                int[] nearDistance=new int[8];
                byte pixelCount=0;


                int temp_0=0;
                int temp_1=0;


                for( k=0;k<8;++k){
                    int x=i+drction_x[k];
                    int y=j+drction_y[k];
                    if (x>=0 &&x<height &&y >=0&&y<width){

      //                   迭代公式版本二
                        int tempDist=Math.abs(garyImg[x][y]-garyImg[i][j]);
                        temp_0+=tempDist;
                        temp_1+=tempDist*garyImg[x][y];



//                        pixelCount++;
//                        nearDistance[k]=Math.abs(garyImg[x][y]-garyImg[i][j]);
//                        pixelSum+=(1-nearDistance[k]/256)*garyImg[x][y];
//                        //获得最小距离。
//                        if (minDistance>nearDistance[k]){
//                            minDistance=nearDistance[k];
//                        }
                    }
                }

                //迭代公式版本二。
                newImg[i][j]=Math.abs((int)((1.0*temp_0/256+1)*garyImg[i][j]-1.0*temp_1/256));


             //   newImg[i][j]=((1-minDistance/256)*garyImg[i][j]+pixelSum)/(pixelCount+1);
            }
        }
        return newImg;
    }
    /**

     *@描述 :获得图像所有点与周围像素距离矩阵。
     *@参数 :[input, height, width]
     *@返回值:int[][]
     *@创建人 : 黄强
     *@创建时间  2018/11/23 22:36
     *@修改人和其它信息：
     *@版本：

     */
    private static int [][] getDistanceMatrix(int [][]input,int height,int width){
        int i,j,k;
        int [][]distanceMatrix=new int[height][width];
        //遍历图像，构建距离矩阵。
        byte []drction_x={-1,-1,-1,0,1,1,1,0};
        byte []drction_y={-1,0,1,1,1,0,-1,-1};
        for (i=0;i<height;++i) {
            for (j = 0; j < width; ++j) {
                int pixelSum = 0;
                byte pixelCount=0;
                for(k=0;k<8;++k){
                    int x=i+drction_x[k];
                    int y=j+drction_y[k];
                    if (x>=0 &&x<height &&y >=0&&y<width){
                        pixelCount++;
                        pixelSum+= Math.abs(input[i][j]-input[x][y]);
                    }
                }
                distanceMatrix[i][j]=pixelSum/pixelCount;
            }
        }
        return distanceMatrix;
    }


    /**

     *@描述 :获得最佳距离阈值，便于更好的二分图像。
     *@参数 :[distanceMatrix, height, width]
     *@返回值:int
     *@创建人 : 黄强
     *@创建时间  2018/11/23 22:37
     *@修改人和其它信息：
     *@版本：

     */
    private static int getThreshold(int [][] distanceMatrix,int height,int width){
        //距离变为一纬数组。
        int []distanceArray =new int[width*height];
        int i,j,k;
        int maxDistance=Integer.MIN_VALUE;
        //绘图使用。
        HashMap<Double,Double> innerVariMap=new HashMap<>();
        HashMap<Double,Double> outerVariMap=new HashMap<>();
        HashMap<Double,Double> ratioVariMap=new HashMap<>();
        HashMap<Integer,Integer> histMap= new HashMap<>();
        //距离变为一纬数组。
        for (i=0;i<height;++i){
            for (j=0;j<width;++j){
                distanceArray[i*width+j]=distanceMatrix[i][j];
                if (maxDistance<distanceArray[i*width+j]){
                    maxDistance=distanceArray[i*width+j];
                }
            }
        }
        //构建数组，数组下标为距离，值为个数。
        int []distanceCount =new int[maxDistance+1];
        for (int distance:distanceArray
        ) {
            distanceCount[distance]=distanceCount[distance]+1;
        }
        //绘图使用
        for (k=0;k<maxDistance+1;++k){
            histMap.put(k,distanceCount[k]);
        }
        showHist(histMap);

        int b=0;
        for (int a:distanceCount
             ) {
            b+=a;
        }
        System.out.println("total pixel:"+b+" count:"+ Arrays.toString(distanceCount));

        //小于某一个距离的像素个数。
        int lowCountSum=0;
        //方差商值。
        double result=Double.MIN_VALUE;
        //最佳阈值。
        int bestThreshold=0;
        //遍历所有距离求得最佳距离阈值。
        for (k=1;k<maxDistance;++k) {
            if (distanceCount[k] != 0) {
                lowCountSum += distanceCount[k - 1];
                //大于某个距离的像素的个数。
                int upCountSum = width * height - lowCountSum - distanceCount[k];
                //同一类概率
                double sameClassProb = (lowCountSum *1.0)/ (width * height);
                //不同类概率。
                double diffClassProb = (upCountSum*1.0) / (width * height);
                //同一类的距离均值。
                double sameClassAvg = 0;
                //不同类的距离均值。
                double diffClassAvg = 0;
                for (int m = 0; m < k; ++m) {
                    sameClassAvg += (m * distanceCount[m]*1.0) / (lowCountSum);
                }
                for (int m = k + 1; m <= maxDistance; ++m) {
                    diffClassAvg += (1.0*m * distanceCount[m]) / (upCountSum);
                }
                //同一类方差。
                double sameClassVari = 0;
                //不同类方差。
                double diffClassVari = 0;
                for (int m = 0; m < k; ++m) {
                    sameClassVari += (Math.pow(m - sameClassAvg, 2) * distanceCount[m]) / lowCountSum;
                }
                for (int m = k + 1; m <=maxDistance; ++m) {
                    diffClassVari += (Math.pow(m - diffClassAvg, 2) * distanceCount[m] )/ upCountSum;
                }
                //类内方差。
                double innerVari = sameClassProb * sameClassVari + diffClassProb * diffClassVari;
                //计算中的距离均值。
                double totalAvg = 0;
                for (int m = 0; m <=maxDistance; ++m) {
                    totalAvg += m * distanceCount[m];
                }
                totalAvg = totalAvg / (width * height);
                //计算类间方差。
                double outerVari = sameClassProb * Math.pow(sameClassAvg - totalAvg, 2) + diffClassProb * Math.pow(diffClassAvg - totalAvg, 2);

                double tempResult = innerVari / outerVari;
                innerVariMap.put((double)k,innerVari);
                outerVariMap.put((double)k,outerVari);
                ratioVariMap.put((double)k,tempResult);
                System.out.println(k+" "+innerVari+" "+outerVari+" "+tempResult);
               // 用最大类间方差作为判决标准。
                if (outerVari>result){
                    result =outerVari;
                    bestThreshold = k;
                }
            }
        }
        //绘图使用。
        plot(innerVariMap,"inner_vari");
        plot(outerVariMap,"outer_vari");
        plot(ratioVariMap,"ratio_vari");
        System.out.println("k:"+bestThreshold);
        return bestThreshold;
    }
    /**

     *@描述 :在区域边缘绘制边界。
     *@参数 :[originalImg, distanceMatrx, height, width, k]
     *@返回值:void
     *@创建人 : 黄强
     *@创建时间  2018/11/23 22:38
     *@修改人和其它信息：
     *@版本：

     */
    private static void drawEdgeLine(BufferedImage  originalImg,int [][]distanceMatrx,int height,int width,int k){
        int i,j;
        //标记边缘点。
        for (i=0;i<height;++i){
            for (j=0;j<width;++j){
                if (distanceMatrx[i][j]>k){
                    originalImg.setRGB(j,i,0xff0000);
                }
            }
        }
        //输出图像。
        try {
            ImageIO.write(originalImg,"jpg",new File("edgeLines.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("\n边缘线图片写出失败。\n");
        }
    }
    /**

     *@描述 :在原图像的不同区域绘制不同颜色。
     *@参数 :[origionalImg, distanceMatrix, height, width, k]
     *@返回值:void
     *@创建人 : 黄强
     *@创建时间  2018/11/23 22:38333333333333333333
     *@修改人和其它信息：
     *@版本：

     */
    private static  void divideArea(BufferedImage origionalImg,int [][] distanceMatrix,int height,int width,int k){
        int i,j;
        byte [][]flagMap=new  byte[height][width];
        Stack<Pixel> pixelStack=new Stack<>();
        for (i=0;i<height;++i){
            for (j=0;j<width;++j){
                if (distanceMatrix[i][j]<k &&flagMap[i][j]==0){
                    //每次进入处理一个区域。
                    pixelStack.push(new Pixel(i,j));
                    DPFGrown(origionalImg,distanceMatrix,flagMap,pixelStack,k);
                }
            }
        }
        try {
            ImageIO.write(origionalImg,"jpg",new File("areas.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
            throw new  RuntimeException("\n区域划分文件输出失败。\n");
        }
    }
    /**

     *@描述 :深度优先种子生长算法。
     *@参数 :[origionalImg, distanceMatrix, flagMap, pixelStack, threshold]
     *@返回值:void
     *@创建人 : 黄强
     *@创建时间  2018/11/23 22:39
     *@修改人和其它信息：
     *@版本：

     */
    private static  void DPFGrown(BufferedImage origionalImg,int [][]distanceMatrix,byte [][]flagMap,Stack<Pixel> pixelStack,int threshold){
        byte[] direction = {0, 0, -1, 1};
        int height=distanceMatrix.length;
        int width=distanceMatrix[0].length;
        Random random=new Random();
        //获得随机颜色。
        int fillColor=(random.nextInt(255)<<8)|(random.nextInt(255)<<4)|random.nextInt(255);
        //深度优先种子区域生长，将种子点的整个区域上色。。
        while (!pixelStack.empty()){
            Pixel seed=pixelStack.pop();
            origionalImg.setRGB(seed.getY(),seed.getX(),fillColor);
            for (int k = 0; k < 4; ++k) {
                int nextX = seed.getX() + direction[k];
                int nextY = seed.getY()+ direction[3 - k];
                //领域显色入栈。
                if (nextX>=0&&nextX<height&&nextY>=0&&nextY<width&&flagMap[nextX][nextY]==0&&distanceMatrix[nextX][nextY]<threshold){
                    flagMap[nextX][nextY]=1;
                    pixelStack.push(new Pixel(nextX,nextY));
                }
            }
        }
    }
    /**

    *@描述 :绘制图像。
    *@参数 :[hashMap, name]
    *@返回值:void
    *@创建人 : 黄强
    *@创建时间  2018/11/26 21:16
    *@修改人和其它信息：
    *@版本：

    */
    private static void plot(HashMap<Double,Double> hashMap,String name){

        XYSeries series = new XYSeries("xySeries");
        //初始化数值。
        for (Map.Entry<Double,Double> entry:hashMap.entrySet()
             ) {
            series.add(entry.getKey(),entry.getValue());
        }
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);
        JFreeChart chart = ChartFactory.createXYLineChart(
                "distance-variance", 
                "distance", 
                "variance", 
                dataset, 
                PlotOrientation.VERTICAL,
                false, 
                false, 
                false 
        );
        //绘图。
        ChartFrame frame = new ChartFrame(name, chart);
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    /**
    
    *@Describtion :绘制柱状图。
    *@Parms  :[histMap]
    *@Returns:void
    *@Author : 黄强
    *@Date   :  2018/12/3 17:40
    *@Modified：
    *@Version：
     
    */

    public static void showHist(HashMap<Integer,Integer> histMap) {
        // 取得数据集合
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (Map.Entry<Integer,Integer> entry:histMap.entrySet()
        ) {
            dataset.addValue(entry.getValue(),"",entry.getKey());
        }
        // 图表初始化。
        JFreeChart chart2 = ChartFactory.createBarChart3D("距离分布图",
                // 目录轴的显示标签
                "距离",
                // 数值轴的显示标签
                "数量",
                dataset,
                PlotOrientation.VERTICAL,
                false,
                false,
                false
        );
        Font titleFont = new Font("黑体", Font.BOLD, 20);
        Font plotFont = new Font("宋体", Font.PLAIN, 16);

        TextTitle textTitle2 = chart2.getTitle();
        // 为标题设置上字体
        textTitle2.setFont(titleFont);

        CategoryPlot categoryPlot = chart2.getCategoryPlot();
        categoryPlot.getRangeAxis().setLabelFont(plotFont);
        categoryPlot.getDomainAxis().setLabelFont(plotFont);
        categoryPlot.getDomainAxis().setTickLabelFont(plotFont);

        try {

            ChartFrame frame = new ChartFrame("距离分布图", chart2);
            frame.pack();
            frame.setVisible(true);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


//            ChartUtilities.writeChartAsJPEG(new FileOutputStream("hist.jpg"), 1.0f, chart2,
//                    1600, 900, null);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("\n图标无法输出！\n");        }
    }
}
