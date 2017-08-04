#include <jni.h>

#ifndef _Included_com_megvii_fppapidemo_Api
#define _Included_com_megvii_fppapidemo_Api
#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jlong JNICALL Java_com_megvii_facepp_sdk_jni_NativeFaceppAPI_nativeInit(
        JNIEnv *, jobject, jobject, jbyteArray);

JNIEXPORT jintArray JNICALL Java_com_megvii_facepp_sdk_jni_NativeFaceppAPI_nativeGetFaceppConfig(
        JNIEnv *env, jobject, jlong);

JNIEXPORT jint JNICALL Java_com_megvii_facepp_sdk_jni_NativeFaceppAPI_nativeSetFaceppConfig(
        JNIEnv *, jobject, jlong, jint, jint, jint, jint, jint, jint, jint,
        jint, jint);

JNIEXPORT jint JNICALL Java_com_megvii_facepp_sdk_jni_NativeFaceppAPI_nativeDetect(
        JNIEnv *, jobject, jlong, jbyteArray, jint, jint, jint);

JNIEXPORT jfloatArray JNICALL Java_com_megvii_facepp_sdk_jni_NativeFaceppAPI_nativeFaceInfo(
        JNIEnv *, jobject, jlong, jint);

JNIEXPORT jfloatArray JNICALL Java_com_megvii_facepp_sdk_jni_NativeFaceppAPI_nativeLandMark(
        JNIEnv *, jobject, jlong, jint, jint);

JNIEXPORT jfloatArray JNICALL Java_com_megvii_facepp_sdk_jni_NativeFaceppAPI_nativeAttribute(
        JNIEnv *, jobject, jlong, jint);

JNIEXPORT jfloatArray JNICALL Java_com_megvii_facepp_sdk_jni_NativeFaceppAPI_nativePose3D(
        JNIEnv *, jobject, jlong, jint);
JNIEXPORT jfloatArray JNICALL Java_com_megvii_facepp_sdk_jni_NativeFaceppAPI_nativeEyeStatus(
        JNIEnv *, jobject, jlong, jint);
JNIEXPORT jfloatArray JNICALL Java_com_megvii_facepp_sdk_jni_NativeFaceppAPI_nativeMouthStatus(
        JNIEnv *, jobject, jlong, jint);
JNIEXPORT jfloatArray JNICALL Java_com_megvii_facepp_sdk_jni_NativeFaceppAPI_nativeMinority(
        JNIEnv *, jobject, jlong, jint);
JNIEXPORT jfloatArray JNICALL Java_com_megvii_facepp_sdk_jni_NativeFaceppAPI_nativeBlurness(
        JNIEnv *, jobject, jlong, jint);
JNIEXPORT jfloatArray JNICALL Java_com_megvii_facepp_sdk_jni_NativeFaceppAPI_nativeAgeGender(
        JNIEnv *, jobject, jlong, jint);

JNIEXPORT jlongArray JNICALL Java_com_megvii_facepp_sdk_jni_NativeFaceppAPI_nativeGetAlgorithmInfo(
        JNIEnv *, jobject, jbyteArray);

JNIEXPORT jint JNICALL Java_com_megvii_facepp_sdk_jni_NativeFaceppAPI_nativeExtractFeature(
        JNIEnv *, jobject, jlong, jint);
JNIEXPORT jbyteArray JNICALL Java_com_megvii_facepp_sdk_jni_NativeFaceppAPI_nativeGetFeatureData(
        JNIEnv *, jobject, jlong, jint);
JNIEXPORT jdouble JNICALL Java_com_megvii_facepp_sdk_jni_NativeFaceppAPI_nativeFaceCompare(
        JNIEnv *, jobject, jlong, jbyteArray, jbyteArray, jint);

JNIEXPORT void JNICALL
Java_com_megvii_facepp_sdk_jni_NativeFaceppAPI_nativeRelease(JNIEnv *, jobject,
                                                             jlong);

JNIEXPORT jlong JNICALL Java_com_megvii_facepp_sdk_jni_NativeFaceppAPI_nativeGetApiExpication(
        JNIEnv *, jobject, jobject);

JNIEXPORT jlong JNICALL Java_com_megvii_facepp_sdk_jni_NativeFaceppAPI_nativeGetApiExpication(
        JNIEnv *, jobject, jobject);

JNIEXPORT jstring JNICALL Java_com_megvii_facepp_sdk_jni_NativeFaceppAPI_nativeGetVersion(
        JNIEnv *, jobject);

JNIEXPORT jlong JNICALL Java_com_megvii_facepp_sdk_jni_NativeFaceppAPI_nativeGetApiName(
        JNIEnv *, jobject);

JNIEXPORT jint JNICALL Java_com_megvii_facepp_sdk_jni_NativeFaceppAPI_nativeGetSDKAuthType(
        JNIEnv *, jobject);


#ifdef __cplusplus
}
#endif
#endif
