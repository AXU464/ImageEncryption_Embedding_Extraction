<template>
  <view>
    <input 
      type="text" 
      v-model="backendIP" 
      placeholder="请输入后端 IP 地址（例如 10.236.71.10）" 
    />
    <button @click="saveBackendIP">保存</button>
  </view>
</template>

<script>
export default {
  data() {
    return {
      backendIP: '', // 用户输入的后端 IP
    };
  },
  methods: {
    saveBackendIP() {
      if (!this.backendIP) {
        uni.showToast({
          title: '请输入 IP 地址',
          icon: 'none',
        });
        return;
      }
      // 保存 IP 地址到本地存储
      uni.setStorageSync('backendIP', this.backendIP);
      uni.showToast({
        title: 'IP 地址已保存',
        icon: 'success',
      });
    },
  },
};
</script>
