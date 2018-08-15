package Grown;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.LinkedList;

/**
 * @创建人：黄强
 * @时间 ：2018/7/25 11:11
 * @描述 ：获得图像基本信息。
 */
public class Util {
    static int width;
    static int height;
    static  BufferedImage inputImage;

    /*
     *
     *@描述 :获得图像彩色二维像素点。
     *@参数 :[path]
     *@返回值:java.awt.Color[][]
     *@创建人 : 黄强
     *@创建时间  2018/8/2 20:39
     *@修改人和其它信息：
     *@版本：
     */


    public static Color[][]getColorArray(String path){

        
//读取文件。
        try {
            inputImage=ImageIO.read(new File(path));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("\n文件读取出错！请检查路径是否正确!\n");
        }
        width=inputImage.getWidth();
        height=inputImage.getHeight();
        Color colorArray[][]=new Color[height][width];
        //初始化数组。
        for (int i=0;i<width;i++){
            for (int j=0;j<height;j++){
                colorArray[j][i]=new Color(inputImage.getRGB(i,j));
            }
        }
        return colorArray;
    }

    /*
     *
     *@描述 ：根据数组点对输出图像进行绘制边界。
     *@参数 :[RGB, printMap, outputImage]
     *@返回值:void
     *@创建人 : 黄强
     *@创建时间  2018/8/11 13:20
     *@修改人和其它信息：
     *@版本：
     */
    public static  void printImage(int RGB,byte[][]printMap,BufferedImage outputImage){
        //对图像绘制边缘线。
        for (int i=0;i<height;i++){
            for (int j=0;j<width;j++){
                if (printMap[i][j]==1){
                    outputImage.setRGB(j,i,RGB);
                }
            }
        }
        //输出最终图像。
        Date date=new Date();
        try {
            ImageIO.write(outputImage,"jpg",new File("Result\\grownPicture\\ "+date.getHours()+date.getMinutes()+".jpg"));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("\n生长文件无法写出！\n");
        }
    }



}
