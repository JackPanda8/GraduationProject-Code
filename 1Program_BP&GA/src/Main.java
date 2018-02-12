import java.util.*;
import com.sun.javaws.exceptions.ExitException;
import java.io.IOException;

public class Main extends GeneticAlgorithm{
    private static final float PORTION = 0.3f;
    private static final int TRAINING_NUMBER = 3000;
    private static final float THRESHOLD = 0.8f;
    private static final int POPULATION_SIZE = 20;//种群数量设置

    private ArrayList<BPDeep> bpList;//神经网络组成的种群
    private ArrayList<Double> fitnessList;//种群中的每一个神经网络对应的适应度函数

    private HashMap<Double, Double> expectedAndActual;//key：期望输出 value：实际输出

    public Main() {
        super(10000, 100, 500, 0.01, 0.3, 0.01, 0.6, 0.3, 0.01);
    }

    public static void main(String[] args) {
        Main main = new Main();

        //读数据
        Data data = new Data();
        try {
            data.getTrainData(PORTION);
        } catch (Exception e) {
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
        for (int i = 0; i < inputSize; i++) {
            for (int j = 0; j < inputLayerNumber; j++) {
                inputdata[i][j] = trainInput[i].getAttributeByIndex(j);
            }
        }

        double[] trainTarget = data.getTrainTarget();
        int targetSize = trainTarget.length;
        double[][] target = new double[targetSize][2];
        for (int i = 0; i < targetSize; i++) {
            //（1,0）代表相似，（0,1）代表不相似
            if (trainTarget[i] == 1.0) {
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

        //【1】随机初始化POPULATION_SIZE个BP网络；并进行迭代训练
        main.bpList = new ArrayList<BPDeep>(POPULATION_SIZE);
        for(int i = 0; i < POPULATION_SIZE; i++) {
            BPDeep bp = new BPDeep(new int[]{inputLayerNumber, hiddenLayerNumber, 1}, 0.15, 0.8);
            for (int n = 0; n < TRAINING_NUMBER; n++) {
                for (int j = 0; j < inputSize; j++) {
                    bp.train(inputdata[j], target[j]);
                }
            }

            main.bpList.add(bp);
        }

        //由bpList初始化种群
        ArrayList<Chromosome> population = new ArrayList<Chromosome>();
        

        //【2】使用GA算法进行种群调整
        main.expectedAndActual = new HashMap<Double, Double>();
        for(BPDeep bpDeep : main.bpList) {
            main.expectedAndActual.clear();
            for(int j=0;j<inputSize;j++){
                double[] result = bpDeep.computeOut(inputdata[j]);
                ArrayList<People> tempData = data.getDataset();

                int index1 = j/data.getDataset().size();
                int index2 = j - index1*data.getDataset().size();
                People p1 = tempData.get(index1);
                People p2 = tempData.get(index2);
                String s1 = p1.getRec_id();
                String s2 = p2.getRec_id();
                String[] array1 = s1.split("-");
                String[] array2 = s2.split("-");
                //得到该染色体的实际输出和理论输出
                if(array1[1].equals(array2[1])) {
                    double actual = (result[0] > THRESHOLD)?(1.0):(0.0);
                    main.expectedAndActual.put(1.0, actual);
                } else {
                    double actual = (result[0] < 1-THRESHOLD)?(0.0):(1.0);
                    main.expectedAndActual.put(0.0, actual);
                }
            }


            main.caculate(population);

        }

        //【3】再次对BP神经网络进行迭代训练
        for (int n = 0; n < TRAINING_NUMBER; n++) {
            for (int i = 0; i < inputSize; i++) {
                bp.train(inputdata[i], target[i]);
            }
        }

        //【4】使用完整数据集进行检测
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

        int ecount = 0;
        int ucount = 0;
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

    }

    /* 重写适应度函数的实现:f = 1/(1+E), E是全局误差*/
    @Override
    public double caculateY(Chromosome x) {
        // TODO Auto-generated method stub
        double E = 0.0;
        for(double key : expectedAndActual.keySet()) {
            double value = expectedAndActual.get(key);
            E += (key - value)*(key - value);
        }
        return 1.0/(1.0 + E);
    }

}
