package Grown;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
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
}
