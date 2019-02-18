package Grown.Version_2;

import java.util.Objects;

/**
 * @创建人：黄强
 * @时间 ：2018/11/22 23:47
 * @描述 ：像素点，包含像素点横纵坐标。
 */
public class Pixel {
    /**纵向 对应height。*/
    private int x;
    /**横向，对应width。*/
    private int y;

    private int value;
    private int k;

    public int getK() {
        return k;
    }

    public void setK(int k) {
        this.k = k;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public Pixel(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Pixel(int x, int y, int value) {
        this.x = x;
        this.y = y;
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {return true;}
        if (o == null || getClass() != o.getClass()) {return false;}
        Pixel pixel = (Pixel) o;
        return x == pixel.x &&
                y == pixel.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }
}
