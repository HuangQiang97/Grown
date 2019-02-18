package Grown;


import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.LinkedList;


public class Main {
    public static void main(String[] args) {
        //输入图像文件路径。
        String filePath = "F:\\IdeaProject\\DIP\\Grown\\inputImage\\fish.png";
        //获得输入图像的彩色像素点二维数组。
        Color[][] colorArray = Util.getColorArray(filePath);
        //获得初步种子链表。
        LinkedList<Seed> tempSeedList = new DivideClass(10000, 5000).divideClass(colorArray);
        long startTime = System.currentTimeMillis();
        //把种子链表分为4个多线程计算最佳阈值。
        LinkedList<Seed> seedList_1 = new LinkedList<>();
        LinkedList<Seed> seedList_2 = new LinkedList<>();
        LinkedList<Seed> seedList_3 = new LinkedList<>();
        LinkedList<Seed> seedList_0 = new LinkedList<>();
        for (int i = 0; i < tempSeedList.size(); i++) {
            if (i % 4 == 0) {
                seedList_0.add(tempSeedList.get(i));
            } else {
                if (i % 4 == 1) {
                    seedList_1.add(tempSeedList.get(i));
                } else {
                    if (i % 4 == 2) {
                        seedList_2.add(tempSeedList.get(i));
                    } else {
                        seedList_3.add(tempSeedList.get(i));
                    }
                }
            }
        }
        Thread thread_0 = new Thread(new GetThreshold(seedList_0, colorArray));
        Thread thread_1 = new Thread(new GetThreshold(seedList_1, colorArray));
        Thread thread_2 = new Thread(new GetThreshold(seedList_2, colorArray));
        Thread thread_3 = new Thread(new GetThreshold(seedList_3, colorArray));
        thread_0.start();
        thread_1.start();
        thread_2.start();
        thread_3.start();
        //在子线程执行完毕后main再继续执行。
        try {
            thread_0.join();
            thread_1.join();
            thread_2.join();
            thread_3.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException("\n多线程处理阈值时异常！\n");
        }
        System.out.println("种子点阈值信息：");
        for (Seed seed : tempSeedList
        ) {
            System.out.println(seed.getX() + " " + seed.getY() + " " + seed.getRed() + " " + seed.getGreen() + " " + seed.getBlue() + " " + seed.getBestThreshold());
        }
        System.out.println("选取阈值耗时：" + (System.currentTimeMillis() - startTime) + "ms");
        int width = Util.width;
        int height = Util.height;

        BufferedImage outputImage = Util.inputImage;
        //定义标记数组，分别标记整个图像的像素点是否已经处理，所有种子点的同一类点是否都已经处理。
        byte[][] processMap = new byte[height][width];
        byte[][] sameClassProcessMap = new byte[height][width];
        //对后加入的种子点，他们无同一类种子栈，要在初始种子点中遍历，记录下初始链表长度。
        int initSize = tempSeedList.size();
        int[] processInitSeed = new int[initSize];
        LinkedList<Area> areaLinkedList = new LinkedList<>();
        byte[][] printMap = new byte[height][width];

        //遍历种子链表进行生长，并不断加入新的种子点。
        DPFGrown dpfGrown = new DPFGrown(colorArray, sameClassProcessMap, tempSeedList, areaLinkedList, printMap);
        for (int i = 0; i < tempSeedList.size(); i++) {
            Seed seed = tempSeedList.get(i);
            //对种子点进行生长。
            dpfGrown.DPFGrown(seed, processMap);


            /*
            此处有一个问题，即种子点重复加入链表。
            对于后加入种子点他没有同一类的像素栈堆，只能去初始化的种子点的同一类像素栈，由于后加入的点后处理，所以初始点的同一类栈
            中很多点未被处理，所以会被多次当作新的种子点加入链表。
             */

//           解决方法一：对于初始点只在自己的同一类栈中寻找新的点，对于后加入的点在自己所在的同一类栈中寻找。
//            if (i<initSize){
//                for (Grown.Seed sameClassSeed:tempSeedList.get(i).sameClassSeed
//                     ) {
//                    if (sameClassPeocessMap[sameClassSeed.getX()][sameClassSeed.getY()]==0){
//                        sameClassSeed.setBestThreshhold((int)tempSeedList.get(i).getBestThreshold());
//                        tempSeedList.add(sameClassSeed);
//                        break;
//                    }
//                }
//            }else {
//                breakPoint:{
//                for (int j=0;j<initSize;j++){
//                    for (Grown.Seed classSeed:tempSeedList.get(j).sameClassSeed
//                         ) {
//                        if (classSeed==seed) {
//                            for (Grown.Seed classSeed_1 : tempSeedList.get(j).sameClassSeed) {
//                                if (sameClassPeocessMap[classSeed_1.getX()][classSeed_1.getY()] == 0) {
//                                    classSeed_1.setBestThreshhold((int) tempSeedList.get(j).getBestThreshold());
//                                    tempSeedList.add(classSeed_1);
//                                    break breakPoint;
//                                }
//                            }
//
//                        }
//                    }
//                    }
//                }
//            }

//解决方法二：鸵鸟算法，不处理，在进行生长时同一点会被跳过。
            breakPoint:
            {
                //遍历初始种子链表。
                for (int j = 0; j < initSize; j++) {
                    if (processInitSeed[j] == 0) {
                        //遍历该同一类栈。
                        for (Seed sameClassSeed : tempSeedList.get(j).sameClassSeed
                        ) {
                            //该点未被处理时处理。
                            if (sameClassProcessMap[sameClassSeed.getX()][sameClassSeed.getY()] == 0) {

                                //新加入点未设置阈值，在这里设置阈值。
                                sameClassSeed.setBestThreshhold((int) tempSeedList.get(i).getBestThreshold());
                                tempSeedList.add(sameClassSeed);
                                //加入一个点就跳出大循环，否则整个栈中未处理的点都会被加入链表。
                                break breakPoint;
                            }
                        }
                        //如果执行到这里还未跳出循环即是该栈中点都已经被处理，作标记，以后不再遍历他。
                        processInitSeed[j] = 1;
                    }
                }
            }
        }


        //统计最终种子数目,没什么用就看看。
        HashMap<Seed, Integer> hashMap = new HashMap<>();
        for (Seed seed : tempSeedList
        ) {
            if (hashMap.containsKey(seed)) {
                hashMap.put(seed, hashMap.get(seed) + 1);
            } else {
                hashMap.put(seed, 1);
            }
        }
        System.out.println("最终种子数 区域数\n" + hashMap.size() + "      " + areaLinkedList.size());


//        //获得各个区域边缘线集合。
//        LinkedList<LinkedList<Seed>> edgeLineList = dpfGrown.getEdgeLineList();
//        //获得区域集合。
//        LinkedList<Area> areaList = dpfGrown.getAreaList();
//        Combine combineArea=new Combine(areaLinkedList,printMap,edgeLineList);
//        //获得各个区域相邻关系。
//        byte[][] nearMap=combineArea.getNearMap(edgeLineList);
//        //合并过小区域。
//        combineArea.combineSmallArea(nearMap);
//        //获得区域间阈值。
//        int T =combineArea.getThreshold();
//        combineArea.initDistanceArray(T,nearMap);
//        //进行区域合并。
//        combineArea.combineArea(T,nearMap);
//        //输出最终图像。
        Util.printImage(0xff0000,printMap,outputImage);


    }
}
