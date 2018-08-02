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
 * @时间 ：2018/7/25 11:41
 * @描述 ：利用canopy算法初步统计类块数。
 */
public class DivideClass {

    //此处把T1，T2定义为静态变量以便以后计算同一类而不相链接的点时调用。
    static int T1;
    static int T2;

    public DivideClass(int T1,int T2) {
        //构造方法初始化T1，T2。
        this.T1=T1;
        this.T2=T2;
    }

    /*
     *
     *@描述 :划分类别。
     *@参数 :[colorArray]
     *@返回值:java.util.LinkedList<Grown.Seed>
     *@创建人 : 黄强
     *@创建时间  2018/8/1 20:23
     *@修改人和其它信息：
     *@版本：
     */
    public LinkedList<Seed> divideClass(Color[][]colorArray){

        int index=0;
        int width= Util.width;
        int height= Util.height;
        LinkedList <Seed> seedList=new LinkedList<>();
        while (index<width*height){
            //便利所有像素点。
            int x=index/width;
            int y=index%width;
            Color popColor=colorArray[x][y];
            //种子点为空时处理。
            if (seedList.isEmpty()){
                seedList.add(new Seed(x,y,popColor.getRGB()));
            }else
                //设置断点。
                breakPoint: {
                    //设置标记该点能否归入某canopy。
                    byte belongCanopyFlag = 0;
                    //遍历种子点对弹出像素点归类。
                    for (Seed seed:seedList
                    ) {
                        int distance = (int)(Math.pow(seed.getRed() - popColor.getRed(), 2) +
                                Math.pow(seed.getGreen() - popColor.getGreen(), 2) +
                                Math.pow(seed.getBlue() - popColor.getBlue(), 2));
                        //当距离小于T2时，该点到种子点已经足够近，可以归为一类，并且跳出对种子链表的遍历。
                        if (distance < T2) {
                            seed.sameClassSeed.add(new Seed(x,y,popColor.getRGB()));
                            break breakPoint;
                            //当距离小于T1，该点可以与种子点归为同一canopy。
                        } else if (distance < T1) {
                            belongCanopyFlag = 1;
                        }
                    }//若到此时还未跳出遍历，该点不能与任一种子点归为一类，此时判断他是否能归为某一个canopy。
                    if (belongCanopyFlag==0){
                        //不属于任一canopy可以作为一个全新的种子点。
                        seedList.add(new Seed(x,y,popColor.getRGB()));
                    }
                }
            index++;
        }
        System.out.println("原始种子点数量:"+seedList.size()+"\n种子点信息:x y RGB");

        //输出分类完成的显色图像。
        BufferedImage outputImage=new BufferedImage(100*seedList.size(),1000,BufferedImage.TYPE_3BYTE_BGR);
        for (int k=0;k<seedList.size();k++){
            for (int p=100*k;p<100*k+100;p++){
                for (int q=0;q<1000;q++){
                    outputImage.setRGB(p,q,(int)seedList.get(k).getRGB());
                }
            }
        }
        try {
            ImageIO.write(outputImage,"jpg",new File("Result\\sortedColor\\"+
                    new Date().getHours()+new Date().getMinutes()+".jpg"
            ));
        } catch (IOException e) {
            e.printStackTrace();
            throw  new RuntimeException("\n颜色分类文件无法输出！\n");
        }
        return seedList;
    }
}
