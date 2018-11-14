
/****************** Deepano Sample Code *****************/

Notice: 使用 deepano so 文件必须自行通过 NDK 再封装 (该 sample code 就是封装示例),不可直接通过 java 调用。

说明：
1.cpp/ 文件夹下:
  *. Common.h, dp_api.h, dp_api_type.h, Fp16Convert.h, interpret_output.h, mv_types.h, Region.h 为deepano Android .so 的开发所需要的头文件.
  *. dpnJniEntry.cpp 是 JNI 的 sample code.

2.java/ 文件夹下:
  *. DeepanoApiFactory.java 是 JNI 的入口函数.
  *. MainActivity.java 是 Android 示例代码.

3.libs/ 文件夹下:
  *. Android 各个 cpu 架构下的 deepano sdk 的 so 库

4.大致流程：
  *. 该 sample 只提供了 initDevice(初始化并且打开设备)/getFrameBuffer(获取视频帧)/startCamera(打开camera)/netProc(处理神经网络，在此以 SSD 为例) 等几个最核心的api

5. 具体开发流程可参考 sample code 和之前 deepano 给你们发的接口文档 .有问题可随时联系.



     
