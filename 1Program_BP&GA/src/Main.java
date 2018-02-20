import java.util.*;

public class Main extends GeneticAlgorithm{
    private static final float PORTION = 0.3f;//神经网络的训练数据集占总数据集的比例
    private static final int TRAINING_NUMBER = 100;//神经网络的训练次数
    private static final double LEARNING_RATE = 0.15;//学习系数
    private static final double MOBP = 0.8;//动量系数
    private static final float THRESHOLD = 0.8f;//神经网络的输出层阈值

    private static final int POPULATION_SIZE = 3;//种群数量设置
    private static final int MAX_ITER_NUM = 100;//最大迭代次数
    private static final double MUTATION_RATE = 0.01;//变异概率
    private static final double MUTATION_MAX_SCALE = 0.3;//最大变异率
    private static final double MUTATION_MINSCALE = 0.6;//最小变异率
    private static final double CROSSOVER_RATE = 0.6;//交叉概率
    private static final double CROSSOVER_MAX_SCALE = 0.3;//最大交叉率
    private static final double CROSSOVER_MIN_SCALE = 0.01;//最小交叉率

    private ArrayList<BPDeep> bpList;//神经网络组成的种群
    private ArrayList<Chromosome> population;//种群
    public static int geneSize;//染色体中基因的长度
    private ArrayList<HashMap<Double, Double>> expectedAndActual;//种群中染色体对应的期望输出与实际输出，key：期望输出 value：实际输出

    public Main() {
        super(geneSize, POPULATION_SIZE, MAX_ITER_NUM, MUTATION_RATE, MUTATION_MAX_SCALE, MUTATION_MINSCALE, CROSSOVER_RATE, CROSSOVER_MAX_SCALE, CROSSOVER_MIN_SCALE);
    }

