/**
 * Created by JackPanda8 on 2018/1/2.
 */
import java.util.*;

public class TransitiveClosure {
    //计算传递闭包的warshell算法-基于邻接矩阵
    public static int[][] getTransitiveClosure(int[][] matrix, int N) {
        for(int i = 0; i < N; i++) {
            for(int j = 0; j < N; j++) {
                if(matrix[i][j] == 1) {
                    for(int k = 0; k < N; k++) {
                        if(matrix[j][k] == 1) {
                            matrix[i][k] = 1;
                        }
                    }
                }
            }
        }

        return matrix;
    }

    //计算传递闭包的队列实现算法-基于邻接表
    public static void getTransitiveClosureOfList(HashMap<Integer, ArrayList<Integer>> map){
        Set<Integer> set = map.keySet();
        Object[] array = set.toArray();
        for(Object i : array) {
            int key = (int)i;
            ArrayList<Integer> value = map.get(key);

            int[] flag = new int[array.length];
            for(int j = 0; j < array.length; j++) {
                flag[j] = 0;
            }

            Queue<Integer> queue = new PriorityQueue<Integer>();
            for(int x : value) {
                queue.add(x);//将直系的后缀加入优先级队列
                flag[x] = 1;
            }
            flag[key] = 1;

            while (!queue.isEmpty()) {
                int head = queue.poll();
                if(!value.contains(head) && flag[head] == 0) {
                    value.add(head);
                    flag[head] = 1;
                }

                for(int temp : map.get(head)) {
                    if(!queue.contains(temp) && flag[temp] == 0) {
                        queue.add(temp);
                    }
                }
            }


            map.put(key, value);
        }

    }
}