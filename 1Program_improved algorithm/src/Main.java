import java.util.*;
import java.io.*;

enum Comparation {
    Lower, Equal, Higher
}

public class Main {
//    public static final String DATA_SET = "D:\\毕业设计\\1数据集\\dataset_500000_100000_3_1_1_uniform_phonetic_0.csv";
//    public static final String DATA_SET = "D:\\毕业设计\\1数据集\\dataset_200000_40000_3_1_1_uniform_phonetic_0.csv";
    public static final String DATA_SET = "D:\\毕业设计\\1数据集\\dataset_100000_20000_3_1_1_uniform_phonetic_0.csv";
//    public static final String DATA_SET = "D:\\毕业设计\\1数据集\\dataset_80000_16000_3_1_1_uniform_phonetic_0.csv";
//    public static final String DATA_SET = "D:\\毕业设计\\1数据集\\dataset_50000_10000_3_1_1_uniform_all_0.csv";
//    public static final String DATA_SET = "D:\\毕业设计\\1数据集\\dataset_20000_4000_3_1_1_uniform_phonetic_0.csv";
//    public static final String DATA_SET = "D:\\毕业设计\\1数据集\\dataset_10000_2000_3_1_1_uniform_phonetic_0.csv";
//    public static final String DATA_SET = "D:\\毕业设计\\1数据集\\dataset_5000_1000_3_1_1_uniform_phonetic_0.csv";





    public static final int WINDOW_SIZE = 5;//滑动窗口的大小
    public static final int WINDOW_MAX_SIZE = 20;//滑动窗口的最大值
    public static final int WINDOW_MIN_SIZE = 3;//滑动窗口的最小值
    public static final double VERY_CLOSE_CONSTANT = 0.80;//暂时定义very_close的衡量尺度为相似度>=0.8
    public static final double CLOSE_CONSTANT = 0.6;//暂时定义close的衡量尺度为相似度>=0.6,所以0.6~0.8即为close_but_not_much的范围

    private ArrayList<People> dataset;
    private ArrayList<String> fields;//所有的字段
    private HashMap<String, Integer> fieldsDifference;//每个字段对应的不同记录数，是该字段对于整体数据的一个区分度
    private ArrayList<String> sortedFieldsByDifference;
    private HashMap<People, Integer> mapOfDatasetAfterSort;
    private ArrayList<People> datasetAfterSort;
    private ArrayList<People> datasetAfterSort1;
    private ArrayList<People> datasetAfterSort2;
    private HashSet<Integer> indexOfTuplesWithIncompleteSortKey;//排序key是不完整的元组在datasetaftersort总的的索引位置集合
    private HashSet<People> duplicateTuples;
    //    private int[][] duplicateMatrix;
    private HashMap<Integer, ArrayList<Integer>> duplicateList;//邻接链表替代邻接矩阵
    private ArrayList<People> cleanDataset;
    private int totalDup;//算法检查出的总重复
    private int actualTotalDup;//实际总重复数
    private int trueDup;//算法中正确重复
//    private int[] flagForCountTrueDup;//为统计正确重复设立的flag数组
    private int falseDup;//算法中错误重复

    public static void main(String[] args) throws IOException{
        //计算程序运行时间
        long startTime=System.currentTimeMillis();   //获取开始时间

        String datasetName = Main.DATA_SET;
        String[] array = datasetName.split("_");
        String stringValue = array[2];

        Main main = new Main();
        main.actualTotalDup = Integer.valueOf(stringValue);
        main.trueDup = 0;
        main.falseDup = 0;
        main.getData();
        int size = main.dataset.size();

        main.sortByDiff();
        main.formSet();//将people与duplicateList建立1对1的映射，以便计算传递闭包用

        //第一趟
        main.generateKey();
        main.quickSort();
        main.slideWindowProcess(WINDOW_SIZE);

        //第二趟
        main.generateKey1();
        main.quickSort1();
        main.slideWindowProcess1(WINDOW_SIZE);

        //第三趟
        main.generateKey2();
        main.quickSort2();
        main.slideWindowProcess2(WINDOW_SIZE);

        //将之前步骤积累的含有空排序字段的记录与已经合并过的记录集进行比较，发掘新的重复集合
        int count = 0;
        for(int i : main.indexOfTuplesWithIncompleteSortKey) {
            if(main.duplicateList.get(i).isEmpty()) {
                count++;
            }
        }
        System.out.println("含有空排序key并且没有与之重复的记录 = " + count);
        main.cleanDataset = new ArrayList<People>();
        main.eliminateDuplication();
        main.processMarkedRecord();
        main.eliminateDuplication();

        main.evaluation();

        long endTime=System.currentTimeMillis(); //获取结束时间
        System.out.println("改进算法运行时间： "+(endTime-startTime)/1000.0+"s");
    }


    //衡量算法的查全率和查准率以及误识别率
    private void evaluation() {
        int totalDupNumber = 0;
        int size = this.dataset.size();
        int[] flagArray = new int[size];
        for(int i = 0; i < size; i++) {
            flagArray[i] = 0;
        }
        for(int i = 0; i < size; i++) {
            ArrayList<Integer> list = this.duplicateList.get(i);
            if(list.isEmpty()) {
                flagArray[i] = 1;
            } else {
                if(flagArray[i] == 0) {
                    flagArray[i] = 1;
                    for(int e : list) {
                        flagArray[e] = 1;
                        totalDupNumber++;

                        String s1 = this.dataset.get(i).getRec_id();
                        String s2 = this.dataset.get(e).getRec_id();
                        String[] array1 = s1.split("-");
                        String[] array2 = s2.split("-");
                        if(!(array1[2].equals("org") && array2[2].equals("org")) && (array1[1].equals(array2[1]))) {
                            this.trueDup++;
                        }
                    }
                }
            }

        }

        this.totalDup = totalDupNumber;
        this.falseDup = this.totalDup - this.trueDup;

        double chaquanlv = ((double)this.trueDup)/((double)this.actualTotalDup) * 100.0;
        double chazhunlv = ((double)this.trueDup)/((double)this.totalDup) * 100.0;
        double wushibielv = ((double)this.falseDup)/((double)this.totalDup) * 100.0;

        System.out.println("改进算法的查全率为："+chaquanlv+"，查准率为："+chazhunlv+"，误识别率为："+wushibielv);
    }


