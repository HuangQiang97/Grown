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
import sun.awt.image.ImageWatched;

/**
 * @作者 ：黄强
 * @时间 ：2018/11/22 23:53
 * @描述 ：灰度->距离->聚类->otsu
 */

public class ImgProcess_5 {
    public static void main(String[] args) {
        File imgFile = new File("F:\\IdeaProject\\DIP\\Grown\\inputImage\\field.png");
        int height;
        int width;
        BufferedImage inputImage;
        try {
            inputImage = ImageIO.read(imgFile);
            height = inputImage.getHeight();
            width = inputImage.getWidth();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("\n文件读取错误！\n");
        }
        ArrayList<ArrayList<Pixel>> canopyList = new ArrayList<>();
        int[][] img = redefineImg(inputImage, height, width);
        int[][] distanceMatrix = getDistanceMatrix(img, height, width);
        LinkedList<Pixel> seedList = divideClass(distanceMatrix, 20, 10, canopyList);
        int [] conpySize=new int[canopyList.size()];
        for (int i=0;i<canopyList.size();++i){
            conpySize[i]=canopyList.get(i).size();
        }
        getThreshold(distanceMatrix, 20,10,seedList,conpySize);
        draw(distanceMatrix,seedList,canopyList,inputImage);




        DPFGrown(seedList, inputImage, distanceMatrix);
        try {
            ImageIO.write(inputImage, "jpg", new File("result.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @描述 :从转灰，依据周围点更新自身值，增强与周围图像关联。
     * @参数 :[inputImage, height, width]
     * @返回值:int[][]
     * @创建人 : 黄强
     * @创建时间 2018/11/23 13:43
     * @修改人和其它信息：
     * @版本：
     */
    private static int[][] redefineImg(BufferedImage inputImage, int height, int width) {
        int i, j;
        int[][] garyImg = new int[height][width];
        //图像转灰。
        for (i = 0; i < height; ++i) {
            for (j = 0; j < width; ++j) {
                Color color = new Color(inputImage.getRGB(j, i));
                garyImg[i][j] = (color.getRed() * 299 + color.getGreen() * 587 + color.getBlue() * 114) / 1000;
            }
        }
        //遍历所有像素点的像素八领域。
        return garyImg;
    }

    /**
     * @描述 :
     * @参数 :[colorArray]
     * @返回值:java.util.LinkedList<Grown.Seed>
     * @创建人 : 黄强
     * @创建时间 2018/11/26 23:40
     * @修改人和其它信息：
     * @版本：
     */
    private static LinkedList<Pixel> divideClass(int[][] colorArray, int T1, int T2, ArrayList<ArrayList<Pixel>> canopyList) {
        int index = 0;
        int width = colorArray[0].length;
        int height = colorArray.length;
        LinkedList<Pixel> seedList = new LinkedList<>();
        while (index < width * height) {
            //便利所有像素点。
            int x = index / width;
            int y = index % width;
            int popColor = colorArray[x][y];
            //种子点为空时处理。
            if (seedList.isEmpty()) {
                seedList.add(new Pixel(x, y, popColor));
                ArrayList<Pixel> pixels = new ArrayList<>();
                pixels.add(new Pixel(x, y));
                canopyList.add(pixels);
            } else {
                //设置断点。
                breakPoint:
                {
                    //设置标记该点能否归入某canopy。
                    byte belongCanopyFlag = 0;
                    //遍历种子点对弹出像素点归类。
                    for (Pixel seed : seedList
                    ) {
                        int distance = Math.abs(seed.getValue() - popColor);
                        //当距离小于T2时，该点到种子点已经足够近，可以归为一类，并且跳出对种子链表的遍历。
                        if (distance < T2) {
                            for (ArrayList<Pixel> singleList : canopyList
                            ) {
                                if (singleList.indexOf(seed) != -1) {
                                    singleList.add(new Pixel(x, y));
                                }
                            }
                            break breakPoint;
                            //当距离小于T1，该点可以与种子点归为同一canopy。
                        } else if (distance < T1) {
                            for (ArrayList<Pixel> singleList : canopyList
                            ) {
                                if (singleList.indexOf(seed) != -1) {
                                    singleList.add(new Pixel(x, y));
                                }
                            }
                            belongCanopyFlag = 1;
                        }
                    }//若到此时还未跳出遍历，该点不能与任一种子点归为一类，此时判断他是否能归为某一个canopy。
                    if (belongCanopyFlag == 0) {
                        //不属于任一canopy可以作为一个全新的种子点。
                        seedList.add(new Pixel(x, y, popColor));
                        ArrayList<Pixel> pixels = new ArrayList<>();
                        pixels.add(new Pixel(x, y));
                        canopyList.add(pixels);
                    }
                }
                index++;
            }
        }
        return seedList;
    }


    private static int[][] getDistanceMatrix(int[][] input, int height, int width) {
        int i, j, k;
        int[][] distanceMatrix = new int[height][width];
        byte[] drction_x = {-1, -1, -1, 0, 1, 1, 1, 0};
        byte[] drction_y = {-1, 0, 1, 1, 1, 0, -1, -1};
        for (i = 0; i < height; ++i) {
            for (j = 0; j < width; ++j) {
                int pixelSum = 0;
                byte pixelCount = 0;
                for (k = 0; k < 8; ++k) {
                    int x = i + drction_x[k];
                    int y = j + drction_y[k];
                    if (x >= 0 && x < height && y >= 0 && y < width) {
                        pixelCount++;
                        pixelSum += Math.abs(input[i][j] - input[x][y]);
                    }
                }
                distanceMatrix[i][j] = pixelSum / pixelCount;
            }
        }
        return distanceMatrix;
    }


    /**
     * @描述 :
     * @参数 :[colorArray, seedList]
     * @返回值:void
     * @创建人 : 黄强
     * @创建时间 2018/11/27 0:49
     * @修改人和其它信息：
     * @版本：
     */
    private static void  getThreshold(int[][] distanceMatrix,int T1,int T2, LinkedList<Pixel>seedList, int[] canpySize) {
        //距离变为以为数组。
        int height=distanceMatrix.length;
        int width=distanceMatrix[0].length;
        int[] distanceArray = new int[width * height];
        int i, j, k;
        int maxDistance = Integer.MIN_VALUE;
        for (i = 0; i < height; ++i) {
            for (j = 0; j < width; ++j) {
                distanceArray[i * width + j] = distanceMatrix[i][j];
                if (maxDistance < distanceArray[i * width + j]) {
                    maxDistance = distanceArray[i * width + j];
                }
            }
        }
        //数组下标为距离，值为个数。
        int[] distanceCount = new int[maxDistance + 1];
        for (int distance : distanceArray
        ) {
            distanceCount[distance] = distanceCount[distance] + 1;
        }
        System.out.println(" count:" + Arrays.toString(distanceCount));
        for (int h=0;h<seedList.size();++h) {
            HashMap<Double,Double> innerVariMap=new HashMap<>();
            HashMap<Double,Double> outerVariMap=new HashMap<>();
            HashMap<Double,Double> ratioVariMap=new HashMap<>();
            Pixel singleSeed=seedList.get(h);
            int lowCountSum = 0;
            //比较值。
            double result = Double.MIN_VALUE;
            //最佳阈值。
            int bestThreshold = 0;
            for (k = singleSeed.getValue() - T1; k < singleSeed.getValue() + T1; ++k) {
                //遍历所有距离求得最佳距离阈值。
                if ( k > 0 && k <= maxDistance&&distanceCount[k] != 0 ) {
                    //小于某一个距离的像素个数。
                    lowCountSum += distanceCount[k - 1];
                    //大于某个距离的像素的个数。
                    int upCountSum = canpySize[h] - lowCountSum - distanceCount[k];
                    //同一类概率
                    double sameClassProb = (lowCountSum * 1.0) / (canpySize[h]);
                    //不同类概率。
                    double diffClassProb = (upCountSum * 1.0) / (canpySize[h]);
                    //同一类的距离均值。
                    double sameClassAvg = 0;
                    //不同类的距离均值。
                    double diffClassAvg = 0;
                    for (int m = singleSeed.getValue()-T1; m < k; ++m) {
                        if (m>=0) {
                            sameClassAvg += (m * distanceCount[m] * 1.0) / (lowCountSum);
                        }
                    }
                    for (int m = k + 1; m <= singleSeed.getValue()+T1; ++m) {
                        if (m<maxDistance+1) {
                            diffClassAvg += (1.0 * m * distanceCount[m]) / (upCountSum);
                        }
                    }
                    //同一类方差。
                    double sameClassVari = 0;
                    //不同类方差。
                    double diffClassVari = 0;
                    for (int m = singleSeed.getValue()-T1; m < k; ++m) {
                        if (m >= 0) {
                            sameClassVari += (Math.pow(m - sameClassAvg, 2) * distanceCount[k]) / lowCountSum;
                        }
                    }
                    for (int m = k + 1; m <= singleSeed.getValue()+T1; ++m) {
                        if (m < maxDistance + 1) {
                            diffClassVari += (Math.pow(m - diffClassAvg, 2) * distanceCount[k]) / upCountSum;
                        }
                    }
                    //类内方差。
                    double innerVari = sameClassProb * sameClassVari + diffClassProb * diffClassVari;
                    //计算中的距离均值。
                    double totalAvg = 0;
                    for (int m = singleSeed.getValue()-T2; m <= singleSeed.getValue()+T2; ++m) {
                        if (m>=0 &&m<=maxDistance&&distanceCount[m]!=0) {
                            totalAvg += m * distanceCount[m];
                        }
                    }
                    totalAvg = totalAvg / (canpySize[h]);
                    //计算类间方差。
                    double outerVari = sameClassProb * Math.pow(sameClassAvg - totalAvg, 2) + diffClassProb * Math.pow(diffClassAvg - totalAvg, 2);
                    double tempResult = innerVari / outerVari;

                    innerVariMap.put((double)k,innerVari);
                    outerVariMap.put((double)k,outerVari);
                    ratioVariMap.put((double)k,tempResult);

                    //System.out.println(k+" "+sameClassProb +" "+diffClassProb+" "+sameClassAvg+" "+diffClassAvg+" "+innerVari+" "+outerVari+" "+tempResult);
                    System.out.println(k + " " + innerVari + " " + outerVari + " " + tempResult);
                    // if (tempResult < result) {
                    if (outerVari > result) {
                        result = outerVari;
                        bestThreshold = k;
                    }
                }
            }
            plot(innerVariMap,h+":inner_vari");
            plot(outerVariMap,h+":outer_vari");
            plot(ratioVariMap,h+":ratio_vari");
            singleSeed.setK(bestThreshold);
        }
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
                    originalImg.setRGB(j,i,0x0000ff);
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
//    private static  void divideArea(BufferedImage origionalImg,int [][] distanceMatrix,int height,int width,int k){
//        int i,j;
//        byte [][]flagMap=new  byte[height][width];
//        Stack<Pixel> pixelStack=new Stack<>();
//        for (i=0;i<height;++i){
//            for (j=0;j<width;++j){
//                if (distanceMatrix[i][j]<k &&flagMap[i][j]==0){
//                    pixelStack.push(new Pixel(i,j));
//                    DPFGrown(origionalImg,distanceMatrix,flagMap,pixelStack,k);
//                }
//            }
//        }
//        try {
//            ImageIO.write(origionalImg,"jpg",new File("areas.jpg"));
//        } catch (IOException e) {
//            e.printStackTrace();
//            throw new  RuntimeException("\n区域划分文件输出失败。\n");
//        }
//    }
    /**

     *@描述 :深度优先种子生长算法。
     *@参数 :[origionalImg, distanceMatrix, flagMap, pixelStack, threshold]
     *@返回值:void
     *@创建人 : 黄强
     *@创建时间  2018/11/23 22:39
     *@修改人和其它信息：
     *@版本：

     */
    private static  void DPFGrown(LinkedList<Pixel> seedList,BufferedImage origionalImg,int [][] imgArray) {
        int height = origionalImg.getHeight();
        int width = origionalImg.getWidth();
        byte[][] printMap = new byte[height][width];
        byte[][] processMap = new byte[height][width];
        for (Pixel seed : seedList
        ) {
            if (processMap[seed.getX()][seed.getY()] == 0) {




                byte[] direction = {0, 0, -1, 1};
                Stack<Pixel> seedStack = new Stack<>();
                seedStack.add(seed);
                while (!seedStack.isEmpty()) {
                    //对该区域点进行统计。
                    Pixel popSeed = seedStack.pop();
                    int x = popSeed.getX();
                    int y = popSeed.getY();
                    //标志出栈点已经被处理。
                    processMap[x][y] = 1;
                    //outputImage.setRGB(y,x,RGB);
                    //以4邻域处理周边点。
                    for (int k = 0; k < 4; k++) {
                        int nextX = x + direction[k];
                        int nextY = y + direction[3 - k];
                        if (nextX >= 0 && nextX < height && nextY >= 0 && nextY < width) {
                            //判断能否把周边点归入种子点所在区域。
                            // processMap[nextX][nextY]=1;

                            if (Math.abs(seed.getValue()-imgArray[nextX][nextY])<= seed.getK() &&processMap[nextX][nextY]==0) {
                                //若能归入该区域把该点入栈。
                                seedStack.add(new Pixel(nextX, nextY));
                                //printMap[nextX][nextY]=1;
                            } else {
                                //标记点可以被着色。
                                if(processMap[nextX][nextY]==0) {
                                    printMap[nextX][nextY] = 1;
                                    processMap[nextX][nextY]=1;
                                }

                            }
                        }
                    }
                }
                //把该区域数据存储。
            }
        }
        for (int i=0;i<height;++i){
            for (int j=0;j<width;++j){
                if (printMap[i][j]==1){
                    origionalImg.setRGB(j,i,0xff);
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
    private  static void draw(int [][] distanceMatrix,LinkedList<Pixel> seedList,ArrayList<ArrayList<Pixel>> caopyList,BufferedImage img){
        for (int i=0;i<seedList.size();++i) {
            for (Pixel p :caopyList.get(i)) {
                if (distanceMatrix[p.getX()][p.getY()]>seedList.get(i).getK()){
                    img.setRGB(p.getY(),p.getX(),0xff0000);
                }
            }
        }
        try{
            ImageIO.write(img,"jpg",new File("result_0.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
