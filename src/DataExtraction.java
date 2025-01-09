import java.util.ArrayList;
import java.util.List;

public class DataExtraction {

    public  List<Integer> extract(int[][] dImage, String embedKey, int length) {
        int rows = dImage.length;
        int cols = dImage[0].length;
        List<Integer> keystream = generateKeystream(embedKey, rows * cols);
        StringBuilder embedKeyBuilder = new StringBuilder();
        for (Integer k : keystream) {
            embedKeyBuilder.append(k < 100 ? '0' : '1');
        }
        char[] embedKeyArray = embedKeyBuilder.toString().toCharArray();
        int[][] H0 = new int[rows][cols];
        int[][] H1 = new int[rows][cols];
        int[][] H2 = new int[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                H0[i][j] = dImage[i][j];
                H1[i][j] = dImage[i][j];
            }
        }
        List<Integer> data = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 8; j++) {
                for (int x = (int) (rows / 4 * i); x < (int) (rows / 4 * (i + 1)); x++) {
                    for (int y = (int) (cols / 8 * j); y < (int) (cols / 8 * (j + 1)); y++) {
                        if (embedKeyArray[x * cols + y] == '0') {
                            H0[x][y] ^= 0b00000111;
                        } else {
                            H1[x][y] ^= 0b00000111;
                        }
                    }
                }
                int f1 = 0;
                int f2 = 0;
                for (int x = (int) (rows / 4 * i) + 1; x < (int) (rows / 4 * (i + 1)) - 1; x++) {
                    for (int y = (int) (cols / 8 * j) + 1; y < (int) (cols / 8 * (j + 1)) - 1; y++) {
                        f1 += Math.abs(H0[x][y] - (H0[x - 1][y] + H0[x + 1][y] + H0[x][y - 1] + H0[x][y + 1]) / 4);
                        f2 += Math.abs(H1[x][y] - (H1[x - 1][y] + H1[x + 1][y] + H1[x][y - 1] + H1[x][y + 1]) / 4);
                    }
                }
                if (f1 < f2) {
                    data.add(0);
                    for (int x = (int) (rows / 4 * i); x < (int) (rows / 4 * (i + 1)); x++) {
                        for (int y = (int) (cols / 8 * j); y < (int) (cols / 8 * (j + 1)); y++) {
                            H2[x][y] = H0[x][y];
                        }
                    }
                } else {
                    data.add(1);
                    for (int x = (int) (rows / 4 * i); x < (int) (rows / 4 * (i + 1)); x++) {
                        for (int y = (int) (cols / 8 * j); y < (int) (cols / 8 * (j + 1)); y++) {
                            H2[x][y] = H1[x][y];
                        }
                    }
                }
            }
        }
        return data;
    }

    private  List<Integer> generateKeystream(String key, int length) {
        int[] S = new int[256];
        for (int i = 0; i < 256; i++) {
            S[i] = i;
        }
        int j = 0;
        for (int i = 0; i < 256; i++) {
            j = (j + S[i] + key.charAt(i % key.length())) % 256;
            int temp = S[i];
            S[i] = S[j];
            S[j] = temp;
        }
        int a = 0;
        int b = 0;
        List<Integer> keystream = new ArrayList<>();
        for (int c = 0; c < length; c++) {
            a = (a + 1) % 256;
            b = (b + S[a]) % 256;
            int temp = S[a];
            S[a] = S[b];
            S[b] = temp;
            int k = S[(S[a] + S[b]) % 256];
            keystream.add(k);
        }
        return keystream;
    }
}