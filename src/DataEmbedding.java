import java.util.*;

public class DataEmbedding {

    public  int[][] embed(int[][] eImage, List<Integer> data, String embedKey, int length) {
        int rows = eImage.length;
        int cols = eImage[0].length;
        List<Integer> keystream = generateKeystream(embedKey, rows * cols);
        StringBuilder embedKeyBuilder = new StringBuilder();
        for (Integer k : keystream) {
            embedKeyBuilder.append(k < 100 ? '0' : '1');
        }
        char[] embedKeyArray = embedKeyBuilder.toString().toCharArray();
        int[][] H0 = new int[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                H0[i][j] = eImage[i][j];
            }
        }
        Queue<Integer> dataQueue = new LinkedList<>(data);
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 8; j++) {
                int message = dataQueue.poll();
                for (int x = (int) (rows / 4 * i); x < (int) (rows / 4 * (i + 1)); x++) {
                    for (int y = (int) (cols / 8 * j); y < (int) (cols / 8 * (j + 1)); y++) {
                        if (embedKeyArray[x * cols + y] == '0') {
                            if (message == 0) {
                                H0[x][y] ^= 0b00000111;
                            }
                        } else {
                            if (message == 1) {
                                H0[x][y] ^= 0b00000111;
                            }
                        }
                    }
                }
            }
        }
        return H0;
    }

    private List<Integer> generateKeystream(String key, int length) {
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