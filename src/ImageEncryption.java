public class ImageEncryption {

    // 对二维数组进行加密
    public  int[][] encryptArray(int[][] grayArray, RC4 rc4) {
        int height = grayArray.length;
        int width = grayArray[0].length;

        // 创建一个新的二维数组来存储加密后的灰度值
        int[][] encryptedArray = new int[height][width];

        // 遍历二维数组
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = grayArray[y][x]; // 获取灰度值
                int encryptedPixel = 0;

                // 处理每个bit
                for (int k = 0; k < 8; k++) {
                    int bijk = (pixel / (1 << k)) % 2; // 提取bit
                    int rijk = rc4.nextByte() & 0x01; // 获取RC4密钥流的bit
                    int Bijk = bijk ^ rijk; // 异或操作
                    encryptedPixel |= (Bijk << k); // 重新组合bit
                }

                // 将加密后的灰度值存储到新数组中
                encryptedArray[y][x] = encryptedPixel;
            }
        }

        return encryptedArray;
    }
}