    //[改进点2：处理被标记过并且无与之重复的记录，将其分别与已经归并过的记录进行比较]
    private void processMarkedRecord() {
        for(int index : this.indexOfTuplesWithIncompleteSortKey) {
            if (this.duplicateList.get(index).isEmpty()) {
                ArrayList<Integer> temp = new ArrayList<Integer>();

                Iterator<People> iter = this.cleanDataset.iterator();
                while(iter.hasNext()){
                    People p = iter.next();
                    if(judgeEqual(p, this.dataset.get(index))) {
                        if(this.cleanDataset.contains(this.dataset.get(index))) {
//                            this.cleanDataset.remove(this.dataset.get(index));
                            temp.add(index);

                        }

                        int indexI = index;
                        int indexTail = this.mapOfDatasetAfterSort.get(p);

                        this.duplicateTuples.add(this.dataset.get(indexI));
                        this.duplicateTuples.add(this.dataset.get(indexTail));

                        if(!this.duplicateList.get(indexTail).contains(indexI) && indexI != indexTail) {
                            this.duplicateList.get(indexTail).add(indexI);
                        }
                        if(!this.duplicateList.get(indexI).contains(indexTail) && indexI != indexTail) {
                            this.duplicateList.get(indexI).add(indexTail);
                        }


                    }
                }

                for(int ele : temp) {
                    if(this.cleanDataset.contains(this.dataset.get(ele))) {
                        this.cleanDataset.remove(this.dataset.get(ele));

                    }
                }

            }
        }
    }


    //[5]消除重复
    private void eliminateDuplication() {
//        this.cleanDataset = new ArrayList<People>();

//        this.duplicateMatrix = TransitiveClosure.getTransitiveClosure(this.duplicateMatrix, this.datasetAfterSort.size());
        TransitiveClosure.getTransitiveClosureOfList(this.duplicateList);

        int[] flagArray = new int[this.dataset.size()];
        for (int i = 0; i < flagArray.length; i++) {
            flagArray[i] = 0;
        }

        for (int i = 0; i < flagArray.length; i++) {
            if(flagArray[i] == 0) {
                flagArray[i] = 1;
                if(!this.cleanDataset.contains(this.dataset.get(i))) {
                    this.cleanDataset.add(this.dataset.get(i));
                }
                for(int temp : this.duplicateList.get(i)) {
                    flagArray[temp] = 1;
                }
            } else {
                continue;
            }
        }

    }




    //【改进点3：动态滑动窗口】
    //[4.1]滑动窗口归并,w是滑动窗口的大小
    private void slideWindowProcess(int w) {
        if(w > this.datasetAfterSort.size()) {
            System.out.println("Error：滑动窗口的大小" + w + "超过了数据集的大小" + this.datasetAfterSort.size());
        }



        int size = this.datasetAfterSort.size();
//        this.duplicateMatrix = new int[size][size];
        this.duplicateList = new HashMap<Integer, ArrayList<Integer>>();

        for(int i = 0; i < size; i++) {
            ArrayList<Integer> tempList = new ArrayList<Integer>();
            this.duplicateList.put(i, tempList);
        }

//        int tail = w-1;
        int tail = 0;
        this.duplicateTuples = new HashSet<People>();
        while(tail < this.datasetAfterSort.size()) {
            People tailPeople = this.datasetAfterSort.get(tail);
            if(tail < w-1) {//比较起始情况下，前w大小的滑动窗口内重复记录
                for(int i = 0; i < tail; i++) {
                    People tempPeople = this.datasetAfterSort.get(i);
                    if(judgeEqual(tempPeople, tailPeople)) {
                        int indexI = this.mapOfDatasetAfterSort.get(this.datasetAfterSort.get(i));
                        int indexTail = this.mapOfDatasetAfterSort.get(this.datasetAfterSort.get(tail));

                        this.duplicateTuples.add(this.dataset.get(indexI));
                        this.duplicateTuples.add(this.dataset.get(indexTail));

                        if(!this.duplicateList.get(indexTail).contains(indexI) && indexI != indexTail) {
                            this.duplicateList.get(indexTail).add(indexI);
                        }
                        if(!this.duplicateList.get(indexI).contains(indexTail) && indexI != indexTail) {
                            this.duplicateList.get(indexI).add(indexTail);
                        }

                    }
                }

                //更改下一次滑动窗口的大小
                double denominator = 0;
                double numerator = 0;
                for(int j = 0; j < tail; j++) {
                    denominator+=Math.abs(j-tail);
                    boolean equal = judgeEqual(this.datasetAfterSort.get(j), this.datasetAfterSort.get(tail));
                    numerator+=Math.abs(j-tail)*((equal)?(1):(0));
                }
                if(denominator!=0) {
                    w= (int) (WINDOW_MIN_SIZE+(WINDOW_MAX_SIZE- WINDOW_MIN_SIZE)*(numerator)/(denominator));
                }
                System.out.println("1窗口内过程，下一次w大小为:"+w);

            } else {
                for(int i = tail-w+1; i < tail; i++) {
                    People tempPeople = this.datasetAfterSort.get(i);
                    if(judgeEqual(tempPeople, tailPeople)) {
                        int indexI = this.mapOfDatasetAfterSort.get(this.datasetAfterSort.get(i));
                        int indexTail = this.mapOfDatasetAfterSort.get(this.datasetAfterSort.get(tail));

                        this.duplicateTuples.add(this.dataset.get(indexI));
                        this.duplicateTuples.add(this.dataset.get(indexTail));

                        if(!this.duplicateList.get(indexTail).contains(indexI) && indexI != indexTail) {
                            this.duplicateList.get(indexTail).add(indexI);
                        }
                        if(!this.duplicateList.get(indexI).contains(indexTail) && indexI != indexTail) {
                            this.duplicateList.get(indexI).add(indexTail);
                        }

                    }
                }

                //更改下一次滑动窗口的大小
                double denominator = 0;
                double numerator = 0;
                for(int j = tail-w+1; j < tail; j++) {
                    denominator+=Math.abs(j-tail);
                    boolean equal = judgeEqual(this.datasetAfterSort.get(j), this.datasetAfterSort.get(tail));
                    numerator+=Math.abs(j-tail)*((equal)?(1):(0));
                }
                if(denominator!=0) {
                    w= (int) (WINDOW_MIN_SIZE+(WINDOW_MAX_SIZE- WINDOW_MIN_SIZE)*(numerator/denominator));
                }
                System.out.println("1普通过程，下一次w大小为:"+w);

            }


            tail++;
        }
    }

