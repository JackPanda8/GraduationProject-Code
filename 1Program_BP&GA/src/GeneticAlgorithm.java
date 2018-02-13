import java.util.*;

public abstract class GeneticAlgorithm {
    private List<Chromosome> population = new ArrayList<Chromosome>();
    private int popSize = 100;//种群数量
    private int geneSize;//基因最大长度
    private int maxIterNum = 500;//最大迭代次数
    private double mutationRate = 0.01;//基因变异的概率
    private double mutationMaxScale = 0.3;//变异的最大长度占染色体总长度的比例scale
    private double mutationMinScale = 0.01;//变异的最小长度占染色体总长度的比例scale
//    private int maxMutationNum = 3;//最大变异步长
    private double crossoverRate = 0.6;//染色体交叉的概率
    private double crossoverMaxScale = 0.3;//交叉的最大长度占染色体总长度的比例scale
    private double crossoverMinScale = 0.01;//交叉的最小长度占染色体总长度的比例scale

    private int generation = 1;//当前遗传到第几代

    private double bestScore;//最好得分
    private double worstScore;//最坏得分
    private double totalScore;//总得分
    private double averageScore;//平均得分

    private Chromosome x; //记录历史种群中最好的X值
    private double y; //记录历史种群中最好的Y值
    private int geneI;//x y所在代数

    public GeneticAlgorithm(){}
    public GeneticAlgorithm(int geneSize) {
        this.geneSize = geneSize;
    }
    public GeneticAlgorithm(int geneSize, int popSize, int maxIterNum,
                            double mutationRate, double mutationMaxScale, double mutationMinScale,
                            double crossoverRate, double crossoverMaxScale, double crossoverMinScale ) {
        this.geneSize = geneSize;
        this.popSize = popSize;
        this.maxIterNum = maxIterNum;
        this.mutationRate = mutationRate;
//        this.maxMutationNum = maxMutationNum;
        this.mutationMaxScale = mutationMaxScale;
        this.mutationMinScale = mutationMinScale;
        this.crossoverRate = crossoverRate;
        this.crossoverMaxScale = crossoverMaxScale;
        this.crossoverMinScale = crossoverMinScale;
    }

    public void caculate(ArrayList<Chromosome> population) {
        //初始化种群
        generation = 1;
//        init();
        initWithChromosomes(population);
        while (generation < maxIterNum) {
            //种群遗传
            evolve();
            print();
            generation++;
        }
    }

    /**
     * @Description: 输出结果
     */
    private void print() {
        System.out.println("--------------------------------");
        System.out.println("the generation is:" + generation);
        System.out.println("the best fitness is:" + bestScore);
        System.out.println("the worst fitness is:" + worstScore);
        System.out.println("the average fitness is:" + averageScore);
        System.out.println("the total fitness is:" + totalScore);
        System.out.println("geneI:" + geneI + "\tx:" + x + "\ty:" + y);
    }


    /**
     * @Description: 初始化种群
     */
//    private void init() {
//        population = new ArrayList<Chromosome>();
//        for (int i = 0; i < popSize; i++) {
//            Chromosome chro = new Chromosome(geneSize);
//            population.add(chro);
//        }
//        caculteScore();
//    }

    /**
     * @Description: 由外界输入初始化种群
     */
    private void initWithChromosomes(ArrayList<Chromosome> population) {
        this.population = new ArrayList<Chromosome>();
        for(Chromosome chromosome : population) {
            this.population.add(chromosome);
        }
        caculteScore();
    }

    /**
     * @Description:种群进行遗传
     */
    private void evolve() {
        List<Chromosome> childPopulation = new ArrayList<Chromosome>();
        //生成下一代种群
        while (childPopulation.size() < popSize) {
            //选择
            Chromosome p1 = getParentChromosome();
            Chromosome p2 = getParentChromosome();
//            List<Chromosome> children = Chromosome.genetic(p1, p2);

            //交叉
            crossover(p1, p2);
            //基因突变
            mutation(p1, p2);

            childPopulation.add(p1);
            childPopulation.add(p2);
        }
        //新种群替换旧种群
        List<Chromosome> t = population;
        population = childPopulation;
        t.clear();
        t = null;

        //计算新种群的适应度
        caculteScore();
    }

