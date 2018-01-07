import java.util.*;
import java.io.*;

enum Comparation {
    Lower, Equal, Higher
}

public class Main {
//    public static final String DATA_SET = "D:\\毕业设计\\1数据集\\dataset-500-100-5-2-2-uniform-all-0.csv";
//    public static final String DATA_SET = "D:\\毕业设计\\1数据集\\dataset-500-500-1-1-1-uniform-all-0.csv";

    public static final String DATA_SET = "D:\\毕业设计\\1数据集\\dataset-10000-5000-1-1-1-uniform-all-0.csv";
    public static final int SLIDEWINDOW_SIZE = 5;//滑动窗口的大小
    public static final double VERY_CLOSE_CONSTANT = 0.8;//暂时定义very_close的衡量尺度为相似度>=0.8
    public static final double CLOSE_CONSTANT = 0.6;//暂时定义close的衡量尺度为相似度>=0.6,所以0.6~0.8即为close_but_not_much的范围

    private ArrayList<People> dataset;
    private ArrayList<People> datasetAfterSort;
    private HashMap<People, Integer> mapOfDatasetAfterSort;
    private ArrayList<People> datasetAfterSort1;
    private ArrayList<People> datasetAfterSort2;
    private HashSet<People> duplicateTuples;
    private int[][] duplicateMatrix;
    private ArrayList<People> cleanDataset;

    public static void main(String[] args) throws IOException{
        int i = 0;
        for(; i < 5; i++) {
            if(i == 4) {
                break;
            }
        }

        Main main = new Main();
        main.getData();

        //第一趟
        main.generateKey();
        main.quickSort();
        main.formSet();//将people与duplicateMatrix建立1对1的映射，以便计算传递闭包用
        main.slideWindowProcess(SLIDEWINDOW_SIZE);

        //第二趟
        main.generateKey1();
        main.quickSort1();
        main.slideWindowProcess1(SLIDEWINDOW_SIZE);

        //第三趟
        main.generateKey2();
        main.quickSort2();
        main.slideWindowProcess2(SLIDEWINDOW_SIZE);


        main.eliminateDuplication();

        int numDup = 0;
        int numOri = 0;
        for(People p: main.duplicateTuples) {
//            System.out.println(p.getRec_id());
            String id = p.getRec_id();
            if(id.contains("dup")) {
                numDup++;
            } else {
                numOri++;
            }
        }
        System.out.println("dup : "+numDup + ", ori : "+numOri);

    }





    //[5]消除重复
    private void eliminateDuplication() {
        this.cleanDataset = new ArrayList<People>();
        this.duplicateMatrix = TransitiveClosure.getTransitiveClosure(this.duplicateMatrix, this.datasetAfterSort.size());

        for(int i = 0; i < this.datasetAfterSort.size(); i++) {
            int j = 0;
            for(; j < i; j++) {
                if(this.duplicateMatrix[i][j] == 1) {
                    break;
                }
            }
            if(j == i) {
                this.cleanDataset.add(this.datasetAfterSort.get(i));
            }

        }
    }





