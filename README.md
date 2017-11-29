# Facepp人脸检测SDK集成说明文档
## 一、简介

Facepp人脸检测SDK是旷视科技推出的人脸检测工具包，提供快速，简洁的开发接口，支持Android4.2及以上的移动设备上实现人脸检测

### 功能特性
* 支持人脸的fast检测
* 支持人脸的robust检测
* 支持获取人脸关键点
* 支持人脸比对
* 支持获取人脸框
* 支持检测年龄性别
* 支持获取3dpose
* 支持 ARMv7a和ARM64v8a架构
* 支持Android 4.2及以上系统
* 快速，库文件小，贴合度高

### 组件及资源
FaceppSDK包含demo、sdk、docs三部分，[demo和sdk可以查看github](https://github.com/FacePlusPlus/MegviiFacepp-Android-SDK)

* demo部分：里面包含一个示例工程，实现了人脸检测的功能，有完整的源代码。
* sdk部分：里面包含了一套jni的封装，可以实现aar和c的两种集成方式。
* 库文件：人脸检测的实现库，路径以及目录如下sdk/src/main/jniLibs
├── armeabi-v7a
│   ├── libmegface-new .so
│   ├── libMegviiFacepp-0.5.2 .so
├── arm64-v8a
│   ├── libmegface-new .so
│   ├── libMegviiFacepp-0.5.2 .so
* 模型文件：人脸检测的训练模型，模型目前分在线授权和离线授权两种，路径/faceppdemo/src/main/res/raw/megviifacepp_0_5_2_model
> 试⽤版SDK有使用时间和使用次数的限制，如需正式版请通过[Face++官网](https://www.faceplusplus.com.cn)联系商务合作。

## 二、开发指南
### 阅读对象
本文档为技术文档，需要阅读者具有基本的Android开发能力，如果想对c定制开发需要有基本的c开发能力。

### 文档综述
FaceppSDK是适用android平台下的人脸检测SDK，提供aar，jar，c的三种接入方式，提供了简捷的接口方便接入。

### 开发准备

#### 在线授权的准备
1. [官网](https://www.faceplusplus.com.cn/)注册账号
2. 申请[key和secret](https://console.faceplusplus.com.cn/dashboard),用于在线授权。
3. 申请[Bundle ID（applicationId）](https://console.faceplusplus.com.cn/dashboard)，用于模型的验证。

### 集成SDK
1. 启动Android Studio，并导入工程（以MegviiFacepp-Android-SDK 0.5.2版本为例）
2. 已有工程导入SDK
* Demo Project目录如下：
<img src="https://github.com/FacePlusPlus/Document-Resource/raw/master/MegviiFacepp/img/facepp_all.png" width="300" height="288" align=center>

红色：为demo module，展示用户集成部分
蓝色：为sdk module，如果需要定制开发，或者更高的效率，可以自己开发jni部分。
* Demo Module的目录如下：
<img src="https://github.com/FacePlusPlus/Document-Resource/raw/master/MegviiFacepp/img/facepp_demo.png" width="300" height="382" align=center>
红色：sdk module编译的aar，如非需要定制可直接使用
蓝色：为联网授权aar，如果是离线授权可以去掉，另外联网授权申请的key和secret需要添加到/faceppdemo/src/main/java/com/facepp/demo/util/Util.java，还需要申请一个工程的applicationId的bundle。
绿色：为申请到的module，是人脸检测的训练模型

### 功能使用和API接口介绍
下面采用最常用的获取人脸关键流程来讲解，常用API。
##### 1. 初始化SDK
初始化实例
```java
facepp = new Facepp();
```
初始化模型,如果模型加载失败，会有相应的code提示
```java
String errorCode = facepp.init(this, ConUtil.getFileContent(this, R.raw.megviifacepp_0_5_2_model), isOneFaceTrackig ? 1 : 0);
```
#### 2. 检测参数设置
主要根据需要的模型的能力去设置detectionMode，其他的参数使用默认即可
```java
Facepp.FaceppConfig faceppConfig = facepp.getFaceppConfig();
faceppConfig.detectionMode = Facepp.FaceppConfig.DETECTION_MODE_TRACKING_FAST;
facepp.setFaceppConfig(faceppConfig);
```
#### 3.人脸检测
这里会获取检测到人脸的数目，和人脸框置信度等基本信息。参数为检测图片的属性，imageMode目前支持两种bgr nv21
```java
final Facepp.Face[] faces = facepp.detect(imgData, width, height, Facepp.IMAGEMODE_NV21);
```
#### 4.获取关键点
获取人脸的关键点，tracking检测的会做平滑，参数pointNum有81点和106点
```java
facepp.getLandmarkRaw(faces[c], Facepp.FPP_GET_LANDMARK81);
```
#### 5.释放资源
释放资源
```java
facepp.release();
```
#### 6.切换摄像头
切换摄像头需要重置track
```java
facepp.resetTrack();
```

## 版本更新
v0.5.2
* sdk
1. 增加detect_rect模式：只输出人脸框
2. 删除track_normal模式，内部将track_normal映射成track_robust，并提示用户此模式已废弃，以后会在接口中直接删除
3. 接口变化
3.1  增加 CreateApiHandleWithMaxFaceCount，在初始化时设置最大跟踪的人脸数。config中one_face_tracking被废弃
3.2 增加 GetJenkinsNumber 输出jenkins打包时版本号，方便调试
3.3 增加 ResetTrack 清除track缓存
3.4 增加 GetRect 在detect_rect模式下，输出人脸框信息
3.5 更新 GetAlgorithmInfo ，增加返回限制的bundleid
3.6 更新 MG_FPP_APICONFIG, 增加
face_confidence_filter,人脸置信度过滤阈值，低于此值的数据将被过滤掉，默认 0.1
3.7 单脸跟踪需在初始化时设置，初始化接口增加了最多跟踪几张人脸的参数。默认为0，不限制。设置中的one_face_tracking已失效
* demo
1. 简化在线授权的逻辑。
2. module的合并
3. 增加人脸比对
