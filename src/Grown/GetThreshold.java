package Grown;

import Grown.Util;

import java.awt.*;
import java.util.LinkedList;

/**
 * @创建人：黄强
 * @时间 ：2018/7/25 14:49
 * @描述 ：获得种子点最佳阈值。
 */
public class GetThreshold  implements Runnable{
    LinkedList<Seed> seedList;
    Color[][]colorArray;
    public GetThreshold(LinkedList<Seed> seedList, Color[][]colorArray) {
        //初始化数据。
        this.colorArray = colorArray;
        this.seedList = seedList;
    }
    /*
     *
     *@描述 :获取最佳阈值。
     *@参数 :[]
     *@返回值:void
     *@创建人 : 黄强
     *@创建时间  2018/8/2 20:05
     *@修改人和其它信息：
     *@版本：
     */
    public void run(){

        int width= Util.width;
        int height=Util.height;
        for (Seed seed:seedList
        ) {
            int maxVariance=0;
            boolean getDistance=false;
            int bestThreshold=0;
            int [][]distanceArray=new int[height][width];
            //以不同阈值循环划分种子点与周边区域，计算类间反差与类内方差的最大比值，此时即为最佳阈值。。
            for (int threshold=1000;threshold<80000;threshold+=500) {
                int lowRedSum=0;
                int lowBlueSum=0;
                int lowGreenSum=0;
                int lowCount=0;
                int upRedSum=0;
                int upGreenSum=0;
                int upBlueSum=0;
                int upCount=0;
                double g=0;
                double maxG=0;
                LinkedList<Color> lowSeedList=new LinkedList<>();
                //遍历像素点。
                for (int i = 0; i < width; i++) {
                    for (int j = 0; j < height; j++) {
                        Color popColor = colorArray[j][i];
                        //getdiatance用于设置标记，因为根据不同阈值计算最佳阈值时，种子点未变，所以所有点到他的距离不变，距离只用计算一次，提高效率。
                        if (!getDistance) {
                            int distance = (int) (Math.pow(seed.getRed() - popColor.getRed(), 2) +
                                    Math.pow(seed.getGreen() - popColor.getGreen(), 2) +
                                    Math.pow(seed.getBlue() - popColor.getBlue(), 2));
                            distanceArray[j][i] = distance;
                        }
                        //距离小于阈值时处理。
                        if (distanceArray[j][i] < threshold) {
                            lowCount++;
                            lowRedSum += popColor.getRed();
                            lowGreenSum += popColor.getGreen();
                            lowBlueSum += popColor.getBlue();
                            lowSeedList.add(popColor);
                        } else {
                            //距离大于阈值时处理。
                            upCount++;
                            upRedSum += popColor.getRed();
                            upGreenSum += popColor.getGreen();
                            upBlueSum += popColor.getBlue();
                        }
                    }
                }
                //计算类间反差与类内方差的最大比值。
                if (lowCount!=0&&upCount!=0) {
                    int avgRed = (lowRedSum + upRedSum) / (width * height);
                    int avgGreen = (lowGreenSum + upGreenSum) / (width * height);
                    int avgBlue = (lowBlueSum + upBlueSum) / (width * height);
                    int lowAvgRed = lowRedSum / lowCount;
                    int lowAvgGreen = lowGreenSum / lowCount;
                    int lowAvgBlue = lowBlueSum / lowCount;
                    int upAvgRed = upRedSum / upCount;
                    int upAvgGreen = upGreenSum / upCount;
                    int upAvgBlue = upBlueSum / upCount;
                    int lowPowSum=0;
                    //计算类间方差。
                    int variance_1  = (int) (((double) lowCount / (width * height))*(Math.pow(avgRed - lowAvgRed, 2) + Math.pow(avgGreen - lowAvgGreen, 2) + Math.pow(avgBlue - lowAvgBlue, 2)) +((double)upCount/(width * height))*(Math.pow(avgRed - upAvgRed, 2) +Math.pow(avgGreen - upAvgGreen, 2) + Math.pow(avgBlue - upAvgBlue, 2)));
                    //计算类内方差。
                    for (Color color :lowSeedList
                    ) {
                        lowPowSum+=(int)(Math.pow(color.getRed()-lowAvgRed,2)+Math.pow(color.getGreen()-lowAvgGreen,2)+Math.pow(color.getBlue()-lowAvgBlue,2));
                    }
                    //计算二者比值。
                    int variance_2=lowPowSum/lowCount;

                    if (variance_2!=0) {
                        if ((g = (variance_1 )/variance_2) > maxG) {
                            maxG = g;
                            bestThreshold = threshold;
                        }
                    }
                }
                //当第一次计算到此处时，种子点与所有像素点已经计算完毕，后续对于该种子点可以不同再计算。
                getDistance=true;
            }
            //设置最佳阈值。
            seed.setBestThreshhold(bestThreshold-10000);
        }
    }
}
