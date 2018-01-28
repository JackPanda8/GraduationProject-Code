import java.io.IOException;
import java.util.*;

public class Main {

//    public static void main(String[] args) throws IOException {
//        BPNetwork bp = new BPNetwork(32, 15, 4, 0.05);
//
//        Random random = new Random();
//
//        List<Integer> list = new ArrayList<Integer>();
//        for (int i = 0; i != 6000; i++) {
//            int value = random.nextInt();
//            list.add(value);
//        }
//
//        for (int i = 0; i !=25; i++) {
//            for (int value : list) {
//                double[] real = new double[4];
//                if (value >= 0)
//                    if ((value & 1) == 1)
//                        real[0] = 1;
//                    else
//                        real[1] = 1;
//                else if ((value & 1) == 1)
//                    real[2] = 1;
//                else
//                    real[3] = 1;
//
//                double[] binary = new double[32];
//                int index = 31;
//                do {
//                    binary[index--] = (value & 1);
//                    value >>>= 1;
//                } while (value != 0);
//
//                bp.train(binary, real);
//
//
//
//            }
//        }
//
//
//
//
//        System.out.println("训练完毕，下面请输入一个任意数字，神经网络将自动判断它是正数还是复数，奇数还是偶数。");
//
//        while (true) {
//
//            byte[] input = new byte[10];
//            System.in.read(input);
//            Integer value = Integer.parseInt(new String(input).trim());
//            int rawVal = value;
//            double[] binary = new double[32];
//            int index = 31;
//            do {
//                binary[index--] = (value & 1);
//                value >>>= 1;
//            } while (value != 0);
//
//            double[] result =new double[4];
//            bp.predict(binary,result);
//
//
//            double max = -Integer.MIN_VALUE;
//            int idx = -1;
//
//            for (int i = 0; i != result.length; i++) {
//                if (result[i] > max) {
//                    max = result[i];
//                    idx = i;
//                }
//            }
//
//            switch (idx) {
//                case 0:
//                    System.out.format("%d是一个正奇数\n", rawVal);
//                    break;
//                case 1:
//                    System.out.format("%d是一个正偶数\n", rawVal);
//                    break;
//                case 2:
//                    System.out.format("%d是一个负奇数\n", rawVal);
//                    break;
//                case 3:
//                    System.out.format("%d是一个负偶数\n", rawVal);
//                    break;
//            }
//        }
//    }


    public static void main(String[] args) {
        //初始化神经网络的基本配置
        //第一个参数是一个整型数组，表示神经网络的层数和每层节点数，比如{3,10,10,10,10,2}表示输入层是3个节点，输出层是2个节点，中间有4层隐含层，每层10个节点
        //第二个参数是学习步长，第三个参数是动量系数
        Data data = new Data();
        int inputNumber = data.getFieldsNumber();
        int hiddenNumber =
        BPDeep bp = new BPDeep(new int[]{,10,2}, 0.15, 0.8);

        //设置样本数据，对应上面的4个二维坐标数据
        double[][] data = new double[][]{{1,2},{2,2},{1,1},{2,1}};
        //设置目标数据，对应4个坐标数据的分类
        double[][] target = new double[][]{{1,0},{0,1},{0,1},{1,0}};

        //迭代训练5000次
        for(int n=0;n<5000;n++)
            for(int i=0;i<data.length;i++)
                bp.train(data[i], target[i]);

        //根据训练结果来检验样本数据
        for(int j=0;j<data.length;j++){
            double[] result = bp.computeOut(data[j]);
            System.out.println(Arrays.toString(data[j])+":"+Arrays.toString(result));
        }

        //根据训练结果来预测一条新数据的分类
        double[] x = new double[]{3,1};
        double[] result = bp.computeOut(x);
        System.out.println(Arrays.toString(x)+":"+Arrays.toString(result));
    }

    //求隐含层神经元个数
    private int getHiddenNumber(int inputNumber) {

    }

}
