package Grown;

import java.util.LinkedList;

/**
 * @创建人：黄强
 * @时间 ：2018/8/4 11:22
 * @描述 ：对图像中区域进行合并。
 */
public class Combine {
    LinkedList<Area> areaLinkedList;
    byte[][]printMap;
    byte[]combineAreaMap;
    LinkedList<LinkedList<Seed>> edgeLineList;
    int [][]distanceArray;
    int[][] comprehensiveDistance;

    /*

     *@描述 :利用构造方法进行数据初始化。
     *@参数 :[areaLinkedList, printMap, edgeLineList]
     *@返回值:
     *@创建人 : 黄强
     *@创建时间  2018/8/13 0:20
     *@修改人和其它信息：
     *@版本：

     */
    public Combine(LinkedList<Area>areaLinkedList,byte[][]printMap,LinkedList<LinkedList<Seed>> edgeLineList){

        this.areaLinkedList=areaLinkedList;
        this.printMap=printMap;
        combineAreaMap=new byte[areaLinkedList.size()];
        this.edgeLineList=edgeLineList;
        this.distanceArray=new int[areaLinkedList.size()][areaLinkedList.size()];
        this.comprehensiveDistance=new int [edgeLineList.size()][edgeLineList.size()];
    }


    /*
     *
     *@描述 :获得邻接表。
     *@参数 :[nearLineArray]
     *@返回值:byte[][]
     *@创建人 : 黄强
     *@创建时间  2018/8/5 21:06
     *@修改人和其它信息：
     *@版本：
     */
    public byte [][] getNearMap(LinkedList<LinkedList<Seed>>nearLineList){

        int listLength=nearLineList.size();
        byte[][]nearAreaMap=new byte[listLength][listLength];
        for (int i=0;i<listLength;i++ ){
            for (int j=i+1;j<listLength;j++){
                breakPoint:{

                for (Seed edgeSeed:nearLineList.get(i)
                ) {
                    if (nearLineList.get(j).indexOf(edgeSeed) != -1) {
                        nearAreaMap[i][j] = 1;
                        nearAreaMap[j][i] = 1;
                        break;
                    }

//                    for (Seed seed:nearLineList.get(j)
//                         ) {
//                        if (seed ==edgeSeed){
//                            nearAreaMap[i][j]=1;
//                            nearAreaMap[j][i]=1;
//                            break breakPoint;
//
//                        }
//                    }
                }



                }
            }
        }
        return nearAreaMap;
    }


    /*

     *@描述 :合并过小区域。
     *@参数 :[nearMap]
     *@返回值:void
     *@创建人 : 黄强
     *@创建时间  2018/8/11 16:46
     *@修改人和其它信息：
     *@版本：

     */
    public void combineSmallArea(byte[][]nearMap){
        //遍历寻找较小区域。

        for (int i=0;i<areaLinkedList.size();i++) {
            if (combineAreaMap[i] == 0&&areaLinkedList.get(i).getQuantity() < Util.width * Util.height / 1500) {
                for (int j = 0; j < areaLinkedList.size(); j++) {
                    Area area_0 = areaLinkedList.get(i);
                    Area area_1 = areaLinkedList.get(j);
                    //在区域的四周寻找比他大，并且未被合并的点并入其中。
                    if (nearMap[i][j] == 1&&combineAreaMap[j]==0&&area_0.getQuantity()<=area_1.getQuantity()) {
                        area_1.setQuantity(area_0.getQuantity() + area_1.getQuantity());
                        area_1.setRedSum(area_0.getRedSum() + area_1.getRedSum());
                        area_1.setBlueSum(area_0.getBlueSum() + area_1.getBlueSum());
                        area_1.setGreenSum(area_0.getGreenSum() + area_1.getGreenSum());
                        //标记小区域被合并。
                        combineAreaMap[i] = 1;
                        for (int k=0;k<areaLinkedList.size();k++){
                            nearMap[i][k]=0;
                            nearMap[k][i]=0;
                        }
                        break;
                    }
                }
            }
        }
    }
    /*
     *
     *@描述 :获得区域间阈值。
     *@参数 :[]
     *@返回值:int
     *@创建人 : 黄强
     *@创建时间  2018/8/5 21:06
     *@修改人和其它信息：
     *@版本：
     */
    public int getThreshold (){

        int listSize=areaLinkedList.size();
        int distanceSum=0;
        int powSum=0;
        //距离矩阵跳过已经合并过的区域。

        for (int i=0;i<listSize;i++){
            for (int k=i+1;k<listSize;k++) {
                if (combineAreaMap[i] != 1 && combineAreaMap[k] != 1) {
                    distanceArray[i][k] = (int) (Math.sqrt(Math.pow(areaLinkedList.get(i).getRedAvg() - areaLinkedList.get(k).getRedAvg(), 2) +
                            Math.pow(areaLinkedList.get(i).getGreenAvg() - areaLinkedList.get(k).getGreenAvg(), 2) +
                            Math.pow(areaLinkedList.get(i).getBlueAvg() - areaLinkedList.get(k).getBlueAvg(), 2)));
                    distanceArray[k][i] = distanceArray[i][k];
                    distanceSum += distanceArray[i][k];
                }
            }
        }
        //统计小区域合并后剩余区域数。
        int totalEle=0;
        for (byte near:combineAreaMap
             ) {
            if (near!=1){
                totalEle++;
            }
        }
        int distanceAvg=distanceSum/((listSize*(listSize-1)/2)-totalEle);

        //计算方差。
        for (int i=0;i<listSize;i++){
            for (int k=i+1;k<listSize;k++) {
                powSum+= Math.pow(distanceArray[i][k]-distanceAvg,2);
            }
        }


        //返回区域间阈值。
        return distanceAvg-(int)(0.4*Math.sqrt(powSum/((listSize*(listSize-1)/2)-totalEle)));

    }
/**

*@描述 :初始化综合距离数组。
*@参数 :[T, nearMap]
*@返回值:void
*@创建人 : 黄强
*@创建时间  2018/8/13 13:57
*@修改人和其它信息：
*@版本：

*/