    //[4.2]滑动窗口归并,w是滑动窗口的大小
    private void slideWindowProcess1(int w) {
        if(w > this.datasetAfterSort1.size()) {
            System.out.println("Error：滑动窗口的大小" + w + "超过了数据集的大小" + this.datasetAfterSort.size());
        }

        int tail = 0;
        while(tail < this.datasetAfterSort1.size()) {
            People tailPeople = this.datasetAfterSort1.get(tail);
            if(tail < w-1) {//比较起始情况下，前w大小的滑动窗口内重复记录
                for(int i = 0; i < tail; i++) {
                    People tempPeople = this.datasetAfterSort1.get(i);
                    if(judgeEqual(tempPeople, tailPeople)) {
                        int indexI = this.mapOfDatasetAfterSort.get(this.datasetAfterSort1.get(i));
                        int indexTail = this.mapOfDatasetAfterSort.get(this.datasetAfterSort1.get(tail));

                        this.duplicateTuples.add(this.dataset.get(indexI));
                        this.duplicateTuples.add(this.dataset.get(indexTail));

                        if(!this.duplicateList.get(indexTail).contains(indexI) && indexI != indexTail) {
                            this.duplicateList.get(indexTail).add(indexI);
                        }
                        if(!this.duplicateList.get(indexI).contains(indexTail) && indexI != indexTail) {
                            this.duplicateList.get(indexI).add(indexTail);
                        }

                    }
                }

                //更改下一次滑动窗口的大小
                double denominator = 0;
                double numerator = 0;
                for(int j = 0; j < tail; j++) {
                    denominator+=Math.abs(j-tail);
                    boolean equal = judgeEqual(this.datasetAfterSort1.get(j), this.datasetAfterSort1.get(tail));
                    numerator+=Math.abs(j-tail)*((equal)?(1):(0));
                }
                if(denominator!=0) {
                    w= (int) (WINDOW_MIN_SIZE+(WINDOW_MAX_SIZE- WINDOW_MIN_SIZE)*(numerator/denominator));
                }
                System.out.println("2窗口内过程，下一次w大小为:"+w);

            } else {
                for(int i = tail-w+1; i < tail; i++) {
                    People tempPeople = this.datasetAfterSort1.get(i);
                    if(judgeEqual(tempPeople, tailPeople)) {
                        int indexI = this.mapOfDatasetAfterSort.get(this.datasetAfterSort1.get(i));
                        int indexTail = this.mapOfDatasetAfterSort.get(this.datasetAfterSort1.get(tail));

                        this.duplicateTuples.add(this.dataset.get(indexI));
                        this.duplicateTuples.add(this.dataset.get(indexTail));

                        if(!this.duplicateList.get(indexTail).contains(indexI) && indexI != indexTail) {
                            this.duplicateList.get(indexTail).add(indexI);
                        }
                        if(!this.duplicateList.get(indexI).contains(indexTail) && indexI != indexTail) {
                            this.duplicateList.get(indexI).add(indexTail);
                        }

                    }
                }

                //更改下一次滑动窗口的大小
                double denominator = 0;
                double numerator = 0;
                for(int j = tail-w+1; j < tail; j++) {
                    denominator+=Math.abs(j-tail);
                    boolean equal = judgeEqual(this.datasetAfterSort1.get(j), this.datasetAfterSort1.get(tail));
                    numerator+=Math.abs(j-tail)*((equal)?(1):(0));
                }
                if(denominator!=0) {
                    w= (int) (WINDOW_MIN_SIZE+(WINDOW_MAX_SIZE- WINDOW_MIN_SIZE)*(numerator/denominator));
                }
                System.out.println("2普通过程，下一次w大小为:"+w);

            }
            tail++;
        }
    }

