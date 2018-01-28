import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Created by Administrator on 2018/1/27.
 */
public class Data {
    //    public static final String DATA_SET = "D:\\毕业设计\\1数据集\\dataset-5-4-3-1-1-uniform-all-0.csv";
//    public static final String DATA_SET = "D:\\毕业设计\\1数据集\\dataset-500-100-5-2-2-uniform-all-0.csv";
//    public static final String DATA_SET = "D:\\毕业设计\\1数据集\\dataset-500-500-1-1-1-uniform-all-0.csv";
//    public static final String DATA_SET = "D:\\毕业设计\\1数据集\\dataset-5000-1000-5-2-2-uniform-all-0.csv";

    public static final String DATA_SET = "D:\\毕业设计\\1数据集\\dataset-10000-5000-1-1-1-uniform-all-0.csv";

//    public static final String DATA_SET = "D:\\毕业设计\\1数据集\\dataset-20000-1000-5-2-2-uniform-all-0.csv";
//    public static final String DATA_SET = "D:\\毕业设计\\1数据集\\dataset-50000-1000-5-2-2-uniform-all-0.csv";
//    public static final String DATA_SET = "D:\\毕业设计\\1数据集\\dataset-100000-5000-1-1-1-uniform-all-0.csv";


    private ArrayList<People> dataset;
    private int fieldsNumber;
    private FieldVector[][] input;
    private double[][] target;
    private ArrayList<People> trainset;
    private FieldVector[][] trainInput;
    private double[][] trainTarget;


    public int getFieldsNumber() {
        return fieldsNumber;
    }
    public FieldVector[][] getInput() {
        return this.input;
    }

    public double[][] getTarget() {
        return  this.target;
    }

    public FieldVector[][] getTrainInput(double proportion) {
        return this.trainInput;
    }

    public double[][] getTrainTarget() {
        return  this.trainTarget;
    }


    public void getData() throws IOException {
        this.dataset = new ArrayList<People>();

        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(DATA_SET)));
        String line = null;
        String[] fieldsArray = br.readLine().split(",");
        this.fieldsNumber = fieldsArray.length;
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

    //percent:训练数据集大小占总数据量的比例
    //由于数据集中的重复记录是均匀分配的，所以这里取从start位置开始长度为size的一段连续数据作为训练数据集合
    public void getTrainset(double proportion) {
        int size = (int)((double)this.dataset.size() * proportion);
        Random random = new Random();
        int start = random.nextInt(this.dataset.size() - size + 1);
        this.trainset = new ArrayList<People>();
        for(int i = start; i < start + size; i++) {
            this.trainset.add(this.dataset.get(i));
        }

    }

    public void calTrainInput(double proportion) throws IOException{
        getData();
        getTrainset(proportion);
        int size = this.trainset.size();
        this.trainInput = new FieldVector[size][size];
        this.trainTarget = new double[size][size];

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if(i == j) {
                    this.trainInput[i][i] = new FieldVector(1.0, 1.0, 1.0, 1.0, 1.0,  1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0);
                    this.trainTarget[i][i] = 1.0;
                } else {
                    People p1 = this.trainset.get(i);
                    People p2 = this.trainset.get(j);
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
                    FieldVector fieldVector = new FieldVector(simi0, simi1, simi2, simi3, simi4, simi5, simi6, simi7, simi8, simi9, simi10, simi11, simi12, simi13, simi14, simi15, simi16);
                    this.trainInput[i][j] = fieldVector;


                    String s1 = p1.getRec_id();
                    String s2 = p2.getRec_id();
                    String[] array1 = s1.split("-");
                    String[] array2 = s2.split("-");
                    if(!(array1[2].equals("org") && array2[2].equals("org")) && (array1[1].equals(array2[1]))) {
                        this.trainTarget[i][j] = 1.0;
                    } else {
                        this.trainTarget[i][j] = 0.0;
                    }
                }
            }
        }


    }

    public double calFieldSimilarity(String s1, String s2) {
        JaroWinklerDistance jaroWinklerDistance = new JaroWinklerDistance();

        double result = (double) jaroWinklerDistance.getDistance(s1, s2);

        return result;
    }
}