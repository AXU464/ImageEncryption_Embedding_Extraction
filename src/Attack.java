import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.math.BigInteger;
import java.util.ArrayList;


public class Attack {
    public static void main(String[] args) {
        try {
            String embedfile = "image/embed/embed_6.png";
            String originfile="image/origin/6.jpg";

            // 加密密钥实际未知,加密和解密实际上是一个密钥

            //猜测的加密密钥的起始点
            BigInteger encryptstart = new BigInteger("1234567890123400");
            //猜测的加密密钥的终止点
            BigInteger encryptend = new BigInteger("1234567890123457");
            //猜测的嵌入密钥的起始点
            int embedkeystart = 12345650;
            //猜测的嵌入密钥的终止点
            int embedkeyend = 12345700;

            // 设置分块大小
            int S = 32;
            //真实嵌入数据，用于比较判断获得爆破成功
            String realmessage = "12345678";

            ImageEncryption enc = new ImageEncryption();
            DataExtraction ext = new DataExtraction();
            ImageUtils util = new ImageUtils();

            // 加载嵌入后的图像
            BufferedImage embeddedImage = ImageIO.read(new File(embedfile));

            // 加载原始的图像
            BufferedImage originImage = ImageIO.read(new File(originfile));
            BufferedImage grayoriginImage = util.convertToGray(originImage);

            // 检查图像是否成功加载
            if (embeddedImage == null) {
                System.err.println("加载图像失败，请检查文件路径！");
                return;
            }


            String bestEncryptionKey = null; // 用于存储最小MSE对应的密钥
            double minmse = Double.MAX_VALUE; // 用于记录最小MSE
            for (BigInteger i = encryptstart; i.compareTo(encryptend) < 0;  i = i.add(BigInteger.ONE)) {
                // 转换为 String 类型
                String encryptionKey = i.toString();
                RC4 rc4ForDecryption = new RC4(encryptionKey); // 重新初始化RC4用于解密
                int[][] embedArray2 = util.convertToArray(embeddedImage);
                int[][] decArray1 = enc.encryptArray(embedArray2, rc4ForDecryption);//解密
                BufferedImage decryptedImage = util.convertToImage(decArray1);
                double currmse = util.calculateMSE(decryptedImage, grayoriginImage);
                // 如果当前 MSE 小于最小 MSE，更新最小 MSE 和对应的加密密钥
                if (currmse < minmse) {
                    minmse = currmse;
                    bestEncryptionKey = encryptionKey;
                }
                System.out.println("尝试加密密钥为: " + encryptionKey + "，MSE: " + currmse);
            }
            // 输出最小 MSE 对应的加密密钥
            System.out.println("最小 MSE 对应的加密密钥: " + bestEncryptionKey);
            System.out.println("最小 MSE: " + minmse);

            if(bestEncryptionKey == null) {
                System.out.println("未找到对应的加密密钥 " );
            }
            else{
                // 对嵌入数据后的加密图像进行解密
                RC4 rc4ForDecryption = new RC4(bestEncryptionKey); // 重新初始化RC4用于解密
                int[][] embedArray2 = util.convertToArray(embeddedImage);
                int[][] decArray1 = enc.encryptArray(embedArray2, rc4ForDecryption); // 解密
                BufferedImage decryptedImage = null;
                decryptedImage = util.convertToImage(decArray1); // 获得解密后的图像

                List<String> foundKeys = new ArrayList<>(); // 用于存储所有可能的密钥
                List<String> extractedDataList = new ArrayList<>(); // 用于存储每个密钥提取到的数据

               // 嵌入密钥实际未知
                for (int i = embedkeystart; i < embedkeyend; i++) {
                    String embeddingKey = String.format("%08d", i); // 生成八位数的字符串
                    System.out.println("本次尝试密钥为: " + embeddingKey);

                    // 从解密后的图像中根据该次猜测的嵌入密钥提取信息
                    int[][] decArray2 = util.convertToArray(decryptedImage);
                    List<Integer> data = ext.extract(decArray2, embeddingKey, S);
                    String extractedData = util.listToStr(data);

                    // 计算比特匹配率
                    double bitMatchRate = util.calculateBitMatchRate(util.StrToList(realmessage), data);

                    // 判断比特匹配率
                    if (bitMatchRate == 1.0) {
                        System.out.println("破解出的嵌入密钥为: " + embeddingKey);
                        System.out.println("提取到的数据为: " + data);
                        System.out.println("字符串表示: " + extractedData);
                        System.out.println("比特匹配率: " + bitMatchRate);
                        System.out.println("该次破解成功");

                        // 将找到的密钥和对应提取到的数据加入列表
                        foundKeys.add(embeddingKey);
                        extractedDataList.add(extractedData);
                    } else {
                        System.out.println("猜测的嵌入密钥为: " + embeddingKey);
                        System.out.println("提取数据: " + data);
                        System.out.println("字符串表示: " + extractedData);
                        System.out.println("比特匹配率: " + bitMatchRate);
                        System.out.println("该次嵌入密钥猜测失败，即将尝试下一个密钥...");
                    }
                }

                // 输出最终结果
                if (foundKeys.isEmpty()) {
                    System.out.println("密钥爆破失败，未能找到任何正确的密钥！");
                } else {
                    System.out.println("密钥爆破完成，共找到以下可能的密钥：");
                    for (int j = 0; j < foundKeys.size(); j++) {
                        System.out.println("密钥: " + foundKeys.get(j));
                        System.out.println("提取数据: " + extractedDataList.get(j));
                    }
                }


            }
        } catch (IOException e) {
            System.err.println("读取图像文件时发生错误，请检查文件路径或文件格式是否正确！");
            e.printStackTrace();
        }
    }
}
