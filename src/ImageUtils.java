import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.color.ColorSpace;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ImageUtils {

    //灰度图像转二维数组
    public  int[][] convertToArray(BufferedImage grayImage) {
        // 获取图像的宽度和高度
        int width = grayImage.getWidth();
        int height = grayImage.getHeight();

        // 创建一个二维数组来存储灰度值
        int[][] grayArray = new int[height][width];

        // 遍历图像的每个像素
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // 获取当前像素的灰度值
                // 对于灰度图像，getRGB()返回的int值的低8位是灰度值
                int pixel = grayImage.getRGB(x, y);
                int grayValue = pixel & 0xFF; // 提取灰度值
                grayArray[y][x] = grayValue; // 存储到二维数组中
            }
        }

        return grayArray;
    }

    //数组转图像
    public  BufferedImage convertToImage(int[][] grayArray) {
        // 获取二维数组的高度和宽度
        int height = grayArray.length;
        int width = grayArray[0].length;

        // 创建一个新的灰度图像
        BufferedImage grayImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        // 遍历二维数组，将灰度值填充到图像中
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // 获取当前像素的灰度值
                int grayValue = grayArray[y][x];

                // 确保灰度值在0到255之间
                grayValue = Math.max(0, Math.min(255, grayValue));

                // 将灰度值转换为RGB值（灰度图像的RGB值是相同的）
                int rgb = (grayValue << 16) | (grayValue << 8) | grayValue;

                // 设置图像的像素值
                grayImage.setRGB(x, y, rgb);
            }
        }

        return grayImage;
    }

    //彩图灰度化
    public  BufferedImage convertToGray(BufferedImage colorImage) {
        // 创建一个与原始图像大小相同的灰度图像
        BufferedImage grayImage = new BufferedImage(
                colorImage.getWidth(),
                colorImage.getHeight(),
                BufferedImage.TYPE_BYTE_GRAY);

        // 使用ColorConvertOp将彩色图像转换为灰度图像
        ColorConvertOp op = new ColorConvertOp(
                ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
        op.filter(colorImage, grayImage);

        return grayImage;
    }

    // 保存图像到文件
    public  void saveImage(BufferedImage image, String filePath, String format) throws IOException {
        ImageIO.write(image, format, new File(filePath));
    }

    //字符串转bit列表
    public  List<Integer> StrToList(String s) {
        int i = Integer.parseInt(s);
        List<Integer> L = new ArrayList<>();
        for (int x = 0; x < s.length(); x++) {
            int temp = (int) Math.floor(i / Math.pow(10, s.length() - 1 - x));
            i = i % (int) Math.pow(10, s.length() - 1 - x);
            String to2 = Integer.toBinaryString(temp);
            to2 = String.format("%4s", to2).replace(' ', '0');
            for (int y = 0; y < to2.length(); y++) {
                L.add(Integer.parseInt(String.valueOf(to2.charAt(y))));
            }
        }
        return L;
    }

    // 将List<Integer>转换为字符串
    public String listToStr(List<Integer> bitList) {
        if (bitList == null || bitList.isEmpty()) {
            throw new IllegalArgumentException("输入列表不能为空");
        }

        StringBuilder result = new StringBuilder();
        StringBuilder binaryStringBuilder = new StringBuilder();

        // 将列表中的每个bit拼接成二进制字符串
        for (Integer bit : bitList) {
            if (bit != 0 && bit != 1) {
                throw new IllegalArgumentException("列表中的值必须是0或1");
            }
            binaryStringBuilder.append(bit);
        }

        // 将二进制字符串按4位一组分割，转换为字符
        String binaryString = binaryStringBuilder.toString();
        for (int i = 0; i < binaryString.length(); i += 4) {
            String chunk = binaryString.substring(i, Math.min(i + 4, binaryString.length()));
            int decimalValue = Integer.parseInt(chunk, 2); // 将4位二进制转换为十进制
            result.append(decimalValue); // 将十进制值追加到结果字符串
        }

        return result.toString();
    }

    // 计算两个整数列表的bit匹配率
    public  double calculateBitMatchRate(List<Integer> originalData, List<Integer> extractedData) {
        if (originalData == null || extractedData == null || originalData.isEmpty() || extractedData.isEmpty()) {
            throw new IllegalArgumentException("输入数据不能为空");
        }

        int totalBits = originalData.size() * 32; // 总bit数量（每个Integer有32位）
        int matchedBits = 0; // 匹配的bit数量

        // 逐整数比较
        for (int i = 0; i < Math.min(originalData.size(), extractedData.size()); i++) {
            int originalInt = originalData.get(i);
            int extractedInt = extractedData.get(i);

            // 逐bit比较
            for (int j = 0; j < 32; j++) {
                int originalBit = (originalInt >> (31 - j)) & 0x01; // 提取原始数据的bit
                int extractedBit = (extractedInt >> (31 - j)) & 0x01; // 提取提取数据的bit

                if (originalBit == extractedBit) {
                    matchedBits++;
                }
            }
        }

        // 计算匹配率
        return (double) matchedBits / totalBits;
    }

    // 计算两幅灰度图像的MSE
    public static double calculateMSE(BufferedImage image1, BufferedImage image2) {
            int width = image1.getWidth();
            int height = image1.getHeight();

            double mse = 0;

            // 遍历每个像素并计算差异
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    int pixel1 = image1.getRGB(i, j);
                    int pixel2 = image2.getRGB(i, j);

                    // 获取灰度值（假设图像为灰度图像，像素值为单一的R, G, B）
                    int gray1 = (pixel1 >> 16) & 0xff;  // 提取红色通道值
                    int gray2 = (pixel2 >> 16) & 0xff;  // 提取红色通道值

                    // 计算平方差并累加
                    mse += Math.pow(gray1 - gray2, 2);
                }
            }

            // 计算均方误差（平均平方差）
            mse /= (width * height);

            return mse;
    }



}
