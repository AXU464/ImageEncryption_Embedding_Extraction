import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        try {
            ImageEncryption enc = new ImageEncryption();
            DataEmbedding emb = new DataEmbedding();
            DataExtraction ext = new DataExtraction();
            ImageUtils util = new ImageUtils();

            // 指定输入图像文件夹路径和输出文件夹路径
            String inputFolderPath = "image/origin/";
            String encryptedFolderPath = "image/encrypt/";
            String embeddedFolderPath = "image/embed/";
            String decryptedFolderPath = "image/decrypt/";
            String grayFolderPath = "image/grayImage/";

            // 获取 origin 文件夹中的所有图像文件
            File inputFolder = new File(inputFolderPath);
            File[] imageFiles = inputFolder.listFiles((dir, name) ->
                    name.toLowerCase().endsWith(".png") ||
                            name.toLowerCase().endsWith(".jpg") ||
                            name.toLowerCase().endsWith(".jpeg"));

            if (imageFiles == null || imageFiles.length == 0) {
                System.out.println("未找到任何图像文件！");
                return;
            }

            // 密钥设置
            String encryptionKey = "1234567890123456"; //加密密钥
            String embeddingKey = "12345678"; // 嵌入密钥

            //设置分块大小
            int S = 32;
            System.out.println("分块大小S: "+S);

            // 定义嵌入数据
            String m = "12345678";
            List<Integer> message = util.StrToList(m);
            System.out.println("嵌入数据: "+m );
            System.out.println("bit表示: "+ message);

            System.out.println("---------------------------------------------------------------------------------------------");

            for (File imageFile : imageFiles) {
                String fileName = imageFile.getName();
                String baseName = fileName.substring(0, fileName.lastIndexOf('.'));

                String grayImagePath = grayFolderPath + "gray_" + baseName + ".png";
                String encryptedImagePath = encryptedFolderPath + "encrypt_" + baseName + ".png";
                String embeddedImagePath = embeddedFolderPath + "embed_" + baseName + ".png";
                String decryptedImagePath = decryptedFolderPath + "decrypt_" + baseName + ".png";

                System.out.println("正在处理图像: " + fileName);

                // 读取彩色图像
                BufferedImage colorImage = ImageIO.read(imageFile);

                // 灰度+数组化
                BufferedImage grayImage = util.convertToGray(colorImage);
                util.saveImage(grayImage, grayImagePath, "png");
                int[][] grayArray = util.convertToArray(grayImage);

                // 对灰度图像进行加密
                RC4 rc4ForEncryption = new RC4(encryptionKey);//流密码初始化
                int[][] encArray1 = enc.encryptArray(grayArray, rc4ForEncryption);//加密
                BufferedImage encryptedImage = util.convertToImage(encArray1);
                util.saveImage(encryptedImage, encryptedImagePath, "png");//保存图像

                // 信息嵌入
                int[][] encArray2 = util.convertToArray(encryptedImage);
                int[][] embedArray1 = emb.embed(encArray2, message, embeddingKey, S);
                BufferedImage embeddedImage = util.convertToImage(embedArray1);
                util.saveImage(embeddedImage, embeddedImagePath, "png");

                // 对嵌入数据后的加密图像进行解密
                RC4 rc4ForDecryption = new RC4(encryptionKey); // 重新初始化RC4用于解密
                int[][] embedArray2 = util.convertToArray(embeddedImage);
                int[][] decArray1 = enc.encryptArray(embedArray2, rc4ForDecryption);//解密
                BufferedImage decryptedImage = util.convertToImage(decArray1);
                util.saveImage(decryptedImage, decryptedImagePath, "png");

                // 从解密后的图像中提取信息
                int[][] decArray2 = util.convertToArray(decryptedImage);
                List<Integer> data = ext.extract(decArray2, embeddingKey, S);
                System.out.println("提取数据: "+data );
                String data_str=util.listToStr(data);
                System.out.println("字符串表示: "+data_str);

                double MatchRate=util.calculateBitMatchRate(message,data);// 匹配率
                System.out.println("匹配率: "+MatchRate);

                System.out.println("图像处理完成，结果已保存到对应文件夹！");
                System.out.println("---------------------------------------------------------------------------------------------");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
