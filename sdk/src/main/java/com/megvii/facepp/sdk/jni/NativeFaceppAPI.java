package com.megvii.facepp.sdk.jni;

import android.content.Context;

/**
 * @brief jni 接口类
 * <p>
 * 该类加载了 jni 库
 */
public class NativeFaceppAPI {

    public static native long nativeInit(Context context, byte[] model);

    public static native int[] nativeGetFaceppConfig(long handle);

    public static native int nativeSetFaceppConfig(long handle, int minFaceSize, int rotation, int interval,
                                                   int detectionMode, int roi_left, int roi_top, int roi_right, int
                                                           roi_bottom, int one_face_tracking);

    public static native int nativeDetect(long handle, byte[] imageData, int width, int height, int imageMode);

    public static native float[] nativeFaceInfo(long handle, int index);

    public static native float[] nativeLandMark(long handle, int index, int pointNum);

    public static native float[] nativeAttribute(long handle, int index);

    public static native float[] nativePose3D(long handle, int index);

    public static native float[] nativeEyeStatus(long handle, int index);

    public static native float[] nativeMouthStatus(long handle, int index);

    public static native float[] nativeMinority(long handle, int index);

    public static native float[] nativeBlurness(long handle, int index);

    public static native float[] nativeAgeGender(long handle, int index);

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

    /**
     * 这里加载的名称要根据 so 不同的版本号进行修改
     */
    static {
        System.loadLibrary("MegviiFacepp-0.4.7");
        System.loadLibrary("MegviiFacepp-jni-0.4.7");
    }

}