    /**
     * @Description: 计算种群适应度
     */
    private void caculteScore() {
        setChromosomeScore(population.get(0));
        bestScore = population.get(0).getScore();
        worstScore = population.get(0).getScore();
        totalScore = 0;
        for (Chromosome chro : population) {
            setChromosomeScore(chro);
            if (chro.getScore() > bestScore) { //设置最好基因值
                bestScore = chro.getScore();
                if (y < bestScore) {
                    x = chro;
                    y = bestScore;
                    geneI = generation;
                }
            }
            if (chro.getScore() < worstScore) { //设置最坏基因值
                worstScore = chro.getScore();
            }
            totalScore += chro.getScore();
        }
        averageScore = totalScore / popSize;
        //因为精度问题导致的平均值大于最好值，将平均值设置成最好值
        averageScore = averageScore > bestScore ? bestScore : averageScore;
    }

    /**
     * @return
     * @Description: 【选择】轮盘赌法选择可以遗传下一代的染色体
     */
    private Chromosome getParentChromosome (){
        double slice = Math.random() * totalScore;
        double sum = 0;
        for (Chromosome chro : population) {
            sum += chro.getScore();
            if (sum > slice && chro.getScore() >= averageScore) {
                return chro;
            }
        }
        return null;
    }

    /**
     * @return
     * @Description: 【交叉】：适应度高，交叉的部分少,(1-0.5*((y1+y2)/ymax)) * (交叉范围)
     */
    private void crossover (Chromosome p1, Chromosome p2){
        if (Math.random() < crossoverRate) { //发生交叉
            int maxCrossoverSize = (int)(geneSize*crossoverMaxScale);
            int minCrossoverSize = (int)(geneSize*crossoverMinScale);
            int crossoverSize = (int)( (1 - 0.5*((p1.getScore()+p2.getScore())/y)) *
                    (maxCrossoverSize-minCrossoverSize) );
            int maxStart = geneSize - crossoverSize;
            int actualStart = (int)(Math.random() * (maxStart+1) );//随机得到的产生交叉操作的起始位置
            for(int i = actualStart; i < crossoverSize; i++) {
                float temp = p1.getGene()[i];
                p1.setGeneAtPos(i, p2.getGene()[i]);
                p2.setGeneAtPos(i, temp);
            }
        }
    }

    /**
     * 【变异】基因突变：适应度越高，变异位数越小
     */
    private void mutation(Chromosome p1, Chromosome p2) {
        if (Math.random() < mutationRate) { //发生基因突变
            int maxMutationSize = (int)(geneSize*mutationMaxScale);
            int minMutationSize = (int)(geneSize*mutationMinScale);
            int mutationSize = (int)( (1 - 0.5*((p1.getScore()+p2.getScore())/y)) *
                    (maxMutationSize-minMutationSize) );
            int maxStart = geneSize - mutationSize;
            int actualStart = (int)(Math.random() * (maxStart+1) );//随机得到的产生交叉操作的起始位置
            for(int i = actualStart; i < actualStart; i++) {
                p1.mutation(i, generation, maxIterNum);
                p2.mutation(i, generation, maxIterNum);
            }

        }
    }

    /**
     * @param chro
     * @Description: 设置染色体得分
     */
    private void setChromosomeScore(Chromosome chro) {
        if (chro == null) {
            return;
        }
//        double x = changeX(chro);
        double y = caculateY(chro);
        chro.setScore(y);

    }

    /**
     * @param chro
     * @return
     * @Description: 将二进制转化为对应的X
     */
//    public abstract double changeX(Chromosome chro);


    /**
     * @param x 染色体
     * @return
     * @Description: 适应度函数：根据X计算Y值 Y=F(X)
     */
    public abstract double caculateY(Chromosome x);
//    public abstract double caculateY(HashMap<Double, Double> expectedAndActual);

    public void setPopulation(List<Chromosome> population) {
        this.population = population;
    }

    public void setPopSize(int popSize) {
        this.popSize = popSize;
    }

    public void setGeneSize(int geneSize) {
        this.geneSize = geneSize;
    }

    public void setMaxIterNum(int maxIterNum) {
        this.maxIterNum = maxIterNum;
    }

    public void setMutationRate(double mutationRate) {
        this.mutationRate = mutationRate;
    }

//    public void setMaxMutationNum(int maxMutationNum) {
//        this.maxMutationNum = maxMutationNum;
//    }

    public double getBestScore() {
        return bestScore;
    }

    public double getWorstScore() {
        return worstScore;
    }

    public double getTotalScore() {
        return totalScore;
    }

    public double getAverageScore() {
        return averageScore;
    }

    public Chromosome getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}
