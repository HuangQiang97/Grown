package Grown;

import java.util.Objects;
import java.util.Stack;

/**
 * @创建人：黄强
 * @时间 ：2018/7/25 11:28
 * @描述 ：种子点结构。
 */
public class Seed {
    //坐标，RGB。
    private int x,y;
    private int  RGB;
    //最佳阈值。
    private int bestThreshold;
    //存储与该点可以被归为一类的点。
    Stack<Seed> sameClassSeed=new Stack<>();
//构造方法。
public Seed(int x, int y, int RGB) {
    this.x = x;
    this.y = y;
    this.RGB = RGB;
}
    public Seed (){}
//获得R，G，B值。
public int  getRed(){
    return (int)((RGB&0xff0000)>>16);
}
    public int getGreen(){
        return (int)((RGB&0xff00)>>8);
    }
    public int getBlue(){
        return  (int)(RGB&0xff);
    }
//获得坐标。
public int getX() {
    return x;
}
//设置坐标。
public void setX(int x) {
    this.x = x;
}

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
//设置、获得ＲＧＢ .
public long getRGB() {
    return RGB;
}

    public void setRGB(int  RGB) {
        this.RGB = RGB;
    }
//设置获得最佳阈值。
public void setBestThreshhold(int bestThreshhold) {
    this.bestThreshold = bestThreshhold;
}

    public double getBestThreshold() {
        return bestThreshold;
    }

    //重写equals方法，如果两个点坐标相同就是同一个点。
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Seed seed = (Seed) o;
        return x == seed.x &&
                y == seed.y;
    }

    //重写hashcode。依据坐标计算hash值。
    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

}
