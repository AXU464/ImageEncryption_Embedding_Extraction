import math
import queue
import numpy as np
from Crypto.Cipher import ARC4
from PIL import Image


def DtoB(s):
    """
    将十进制数字字符串(如'12345678')转为二进制数组，每位数字转4位二进制，然后拼接。
    如'12' => [0001,0010] => [0,0,0,1,0,0,1,0]
    """
    i = int(s)
    L = []
    for x in range(len(s)):
        temp = math.floor(i / pow(10, len(s) - 1 - x))
        i = i % pow(10, len(s) - 1 - x)
        to2 = bin(temp)[2:].zfill(4)
        for y in range(len(to2)):
            L.append(int(to2[y]))
    return L


def generate_keystream(key, length):
    """
    生成 RC4 的 keystream。
    """
    S = list(range(256))
    j = 0
    for i in range(256):
        j = (j + S[i] + ord(key[i % len(key)])) % 256
        S[i], S[j] = S[j], S[i]
    a = 0
    b = 0
    keystream = []
    for c in range(length):
        a = (a + 1) % 256
        b = (b + S[a]) % 256
        S[a], S[b] = S[b], S[a]
        k = S[(S[a] + S[b]) % 256]
        keystream.append(k)
    return keystream


def embed(e_image, data, embedkey, length):
    """
    将 data(二进制序列)嵌入到 e_image 中
    """
    embed_key = ''
    shape = e_image.shape
    rows = shape[0]
    cols = shape[1]
    keystream = generate_keystream(embedkey, rows * cols)

    # 根据 keystream 大小来决定 embed_key 每一位是 '0' 还是 '1'
    for i in range(len(keystream)):
        temp = '0' if keystream[i] < 100 else '1'
        embed_key = embed_key + temp
    embed_key = np.array(list(embed_key))
    embed_key = np.reshape(embed_key, (rows, cols))

    H0 = np.zeros((rows, cols), dtype=np.byte)
    H0[::] = e_image

    data_queue = queue.Queue(length)
    for i in data:
        data_queue.put(i)

    # 将 e_image 分成 4 x 8 块，每块嵌入 1 bit
    for i in range(4):
        for j in range(8):
            message = data_queue.get()
            for x in range(int(rows / 4 * i), int(rows / 4 * (i + 1))):
                for y in range(int(cols / 8 * j), int(cols / 8 * (j + 1))):
                    if (embed_key[x][y] == '0'):
                        if (message == 0):
                            H0[x][y] = H0[x][y] ^ 0b00000111
                    else:
                        if (message == 1):
                            H0[x][y] = H0[x][y] ^ 0b00000111
    return H0


def extract(d_image, embedkey, length):
    """
    从 d_image 中提取数据（与 embed 对应），返回提取的二进制序列 data 和 修复后的图像 H2
    """
    embed_key = ''
    shape = d_image.shape
    rows = shape[0]
    cols = shape[1]
    keystream = generate_keystream(embedkey, rows*cols)

    for i in range(len(keystream)):
        temp = '0' if keystream[i] < 100 else '1'
        embed_key = embed_key + temp
    embed_key = np.array(list(embed_key))
    embed_key = np.reshape(embed_key, (rows, cols))

    H0 = np.zeros((rows, cols), dtype=np.int32)
    H1 = np.zeros((rows, cols), dtype=np.int32)
    H2 = np.zeros((rows, cols), dtype=np.int32)
    H0[::] = d_image
    H1[::] = d_image
    data = []

    # 与 embed 时相同的分块逻辑
    for i in range(4):
        for j in range(8):
            for x in range(int(rows / 4 * i), int(rows / 4 * (i + 1))):
                for y in range(int(cols / 8 * j), int(cols / 8 * (j + 1))):
                    if (embed_key[x][y] == '0'):
                        H0[x][y] = H0[x][y] ^ 0b00000111
                    else:
                        H1[x][y] = H1[x][y] ^ 0b00000111
            f1 = 0
            f2 = 0
            # 计算 f1, f2 判断是哪种嵌入方式(0 或 1)
            for x in range(int((rows / 4 * i) + 1), int((rows / 4 * (i + 1)) - 1)):
                for y in range(int((cols / 8 * j) + 1), int((cols / 8 * (j + 1)) - 1)):
                    f1 += abs(H0[x][y] - (H0[x - 1][y] + H0[x + 1]
                              [y] + H0[x][y - 1] + H0[x][y + 1]) / 4)
                    f2 += abs(H1[x][y] - (H1[x - 1][y] + H1[x + 1]
                              [y] + H1[x][y - 1] + H1[x][y + 1]) / 4)
            if f1 < f2:
                data.append(0)
                for x in range(int(rows / 4 * i), int(rows / 4 * (i + 1))):
                    for y in range(int(cols / 8 * j), int(cols / 8 * (j + 1))):
                        H2[x][y] = H0[x][y]
            else:
                data.append(1)
                for x in range(int(rows / 4 * i), int(rows / 4 * (i + 1))):
                    for y in range(int(cols / 8 * j), int(cols / 8 * (j + 1))):
                        H2[x][y] = H1[x][y]
    H2 = H2.astype(np.byte)
    return data, H2


def encryption(o_image, key):
    """
    RC4 加密
    """
    f_image = o_image.flatten()
    cipher = ARC4.new(key)
    e_data = cipher.encrypt(bytes(f_image))
    e_image = np.frombuffer(e_data, dtype=np.uint8).reshape(o_image.shape)
    return e_image


def decrypted(e_image, key):
    """
    RC4 解密
    """
    f_image = e_image.flatten()
    cipher = ARC4.new(key)
    d_data = cipher.decrypt(bytes(f_image))
    d_image = np.frombuffer(d_data, dtype=np.uint8).reshape(e_image.shape)
    return d_image
