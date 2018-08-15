package Grown;

/**
 * @创建人：黄强
 * @时间 ：2018/8/4 11:03
 * @描述 ：
 */
public class Area {
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setRedSum(int redSum) {
        this.redSum = redSum;
    }

    public void setGreenSum(int greenSum) {
        this.greenSum = greenSum;
    }

    public void setBlueSum(int blueSum) {
        this.blueSum = blueSum;
    }

    private int quantity;
    private  int redSum;
    private int greenSum;
    private int blueSum;





    public int getQuantity() {
        return quantity;
    }

    public int getRedSum() {
        return redSum;
    }

    public int getGreenSum() {
        return greenSum;
    }

    public int getBlueSum() {
        return blueSum;
    }

    public int getRedAvg() {

        return redSum/quantity;
    }

    public int getGreenAvg() {
        return greenSum/quantity;
    }

    public int getBlueAvg() {
        return blueSum/quantity;
    }

    public Area(int quantity, int redSum, int greenSum, int blueSum) {
        this.quantity = quantity;
        this.redSum = redSum;
        this.greenSum = greenSum;
        this.blueSum = blueSum;
    }
}
