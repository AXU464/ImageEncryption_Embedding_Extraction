<template>
  <div id="app">
    <h1>接收端页面</h1>

    <!-- 嵌入图像部分 -->
    <div class="section">
      <h2>嵌入后的图像</h2>
      <button class="button" @click="fetchLatestImage">获取最新图像（获取图像后请等待几秒再点击解密）</button>
      <div v-if="latestImageUrl" class="image-container">
        <img :src="latestImageUrl" alt="嵌入图像" />
      </div>
      <div v-else class="message">
        <p>尚未获取到嵌入图像。</p>
      </div>
    </div>

    <!-- 解密图像部分 -->
    <div class="section">
      <h2>解密后的图像与嵌入信息</h2>
      <button class="button" @click="decryptImage">解密该图像（解密可能需要花费更多时间，请不要重复点击）</button>
      <div v-if="decryptedImageUrl" class="image-container">
        <img :src="decryptedImageUrl" alt="解密图像" />
        <div class="info">
          <p>提取到的二进制信息为: {{ extractedData.join(', ') || '未获取到嵌入信息' }}</p>
          <p v-if="decimalData">其可能的十进制为: {{ decimalData }}</p>
        </div>
      </div>
      <div v-else class="message">
        <p>尚未解密图像。</p>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      latestImageUrl: null, // 嵌入后的图像 URL
      decryptedImageUrl: null, // 解密后的图像 URL
      extractedData: [], // 嵌入的二进制信息
      decimalData: null, // 转换为十进制的值
      backendIP: '', // 后端 IP 地址
    };
  },
  computed: {
    // 动态拼接获取嵌入图像的 URL
    getLatestImageUrl() {
      return this.backendIP ? `http://${this.backendIP}:5000/get_latest_image` : '';
    },
    // 动态拼接解密图像的 URL
    decryptImageUrl() {
      return this.backendIP ? `http://${this.backendIP}:5000/decrypt_latest_image` : '';
    },
  },
  onShow() {
    // 页面显示时读取本地存储的后端 IP 地址
    this.backendIP = uni.getStorageSync('backendIP') || '';
    if (!this.backendIP) {
      uni.showToast({
        title: '请先配置后端 IP 地址',
        icon: 'none',
      });
    }
  },
  methods: {
    /**
     * 获取最新嵌入图像
     */
    async fetchLatestImage() {
      const url = this.getLatestImageUrl;
      if (!url) {
        uni.showToast({
          title: '后端 IP 地址未配置',
          icon: 'none',
        });
        return;
      }

      try {
        uni.request({
          url: url,
          method: "GET",
          responseType: "arraybuffer", // 接收二进制数据
          success: (res) => {
            if (res.statusCode === 200) {
              // 将 ArrayBuffer 转换为 Base64
              const base64 = this.arrayBufferToBase64(res.data);
              this.latestImageUrl = `data:image/jpeg;base64,${base64}`;
            } else {
              console.error("获取嵌入图像失败:", res.statusCode);
            }
          },
          fail: (err) => {
            console.error("获取嵌入图像时出错:", err);
          },
        });
      } catch (error) {
        console.error("获取嵌入图像时出错:", error);
      }
    },

    /**
     * 解密图像并获取嵌入信息
     */
    async decryptImage() {
      const url = this.decryptImageUrl;
      if (!url) {
        uni.showToast({
          title: '后端 IP 地址未配置',
          icon: 'none',
        });
        return;
      }

      try {
        uni.request({
          url: url,
          method: "GET",
          responseType: "arraybuffer", // 接收二进制数据
          success: (res) => {
            if (res.statusCode === 200) {
              // 将 ArrayBuffer 转换为 Base64
              const base64 = this.arrayBufferToBase64(res.data);
              this.decryptedImageUrl = `data:image/jpeg;base64,${base64}`;

              // 检查响应头中的嵌入数据
              const embeddedDataHeader = res.header["x-embedded-data"] || res.header["X-Embedded-Data"] || "[]";
              this.extractedData = JSON.parse(embeddedDataHeader);

              if (this.extractedData.length > 0) {
                this.decimalData = this.binaryToDecimal(this.extractedData);
              }
            } else {
              console.error("解密图像失败:", res.statusCode);
            }
          },
          fail: (err) => {
            console.error("解密图像时出错:", err);
          },
        });
      } catch (error) {
        console.error("解密图像时出错:", error);
      }
    },

    /**
     * 将 ArrayBuffer 转换为 Base64 字符串
     * @param {ArrayBuffer} buffer
     * @returns {string}
     */
    arrayBufferToBase64(buffer) {
      let binary = "";
      const bytes = new Uint8Array(buffer);
      const len = bytes.byteLength;
      for (let i = 0; i < len; i++) {
        binary += String.fromCharCode(bytes[i]);
      }
      return btoa(binary);
    },

    /**
     * 将二进制列表转换为十进制字符串
     * @param {Array} binaryList - 二进制列表
     * @returns {string} - 转换后的十进制字符串
     */
    binaryToDecimal(binaryList) {
      const groupedBinary = [];
      for (let i = 0; i < binaryList.length; i += 4) {
        groupedBinary.push(binaryList.slice(i, i + 4).join(""));
      }
      return groupedBinary.map((bin) => parseInt(bin, 2)).join("");
    },
  },
};
</script>

<style scoped>
#app {
  max-width: 900px;
  margin: 0 auto;
  font-family: Arial, sans-serif;
  text-align: center;
  background-color: #f8f9fa;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

.section {
  margin-bottom: 30px;
  padding: 20px;
  background-color: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.image-container {
  margin: 20px 0;
}

img {
  max-width: 100%;
  height: auto;
  border: 1px solid #ddd;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.button {
  padding: 10px 20px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  margin: 10px 0;
  transition: background-color 0.3s;
}

.button:hover {
  background-color: #0056b3;
}

h1 {
  margin-bottom: 30px;
  color: #343a40;
}

h2 {
  margin-bottom: 20px;
  color: #495057;
}

.message {
  color: #6c757d;
  font-size: 16px;
}

.info {
  margin-top: 20px;
  text-align: left;
  font-size: 16px;
  color: #495057;
  background-color: #e9ecef;
  padding: 15px;
  border-radius: 8px;
}
</style>
