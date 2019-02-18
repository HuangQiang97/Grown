package Grown.Version_2;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import javax.swing.JFrame;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * @作者 ：黄强
 * @时间 ：2018/11/22 23:53
 * @描述 ：灰度->otsu
 */

public class ImgProcess_2 {
    public static void main(String[] args) {
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
        int [][]img=redefineImg(inputImage,height,width);
        //int [][]distanceMatrix=getDistanceMatrix(img,height,width);
        int threshold=getThreshold(img,height,width);
        drawEdgeLine(inputImage,img,height,width,threshold);
        divideArea(inputImage,img,height,width,threshold);
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
        int i,j;
        int [][]garyImg=new  int[height][width];
        //图像转灰。
        for (i=0;i<height;++i){
            for (j=0;j<width;++j){
                Color color =new Color(inputImage.getRGB(j,i));
                garyImg[i][j]=(color.getRed()*299+color.getGreen()*587+color.getBlue()*114)/1000;
            }
        }
        return garyImg;
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
        //距离变为以为数组。
        int []distanceArray =new int[width*height];
        int i,j,k;
        int maxDistance=Integer.MIN_VALUE;

        HashMap<Double,Double> innerVariMap=new HashMap<>();
        HashMap<Double,Double> outerVariMap=new HashMap<>();
        HashMap<Double,Double> ratioVariMap=new HashMap<>();

        for (i=0;i<height;++i){
            for (j=0;j<width;++j){
                distanceArray[i*width+j]=distanceMatrix[i][j];
                if (maxDistance<distanceArray[i*width+j]){
                    maxDistance=distanceArray[i*width+j];
                }
            }
        }
        //数组下标为距离，值为个数。
        int []distanceCount =new int[maxDistance+1];
        for (int distance:distanceArray
        ) {
            distanceCount[distance]=distanceCount[distance]+1;
        }
        int b=0;

        for (int a:distanceCount
        ) {
            b+=a;
        }
        System.out.println("total:"+b+" count:"+ Arrays.toString(distanceCount));
        //小于某一个距离的像素个数。
        int lowCountSum=0;
        //比较值。
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
                    sameClassVari += (Math.pow(m - sameClassAvg, 2) * distanceCount[k]) / lowCountSum;
                }
                for (int m = k + 1; m <=maxDistance; ++m) {
                    diffClassVari += (Math.pow(m - diffClassAvg, 2) * distanceCount[k] )/ upCountSum;
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
                //System.out.println(k+" "+sameClassProb +" "+diffClassProb+" "+sameClassAvg+" "+diffClassAvg+" "+innerVari+" "+outerVari+" "+tempResult);
                System.out.println(k+" "+innerVari+" "+outerVari+" "+tempResult);
                // if (tempResult < result) {
                if (outerVari>result){
                    result =outerVari;
                    bestThreshold = k;
                }
            }
        }
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
        for (i=0;i<height;++i){
            for (j=0;j<width;++j){
                if (distanceMatrx[i][j]>k){
                    originalImg.setRGB(j,i,0xff);
                }
            }
        }
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
     *@创建时间  2018/11/23 22:38
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
        int fillColor=(random.nextInt(255)<<8)|(random.nextInt(255)<<4)|random.nextInt(255);
        //深度优先种子区域生长。
        while (!pixelStack.empty()){
            Pixel seed=pixelStack.pop();
            origionalImg.setRGB(seed.getY(),seed.getX(),fillColor);
            for (int k = 0; k < 4; ++k) {
                int nextX = seed.getX() + direction[k];
                int nextY = seed.getY()+ direction[3 - k];
                if (nextX>=0&&nextX<height&&nextY>=0&&nextY<width&&flagMap[nextX][nextY]==0&&distanceMatrix[nextX][nextY]<threshold){
                    flagMap[nextX][nextY]=1;
                    pixelStack.push(new Pixel(nextX,nextY));
                }
            }
        }
    }
    /**

     *@描述 :
     *@参数 :[hashMap, name]
     *@返回值:void
     *@创建人 : 黄强
     *@创建时间  2018/11/26 21:16
     *@修改人和其它信息：
     *@版本：

     */
    private static void plot(HashMap<Double,Double> hashMap,String name){

        XYSeries series = new XYSeries("xySeries");
        for (Map.Entry<Double,Double> entry:hashMap.entrySet()
        ) {
            series.add(entry.getKey(),entry.getValue());
        }

//        for (int x = -100; x < 100; x++) {
//            int y = x*x;
//            series.add(x, y);
//        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);
        JFreeChart chart = ChartFactory.createXYLineChart(
                "distance-variance", // chart title
                "distance", // x axis label
                "variance", // y axis label
                dataset, // data
                PlotOrientation.VERTICAL,
                false, // include legend
                false, // tooltips
                false // urls
        );

        ChartFrame frame = new ChartFrame(name, chart);
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }


}

