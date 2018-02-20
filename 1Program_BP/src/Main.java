import java.util.*;

public class Main {

    private static final float PORTION = 0.3f;
    private static final int TRAINING_NUMBER = 100;
    private static final float THRESHOLD = 0.8f;

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


        //设置样本数据，对应上面的4个二维坐标数据
//        double[][] data = new double[][]{{1,2},{2,2},{1,1},{2,1}};
        //设置目标数据，对应4个坐标数据的分类
//        double[][] target = new double[][]{{1,0},{0,1},{0,1},{1,0}};

        //迭代训练5000次
        for(int n=0;n<TRAINING_NUMBER;n++)
            for(int i=0;i<inputSize;i++)
                bp.train(inputdata[i], target[i]);

        int ecount = 0;
        int ucount = 0;


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

        //衡量算法的查全率和查准率以及误识别率
        int actualTotalDup = 0;//实际的重复数
        int totalDup = 0;//算法的总识别数
        int trueDup = 0;//算法的正确识别数目
        int falseDup = 0;//算法的错误识别数

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
                } else {
                    trueDup++;
                }

                actualTotalDup++;
            } else {
                String OK = (result[0] < 1-THRESHOLD)?("OK"):("!!!");
                if(OK.equals("!!!")) {
                    System.out.println("[不相等]"+OK+"   "+Arrays.toString(result) + "   "+Arrays.toString(testInputData[j]));
                    ucount++;
                    falseDup++;
                }
            }

        }

        totalDup = trueDup + falseDup;

        double chaquanlv = ((double)trueDup)/((double)actualTotalDup) * 100.0;
        double chazhunlv = ((double)trueDup)/((double)totalDup) * 100.0;
        double wushibielv = ((double)falseDup)/((double)totalDup) * 100.0;

        System.out.println("BP算法的查全率为："+chaquanlv+"，查准率为："+chazhunlv+"，误识别率为："+wushibielv);


    }


}