    //[4.1]滑动窗口归并,w是滑动窗口的大小
    private void slideWindowProcess(int w) {
        if(w > this.datasetAfterSort.size()) {
            System.out.println("Error：滑动窗口的大小" + w + "超过了数据集的大小" + this.datasetAfterSort.size());
        }

        int size = this.datasetAfterSort.size();
        this.duplicateMatrix = new int[size][size];
        for(int i = 0; i < size; i++) {
            for(int j = 0; j < size; j++) {
                this.duplicateMatrix[i][j] = 0;
            }
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
                        this.duplicateTuples.add(this.datasetAfterSort.get(i));
                        this.duplicateTuples.add(this.datasetAfterSort.get(tail));

                        this.duplicateMatrix[tail][i] = 1;
                        this.duplicateMatrix[i][tail] = 1;
                    }
                }
            } else {
                for(int i = tail-w+1; i < tail; i++) {
                    People tempPeople = this.datasetAfterSort.get(i);
                    if(judgeEqual(tempPeople, tailPeople)) {
                        this.duplicateTuples.add(this.datasetAfterSort.get(i));
                        this.duplicateTuples.add(this.datasetAfterSort.get(tail));

                        this.duplicateMatrix[tail][i] = 1;
                        this.duplicateMatrix[i][tail] = 1;
                    }
                }
            }
            tail++;
        }
    }

    //[4.2]滑动窗口归并,w是滑动窗口的大小
    private void slideWindowProcess1(int w) {
        if(w > this.datasetAfterSort1.size()) {
            System.out.println("Error：滑动窗口的大小" + w + "超过了数据集的大小" + this.datasetAfterSort1.size());
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
                        this.duplicateTuples.add(this.datasetAfterSort.get(indexI));
                        this.duplicateTuples.add(this.datasetAfterSort.get(indexTail));

                        this.duplicateMatrix[indexTail][indexI] = 1;
                        this.duplicateMatrix[indexI][indexTail] = 1;
                    }
                }
            } else {
                for(int i = tail-w+1; i < tail; i++) {
                    People tempPeople = this.datasetAfterSort1.get(i);
                    if(judgeEqual(tempPeople, tailPeople)) {
                        int indexI = this.mapOfDatasetAfterSort.get(this.datasetAfterSort1.get(i));
                        int indexTail = this.mapOfDatasetAfterSort.get(this.datasetAfterSort1.get(tail));
                        this.duplicateTuples.add(this.datasetAfterSort.get(indexI));
                        this.duplicateTuples.add(this.datasetAfterSort.get(indexTail));

                        this.duplicateMatrix[indexTail][indexI] = 1;
                        this.duplicateMatrix[indexI][indexTail] = 1;
                    }
                }
            }
            tail++;
        }
    }

    //[4.3]滑动窗口归并,w是滑动窗口的大小
    private void slideWindowProcess2(int w) {
        if(w > this.datasetAfterSort2.size()) {
            System.out.println("Error：滑动窗口的大小" + w + "超过了数据集的大小" + this.datasetAfterSort2.size());
        }

//        int tail = w-1;
        int tail = 0;
        while(tail < this.datasetAfterSort2.size()) {
            People tailPeople = this.datasetAfterSort2.get(tail);
            if(tail < w-1) {//比较起始情况下，前w大小的滑动窗口内重复记录
                for(int i = 0; i < tail; i++) {
                    People tempPeople = this.datasetAfterSort2.get(i);
                    if(judgeEqual(tempPeople, tailPeople)) {
                        int indexI = this.mapOfDatasetAfterSort.get(this.datasetAfterSort2.get(i));
                        int indexTail = this.mapOfDatasetAfterSort.get(this.datasetAfterSort2.get(tail));
                        this.duplicateTuples.add(this.datasetAfterSort.get(indexI));
                        this.duplicateTuples.add(this.datasetAfterSort.get(indexTail));

                        this.duplicateMatrix[indexTail][indexI] = 1;
                        this.duplicateMatrix[indexI][indexTail] = 1;
                    }
                }
            } else {
                for(int i = tail-w+1; i < tail; i++) {
                    People tempPeople = this.datasetAfterSort2.get(i);
                    if(judgeEqual(tempPeople, tailPeople)) {
                        int indexI = this.mapOfDatasetAfterSort.get(this.datasetAfterSort2.get(i));
                        int indexTail = this.mapOfDatasetAfterSort.get(this.datasetAfterSort2.get(tail));
                        this.duplicateTuples.add(this.datasetAfterSort.get(indexI));
                        this.duplicateTuples.add(this.datasetAfterSort.get(indexTail));

                        this.duplicateMatrix[indexTail][indexI] = 1;
                        this.duplicateMatrix[indexI][indexTail] = 1;
                    }
                }
            }
            tail++;
        }
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




    //[3.2]排序，使用快速排序
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
    //[3.2]排序，使用快速排序
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





    //[2.1]生成key，部分参考论文中的选择依据：surname的前三个辅音字母 +  given_name的前三个字母 + street_number前三个数字连接address_1的前三个辅音字母
    private void generateKey() {
        for(People people : this.dataset) {
            StringBuilder sortKey = new StringBuilder();
            sortKey.append(getFirstThreeConsonant(people.getSurname()));
            sortKey.append(getFirstThreeLetter(people.getGiven_name()));

            String s = people.getStreet_number();
            StringBuilder sb = new StringBuilder();
            sb.append(getFirstThreeNumber(s));
            sb.append(getFirstThreeConsonant(people.getAddress_1()));
            sortKey.append(sb);

            people.setSortKey(sortKey.toString());
        }
    }

    //[2.2]生成key : culture + surname的前三个辅音字母 + given_name的前三个字母
    private void generateKey1() {
        for(People people : this.dataset) {
            StringBuilder sortKey = new StringBuilder();
            String culture = people.getCulture();
            if(culture == null) {
                culture = "eng";
            }
            sortKey.append(culture);
            sortKey.append(getFirstThreeConsonant(people.getSurname()));

            //            String birthDate = people.getDate_of_birth();
//            if(birthDate == null) {
//                birthDate = "19700101";
//            }
//            sortKey.append(birthDate);

            sortKey.append(getFirstThreeLetter(people.getGiven_name()));


            people.setSortKey(sortKey.toString());
        }
    }

    //[2.3]生成key : soc_sec_id的前6位 + surname的前三个辅音字母 + given_name的前三个字母
    private void generateKey2() {
        for(People people : this.dataset) {
            StringBuilder sortKey = new StringBuilder();
            String socID = people.getSoc_sec_id();
            if(socID == null) {
                socID = "0000000";
            }
            String partOfSocID = socID.substring(0, 5);
            sortKey.append(partOfSocID);
            sortKey.append(getFirstThreeConsonant(people.getSurname()));
            String birthDate = people.getDate_of_birth();
            if(birthDate == null) {
                birthDate = "19700101";
            }

//            sortKey.append(birthDate);
            sortKey.append(getFirstThreeLetter(people.getGiven_name()));

            people.setSortKey(sortKey.toString());
        }
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
        for(People p : this.datasetAfterSort) {
            this.mapOfDatasetAfterSort.put(p, index);
            index++;
        }
    }


    //[1]读取数据
    private void getData() throws IOException {
        this.dataset = new ArrayList<People>();

        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(DATA_SET)));
        String line = null;
        br.readLine();
        while ((line = br.readLine()) != null) {
//            System.out.println(line);
            if (line.length() != 0) {
                String[] item = line.split(",");
                People people = new People();

                people.setRec_id(item[0]);
//                people.setRec2_id(item[1]);
                people.setCulture(getRidofSpace_uncertainNum(item[1]));
                people.setSex(getRidofSpace_uncertainNum(item[2]));
                people.setAge(getRidofSpace_uncertainNum(item[3]));
                people.setDate_of_birth(getRidofSpace_uncertainNum(item[4]));
                people.setTitle(getRidofSpace_uncertainNum(item[5]));
                people.setGiven_name(getRidofSpace_uncertainNum(item[6]));
                people.setSurname(getRidofSpace_uncertainNum(item[7]));
                people.setState(getRidofSpace_uncertainNum(item[8]));
                people.setSuburb(getRidofSpace_uncertainNum(item[9]));
                people.setPostcode(getRidofSpace_uncertainNum(item[10]));
                people.setStreet_number(getRidofSpace_uncertainNum(item[11]));
                people.setAddress_1(getRidofSpace_uncertainNum(item[12]));
                people.setAddress_2(getRidofSpace_uncertainNum(item[13]));
                people.setPhone_number(getRidofSpace_uncertainNum(item[14]));
                people.setSoc_sec_id(getRidofSpace_uncertainNum(item[15]));
                people.setBlocking_number(getRidofSpace_uncertainNum(item[16]));
                people.setFamily_role(getRidofSpace_uncertainNum(item[17]));

                this.dataset.add(people);
            }

        }

    }

    //剔除数据中的不确定个数的空格
    private String getRidofSpace_uncertainNum(String s) {
        char[] c = s.toCharArray();
        int length = c.length;
        int i = 0;
        for(; i < length; i++) {
            if(c[i] != ' ') {
                break;
            }
        }
        if(i == length) {
            return  null;
        }

        StringBuilder stringBuilder = new StringBuilder();
        for(; i < length; i++) {
            if(c[i] != ' ') {
                stringBuilder.append(c[i]);
            }

        }
        return stringBuilder.toString();

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