    //[4.3]滑动窗口归并,w是滑动窗口的大小
    private void slideWindowProcess2(int w) {
        if(w > this.datasetAfterSort2.size()) {
            System.out.println("Error：滑动窗口的大小" + w + "超过了数据集的大小" + this.datasetAfterSort.size());
        }

        int tail = 0;
        while(tail < this.datasetAfterSort2.size()) {
            People tailPeople = this.datasetAfterSort2.get(tail);
            if(tail < w-1) {//比较起始情况下，前w大小的滑动窗口内重复记录
                for(int i = 0; i < tail; i++) {
                    People tempPeople = this.datasetAfterSort2.get(i);
                    if(judgeEqual(tempPeople, tailPeople)) {
                        int indexI = this.mapOfDatasetAfterSort.get(this.datasetAfterSort2.get(i));
                        int indexTail = this.mapOfDatasetAfterSort.get(this.datasetAfterSort2.get(tail));

                        this.duplicateTuples.add(this.dataset.get(indexI));
                        this.duplicateTuples.add(this.dataset.get(indexTail));

                        if(!this.duplicateList.get(indexTail).contains(indexI) && indexI != indexTail) {
                            this.duplicateList.get(indexTail).add(indexI);
                        }
                        if(!this.duplicateList.get(indexI).contains(indexTail) && indexI != indexTail) {
                            this.duplicateList.get(indexI).add(indexTail);
                        }



                    }
                }

                //更改下一次滑动窗口的大小
                double denominator = 0;
                double numerator = 0;
                for(int j = 0; j < tail; j++) {
                    denominator+=Math.abs(j-tail);
                    boolean equal = judgeEqual(this.datasetAfterSort2.get(j), this.datasetAfterSort2.get(tail));
                    numerator+=Math.abs(j-tail)*((equal)?(1):(0));
                }
                if(denominator!=0) {
                    w= (int) (WINDOW_MIN_SIZE+(WINDOW_MAX_SIZE- WINDOW_MIN_SIZE)*(numerator/denominator));
                }
                System.out.println("3窗口内过程，下一次w大小为:"+w);


            } else {
                for(int i = tail-w+1; i < tail; i++) {
                    People tempPeople = this.datasetAfterSort2.get(i);
                    if(judgeEqual(tempPeople, tailPeople)) {
                        int indexI = this.mapOfDatasetAfterSort.get(this.datasetAfterSort2.get(i));
                        int indexTail = this.mapOfDatasetAfterSort.get(this.datasetAfterSort2.get(tail));

                        this.duplicateTuples.add(this.dataset.get(indexI));
                        this.duplicateTuples.add(this.dataset.get(indexTail));

                        if(!this.duplicateList.get(indexTail).contains(indexI) && indexI != indexTail) {
                            this.duplicateList.get(indexTail).add(indexI);
                        }
                        if(!this.duplicateList.get(indexI).contains(indexTail) && indexI != indexTail) {
                            this.duplicateList.get(indexI).add(indexTail);
                        }

                    }
                }

                //更改下一次滑动窗口的大小
                double denominator = 0;
                double numerator = 0;
                for(int j = tail-w+1; j < tail; j++) {
                    denominator+=Math.abs(j-tail);
                    boolean equal = judgeEqual(this.datasetAfterSort2.get(j), this.datasetAfterSort2.get(tail));
                    numerator+=Math.abs(j-tail)*((equal)?(1):(0));
                }
                if(denominator!=0) {
                    w= (int) (WINDOW_MIN_SIZE+(WINDOW_MAX_SIZE- WINDOW_MIN_SIZE)*(numerator/denominator));
                }
                System.out.println("3普通过程，下一次w大小为:"+w);


            }
            tail++;
        }
    }

    //采用基于权值的判等方法
    private boolean judgeEqual1(People p1, People p2) {
        //获取两条数据的区分度最高字段的内容
        int keyindex0 = this.fields.indexOf(this.sortedFieldsByDifference.get(0));
        int keyindex1 = this.fields.indexOf(this.sortedFieldsByDifference.get(1));
        int keyindex2 = this.fields.indexOf(this.sortedFieldsByDifference.get(2));
        int keyindex3 = this.fields.indexOf(this.sortedFieldsByDifference.get(3));
        int keyindex4 = this.fields.indexOf(this.sortedFieldsByDifference.get(4));
        String s10 = p1.getAttributeByIndex(keyindex0);
        String s11 = p1.getAttributeByIndex(keyindex1);
        String s12 = p1.getAttributeByIndex(keyindex2);
        String s13 = p1.getAttributeByIndex(keyindex3);
        String s14 = p1.getAttributeByIndex(keyindex4);
        String s20 = p2.getAttributeByIndex(keyindex0);
        String s21 = p2.getAttributeByIndex(keyindex1);
        String s22 = p2.getAttributeByIndex(keyindex2);
        String s23 = p2.getAttributeByIndex(keyindex3);
        String s24 = p2.getAttributeByIndex(keyindex4);


        if(calculateSimilarDegree(s10, s20) >= VERY_CLOSE_CONSTANT
                && calculateSimilarDegree(s11, s21) >= VERY_CLOSE_CONSTANT
                && calculateSimilarDegree(s12, s22) >= CLOSE_CONSTANT
                && calculateSimilarDegree(s13, s23) >= CLOSE_CONSTANT
                && calculateSimilarDegree(s14, s24) >= CLOSE_CONSTANT) {
            return true;
        }

        return false;
    }

    //对两个record进行比较判断是否可以归并,原论文使用的是OPS5产生式系统（Production System）
    private boolean judgeEqual(People people1, People people2) {
//        if(people1.getSortKey().equals(people2.getSortKey())) {
//            return true;
//        }

        if(similar_ssns_and_names(people1, people2)
                || very_similar_address_decision(people1, people2)
                || similar_addrs_take_2(people1, people2)
                || very_close_ssn_close_address(people1, people2)
                || last_change(people1, people2)) {
            return true;
        }
        return false;
    }

