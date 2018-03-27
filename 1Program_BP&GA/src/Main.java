import java.util.*;

public class Main extends GeneticAlgorithm{
    private static final String FILEPATH = "D:\\毕业设计\\1程序\\TrainedBPNetwork\\";

    private static final float PORTION = 0.003f;//神经网络的训练数据集占总数据集的比例
    private static final int TRAINING_NUMBER = 500;//神经网络的训练次数
    private static final double LEARNING_RATE = 0.15;//学习系数
    private static final double MOBP = 0.9;//动量系数
    private static final float THRESHOLD = 0.8f;//神经网络的输出层阈值

    private static final int POPULATION_SIZE = 15;//种群数量设置
    private static final int MAX_ITER_NUM = 50;//最大迭代次数
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
    private ArrayList<ArrayList<Integer>> duplicateList;

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
        float[] bestGene = bestChromosome.getGene();
        double bestThreshold = bestGene[bestGene.length-1];
        BPDeep bestBP = new BPDeep(new int[]{inputLayerNumber, hiddenLayerNumber, 1}, LEARNING_RATE, MOBP, bestChromosome, inputLayerNumber, hiddenLayerNumber);

        //【3】再次对BP神经网络进行迭代训练
        for(int i = 0; i < POPULATION_SIZE; i++) {
            for (int n = 0; n < TRAINING_NUMBER; n++) {
                for (int j = 0; j < inputSize; j++) {
                    bestBP.train(inputdata[j], target[j]);
                }
            }
        }


        //【3.1】写BP网络的参数以入txt
        FileIO.writeTxtFile(FILEPATH);
        bestgene + 


        //【3.2】读BP从txt
        FileIO.readTxtFile(FILEPATH);


        //【4】使用完整数据集进行检测
        ArrayList<People> testData = data.getDataset();
        int testDataSize = testData.size();
        main.duplicateList = new ArrayList<ArrayList<Integer>>(testData.size());
        for(int i = 0; i < testData.size(); i++) {
            ArrayList<Integer> temp = new ArrayList<Integer>();
            main.duplicateList.add(temp);
        }

        for (int i = 0; i < testDataSize; i++) {
            for (int j = 0; j < testDataSize; j++) {
                if (i == j) {

                } else {
                    People p1 = testData.get(i);
                    People p2 = testData.get(j);
                    JaroWinklerDistance jwd = new JaroWinklerDistance();
                    double simi0 = (double) jwd.getDistance(p1.getAttributeByIndex(1), p2.getAttributeByIndex(1));
                    double simi1 = (double) jwd.getDistance(p1.getAttributeByIndex(2), p2.getAttributeByIndex(2));
                    double simi2 = (double) jwd.getDistance(p1.getAttributeByIndex(3), p2.getAttributeByIndex(3));
                    double simi3 = (double) jwd.getDistance(p1.getAttributeByIndex(4), p2.getAttributeByIndex(4));
                    double simi4 = (double) jwd.getDistance(p1.getAttributeByIndex(5), p2.getAttributeByIndex(5));
                    double simi5 = (double) jwd.getDistance(p1.getAttributeByIndex(6), p2.getAttributeByIndex(6));
                    double simi6 = (double) jwd.getDistance(p1.getAttributeByIndex(7), p2.getAttributeByIndex(7));
                    double simi7 = (double) jwd.getDistance(p1.getAttributeByIndex(8), p2.getAttributeByIndex(8));
                    double simi8 = (double) jwd.getDistance(p1.getAttributeByIndex(9), p2.getAttributeByIndex(9));
                    double simi9 = (double) jwd.getDistance(p1.getAttributeByIndex(10), p2.getAttributeByIndex(10));
                    double simi10 = (double) jwd.getDistance(p1.getAttributeByIndex(11), p2.getAttributeByIndex(11));
                    double simi11 = (double) jwd.getDistance(p1.getAttributeByIndex(12), p2.getAttributeByIndex(12));
                    double simi12 = (double) jwd.getDistance(p1.getAttributeByIndex(13), p2.getAttributeByIndex(13));
                    double simi13 = (double) jwd.getDistance(p1.getAttributeByIndex(14), p2.getAttributeByIndex(14));
                    double simi14 = (double) jwd.getDistance(p1.getAttributeByIndex(15), p2.getAttributeByIndex(15));
                    double simi15 = (double) jwd.getDistance(p1.getAttributeByIndex(16), p2.getAttributeByIndex(16));
                    double simi16 = (double) jwd.getDistance(p1.getAttributeByIndex(17), p2.getAttributeByIndex(17));
//                    FieldVector fieldVector = new FieldVector(simi0, simi1, simi2, simi3, simi4, simi5, simi6, simi7, simi8, simi9, simi10, simi11, simi12, simi13, simi14, simi15, simi16);

                    double[] tempIn = {simi0, simi1, simi2, simi3, simi4, simi5, simi6, simi7, simi8, simi9, simi10, simi11, simi12, simi13, simi14, simi15, simi16};
                    double[] tempResult = bestBP.computeOut(tempIn);
                    boolean isEqual = (tempResult[0] > bestThreshold) ? (true) : (false);
                    if(isEqual) {
                        if(!main.duplicateList.get(i).contains(j)) {
                            main.duplicateList.get(i).add(j);
                        }
                        if(!main.duplicateList.get(j).contains(i)) {
                            main.duplicateList.get(j).add(i);
                        }
                    }

                }
            }
        }


        //衡量算法的查全率和查准率以及误识别率
        int actualTotalDup = 0;//实际的重复数
        int totalDup = 0;//算法的总识别数
        int trueDup = 0;//算法的正确识别数目
        int falseDup = 0;//算法的错误识别数
        int totalDupNumber = 0;
        int[] flagArray = new int[testDataSize];
        for(int i = 0; i < testDataSize; i++) {
            flagArray[i] = 0;
        }
        for(int i = 0; i < testDataSize; i++) {
            ArrayList<Integer> list = main.duplicateList.get(i);
            if(list.isEmpty()) {
                flagArray[i] = 1;
            } else {
                if(flagArray[i] == 0) {
                    flagArray[i] = 1;
                    for(int e : list) {
                        flagArray[e] = 1;
                        totalDupNumber++;

                        String s1 = testData.get(i).getRec_id();
                        String s2 = testData.get(e).getRec_id();
                        String[] array1 = s1.split("-");
                        String[] array2 = s2.split("-");
                        if(!(array1[2].equals("org") && array2[2].equals("org")) && (array1[1].equals(array2[1]))) {
                            trueDup++;
                        }
                    }
                }
            }

        }

        actualTotalDup = data.getActualDupNumber();
        totalDup = totalDupNumber;
        falseDup = totalDup - trueDup;

        double chaquanlv = ((double)trueDup)/((double)actualTotalDup) * 100.0;
        double chazhunlv = ((double)trueDup)/((double)totalDup) * 100.0;
        double wushibielv = ((double)falseDup)/((double)totalDup) * 100.0;

        System.out.println("改进算法的查全率为："+chaquanlv+"，查准率为："+chazhunlv+"，误识别率为："+wushibielv);

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
