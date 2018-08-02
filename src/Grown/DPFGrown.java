package Grown;

import Grown.DivideClass;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.Stack;

/**
 * @创建人：黄强
 * @时间 ：2018/7/25 21:21
 * @描述 ：深度优先生长。
 */
public class DPFGrown {
    int RGB;
    Color[][]colorArray;
    BufferedImage outputImage;
    byte[][]sameClassPricessMap;
    LinkedList<Seed> seedList=new LinkedList<>();
    public DPFGrown(int RGB, Color[][]colorArray, byte[][]sameClassPricessMap, LinkedList<Seed> seedList, BufferedImage outputImage){
        //数据初始化。
        this.RGB=RGB;
        this.colorArray=colorArray;
        this.outputImage=outputImage;
        this.sameClassPricessMap=sameClassPricessMap;
        this.seedList=seedList;
    }


    public void DPFGrown(Seed seed, byte[][]processMap){
        /*
        *
         *@描述 :深度优先生长。
         *@参数 :[seed, processMap]
         *@返回值:void
         *@创建人 : 黄强
         *@创建时间  2018/8/2 15:25
         *@修改人和其它信息：
         *@版本：
         */

//      遍历掉重复种子点。
//        for (Grown.Seed singleSeed:seedList
//             ) {
//            if (singleSeed==seed){
//                return;
//            }
//        }


        byte []direction={0,0,-1,1};
        Stack<Seed> seedStack=new Stack<>();
        seedStack.add(seed);
        while (!seedStack.isEmpty()){
            Seed popSeed=seedStack.pop();
            int x=popSeed.getX();
            int y=popSeed.getY();
            //标志出栈点已经被处理。
            processMap[x][y]=1;
            //outputImage.setRGB(y,x,RGB);
            //以4邻域处理周边点。
            sameClassPricessMap[x][y]=1;
            for (int k=0;k<4;k++){
                int nextX=x+direction[k];
                int nextY=y+direction[3-k];
                if (nextX>=0&&nextX< Util.height&&nextY>=0&&nextY< Util.width&&processMap[nextX][nextY]==0){
                    //判断能否把周边点归入种子点所在区域。
                    if (isSimilar(nextX,nextY,seed)){
                        //若能归入该区域把该点入栈。
                        seedStack.add(new Seed(nextX,nextY,colorArray[nextX][nextY].getRGB()));
                    }else {
                        //若不能，在边界处绘制边界线。
                        // processMap[nextX][nextY]=1;
                        for (int i=nextX-1;i<nextX+1;i++){
                            for (int j=nextY-1;j<nextY+1;j++){
                                if (i>=0&&i< Util.height&&j>=0&&j< Util.width){
                                    outputImage.setRGB(j,i,RGB);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    /*
     *
     *@描述 :判断周边点能否归入种子点区域。
     *@参数 :[x, y, seed]
     *@返回值:boolean
     *@创建人 : 黄强
     *@创建时间  2018/8/2 20:02
     *@修改人和其它信息：
     *@版本：
     */
    public boolean isSimilar(int x, int y, Seed seed){

        int distance=(int) (Math.pow(colorArray[x][y].getRed()-seed.getRed(),2)+
                Math.pow(colorArray[x][y].getGreen()-seed.getGreen(),2)+
                Math.pow(colorArray[x][y].getBlue()-seed.getBlue(),2));
        if (distance< DivideClass.T2){
            sameClassPricessMap[x][y]=1;
        }

        if(distance<seed.getBestThreshold()){
            return true;
        }
        else return false;
    }
}