    /*First Stage*/
    private boolean similar_ssns(People p1, People p2) {
        String s1 = p1.getSoc_sec_id();
        String s2 = p2.getSoc_sec_id();
        if(s1 == null || s2 == null) {
            return false;
        }
        if(s1.equals(s2)) {
            return true;
        }
        return false;
    }
    private boolean similar_names(People p1, People p2) {
        String s1 = p1.getGiven_name();
        String s2 = p1.getSurname();
        String s3 = p2.getGiven_name();
        String s4 = p2.getSurname();

        if(s1 != null && s2 != null && s3 != null && s4 != null && (p1.getGiven_name().equals(p2.getGiven_name())
                || p1.getSurname().equals(p2.getGiven_name())
                || p1.getSurname().equals(p2.getSurname())
                || p1.getGiven_name().equals(p2.getSurname()))) {
            return true;
        }
        return false;
    }
    private boolean similar_addrs(People p1, People p2) {
        if(p1.getAddress_1()!=null && p2.getAddress_1()!=null && p1.getAddress_1().equals(p2.getAddress_1())) {
            return true;
        }
        return false;
    }
    //Equal1
    private boolean similar_ssns_and_names(People p1, People p2) {
        if(similar_ssns(p1,p2) && similar_names(p1, p2)) {
//            System.out.println("Equal1:");
            String s1 = p1.getRec_id().split("-")[1];
            String s2 = p2.getRec_id().split("-")[1];
            if(!s1.equals(s2)) {
                Print.printPeople(p1);
                Print.printPeople(p2);
            }

            return true;
        }
        return false;
    }

    /*Second Stage*/
//    private boolean similar_city(People) {
//
//    }
    private boolean similar_zip(People p1, People p2) {
        if(p1.getPostcode() != null && p2.getPostcode() != null &&p1.getPostcode().equals(p2.getPostcode())) {
            return true;
        }
        return false;
    }
    private boolean similar_state(People p1, People p2) {
        if(p1.getState() !=null && p2.getState() != null && p1.getState().equals(p2.getState())) {
            return true;
        }
        return false;
    }
    private boolean very_similar_addrs(People p1, People p2) {
        if(similar_addrs(p1,p2) && (similar_state(p1,p2) || similar_zip(p1, p2))) {
            return true;
        }
        return false;
    }
    //Equal2
    private boolean very_similar_address_decision(People p1, People p2) {
        if( (similar_ssns(p1, p2) || similar_names(p1, p2)) && (very_similar_addrs(p1, p2)) ) {
//            System.out.println("Equal2:");
            String s1 = p1.getRec_id().split("-")[1];
            String s2 = p2.getRec_id().split("-")[1];
            if(!s1.equals(s2)) {
                Print.printPeople(p1);
                Print.printPeople(p2);
            }

            return true;
        }
        return false;
    }

    /*Third Stage*/
    private boolean very_close_stnum(People p1, People p2) {
        if(p1.getStreet_number()!=null && !p1.getStreet_number().isEmpty() &&
                p2.getStreet_number()!=null && !p2.getStreet_number().isEmpty()
                && very_close_num(p1.getStreet_number(), p2.getStreet_number())) {
            return true;
        }
        return false;
    }
    private boolean very_close_num(String num1, String num2) {
        if(num1 != null && num2 != null &&calculateSimilarDegree(num1, num2) >= VERY_CLOSE_CONSTANT) {
            return true;
        }
        return false;
    }

    //    private boolean very_close_aptm(People p1, People p2) {
//        if() {
//            return true;
//        }
//        return false;
//    }
    private boolean very_similar_addrs1(People p1, People p2) {
        if( (very_close_stnum(p1,p2) && close_but_not_much(p1.getAddress_1(), p2.getAddress_1()) && (similar_state(p1, p2) || similar_zip(p1, p2)) && !similar_addrs(p1, p2))
                || (similar_addrs(p1, p2) && very_close_stnum(p1, p2) && similar_zip(p1, p2)) ) {
            return true;
        }
        return false;
    }
    private boolean close_but_not_much(String strA, String strB) {
        if(strA != null && strB != null && calculateSimilarDegree(strA, strB) >= CLOSE_CONSTANT && calculateSimilarDegree(strA, strB) < VERY_CLOSE_CONSTANT) {
            return true;
        }
        return false;
    }
    //Equal3
    private boolean similar_addrs_take_2(People p1, People p2) {
        if((very_similar_addrs(p1, p2) || very_similar_addrs1(p1, p2)) && (similar_ssns(p1, p2) || similar_names(p1, p2))) {
//            System.out.println("Equal3:");

            String s1 = p1.getRec_id().split("-")[1];
            String s2 = p2.getRec_id().split("-")[1];
            if(!s1.equals(s2)) {
                Print.printPeople(p1);
                Print.printPeople(p2);
            }

            return true;
        }
        return false;
    }
    //Equal4
    private boolean very_close_ssn_close_address(People p1, People p2) {
        if(similar_addrs(p1,p2) && similar_ssns(p1, p2) && !similar_names(p1, p2)) {
//            System.out.println("Equal4:");
            String s1 = p1.getRec_id().split("-")[1];
            String s2 = p2.getRec_id().split("-")[1];
            if(!s1.equals(s2)) {
                Print.printPeople(p1);
                Print.printPeople(p2);
            }
            return true;
        }
        return false;
    }
    //Equal5
    private boolean last_change(People p1, People p2) {
        if(similar_ssns(p1, p2) && (very_similar_addrs(p1, p2) || very_similar_addrs1(p1, p2)) && same_name_or_initial(p1.getGiven_name(), p2.getGiven_name()) && similar_zip(p1, p2)) {
//            System.out.println("Equal5:");
            String s1 = p1.getRec_id().split("-")[1];
            String s2 = p2.getRec_id().split("-")[1];
            if(!s1.equals(s2)) {
                Print.printPeople(p1);
                Print.printPeople(p2);
            }
            return true;
        }
        return false;
    }
    private boolean same_name_or_initial(String strA, String strB) {
        if(strA != null && strB != null) {
            char initialA = strA.toLowerCase().charAt(0);
            char initialB = strB.toLowerCase().charAt(0);
            if(strA.equals(strB) || initialA == initialB) {
                return true;
            }
        }
        return false;
    }