    public void initDistanceArray(int T,byte[][]nearMap){
        int areaSize=nearMap.length;
        for (int i=0;i<areaSize;i++){
            for (int j=i+1;j<areaSize;j++){
                if (nearMap[i][j]==1&&distanceArray[i][j]<T){
                    comprehensiveDistance[i][j]=distanceArray[i][j]*((areaLinkedList.get(i).getQuantity()*areaLinkedList.get(j).getQuantity())
                            /(areaLinkedList.get(i).getQuantity()+areaLinkedList.get(j).getQuantity()));
                    comprehensiveDistance[j][i]=comprehensiveDistance[i][j];
                }
            }
        }
    }



   /**

   *@描述 :合并相邻并且距离小于区域间阈值的区域。
   *@参数 :[T, nearMap]
   *@返回值:void
   *@创建人 : 黄强
   *@创建时间  2018/8/13 12:50
   *@修改人和其它信息：
   *@版本：

   */

    public void combineArea(int T,byte[][]nearMap){


        int areaSize=distanceArray.length;
        int min=Integer.MAX_VALUE;
        int  area_1Index=-1;
        int  area_2Index=-1;
        //寻找综合距离最小的两相邻区域。
       // int[][] comprehensiveDistance=new int [areaSize][areaSize];
        for (int i=0;i<areaSize;i++){

            for (int j=i+1;j<areaSize;j++){

                if (nearMap[i][j]==1&&distanceArray[i][j]<T){

                    comprehensiveDistance[i][j]=distanceArray[i][j]*((areaLinkedList.get(i).getQuantity()*areaLinkedList.get(j).getQuantity())
                            /(areaLinkedList.get(i).getQuantity()+areaLinkedList.get(j).getQuantity()));
                    comprehensiveDistance[j][i]=comprehensiveDistance[i][j];
                    if (min>comprehensiveDistance[i][j])
                    {
                        area_1Index=i;
                        area_2Index=j;
                        min=comprehensiveDistance[i][j];
                    }
                }
            }
        }
        //对两目标区域进行合并。
        if (area_1Index!=-1&&area_2Index!=-1){
            Area area_1=areaLinkedList.get(area_1Index);
            Area area_2=areaLinkedList.get(area_2Index);
            //假定区域1为大区域，把区域2合并如区域1 .

            if (area_1.getQuantity()<area_2.getQuantity()){
                Area tempArea=area_1;
                int tempIndex=area_1Index;
                area_1=area_2;
                area_2=tempArea;
                area_1Index=area_2Index;
                area_2Index=tempIndex;

            }
            //把区域2并入区域1.
            area_1.setQuantity(area_2.getQuantity() + area_1.getQuantity());
            area_1.setRedSum(area_2.getRedSum() + area_1.getRedSum());
            area_1.setBlueSum(area_2.getBlueSum() + area_1.getBlueSum());
            area_1.setGreenSum(area_2.getGreenSum() + area_1.getGreenSum());

            //标记区域2已经并入区域1.
            combineAreaMap[area_2Index] = 1;
            for (int k=0;k<areaLinkedList.size();k++){
                nearMap[area_2Index][k]=0;
                nearMap[k][area_2Index]=0;
            }
            //由于区域2并入区域1，区域1发生变化，需要重新计算区域1到其他区域距离。
            for (int i=0;i<area_1Index;i++){
                distanceArray[i][area_1Index]=(int) (Math.sqrt(Math.pow(areaLinkedList.get(i).getRedAvg()- areaLinkedList.get(area_1Index).getRedAvg(),2)+
                        Math.pow(areaLinkedList.get(i).getGreenAvg()- areaLinkedList.get(area_1Index).getGreenAvg(),2)+
                        Math.pow(areaLinkedList.get(i).getBlueAvg()- areaLinkedList.get(area_1Index).getBlueAvg(),2)));
                distanceArray[area_1Index][i]=distanceArray[i][area_1Index];
            }
            //取消两合并区域间的着色点。
            for (Seed seed_1:edgeLineList.get(area_2Index)
                 ) {
                for (Seed seed_2 : edgeLineList.get(area_1Index)
                ) {
                    if (seed_1 == seed_2) {
                        printMap[seed_1.getX()][seed_1.getY()] = 0;
                        break;
                    }
                }
            }

//
//                if (edgeLineList.get(area_1Index).indexOf(seed)!=-1){
//                    printMap[seed.getX()][seed.getY()]=0;
//                }


//
//            for (int p=0;p<edgeLineList.get(area_2Index).size();p++){
//                Seed seed=edgeLineList.get(area_2Index).get(p);
//
//            }


            //如果还有点相邻并且距离小于阈值，递归合并。
            for (int m=0;m<areaLinkedList.size();m++){
                for (int n=m+1;n<areaLinkedList.size();n++){
                    if (distanceArray[m][n]<T&&nearMap[m][n]==1&&combineAreaMap[m]==0&&combineAreaMap[n]==0){
                        combineArea(T,nearMap);
                    }
                }
            }
        }
    }
}
