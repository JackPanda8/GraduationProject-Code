import java.util.*;
import com.sun.javaws.exceptions.ExitException;
import java.io.IOException;

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

    private static final float PORTION = 0.3f;
    private static final int TRAINING_NUMBER = 3000;
    private static final float THRESHOLD = 0.8f;
    private ArrayList<BPDeep> bpList;

    public static void main(String[] args) {
        Data data = new Data();
        try {
            data.getTrainData(PORTION);
        }catch (Exception e) {
            e.printStackTrace();
        }
        int inputLayerNumber = data.getFieldsNumber();
        int hiddenLayerNumber = data.getHiddenNumber();
        int recordsNumber = data.getTrainRecord().size();

        //设置样本数据，对应上面的4个二维坐标数据
//        double[][] data = new double[][]{{1,2},{2,2},{1,1},{2,1}};
        //设置目标数据，对应4个坐标数据的分类
//        double[][] target = new double[][]{{1,0},{0,1},{0,1},{1,0}};
        FieldVector[] trainInput = data.getTrainInput();
        int inputSize = trainInput.length;
        double[][] inputdata = new double[inputSize][inputLayerNumber];
        for(int i = 0; i < inputSize; i++) {
            for(int j = 0; j < inputLayerNumber; j++) {
                inputdata[i][j] = trainInput[i].getAttributeByIndex(j);
            }
        }

        double[] trainTarget = data.getTrainTarget();
        int targetSize = trainTarget.length;
        double[][] target = new double[targetSize][2];
        for(int i = 0; i < targetSize; i++) {
            //（1,0）代表相似，（0,1）代表不相似
            if(trainTarget[i] == 1.0) {
                target[i][0] = 1.0;
                target[i][1] = 0.0;
            } else {
                target[i][0] = 0.0;
                target[i][1] = 1.0;
            }

        }

        //初始化神经网络的基本配置
        //第一个参数是一个整型数组，表示神经网络的层数和每层节点数，比如{3,10,10,10,10,2}表示输入层是3个节点，输出层是2个节点，中间有4层隐含层，每层10个节点
        //第二个参数是学习步长，第三个参数是动量系数
        BPDeep bp = new BPDeep(new int[]{inputLayerNumber,hiddenLayerNumber,1}, 0.15, 0.8);

        //迭代训练5000次
        for(int n=0;n<TRAINING_NUMBER;n++)
            for(int i=0;i<inputSize;i++)
                bp.train(inputdata[i], target[i]);

        int ecount = 0;
        int ucount = 0;

        //根据训练结果来检验样本数据
//        for(int j=0;j<inputSize;j++){
//            double[] result = bp.computeOut(inputdata[j]);
//            ArrayList<People> tempData = data.getTrainRecord();
//
//            int index1 = j/recordsNumber;
//            int index2 = j - index1*recordsNumber;
//            People p1 = tempData.get(index1);
//            People p2 = tempData.get(index2);
//            String s1 = p1.getRec_id();
//            String s2 = p2.getRec_id();
//            String[] array1 = s1.split("-");
//            String[] array2 = s2.split("-");
//            if(array1[1].equals(array2[1])) {
////                String OK = (result[0] > 0.9)?("OK"):("!!!");
////                System.out.println("[相等]"+OK+"   "+Arrays.toString(result) + "   "+Arrays.toString(inputdata[j]));
//                ecount++;
//            } else {
//                String OK = (result[0] < 0.5)?("OK"):("!!!");
//                if(OK.equals("!!!")) {
//                    System.out.println("[不相等]"+OK+"   "+Arrays.toString(result) + "   "+Arrays.toString(inputdata[j]));
//                }
//
//                ucount++;
//
//            }
//
//
//        }



        //根据完整数据集进行检测
        try {
            data.getTestData();
        }catch (Exception e) {
            e.printStackTrace();
        }
        FieldVector[] testInput = data.getInput();
        int testSize = testInput.length;
        double[][] testInputData = new double[testSize][inputLayerNumber];
        for(int i = 0; i < testSize; i++) {
            for(int j = 0; j < inputLayerNumber; j++) {
                testInputData[i][j] = testInput[i].getAttributeByIndex(j);
            }
        }

        double[] testTarget = data.getTarget();
        int testTargetSize = testTarget.length;
        double[][] testTargetData = new double[testTargetSize][2];
        for(int i = 0; i < testTargetSize; i++) {
            //（1,0）代表相似，（0,1）代表不相似
            if(testTarget[i] == 1.0) {
                testTargetData[i][0] = 1.0;
                testTargetData[i][1] = 0.0;
            } else {
                testTargetData[i][0] = 0.0;
                testTargetData[i][1] = 1.0;
            }

        }

        for(int j=0;j<testSize;j++){
            double[] result = bp.computeOut(testInputData[j]);
            ArrayList<People> tempData = data.getDataset();

            int index1 = j/data.getDataset().size();
            int index2 = j - index1*data.getDataset().size();
            People p1 = tempData.get(index1);
            People p2 = tempData.get(index2);
            String s1 = p1.getRec_id();
            String s2 = p2.getRec_id();
            String[] array1 = s1.split("-");
            String[] array2 = s2.split("-");
            if(array1[1].equals(array2[1])) {
                String OK = (result[0] > THRESHOLD)?("OK"):("!!!");
                if(OK.equals("!!!")) {
                    System.out.println("[相等]"+OK+"   "+Arrays.toString(result) + "   "+Arrays.toString(testInputData[j]));
                    ecount++;

                }
            } else {
                String OK = (result[0] < 1-THRESHOLD)?("OK"):("!!!");
                if(OK.equals("!!!")) {
                    System.out.println("[不相等]"+OK+"   "+Arrays.toString(result) + "   "+Arrays.toString(testInputData[j]));
                    ucount++;

                }
            }

        }

//        //根据训练结果来预测一条新数据的分类
//        double[] x = new double[]{3,1};
//        double[] result = bp.computeOut(x);
//        System.out.println(Arrays.toString(x)+":"+Arrays.toString(result));

        System.out.println();

    }


}