    public static void main(String[] args) {
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

        Main.geneSize = (inputLayerNumber+1)*hiddenLayerNumber + hiddenLayerNumber+1 + 1;
        Main main = new Main();

        //初始化神经网络的基本配置
        //第一个参数是一个整型数组，表示神经网络的层数和每层节点数，比如{3,10,10,10,10,2}表示
        // 输入层是3个节点，输出层是2个节点，中间有4层隐含层，每层10个节点, 第二个参数是学习步长，第三个参数是动量系数


        //【1】随机初始化POPULATION_SIZE个BP网络；并进行迭代训练
        main.bpList = new ArrayList<BPDeep>(POPULATION_SIZE);
        for(int i = 0; i < POPULATION_SIZE; i++) {
            BPDeep bp = new BPDeep(new int[]{inputLayerNumber, hiddenLayerNumber, 1}, LEARNING_RATE, MOBP);
            for (int n = 0; n < TRAINING_NUMBER; n++) {
                for (int j = 0; j < inputSize; j++) {
                    bp.train(inputdata[j], target[j]);
                }
            }

            main.bpList.add(bp);
        }

        //由bpList初始化种群
        main.population = new ArrayList<Chromosome>(POPULATION_SIZE);
        for(BPDeep bpDeep : main.bpList) {
             int geneNumber = 0;
             int layerInputNum = bpDeep.layernum[0] + 1;
             int layerHiddenNum = bpDeep.layernum[1] + 1;
             int layerOutputNum = 1;
            // 由输入层和隐含层连接权值、隐含层阈值、隐含层与输出层连接权值以及输出层阈值4个部分组成。【实际上我的程序只有三个部分】
             geneNumber = layerInputNum*(layerHiddenNum - 1) + layerHiddenNum*1 + 1;
             ArrayList<Float> temp = new ArrayList<Float>();
             //得到输入层与隐含层之间的边的权值
             for(int i = 0; i < layerInputNum; i++) {
                 for(int j = 0; j < layerHiddenNum-1; j++) {
                     float weight = (float)bpDeep.layer_weight[0][i][j];
                     temp.add(weight);
                 }
             }
            //得到隐含层与输出层之间的边的权值
            for(int i = 0; i < layerHiddenNum; i++) {
                for(int j = 0; j < 1; j++) {
                    float weight = (float)bpDeep.layer_weight[1][i][j];
                    temp.add(weight);
                }
            }
            temp.add(THRESHOLD);
            //得到染色体的基因数组
            float[] gene = new float[geneNumber];
            for(int i = 0; i < geneNumber; i++) {
                gene[i] = temp.get(i);
            }
            //初始化染色体
            Chromosome chromosome = new Chromosome(gene);
            main.population.add(chromosome);

        }


        //【2】使用GA算法进行种群调整
        main.expectedAndActual = new ArrayList<HashMap<Double, Double>>(POPULATION_SIZE);
        for(BPDeep bpDeep : main.bpList) {
            HashMap<Double, Double> map = new HashMap<Double, Double>();
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
                    map.put(1.0, actual);
                    main.expectedAndActual.add(map);
                } else {
                    double actual = (result[0] < 1-THRESHOLD)?(0.0):(1.0);
                    map.put(0.0, actual);
                    main.expectedAndActual.add(map);
                }
            }
        }
        //使用GA进行种群的调整，跳出局部最优,最后一代种群中的最优值即为最大值
        main.caculate(main.population);
        Chromosome bestChromosome = main.getX();
        BPDeep bestBP = new BPDeep(new int[]{inputLayerNumber, hiddenLayerNumber, 1}, LEARNING_RATE, MOBP, bestChromosome, inputLayerNumber, hiddenLayerNumber);

        //【3】再次对BP神经网络进行迭代训练
        for(int i = 0; i < POPULATION_SIZE; i++) {
            for (int n = 0; n < TRAINING_NUMBER; n++) {
                for (int j = 0; j < inputSize; j++) {
                    bestBP.train(inputdata[j], target[j]);
                }
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

        //衡量算法的查全率和查准率以及误识别率
        int actualTotalDup = 0;//实际的重复数
        int totalDup = 0;//算法的总识别数
        int trueDup = 0;//算法的正确识别数目
        int falseDup = 0;//算法的错误识别数

        for (int j = 0; j < testSize; j++) {
            double[] result = bestBP.computeOut(testInputData[j]);
            ArrayList<People> tempData = data.getDataset();

            int index1 = j / data.getDataset().size();
            int index2 = j - index1 * data.getDataset().size();
            People p1 = tempData.get(index1);
            People p2 = tempData.get(index2);
            String s1 = p1.getRec_id();
            String s2 = p2.getRec_id();
            String[] array1 = s1.split("-");
            String[] array2 = s2.split("-");
            if (array1[1].equals(array2[1])) {
                String OK = (result[0] > THRESHOLD) ? ("OK") : ("!!!");
//                System.out.println("[相等]" + OK + "   " + Arrays.toString(result) + "   " + Arrays.toString(testInputData[j]));
                if (OK.equals("!!!")) {
                    System.out.println("[相等]" + OK + "   " + Arrays.toString(result) + "   " + Arrays.toString(testInputData[j]));
                } else {
                    trueDup++;
                }
                actualTotalDup++;
            } else {
                String OK = (result[0] < 1 - THRESHOLD) ? ("OK") : ("!!!");
//                System.out.println("[不相等]" + OK + "   " + Arrays.toString(result) + "   " + Arrays.toString(testInputData[j]));
                if (OK.equals("!!!")) {
                    System.out.println("[不相等]" + OK + "   " + Arrays.toString(result) + "   " + Arrays.toString(testInputData[j]));
                    falseDup++;
                } else {
                }
            }
        }


        totalDup = trueDup + falseDup;

        double chaquanlv = ((double)trueDup)/((double)actualTotalDup) * 100.0;
        double chazhunlv = ((double)trueDup)/((double)totalDup) * 100.0;
        double wushibielv = ((double)falseDup)/((double)totalDup) * 100.0;

        System.out.println("BP+BG算法的查全率为："+chaquanlv+"，查准率为："+chazhunlv+"，误识别率为："+wushibielv);

    }

    /* 重写适应度函数的实现:f = 1/(1+E), E是全局误差*/
    @Override
    public double caculateY(Chromosome x) {
        // TODO Auto-generated method stub
        int index = this.population.indexOf(x);
        HashMap<Double, Double> map = this.expectedAndActual.get(index);
        double E = 0.0;

        for(double key : map.keySet()) {
            double value = map.get(key);
            E += (key - value)*(key - value);
        }
        double score = 1.0/(1.0 + E);
//        x.setScore(score);//给染色体x设置适应度值expectedAndActual

        return score;
    }

}
