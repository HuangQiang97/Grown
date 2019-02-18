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
 * @描述 ：灰度->迭代->距离->聚类->otsu
 */

public class ImgProcess_7 {
    public static void main(String[] args) {
        File imgFile=new File("F:\\IdeaProject\\DIP\\Grown\\inputImage\\field.png");
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
        int [][]distanceMatrix=getDistanceMatrix(img,height,width);
        LinkedList<Pixel> seedList=divideClass(distanceMatrix,20,10);
        getThreshold(distanceMatrix,seedList);
        DPFGrown(seedList,inputImage,distanceMatrix);
        try{
            ImageIO.write(inputImage,"jpg",new File("result.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        //遍历所有像素点的像素八领域。
        byte []drction_x={-1,-1,-1,0,1,1,1,0};
        byte []drction_y={-1,0,1,1,1,0,-1,-1};
        for (i=0;i<height;++i){
            for (j=0;j<width;++j){
                int pixelSum=0;
                int minDistance=Integer.MAX_VALUE;
                int[] nearDistance=new int[8];
                byte pixelCount=0;
                for( k=0;k<8;++k){
                    int x=i+drction_x[k];
                    int y=j+drction_y[k];
                    if (x>=0 &&x<height &&y >=0&&y<width){
                        pixelCount++;
                        nearDistance[k]=Math.abs(garyImg[x][y]-garyImg[i][j]);
                        pixelSum+=(1-nearDistance[k]/256)*garyImg[x][y];
                        if (minDistance>nearDistance[k]){
                            minDistance=nearDistance[k];
                        }
                    }
                }
                newImg[i][j]=((1-minDistance/256)*garyImg[i][j]+pixelSum)/(pixelCount+1);
            }
        }
        return newImg;
    }

    /**

     *@描述 :
     *@参数 :[colorArray]
     *@返回值:java.util.LinkedList<Grown.Seed>
     *@创建人 : 黄强
     *@创建时间  2018/11/26 23:40
     *@修改人和其它信息：
     *@版本：

     */
    private static LinkedList<Pixel> divideClass(int[][]colorArray,int T1,int T2){
        int index=0;
        int width= colorArray[0].length;
        int height=colorArray.length;
        LinkedList <Pixel> seedList=new LinkedList<>();
        while (index<width*height) {
            //便利所有像素点。
            int x = index / width;
            int y = index % width;
            int popColor = colorArray[x][y];
            //种子点为空时处理。
            if (seedList.isEmpty()) {
                seedList.add(new Pixel(x, y,popColor));
            } else {
                //设置断点。
                breakPoint:
                {
                    //设置标记该点能否归入某canopy。
                    byte belongCanopyFlag = 0;
                    //遍历种子点对弹出像素点归类。
                    for (Pixel seed : seedList
                    ) {
                        int distance = Math.abs(seed.getValue()-popColor);
                        //当距离小于T2时，该点到种子点已经足够近，可以归为一类，并且跳出对种子链表的遍历。
                        if (distance < T2) {
                            break breakPoint;
                            //当距离小于T1，该点可以与种子点归为同一canopy。
                        } else if (distance < T1) {
                            belongCanopyFlag = 1;
                        }
                    }//若到此时还未跳出遍历，该点不能与任一种子点归为一类，此时判断他是否能归为某一个canopy。
                    if (belongCanopyFlag == 0) {
                        //不属于任一canopy可以作为一个全新的种子点。
                        seedList.add(new Pixel(x,y,popColor));
                    }
                }
                index++;
            }
        }
        return seedList;
    }


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

     *@描述 :
     *@参数 :[colorArray, seedList]
     *@返回值:void
     *@创建人 : 黄强
     *@创建时间  2018/11/27 0:49
     *@修改人和其它信息：
     *@版本：

     */
    private static void  getThreshold(int [][]colorArray,LinkedList<Pixel> seedList){
        int width= colorArray[0].length;
        int height=colorArray.length;
        int a=0;

        for (Pixel seed:seedList
        ) {

            a++;
            boolean getDistance=false;
            int bestThreshold=0;
            int [][]distanceArray=new int[height][width];
            //以不同阈值循环划分种子点与周边区域，计算类间反差与类内方差的最大比值，此时即为最佳阈值。。
            for (int threshold=0;threshold<256;threshold++) {
                int lowSum=0;
                int lowCount=0;
                int upSum=0;
                int upCount=0;
                double g=0;
                double maxG=Double.MIN_VALUE;
                LinkedList<Pixel> lowSeedList=new LinkedList<>();
                LinkedList<Pixel> upSeedList=new LinkedList<>();

                //遍历像素点。
                for (int i = 0; i < width; i++) {
                    for (int j = 0; j < height; j++) {
                        int  popColor = colorArray[j][i];
                        //getdiatance用于设置标记，因为根据不同阈值计算最佳阈值时，种子点未变，所以所有点到他的距离不变，距离只用计算一次，提高效率。
                        if (!getDistance) {
                            int distance =Math.abs(popColor-seed.getValue());
                            distanceArray[j][i] = distance;
                        }
                        //距离小于阈值时处理。
                        if (distanceArray[j][i] < threshold) {
                            lowCount++;
                            lowSum+=distanceArray[j][i];
                            lowSeedList.add(new Pixel(j,i,popColor));
                        } else {
                            //距离大于阈值时处理。
                            upCount++;
                            upSum+=distanceArray[j][i];
                            upSeedList.add(new Pixel(j,i,popColor));
                        }
                    }
                }
                //计算类间反差与类内方差的最大比值。
                if (lowCount!=0&&upCount!=0) {

                    int avg=(lowSum+upSum)/(width*height);
                    int lowAvg=lowSum/lowCount;
                    int upAvg=upSum/upCount;
                    double sameClsaaVari=0;
                    double diffClsaaVari=0;
                    for (Pixel pixel:lowSeedList
                    ) {
                        sameClsaaVari+=Math.pow(pixel.getValue()-lowAvg,2);
                    }
                    sameClsaaVari=sameClsaaVari/lowCount;


                    for (Pixel pixel:upSeedList
                    ) {
                        diffClsaaVari+=Math.pow(pixel.getValue()-upAvg,2);
                    }
                    diffClsaaVari=diffClsaaVari/upCount;
                    //计算类间方差。
                    double variance_1  =(lowCount*1.0*Math.pow(avg-lowAvg,2)/(width*height)+(upCount*1.0*Math.pow(avg-upAvg,2))/(width*height));
                    // (int) (((double) lowCount / (width * height))*(Math.abs(avg - lowAvg)) +((double)upCount/(width * height))*(Math.abs(avg - upAvg) ));
                    //计算类内方差。
                    double variance_2=( (lowCount*1.0)*sameClsaaVari/(width*height)+(upCount*1.0*diffClsaaVari)/(width*height));
                    if (variance_2!=0) {
                        if (a==2){
                            int b=2;
                        }

                        if ((g = (variance_1 )/variance_2) > maxG) {


                            maxG = g;
                            bestThreshold = threshold;
                        }
                    }
//                    if (variance_1>maxG){
//                        maxG=variance_1;
//                        bestThreshold=threshold;
//                    }
                }
                //当第一次计算到此处时，种子点与所有像素点已经计算完毕，后续对于该种子点可以不同再计算。
                getDistance=true;
            }

            //设置最佳阈值。
            seed.setK(bestThreshold-80);
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
}
