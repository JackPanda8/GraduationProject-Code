import java.util.*;

public class Main extends GeneticAlgorithm{
    private static final String FILEPATH = "D:\\毕业设计\\1程序\\TrainedBPNetwork\\";
    private static final String FILENAME = "dataCount500portion0.3trainingNumber100learningRate0.15populationSize3maxIterNum100.txt";

    private static final float PORTION = 0.1f;//神经网络的训练数据集占总数据集的比例
    private static final int TRAINING_NUMBER = 500;//神经网络的训练次数
    private static final double LEARNING_RATE = 0.15;//学习系数
    private static final double MOBP = 0.9;//动量系数
    private static final float THRESHOLD = 0.8f;//神经网络的输出层阈值

    private static final int POPULATION_SIZE = 3;//种群数量设置
    private static final int MAX_ITER_NUM = 30;//最大迭代次数
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
            data.getData();
        } catch (Exception e) {
            e.printStackTrace();
        }
        int inputLayerNumber = data.getFieldsNumber();
        int hiddenLayerNumber = data.getHiddenNumber();

        Main.geneSize = (inputLayerNumber+1)*hiddenLayerNumber + hiddenLayerNumber+1 + 1;
        Main main = new Main();


        //【3.2】读BP从txt
//        Chromosome bestChroFromTXT = FileIO.readTxtFile(FILEPATH, filename.toString());
        Chromosome bestChroFromTXT = FileIO.readTxtFile(FILEPATH,FILENAME);
        BPDeep bestBPFromTXT = new BPDeep(new int[]{inputLayerNumber, hiddenLayerNumber, 1}, LEARNING_RATE, MOBP, bestChroFromTXT, inputLayerNumber, hiddenLayerNumber);
        float bestThresholdFromTXT = bestChroFromTXT.getGene()[bestChroFromTXT.getGene().length-1];


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

                    double[][] tempIn = {{simi0, simi1, simi2, simi3, simi4, simi5, simi6, simi7, simi8, simi9, simi10, simi11, simi12, simi13, simi14, simi15, simi16}};
//                    double[] tempResult = bestBP.computeOut(tempIn[0]);
                    double[] tempResult = bestBPFromTXT.computeOut(tempIn[0]);
                    boolean isEqual = (tempResult[0] >= bestThresholdFromTXT) ? (true) : (false);
                    if(isEqual) {
                        System.out.println("重复："+p1.getAttributeByIndex(0)+" || "+p2.getAttributeByIndex(0));
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