    //计算字符串的相似程度，公式：1-编辑距离/MaxLength(strA, strB)
    public double calculateSimilarDegree(String strA, String strB) {
        if(strA == null || strB == null) {
            return 0.0;
        }
        int maxLength = (strA.length() > strB.length())?(strA.length()):(strB.length());
        return 1.0 - ((double)getEditDistance(strA, strB)/(double)maxLength);
    }

    //两个字符串的编辑距离edit distance
    public int getEditDistance(String strA, String strB){
        int distance=-1;
        /*输入参数合法性检查*/
        if(null==strA||null==strB||strA.isEmpty()||strB.isEmpty()){
            System.out.println("字符串不合法:geteditdistance!");
            return distance;
        }
        /*两个字符串相等，编辑距离为0*/
        if (strA.equals(strB)) {
            return 0;
        }

        int lengthA=strA.length();
        int lengthB=strB.length();
        int length=Math.max(lengthA,lengthB);
        /*申请一个二维数组，存储转移矩阵*/
        int array[][]=new int[length+1][length+1];
        /*边界条件初始化*/
        for(int i=0;i<=length;i++){
            array[i][0]=i;

        }
        /*边界条件初始化*/
        for(int j=0;j<=length;j++){
            array[0][j]=j;
        }
        /*状态转移方程*/
        for(int i=1;i<=lengthA;i++){
            for(int j=1;j<=lengthB;j++){
                array[i][j]=min(array[i-1][j]+1,
                        array[i][j-1]+1,
                        array[i-1][j-1]+(strA.charAt(i-1)==strB.charAt(j-1)?0:1));
            }
        }
        return array[lengthA][lengthB];

    }

    /*取三个数中的最小值*/
    public int  min(int a,int b, int c){
        return Math.min(Math.min(a,b),c);
    }




    //[3.1]排序，使用快速排序
    private void quickSort(){
        People[] arr = new People[this.dataset.size()];
        for(int i = 0; i < this.dataset.size(); i++) {
            arr[i] = this.dataset.get(i);
        }
        qsort(arr, 0, arr.length-1);
        this.datasetAfterSort = new ArrayList<People>();
        for(People people : arr) {
//            System.out.println(people.getSortKey());
            this.datasetAfterSort.add(people);
        }
    }
    //[3.2]排序，使用快速排序
    private void quickSort1(){
        People[] arr = new People[this.dataset.size()];
        for(int i = 0; i < this.dataset.size(); i++) {
            arr[i] = this.dataset.get(i);
        }
        qsort(arr, 0, arr.length-1);
        this.datasetAfterSort1 = new ArrayList<People>();
        for(People people : arr) {
//            System.out.println(people.getSortKey());
            this.datasetAfterSort1.add(people);
        }
    }
    //[3.3]排序，使用快速排序
    private void quickSort2(){
        People[] arr = new People[this.dataset.size()];
        for(int i = 0; i < this.dataset.size(); i++) {
            arr[i] = this.dataset.get(i);
        }
        qsort(arr, 0, arr.length-1);
        this.datasetAfterSort2 = new ArrayList<People>();
        for(People people : arr) {
//            System.out.println(people.getSortKey());
            this.datasetAfterSort2.add(people);
        }
    }

    private void qsort(People[] arr, int low, int high){
        if (low < high){
            int pivot=partition(arr, low, high);        //将数组分为两部分
            qsort(arr, low, pivot-1);                   //递归排序左子数组
            qsort(arr, pivot+1, high);                  //递归排序右子数组
        }
    }

