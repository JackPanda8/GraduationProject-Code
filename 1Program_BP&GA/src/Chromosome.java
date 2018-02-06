import java.util.*;

public class Chromosome {
    //基因序列，采用实数编码。每个个体均为一个实数串，
    // 由输入层和隐含层连接权值、隐含层阈值、隐含层与输出层连接权值以及输出层阈值4个部分组成。【实际上我的程序只有三个部分】
    // 个体包含了神经网络全部的权值和阈值，在网路结构一直的情况下，就可以构成一个结构、权值、阈值确定的神经网络。
    private float[] gene;
    private double score;//对应的函数得分

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    /**
     * @param gene
     * 由gene数组生成基因序列
     */
    public Chromosome(float[] gene) {
        if (gene.length == 0 || gene == null) {
            return;
        }
        initGeneSize(gene.length);
        for (int i = 0; i < gene.length; i++) {
            this.gene[i] = gene[i];
        }
    }

    /**
     * 生成一个新基因
     */
    public Chromosome() {

    }

    /**
     * @param c
     * @return
     * @Description: 克隆基因
     */
    public static Chromosome clone(final Chromosome c) {
        if (c == null || c.gene == null) {
            return null;
        }
        Chromosome copy = new Chromosome();
        copy.initGeneSize(c.gene.length);
        for (int i = 0; i < c.gene.length; i++) {
            copy.gene[i] = c.gene[i];
        }
        return copy;
    }

    /**
     * @param size
     * @Description: 初始化基因长度
     */
    private void initGeneSize(int size) {
        if (size <= 0) {
            return;
        }
        gene = new float[size];
    }


    /**
     * @param p1
     * @param p2
     * @Description: 遗传产生下一代
     */
    public static List<Chromosome> genetic(Chromosome p1, Chromosome p2) {
        if (p1 == null || p2 == null) { //染色体有一个为空，不产生下一代
            return null;
        }
        if (p1.gene == null || p2.gene == null) { //染色体有一个没有基因序列，不产生下一代
            return null;
        }
        if (p1.gene.length != p2.gene.length) { //染色体基因序列长度不同，不产生下一代
            return null;
        }
        Chromosome c1 = clone(p1);
        Chromosome c2 = clone(p2);
        //随机产生交叉互换位置
        int size = c1.gene.length;
        int a = ((int) (Math.random() * size)) % size;
        int b = ((int) (Math.random() * size)) % size;
        int min = a > b ? b : a;
        int max = a > b ? a : b;
        //对位置上的基因进行交叉互换
        for (int i = min; i <= max; i++) {
            float t = c1.gene[i];
            c1.gene[i] = c2.gene[i];
            c2.gene[i] = t;
        }
        List<Chromosome> list = new ArrayList<Chromosome>();
        list.add(c1);
        list.add(c2);
        return list;
    }

    /**
     * @param num
     * @Description: 基因num个位置发生变异
     */
    public void mutation(int num) {
        //允许变异
        int size = gene.length;
        for (int i = 0; i < num; i++) {
            //这里的变异

            //寻找变异位置
            int at = ((int) (Math.random() * size)) % size;
            //变异后的值
            float bool = !gene[at];

            gene[at] = bool;
        }
    }

    /**
     * 随着迭代次数的增加，发生的变异量接近于0，这样选取的函数允许这个算子
     在开始阶段搜索这个空间，在算法的后阶段进行局部搜索。
     * @param position 发生变异的位置
     * @param curGeneration 当前的代数
     * @param maxGene 当前代中的各位置基因的最大值
     * @param T 预先设定的最大迭代数
     */
    public void mutation(int position, int curGeneration, float maxGene, int T) {
        
    }
}
