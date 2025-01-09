# ImageEncryption_Embedding_Extraction
将加密图片上传到平台，平台嵌入秘密信息（水印之类），不破坏原有加密图片可恢复性
代码分为两大板块

首先是前端和后端代码，这两个可以分别部署在前端和后端上，例如HbuilderX以及VScode上。前端代码进仅仅上传了vue页面，其他需自己编写配置。

其次是SRC。SRC中包含的并不是前端和后端代码，而是作为攻击者的代码，运行main相当于平台进行的加密嵌入等操作
attack就是利用获取的嵌入后的代码尝试暴力破解出加密密钥和嵌入密钥，目前准确率有限，不是很高。

实验课小项目，不保证正确性。
具体原理参考Reversible Data Hiding in Encrypted Image这篇文献，由Xinpeng Zhang撰写
