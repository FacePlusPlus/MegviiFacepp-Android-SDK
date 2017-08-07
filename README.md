# MegviiFacepp-Android-SDK
An android wrapper of MegviiFacepp SDK (the mobile SDK).

## How to use
1. Add the JitPack repository to your root build.gradle at the end of repositories:
```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```
2. Add the dependency

```
dependencies {
        compile 'com.github.FacePlusPlus:MegviiFacepp-Android-SDK:0.4.7'
}
```

3. Go to the [official website](https://www.faceplusplus.com.cn/) register an account
4. Apply key and secret from [here](https://console.faceplusplus.com.cn/app/apikey/list), create a file named "key" in assets directory, paste key and secret in format like key;secret
5. Bund your bundle id from [here](https://console.faceplusplus.com.cn/app/bundle/list)
6. Download sdk from [here](https://console.faceplusplus.com.cn/service/face/intro), find the model 
that named with megviifacepp_0_4_7_model and put it in raw directory
7. Run demo or your app.


版本号： 0.4.7
（此版本SDK适用于 Megvii-Facepp 0.4.7A）

[学习如何使用 SDK](https://github.com/FacePlusPlus/MegviiFacepp-Android-SDK/wiki/)

Version: 0.4.7 (This version is compatible to Megvii-Facepp 0.4.7A)

[Learn how to use SDK](https://github.com/FacePlusPlus/MegviiFacepp-Android-SDK/wiki/)