    private int partition(People[] arr, int low, int high){
        People pivot = arr[low];     //枢轴记录
        while (low<high){
            try {
                while (low<high && (compareAToB(arr[high].getSortKey(), pivot.getSortKey()) == Comparation.Higher || compareAToB(arr[high].getSortKey(), pivot.getSortKey()) == Comparation.Equal)) --high;
                arr[low]=arr[high];             //交换比枢轴小的记录到左端
                while (low<high && (compareAToB(arr[low].getSortKey(), pivot.getSortKey()) == Comparation.Lower || compareAToB(arr[low].getSortKey(), pivot.getSortKey()) == Comparation.Equal)) ++low;
                arr[high] = arr[low];           //交换比枢轴小的记录到右端
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        //扫描完成，枢轴到位
        arr[low] = pivot;
        //返回的是枢轴的位置
        return low;
    }

    //字符串比较结果
    private Comparation compareAToB(String a, String b) throws Exception{
        if(a == null || b == null || a.length() == 0 || b.length() == 0) {
            System.out.println("空字符串无法比较");
            throw new NullPointerException();
        }
        if(a.length() != b.length()) {
            System.out.println("字符串长度不一致，无法比较");
            throw new Exception();
        }

        int length = a.length();
        char[] ac = a.toCharArray();
        char[] bc = b.toCharArray();
        for(int i = 0; i < length; i++) {
            if(ac[i] - bc[i] < 0) {
                return Comparation.Lower;
            } else if(ac[i] - bc[i] > 0) {
                return Comparation.Higher;
            } else {
                continue;
            }
        }

        return Comparation.Equal;
    }





    //【改进点1】
    //1.首先统计每个字段的区分度：即在该字段下不同记录的个数
    //2.每次选择区分度较大的字段作为排序用的关键字

    private void sortByDiff() {
        this.fieldsDifference = new HashMap<String, Integer>();
        int iterator = 0;
        for(String field : this.fields) {
            int numberOfDifferenctRecords = 0;

            //计算不同记录个数
            ArrayList<String> tempList = new ArrayList<String>();

            for(People people : this.dataset) {
                String attribute = people.getAttributeByIndex(iterator);
                if(attribute != null) {
                    if(tempList.isEmpty()) {
                        tempList.add(attribute);
                    } else {
                        int flag = 1;//当flag变为0的时候代表不需要增加新元素
                        for(String s : tempList) {
                            if(s.equals(attribute)) {
                                flag = 0;
                                break;
                            } else {
                                continue;
                            }
                        }
                        if(flag == 1) {
                            tempList.add(attribute);
                        }
                    }
                }
            }

            numberOfDifferenctRecords = tempList.size();
            if(!field.equals("rec_id")) {
                this.fieldsDifference.put(field, numberOfDifferenctRecords);
            }

            iterator++;
        }


        HashMap<String, Integer> copy = new HashMap<String, Integer>();
        for(int i = 0; i < this.fieldsDifference.keySet().size(); i++) {
            String tempKey = (String) this.fieldsDifference.keySet().toArray()[i];
            copy.put(tempKey, this.fieldsDifference.get(tempKey));
        }


        List<Map.Entry<String, Integer>> tempList =
                new ArrayList<Map.Entry<String, Integer>>(this.fieldsDifference.entrySet());

        //排序
        Collections.sort(tempList, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return (o2.getValue() - o1.getValue());
//                return (o1.getKey()).toString().compareTo(o2.getKey());
            }
        });

        this.sortedFieldsByDifference = new ArrayList<String>();
        for(Map.Entry<String, Integer> entry : tempList) {
            String value = entry.getKey();
            this.sortedFieldsByDifference.add(value);
        }


    }


    private void generateKey() {
        this.indexOfTuplesWithIncompleteSortKey = new HashSet<Integer>();
        for(People people : this.dataset) {
            StringBuilder sortKey = new StringBuilder();
            int keyindex0 = this.fields.indexOf(this.sortedFieldsByDifference.get(0));
            int keyindex1 = this.fields.indexOf(this.sortedFieldsByDifference.get(1));
            int keyindex2 = this.fields.indexOf(this.sortedFieldsByDifference.get(2));
            String s0 = people.getAttributeByIndex(keyindex0);
            if(s0 == null) {
                this.indexOfTuplesWithIncompleteSortKey.add(this.mapOfDatasetAfterSort.get(people));
            }
            StringBuilder part0 = getFirstThreeChar(s0);

            String s1 = people.getAttributeByIndex(keyindex1);
            if(s1 == null) {
                this.indexOfTuplesWithIncompleteSortKey.add(this.mapOfDatasetAfterSort.get(people));
            }
            StringBuilder part1 = getFirstThreeChar(s1);

            String s2 = people.getAttributeByIndex(keyindex2);
            if(s2 == null) {
                this.indexOfTuplesWithIncompleteSortKey.add(this.mapOfDatasetAfterSort.get(people));
            }
            StringBuilder part2 = getFirstThreeChar(s2);

            String finalKey = sortKey.append(part0).append(part1).append(part2).toString();

            people.setSortKey(finalKey);
        }

    }

    private void generateKey1() {
        for(People people : this.dataset) {
            StringBuilder sortKey = new StringBuilder();
            int keyindex0 = this.fields.indexOf(this.sortedFieldsByDifference.get(0));
            int keyindex1 = this.fields.indexOf(this.sortedFieldsByDifference.get(1));
            int keyindex2 = this.fields.indexOf(this.sortedFieldsByDifference.get(3));
            String s0 = people.getAttributeByIndex(keyindex0);
            if(s0 == null) {
                this.indexOfTuplesWithIncompleteSortKey.add(this.mapOfDatasetAfterSort.get(people));
            }
            StringBuilder part0 = getFirstThreeChar(s0);

            String s1 = people.getAttributeByIndex(keyindex1);
            if(s1 == null) {
                this.indexOfTuplesWithIncompleteSortKey.add(this.mapOfDatasetAfterSort.get(people));
            }
            StringBuilder part1 = getFirstThreeChar(s1);

            String s2 = people.getAttributeByIndex(keyindex2);
            if(s2 == null) {
                this.indexOfTuplesWithIncompleteSortKey.add(this.mapOfDatasetAfterSort.get(people));
            }
            StringBuilder part2 = getFirstThreeChar(s2);

            String finalKey = sortKey.append(part0).append(part1).append(part2).toString();

            people.setSortKey(finalKey);
        }

    }

    private void generateKey2() {
        for(People people : this.dataset) {
            StringBuilder sortKey = new StringBuilder();
            int keyindex0 = this.fields.indexOf(this.sortedFieldsByDifference.get(1));
            int keyindex1 = this.fields.indexOf(this.sortedFieldsByDifference.get(2));
            int keyindex2 = this.fields.indexOf(this.sortedFieldsByDifference.get(3));
            String s0 = people.getAttributeByIndex(keyindex0);
            if(s0 == null) {
                this.indexOfTuplesWithIncompleteSortKey.add(this.mapOfDatasetAfterSort.get(people));
            }
            StringBuilder part0 = getFirstThreeChar(s0);

            String s1 = people.getAttributeByIndex(keyindex1);
            if(s1 == null) {
                this.indexOfTuplesWithIncompleteSortKey.add(this.mapOfDatasetAfterSort.get(people));
            }
            StringBuilder part1 = getFirstThreeChar(s1);

            String s2 = people.getAttributeByIndex(keyindex2);
            if(s2 == null) {
                this.indexOfTuplesWithIncompleteSortKey.add(this.mapOfDatasetAfterSort.get(people));
            }
            StringBuilder part2 = getFirstThreeChar(s2);

            String finalKey = sortKey.append(part0).append(part1).append(part2).toString();

            people.setSortKey(finalKey);
        }

    }

