/**
 * Created by Administrator on 2018/3/27.
 */


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

public class FileIO {
    /**
     * 功能：Java读取txt文件的内容 步骤：
     * 1：先获得文件句柄
     * 2：获得文件句柄当做是输入一个字节码流，需要对这个输入流进行读取
     * 3：读取到输入流后，需要读取生成字节流
     *  4：一行一行的输出。readline()。 备注：需要考虑的是异常情况
     * @param filePath
     */
    public static Chromosome readTxtFile(String filePath, String filename) {
        try {
            String encoding = "GBK";
            File file = new File(filePath+filename);
            if (file.isFile() && file.exists()) { // 判断文件是否存在
                InputStreamReader read = new InputStreamReader(new FileInputStream(file), encoding);// 考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;
                while ((lineTxt = bufferedReader.readLine()) != null) {
//                    System.out.println(lineTxt);
                    String[] array = lineTxt.split(",");
                    float[] gene = new float[array.length];
                    for(int i = 0; i < array.length; i++) {
                        gene[i] = Float.parseFloat(array[i]);
                    }
                    Chromosome chromosome = new Chromosome(gene);
                    return chromosome;
                }
                read.close();

            }
        } catch (Exception e) {
            System.out.println("读取文件内容出错");
            e.printStackTrace();
        }

        return null;
    }

    public static void writeTxtFile(String filepath, String filename, Chromosome best){
        File file=new File(filepath+filename);
        BufferedWriter writer = null;
        try {
            if(file.isFile()&&!file.exists()){
                System.out.println("找不到指定的文件");
                file.createNewFile();// 不存在则创建
            }
            else{
                //writer = new BufferedWriter(new FileWriter(file,true)); //这里加入true 可以不覆盖原有TXT文件内容 续写
                writer = new BufferedWriter(new FileWriter(file));
                float[] gene = best.getGene();
                for(int i = 0; i < gene.length; i++) {
                    if(i == gene.length -1) {
                        writer.write(String.valueOf(gene[i]));
                    } else {
                        writer.write(String.valueOf(gene[i]) + ",");
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
