package Grown.Test;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Stack;

/**
 * @创建人：黄强
 * @时间 ：2018/8/1 15:38
 * @描述 ：
 */
public class Test {
    public static void main(String[] args) throws IOException {
//        BufferedImage bufferedImage=new BufferedImage(100,200,BufferedImage.TYPE_3BYTE_BGR);
//        for(int i=0;i<50;i++){
//            for (int j=0;j<200;j++){
//                bufferedImage.setRGB(i,j,0xff);
//            }
//        }
//        for(int i=50;i<100;i++){
//            for (int j=0;j<200;j++){
//                bufferedImage.setRGB(i,j,0xff00);
//            }
//        }
//        ImageIO.write(bufferedImage,"jpg",new File("1.jpg"));
//
//        int [][]a=new int[2][3];
//        System.out.println(a[1][2]);
               Stack<Integer> integers=new Stack<>();
        integers.add(1);
        integers.add(2);
        System.out.println(integers.indexOf(8));
//        for (Integer i:integers
//             ) {
//            System.out.println(i);
//        }
//        for (Integer i:integers
//        ) {
//            System.out.println(i);
//        }



    }
}
