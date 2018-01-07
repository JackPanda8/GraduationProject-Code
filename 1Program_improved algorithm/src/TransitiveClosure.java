/**
 * Created by JackPanda8 on 2018/1/2.
 */

import java.util.*;

public class TransitiveClosure {
    //计算传递闭包的warshell算法
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
}