    //从string中提取前三个字符，不足三个用*补齐，若为空则返回null
    private StringBuilder getFirstThreeChar(String s) {
        StringBuilder stringBuilder = new StringBuilder();
        if(s != null && s.length() != 0) {
            char[] chars = s.toCharArray();
            for(char c : chars) {
                if(stringBuilder.length() == 3) {
                    break;
                }
                if(c!=' ') {
                    stringBuilder.append(c);
                }
            }
        }

        //不足三个则用'*'补齐
        while(stringBuilder.length() != 3) {
            stringBuilder.append('*');
        }
        return stringBuilder;
    }

    //从string中提取前三个字母，不足三个用*补齐
    private StringBuilder getFirstThreeLetter(String s) {
        StringBuilder stringBuilder = new StringBuilder();
        if(s != null && s.length() != 0) {
            char[] chars = s.toCharArray();
            for(char c : chars) {
                if(stringBuilder.length() == 3) {
                    break;
                }
                if(c >= 'a' && c <= 'z') {
                    stringBuilder.append(c);
                }
            }
        }

        //不足三个则用'*'补齐
        while(stringBuilder.length() != 3) {
            stringBuilder.append('*');
        }
        return stringBuilder;
    }

    //判断字母是否是辅音字母
    private boolean isConsonant(char c) {
        if(c >= 'a' && c <= 'z' && c!= 'a' && c!= 'e' && c!= 'i' && c!= 'o' && c!= 'u') {
            return true;
        }
        return false;
    }

    //从string中提取前三个辅音字母，不足三个则用'#'补齐
    private StringBuilder getFirstThreeConsonant(String s) {
        StringBuilder stringBuilder = new StringBuilder();
        if(s != null && s.length() != 0) {
            char[] chars = s.toCharArray();
            for(char c : chars) {
                if(stringBuilder.length() == 3) {
                    break;
                }
                if(isConsonant(c)) {
                    stringBuilder.append(c);
                }
            }
        }
        //不足三个则用'#'补齐
        while(stringBuilder.length() != 3) {
            stringBuilder.append('#');
        }
        return stringBuilder;
    }

    //提取前三个数字，不足三个用0补齐
    private StringBuilder getFirstThreeNumber(String s) {
        StringBuilder stringBuilder = new StringBuilder();
        if(s != null && s.length() != 0) {
            char[] chars = s.toCharArray();
            for(char c : chars) {
                if(stringBuilder.length() == 3) {
                    break;
                }
                if(c >= '0' && c <= '9') {
                    stringBuilder.append(c);
                }
            }
        }

        //不足三个则用'0'补齐
        while(stringBuilder.length() != 3) {
            stringBuilder.append('0');
        }
        return stringBuilder;
    }


    private void formSet() {
        this.mapOfDatasetAfterSort = new HashMap<People, Integer>();
        int index = 0;
        for(People p : this.dataset) {
            this.mapOfDatasetAfterSort.put(p, index);
            index++;
        }
    }


    //[1]读取数据
    private void getData() throws IOException {
        this.dataset = new ArrayList<People>();

        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(DATA_SET)));
        this.fields = new ArrayList<String >();
        String[] fieldsArray = br.readLine().split(",");
        for(String temp : fieldsArray) {
            this.fields.add(temp);
        }

        String line = null;
        while ((line = br.readLine()) != null) {
//            System.out.println(line);
            if (line.length() != 0) {
                String[] item = line.split(",");
                People people = new People();

                people.setRec_id(item[0]);
//                people.setRec2_id(item[1]);
                people.setCulture(getRidOfSpace(item[1], 3));
                people.setSex(getRidOfSpace(item[2], 1));
                people.setAge(getRidOfSpace(item[3], 1));
                people.setDate_of_birth(getRidOfSpace(item[4], 1));
                people.setTitle(getRidOfSpace(item[5], 1));
                people.setGiven_name(getRidOfSpace(item[6], 1));
                people.setSurname(getRidOfSpace(item[7], 1));
                people.setState(getRidOfSpace(item[8], 1));
                people.setSuburb(getRidOfSpace(item[9], 1));
                people.setPostcode(getRidOfSpace(item[10], 1));
                people.setStreet_number(getRidOfSpace(item[11], 1));
                people.setAddress_1(getRidOfSpace(item[12], 1));
                people.setAddress_2(getRidOfSpace(item[13], 1));
                people.setPhone_number(getRidOfSpace(item[14], 1));
                people.setSoc_sec_id(getRidOfSpace(item[15], 1));
                people.setBlocking_number(getRidOfSpace(item[16], 1));
                people.setFamily_role(getRidOfSpace(item[17], 1));

                this.dataset.add(people);
            }

        }

    }

    //剔除数据s中的num个空格
    private String getRidOfSpace(String s, int num) {
        char[] c = s.toCharArray();
        if(s.length() == num && c[s.length() - 1] == ' ') {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        for(int i = num; i < s.length(); i++) {
            stringBuilder.append(c[i]);
        }
        return stringBuilder.toString();
    }

}