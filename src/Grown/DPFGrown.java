package Grown;

import java.awt.*;
import java.util.LinkedList;
import java.util.Stack;

/**
 * @创建人：黄强
 * @时间 ：2018/7/25 21:21
 * @描述 ：深度优先生长。
 */
public class DPFGrown {
    Color[][]colorArray;

    byte[][]sameClassProcessMap;
    LinkedList<Seed> seedList;
    LinkedList<Area> areaList;
    LinkedList<LinkedList<Seed>> edgeLineList=new LinkedList<>();
    byte[][]printMap;
    int height=Util.height;
    int width=Util.width;


    //数据初始化。
    public DPFGrown(Color[][]colorArray, byte[][]sameClassPr0cessMap, LinkedList<Seed> seedList,LinkedList<Area>areaLinkedList,byte[][]printMap){

        this.colorArray=colorArray;
        this.sameClassProcessMap=sameClassPr0cessMap;
        this.seedList=seedList;
        this.areaList=areaLinkedList;
        this.printMap=printMap;
    }
    //返回各个区域边缘线集合。
    public LinkedList<LinkedList<Seed>> getEdgeLineList(){

        return edgeLineList;
    }

//获得区域链表。
    public LinkedList<Area> getAreaList(){

        return areaList;
    }


    public void DPFGrown(Seed seed, byte[][]processMap) {
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


        if (processMap[seed.getX()][seed.getY()] == 0) {
            LinkedList<Seed> edgeLine=new LinkedList<>();
            byte[] direction = {0, 0, -1, 1};

            int redSum = 0;
            int greenSum = 0;
            int blueSum = 0;
            int quantity = 0;

            Stack<Seed> seedStack = new Stack<>();
            seedStack.add(seed);
            while (!seedStack.isEmpty()) {
                //对该区域点进行统计。
                Seed popSeed = seedStack.pop();
                redSum += popSeed.getRed();
                greenSum +=popSeed.getGreen();
                blueSum += popSeed.getBlue();
                quantity++;
                int x = popSeed.getX();
                int y = popSeed.getY();
                //标志出栈点已经被处理。
                processMap[x][y] = 1;
                //outputImage.setRGB(y,x,RGB);
                //以4邻域处理周边点。
                sameClassProcessMap[x][y] = 1;
                for (int k = 0; k < 4; k++) {
                    int nextX = x + direction[k];
                    int nextY = y + direction[3 - k];
                    if (nextX >= 0 && nextX < Util.height && nextY >= 0 && nextY < Util.width ) {
                        //判断能否把周边点归入种子点所在区域。
                       // processMap[nextX][nextY]=1;
                        boolean similarFlag;
                        if ((similarFlag=isSimilar(nextX, nextY, seed))&& processMap[nextX][nextY] == 0) {
                            //若能归入该区域把该点入栈。
                            seedStack.add(new Seed(nextX, nextY, colorArray[nextX][nextY].getRGB()));
                        } else {
                            //对边缘点加入该集合边缘点链表。
                           if (!similarFlag) {
                                for (int i = nextX - 1; i < nextX + 1; i++) {
                                    for (int j = nextY - 1; j < nextY + 1; j++) {
                                        if (i >= 0 && i < height && j >= 0 && j < width) {
                                            Seed edgeSeed = new Seed(i, j);
                                            if (edgeLine.indexOf(edgeSeed) == -1) {
                                                edgeLine.add(edgeSeed);
                                            }
                                            //标记改点可以被着色。
                                            printMap[i][j] = 1;
                                        }
                                    }
                               }
                            }
                        }
                    }
                }
            }
            //把该区域数据存储。
            areaList.add(new Area(quantity, redSum, greenSum, blueSum));
            edgeLineList.add(edgeLine);
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
    public boolean isSimilar(int x, int y, Seed seed) {

        int distance = (int) (Math.pow(colorArray[x][y].getRed() - seed.getRed(), 2) +
                Math.pow(colorArray[x][y].getGreen() - seed.getGreen(), 2) +
                Math.pow(colorArray[x][y].getBlue() - seed.getBlue(), 2));
        if (distance < DivideClass.T2) {
            sameClassProcessMap[x][y] = 1;
        }

        if (distance < seed.getBestThreshold()) {
            return true;
        } else {
            return false;
        }

    }
}
