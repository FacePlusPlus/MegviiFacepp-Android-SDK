package com.megvii.facepp.sdk.jni;

import android.content.Context;

/**
 * @brief jni 接口类
 * <p>
 * 该类加载了 jni 库
 */
public class NativeFaceppAPI {

    public static native long nativeInit(Context context, byte[] model, int maxFaceNumber);

    public static native float[] nativeGetFaceppConfig(long handle);

    public static native int nativeSetFaceppConfig(long handle, float minFaceSize, float rotation, float interval,
                                                   float detectionMode, float roi_left, float roi_top, float roi_right, float
                                                           roi_bottom, float face_confidence_filter, float one_face_tracking);

    public static native int nativeDetect(long handle, byte[] imageData, int width, int height, int imageMode);

    public static native float[] nativeFaceInfo(long handle, int index);

    public static native float[] nativeLandMark(long handle, int index, int pointNum);

    public static native float[] nativeLandMarkRaw(long handle, int index, int pointNum);

    public static native float[] nativeAttribute(long handle, int index);

    public static native float[] nativePose3D(long handle, int index);

    public static native float[] nativeEyeStatus(long handle, int index);

    public static native float[] nativeMouthStatus(long handle, int index);

    public static native float[] nativeMinority(long handle, int index);

    public static native float[] nativeBlurness(long handle, int index);

    public static native float[] nativeAgeGender(long handle, int index);

    public static native float[] nativeRect(long handle, int index);


    public static native long[] nativeGetAlgorithmInfo(byte[] mode);

    public static native int nativeExtractFeature(long handle, int index);

    public static native byte[] nativeGetFeatureData(long handle, int featureLength);

    public static native double nativeFaceCompare(long handle, byte[] featureData1, byte[] featureData2, int
            featureLength);

    public static native long nativeGetApiExpication(Context context);

    public static native void nativeRelease(long handle);

    public static native String nativeGetVersion();

    public static native long nativeGetApiName();

    public static native int nativeGetSDKAuthType();

    public static native int nativeResetTrack(long handle);


    public static native String nativeGetJenkinsNumber();


    public static native int nativeShutDown();

    /**
     * 这里加载的名称要根据 so 不同的版本号进行修改
     */
    static {
        System.loadLibrary("MegviiFacepp-0.5.2");
        System.loadLibrary("MegviiFacepp-jni-0.5.2");
    }

}
