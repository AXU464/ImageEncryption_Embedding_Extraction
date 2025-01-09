public class RC4 {
    private byte[] S = new byte[256]; // S盒
    private int x = 0;
    private int y = 0;

    // 构造函数，接受字符串作为密钥
    public RC4(String key) {
        // 将字符串转换为字节数组
        byte[] keyBytes = key.getBytes();
        // 初始化S盒
        for (int i = 0; i < 256; i++) {
            S[i] = (byte) i;
        }
        // 打乱S盒
        int j = 0;
        for (int i = 0; i < 256; i++) {
            j = (j + S[i] + keyBytes[i % keyBytes.length]) & 0xFF;
            byte temp = S[i];
            S[i] = S[j];
            S[j] = temp;
        }
    }
    // 生成下一个密钥流字节
    public byte nextByte() {
        x = (x + 1) & 0xFF;
        y = (y + S[x]) & 0xFF;
        byte temp = S[x];
        S[x] = S[y];
        S[y] = temp;
        return S[(S[x] + S[y]) & 0xFF];
    }
}