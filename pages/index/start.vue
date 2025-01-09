<template>
  <view>
	<view class="chooseipconfig">
	  <button class="custom-button" @mouseover="isHovered = true" @mouseleave="isHovered = false"@click="chooseipconfig":class="{ 'custom-button-hover': isHovered }">进入目标后端IP配置页面</button>
	</view>
    <view class="choosesendpage">
      <button class="custom-button" @mouseover="isHovered = true" @mouseleave="isHovered = false"@click="choosesendpage":class="{ 'custom-button-hover': isHovered }">进入发送页面</button>
    </view>
    <view class="choosereceivepage">
      <button class="custom-button" @mouseover="isHovered = true" @mouseleave="isHovered = false"@click="choosereceivepage":class="{ 'custom-button-hover': isHovered }">进入接收页面</button>
    </view>
  </view>
</template>

<script>
export default {
	data() {
	    return {
	      isHovered: false, // 控制悬停效果
	    };
	  },
  methods: {
	  chooseipconfig() {
	    // 跳转到同级目录下的 send 页面
	    uni.navigateTo({
	      url: '/pages/index/ipconfig'
	    });
	  },
    choosesendpage() {
      // 跳转到同级目录下的 send 页面
      uni.navigateTo({
        url: '/pages/index/send'
      });
    },
    choosereceivepage() {
      // 跳转到同级目录下的 receive 页面
      uni.navigateTo({
        url: '/pages/index/receive'
      });
    }
  }
};
</script>

<style>
.choosesendpage {
  margin-bottom: 20px;
}

.custom-button {
  padding: 10px 20px;
  background-color: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  margin: 10px 0;
  transition: background-color 0.3s;
}

.custom-button-hover {
  background-color: #0056b3;
}
</style>
