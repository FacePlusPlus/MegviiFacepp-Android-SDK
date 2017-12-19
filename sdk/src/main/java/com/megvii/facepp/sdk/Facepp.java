package com.megvii.facepp.sdk;

import android.content.Context;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.Log;

import com.megvii.facepp.sdk.jni.NativeFaceppAPI;

import java.util.ArrayList;

/**
 * @brief Face++ 人脸 SDK 的 Android 接口
 * <p>
 * 使用该 SDK 首先必须调用 init 初始化方法，使用完成后必须使用 release 方法释放；
 * 通过 getFaceppConfig 可以获取检测器的配置信息，通过 setFaceppConfig 方法可以修改检测器配置。
 */
public class Facepp {
    public final static int FPP_GET_LANDMARK81 = 81;            ///< 计算 81 个关键点
    public final static int FPP_GET_LANDMARK101 = 101;          ///< 计算 101 个关键点
    public final static int FPP_GET_LANDMARK106 = 106;          ///< 计算 106 个关键点

    public final static int IMAGEMODE_GRAY = 0;                 ///< 灰度图像
    public final static int IMAGEMODE_BGR = 1;                  ///< BGR图像
    public final static int IMAGEMODE_NV21 = 2;                 ///< YUV420（nv21）图像
    public final static int IMAGEMODE_RGBA = 3;                 ///< RGBA图像
    public final static int IMAGEMODE_RGB = 4;                  ///< RGB图像
    public final static int IMAGEMODE_COUNT = 5;                ///< 支持图像总数


    private long FaceppHandle;
    private static ArrayList<Ability> abilities;
    private static long[] algorithmInfo;

    /**
     * @return 成功则返回 null, 失败返回错误原因
     * @brief 初始化人脸检测器
     * @param[in] context 环境变量
     * @param[in] model 模型数据
     */
    public String init(Context context, byte[] model){
        return init(context, model, 0);
    }

    /**
     * @return 成功则返回 null, 失败返回错误原因
     * @brief 初始化人脸检测器
     * @param[in] context 环境变量
     * @param[in] model 模型数据
     * @param[in] maxFaceNumber 跟踪人脸数量
     */
    public String init(Context context, byte[] model, int maxFaceNumber) {
        if (context == null || model == null)
            return getErrorType(MG_RETCODE_INVALID_ARGUMENT);

        getAbility(model);

        long handle = NativeFaceppAPI.nativeInit(context, model, maxFaceNumber);
        String errorType = getErrorType((int) handle);
        if (errorType == null) {
            FaceppHandle = handle;
            return null;
        }

        return errorType;
    }


    /**
     * @return FaceppConfig 包含人脸检测器配置信息
     * @brief 获取人脸配置信息
     */
    public FaceppConfig getFaceppConfig() {
        FaceppConfig faceppConfig = new FaceppConfig();
        if (FaceppHandle==0){
            return faceppConfig;
        }
        float[] configs = NativeFaceppAPI.nativeGetFaceppConfig(FaceppHandle);
        faceppConfig.minFaceSize = (int) configs[0];
        faceppConfig.rotation = (int) configs[1];
        faceppConfig.interval = (int) configs[2];
        faceppConfig.detectionMode = (int) configs[3];
        faceppConfig.roi_left = (int) configs[4];
        faceppConfig.roi_top = (int) configs[5];
        faceppConfig.roi_right = (int) configs[6];
        faceppConfig.roi_bottom = (int) configs[7];
        faceppConfig.face_confidence_filter=configs[8];
        faceppConfig.one_face_tracking = (int) configs[9];
        return faceppConfig;
    }

    /**
     * @brief 设置配置信息
     * <p>
     * 设置人脸配置信息
     * @param[in] faceppConfig 人脸检测器配置信息
     */
    public void setFaceppConfig(FaceppConfig faceppConfig) {
        if (FaceppHandle==0){
            return;
        }
        NativeFaceppAPI.nativeSetFaceppConfig(FaceppHandle, faceppConfig.minFaceSize, faceppConfig.rotation,
                faceppConfig.interval, faceppConfig.detectionMode, faceppConfig.roi_left, faceppConfig.roi_top,
                faceppConfig.roi_right, faceppConfig.roi_bottom, faceppConfig.face_confidence_filter,faceppConfig.one_face_tracking);
    }

    /**
     * @return Face[] 人脸信息数组
     * @brief 检测图片信息
     * <p>
     * 检测图片是否有人脸，有几张脸并获取每张脸的信息
     * @param[in] imageData 图片数据
     * @param[in] width 图片的宽
     * @param[in] height 图片的高
     * @param[in] imageMode 图片数据的格式
     */
    public Face[] detect(byte[] imageData, int width, int height, int imageMode) {
        if (FaceppHandle==0){
            return new Face[0];
        }
        int faceSize = NativeFaceppAPI.nativeDetect(FaceppHandle, imageData, width, height, imageMode);
        Face[] faces = new Face[faceSize];
        for (int i = 0; i < faceSize; i++) {
            float[] points = NativeFaceppAPI.nativeFaceInfo(FaceppHandle, i);
            Face face = new Face();
            loadFaceBaseInfo(face, points);
            loadFacePointsInfo(face, points, 81, 10);
            faces[i] = face;
        }
        return faces;
    }

    /**
     * @brief 获取人脸的Landmark会旋转为竖直方向,jni会旋转,需要原始数据使用getLandmarkRaw
     * <p>
     * 获取指定人脸的Landmark信息，并改变传入的人脸信息
     * @param[in, out] face 人脸信息
     * @param[in] pointNum 需要的人脸关键点点数
     */
    @Deprecated
    public void getLandmark(Face face, int pointNum) {
        if (FaceppHandle==0){
            return;
        }
        float[] points = NativeFaceppAPI.nativeLandMark(FaceppHandle, face.index, pointNum);
        loadFacePointsInfo(face, points, pointNum, 0);
    }

    /**
     * @brief 获取人脸的Landmark原始数据
     * <p>
     * 获取指定人脸的Landmark信息，并改变传入的人脸信息
     * @param[in, out] face 人脸信息
     * @param[in] pointNum 需要的人脸关键点点数
     */
    public void getLandmarkRaw(Face face, int pointNum) {
        float[] points = NativeFaceppAPI.nativeLandMarkRaw(FaceppHandle, face.index, pointNum);
        loadFacePointsInfo(face, points, pointNum, 0);
    }

    /**
     * @brief 获取人脸的所有属性
     * <p>
     * 获取指定人脸的所有属性信息，并改变传入的人脸信息
     * @param[in, out] face 人脸信息
     */
    public void getAttribute(Face face) {
        if (FaceppHandle==0){
            return;
        }
        float[] points = NativeFaceppAPI.nativeAttribute(FaceppHandle, face.index);
        loadFaceAttributeInfo(face, points);
    }

    /**
     * @return 调用是否成功
     * @brief 获取指定人脸的 3DPose 属性信息，并改变传入的人脸信息
     * @param[in, out] face 人脸信息
     */
    public boolean get3DPose(Face face) {
        if (FaceppHandle==0||abilities == null || !abilities.contains(Ability.POSE))
            return false;

        float[] points = NativeFaceppAPI.nativePose3D(FaceppHandle, face.index);
        loadFace3DPoseInfo(face, points);

        return true;
    }

    /**
     * @return 调用是否成功
     * @brief 获取指定人脸的眼睛属性信息，并改变传入的人脸信息
     * @param[in, out] face 人脸信息
     */
    public boolean getEyeStatus(Face face) {
        if (FaceppHandle==0||abilities == null || !abilities.contains(Ability.EYESTATUS))
            return false;
        float[] points = NativeFaceppAPI.nativeEyeStatus(FaceppHandle, face.index);
        loadFaceEyeStatusInfo(face, points);
        return true;
    }

    /**
     * @return 调用是否成功
     * @brief 获取指定人脸的嘴巴属性信息，并改变传入的人脸信息
     * @param[in, out] face 人脸信息
     */
    public boolean getMouthStatus(Face face) {
        if (FaceppHandle==0||abilities == null || !abilities.contains(Ability.MOUTHSTATUS))
            return false;
        float[] points = NativeFaceppAPI.nativeMouthStatus(FaceppHandle, face.index);
        loadFaceMouthStatusInfo(face, points);
        return true;
    }

    /**
     * @return 调用是否成功
     * @brief 获取指定人脸的民族属性信息，并改变传入的人脸信息
     * @param[in, out] face 人脸信息
     */
    public boolean getMinorityStatus(Face face) {
        if (FaceppHandle==0||abilities == null || !abilities.contains(Ability.MINORITY))
            return false;
        float[] points = NativeFaceppAPI.nativeMinority(FaceppHandle, face.index);
        loadFaceMinorityInfo(face, points);
        return true;
    }

    /**
     * @return 调用是否成功
     * @brief 获取指定人脸的模糊程度，并改变传入的人脸信息
     * @param[in, out] face 人脸信息
     */
    public boolean getBlurness(Face face) {
        if (FaceppHandle==0||abilities == null || !abilities.contains(Ability.BLURNESS))
            return false;
        float[] points = NativeFaceppAPI.nativeBlurness(FaceppHandle, face.index);
        loadFaceBlurnessInfo(face, points);
        return true;
    }

    /**
     * @return 调用是否成功
     * @brief 获取指定人脸的年龄性别属性信息，并改变传入的人脸信息
     * @param[in, out] face 人脸信息
     */
    public boolean getAgeGender(Face face) {
        if (FaceppHandle==0||abilities == null || !abilities.contains(Ability.AGEGENDER))
            return false;
        float[] points = NativeFaceppAPI.nativeAgeGender(FaceppHandle, face.index);
        loadFaceAgeGenderInfo(face, points);
        return true;
    }

    public void getRect(Face face){
        if (FaceppHandle==0)
            return ;
        float[] rectArray = NativeFaceppAPI.nativeRect(FaceppHandle, face.index);

        face.rect.left = (int)rectArray[0];
        face.rect.top = (int)rectArray[1];
        face.rect.right = (int)rectArray[2];
        face.rect.bottom = (int)rectArray[3];
    }


    /**
     * @return 调用是否成功
     * @brief 获取指定人脸的特征，并改变传入的人脸信息
     * @param[in, out] face 人脸信息
     */
    public boolean getExtractFeature(Face face) {
        if (abilities == null || !abilities.contains(Ability.SMALLFEATEXT)||FaceppHandle==0||face==null)
            return false;
        int featureLength = NativeFaceppAPI.nativeExtractFeature(FaceppHandle, face.index);
        if (featureLength<=0){
            return false;
        }
        face.feature = NativeFaceppAPI.nativeGetFeatureData(FaceppHandle, featureLength);

        return true;
    }

    /**
     * @return 如果人脸没有抽取过特征返回-1，传入参数正确返回两张人脸的相似度
     * @brief 比较两个人脸，并返回两张人脸的相似度
     * @param[in] face1 人脸1信息
     * @param[in] face2 人脸2信息
     */
    public double faceCompare(Face face1, Face face2) {
        if (FaceppHandle==0||face1 == null || face2 == null || face1.feature == null || face2.feature == null)
            return -1;
        return NativeFaceppAPI.nativeFaceCompare(FaceppHandle, face1.feature, face2.feature, face1.feature.length / 4);
    }

    /**
     * @return 如果传入人脸特征为 null 返回-1，传入参数正确返回两张人脸的相似度
     * @brief 比较两个人脸，并返回两张人脸的相似度
     * @param[in] feature1 人脸1特征
     * @param[in] feature2 人脸2特征
     */
    public double faceCompare(byte[] feature1, byte[] feature2) {
        if (FaceppHandle==0||feature1 == null || feature2 == null)
            return -1;
        return NativeFaceppAPI.nativeFaceCompare(FaceppHandle, feature1, feature2, feature1.length / 4);
    }

    /**
     * @brief 释放人脸检测器
     */
    public void release() {
        if (FaceppHandle == 0)
            return;
        NativeFaceppAPI.nativeRelease(FaceppHandle);
        FaceppHandle = 0;
    }

    /**
     * @return 过期时间戳（单位毫秒）
     * @brief 获取人脸检测器过期时间
     * @param[in] context android环境变量
     * @param[in] model 模型数据
     */
    public static long getApiExpirationMillis(Context context, byte[] mode) {
        if (getSDKAuthType(mode) == 1)
            return getApiExpirationMillis(context);        /// < 联网授权
        else
            return getApiExpirationMillis(mode);           /// < 非联网授权
    }

    /**
     * @return 过期时间戳（单位毫秒）
     * @brief 获取人脸检测器过期时间
     * @param[in] model 模型数据
     */
    private static long getApiExpirationMillis(byte[] mode) {
        if (algorithmInfo == null)
            algorithmInfo = NativeFaceppAPI.nativeGetAlgorithmInfo(mode);

        return algorithmInfo[0] * 1000;
    }

    /**
     * @return 如果是联网授权返回 1，如果是非联网授权返回 2
     * @brief 获取人脸检测器授权类型
     * @param[in] model 模型数据
     */
    public static int getSDKAuthType(byte[] mode) {
        if (algorithmInfo == null)
            algorithmInfo = NativeFaceppAPI.nativeGetAlgorithmInfo(mode);

        return (int) algorithmInfo[1];
    }

    /**
     * @return 人脸检测器能力列表
     * @brief 获取人脸检测器能力列表
     * @param[in] model 模型数据
     */
    public static ArrayList<Ability> getAbility(byte[] mode) {
        if (abilities != null)
            return abilities;
        abilities = new ArrayList<Ability>();
        if (algorithmInfo == null)
            algorithmInfo = NativeFaceppAPI.nativeGetAlgorithmInfo(mode);
        long ability = algorithmInfo[2];
        if ((ability & MG_FPP_ATTR_POSE3D) != 0)
            abilities.add(Ability.POSE);
        if ((ability & MG_FPP_ATTR_EYESTATUS) != 0)
            abilities.add(Ability.EYESTATUS);
        if ((ability & MG_FPP_ATTR_MOUTHSTATUS) != 0)
            abilities.add(Ability.MOUTHSTATUS);
        if ((ability & MG_FPP_ATTR_MINORITY) != 0)
            abilities.add(Ability.MINORITY);
        if ((ability & MG_FPP_ATTR_BLURNESS) != 0)
            abilities.add(Ability.BLURNESS);
        if ((ability & MG_FPP_ATTR_AGE_GENDER) != 0)
            abilities.add(Ability.AGEGENDER);
        if ((ability & MG_FPP_ATTR_EXTRACT_FEATURE) != 0)
            abilities.add(Ability.SMALLFEATEXT);

        return abilities;
    }

    /**
     * @return 人脸检测器版本号
     * @brief 获取人脸检测器版本号
     */
    public static String getVersion() {
        return NativeFaceppAPI.nativeGetVersion();
    }

    /**
     * @return API 标识
     * @brief 获取 API 标识
     * <p>
     * API 标识用于联网授权
     */
    public static long getApiName() {
        return NativeFaceppAPI.nativeGetApiName();
    }

    /**
     * @return 过期时间戳 （单位毫秒）
     * @brief 获取人脸检测器过期时间
     * @param[in] context android环境变量
     */
    private static long getApiExpirationMillis(Context context) {
        return NativeFaceppAPI.nativeGetApiExpication(context) * 1000;
    }

    /**
     * @return 如果是联网授权返回 1，如果是非联网授权返回 2
     * @brief 获取人脸检测器授权类型
     * @deprecated
     */
    public static int getSDKAuthType() {
        return NativeFaceppAPI.nativeGetSDKAuthType();
    }

    /**
     * @return 调用是否成功
     * @brief  切换摄像头调用 出现关键点后调用
     */
    public  int resetTrack() {
        if (FaceppHandle==0)
            return 0;
        return NativeFaceppAPI.nativeResetTrack(FaceppHandle);
    }



    /**
     * @return jenkins nummber
     * @brief  获取打包版本
     */
    public static String getJenkinsNumber() {
        return NativeFaceppAPI.nativeGetJenkinsNumber();
    }



    /**
     * @return sdk 授权类型
     * @brief  新的sdk授权类型
     */
    public static int getSDKAuthTypeNew() {
        return NativeFaceppAPI.nativeGetSDKAuthType();
    }


    /**
     * @return 调用是否成功
     * @brief  清理资源，release后调用
     */
    public static int shutDown() {
        return NativeFaceppAPI.nativeShutDown();
    }




    private void loadFaceBaseInfo(Face face, float[] faceBaseInfo) {
        face.trackID = (int) faceBaseInfo[0];
        face.index = (int) faceBaseInfo[1];
        face.confidence = faceBaseInfo[2];
        Rect rect = new Rect();
        face.rect = rect;
        rect.left = (int) faceBaseInfo[3];
        rect.top = (int) faceBaseInfo[4];
        rect.right = (int) faceBaseInfo[5];
        rect.bottom = (int) faceBaseInfo[6];
        face.pitch = faceBaseInfo[7];
        face.yaw = faceBaseInfo[8];
        face.roll = faceBaseInfo[9];
    }

    private void loadFacePointsInfo(Face face, float[] facePointsInfo, int facePoints, int offset) {
        PointF[] points = new PointF[facePoints];
        face.points = points;
        for (int j = 0; j < facePoints; j++) {
            points[j] = new PointF();
            points[j].x = facePointsInfo[offset + (j * 2)];
            points[j].y = facePointsInfo[offset + (j * 2 + 1)];
        }
    }

    private void loadFaceAttributeInfo(Face face, float[] faceAttribute) {
        face.pitch = faceAttribute[0];
        face.yaw = faceAttribute[1];
        face.roll = faceAttribute[2];
        int offset = 3;
        int eyeLentgh = 6;
        face.leftEyestatus = new float[eyeLentgh];
        for (int i = 0; i < eyeLentgh; i++) {
            face.leftEyestatus[i] = faceAttribute[i + offset];
        }
        offset += eyeLentgh;
        face.rightEyestatus = new float[eyeLentgh];
        for (int i = 0; i < eyeLentgh; i++) {
            face.rightEyestatus[i] = faceAttribute[i + offset];
        }
        offset += eyeLentgh;
        int moutstatusLenght = 4;
        face.moutstatus = new float[moutstatusLenght];
        for (int i = 0; i < moutstatusLenght; i++) {
            face.moutstatus[i] = faceAttribute[i + offset];
        }
        offset += moutstatusLenght;
        face.minority = faceAttribute[offset];
        face.blurness = faceAttribute[offset + 1];
        face.age = faceAttribute[offset + 2];
        face.female = faceAttribute[offset + 3];
        face.male = faceAttribute[offset + 4];
    }

    private void loadFace3DPoseInfo(Face face, float[] face3DPoseInfo) {
        face.pitch = face3DPoseInfo[0];
        face.yaw = face3DPoseInfo[1];
        face.roll = face3DPoseInfo[2];
    }

    private void loadFaceEyeStatusInfo(Face face, float[] faceEyeStatus) {
        int offset = 0;
        int eyeLentgh = 6;
        face.leftEyestatus = new float[eyeLentgh];
        for (int i = 0; i < eyeLentgh; i++) {
            face.leftEyestatus[i] = faceEyeStatus[i + offset];
        }
        offset += eyeLentgh;
        face.rightEyestatus = new float[eyeLentgh];
        for (int i = 0; i < eyeLentgh; i++) {
            face.rightEyestatus[i] = faceEyeStatus[i + offset];
        }
    }

    private void loadFaceMouthStatusInfo(Face face, float[] faceMouthStatus) {
        int offset = 0;
        int moutstatusLenght = 4;
        face.moutstatus = new float[moutstatusLenght];
        for (int i = 0; i < moutstatusLenght; i++) {
            face.moutstatus[i] = faceMouthStatus[i + offset];
        }
    }

    private void loadFaceMinorityInfo(Face face, float[] faceMinority) {
        face.minority = faceMinority[0];
    }

    private void loadFaceBlurnessInfo(Face face, float[] faceBlurness) {
        face.blurness = faceBlurness[0];
    }

    private void loadFaceAgeGenderInfo(Face face, float[] faceAgeGender) {
        face.age = faceAgeGender[0];
        face.female = faceAgeGender[1];
        face.male = faceAgeGender[2];
    }

    private static final long MG_FPP_ATTR_POSE3D = 0x01;          /// < 3dpose 的标识位
    private static final long MG_FPP_ATTR_EYESTATUS = 0x02;       /// < 眼睛状态的标识位
    private static final long MG_FPP_ATTR_MOUTHSTATUS = 0x04;     /// < 嘴巴状态的标识位
    private static final long MG_FPP_ATTR_MINORITY = 0x08;        /// < 少数民族的标识位
    private static final long MG_FPP_ATTR_BLURNESS = 0x10;        /// < 人脸模糊度的标识位
    private static final long MG_FPP_ATTR_AGE_GENDER = 0x20;      /// < 年龄性别的标识位
    private static final long MG_FPP_ATTR_EXTRACT_FEATURE = 0x40; /// < 人脸比对的标识位

    private static final int MG_RETCODE_FAILED = -1;
    private static final int MG_RETCODE_OK = 0;
    private static final int MG_RETCODE_INVALID_ARGUMENT = 1;
    private static final int MG_RETCODE_INVALID_HANDLE = 2;
    private static final int MG_RETCODE_INDEX_OUT_OF_RANGE = 3;
    private static final int MG_RETCODE_EXPIRE = 101;
    private static final int MG_RETCODE_INVALID_BUNDLEID = 102;
    private static final int MG_RETCODE_INVALID_LICENSE = 103;
    private static final int MG_RETCODE_INVALID_MODEL = 104;

    private String getErrorType(int retCode) {
        switch (retCode) {
            case MG_RETCODE_FAILED:
                return "MG_RETCODE_FAILED";
            case MG_RETCODE_OK:
                return "MG_RETCODE_OK";
            case MG_RETCODE_INVALID_ARGUMENT:
                return "MG_RETCODE_INVALID_ARGUMENT";
            case MG_RETCODE_INVALID_HANDLE:
                return "MG_RETCODE_INVALID_HANDLE";
            case MG_RETCODE_INDEX_OUT_OF_RANGE:
                return "MG_RETCODE_INDEX_OUT_OF_RANGE";
            case MG_RETCODE_EXPIRE:
                return "MG_RETCODE_EXPIRE";
            case MG_RETCODE_INVALID_BUNDLEID:
                return "MG_RETCODE_INVALID_BUNDLEID";
            case MG_RETCODE_INVALID_LICENSE:
                return "MG_RETCODE_INVALID_LICENSE";
            case MG_RETCODE_INVALID_MODEL:
                return "MG_RETCODE_INVALID_MODEL";
        }

        return null;
    }

    public enum Ability {
        POSE,             ///< 3dpose 的能力
        EYESTATUS,        ///< 眼睛状态的能力
        MOUTHSTATUS,      ///< 嘴巴状态的能力
        MINORITY,         ///< 少数民族的能力
        BLURNESS,         ///< 人脸模糊度的能力
        AGEGENDER,        ///< 年龄性别的能力
        SMALLFEATEXT      ///< 人脸比对的能力
    }

    public static class Face {
        public int trackID;                ///< 人脸的跟踪标记。
        public int index;                  ///< 人脸数组下标
        public float confidence;           ///< 人脸置信度，为一个 0 ~ 1 之间的浮点数。
        ///< 超过 0.5 表示这确实是一个人脸。

        public float pitch;                ///< 一个弧度，表示物体顺时针饶x轴旋转的弧度。
        public float yaw;                  ///< 一个弧度，表示物体顺时针饶y轴旋转的弧度。
        public float roll;                 ///< 一个弧度，表示物体顺时针饶z轴旋转的弧度。
        public float[] leftEyestatus;      ///< 人左眼状态，每个数值表示概率，总和为 1
        public float[] rightEyestatus;     ///< 人右眼状态，每个数值表示概率，总和为 1
        public float[] moutstatus;         ///< 嘴部状态
        public float minority;             ///< 是否是少数民族（对于汉族而言）
        public float blurness;             ///< 模糊程度，数值越小表示越清晰，0 ~ 1
        public float age;                  ///< 年龄，为浮点数

        /**
         * 男女概率之和为 1
         */
        public float female;               ///< 是女性人脸的概率
        public float male;                 ///< 是男性人脸的概率

        public Rect rect;                  ///< 人脸在图像中的位置，以一个矩形框来刻画。
        public PointF[] points;            ///< 人脸关键点信息。

        public byte[] feature;             ///<feature_data 人脸特征数据，务必保证其内存大小不低于 feature_length
    }


    public static class FaceppConfig {
        public final static int DETECTION_MODE_NORMAL = 0;                 ///< 单张图片人脸检测模式

        public final static int DETECTION_MODE_TRACKING = 1;         ///< 视频人脸跟踪模式

        public final static int DETECTION_MODE_TRACKING_FAST = 3;          ///< 牺牲了人脸关键点的贴合度，提升了人脸跟踪的速度                                                   ///< 此模式下人脸检测与跟踪会更平均的使用 CPU 计算资源。

        public final static int DETECTION_MODE_TRACKING_ROBUST = 4;        ///< 提高了人脸关键点的贴合度，降低了人脸跟踪的速度

        public final static int DETECTION_MODE_TRACKING_RECT = 5;                   ///< 只检测人脸框，并不检测landmark

        public final static int MG_FPP_DETECTIONMODE_TRACK_RECT = 6;                   ///< 只检测人脸框，并不检测landmark





        public int minFaceSize;              ///< 最小检测人脸的尺寸（人脸尺寸一般是指人脸脸颊的宽度）。
        ///< 数值越大检测用的耗时越少。

        public int rotation;                 ///< 输入图像的重力方向，必须是 90 的倍数。
        ///< 表示输入图像顺时针旋转 rotation 度之后为正常的重力方向。
        ///< 推荐使用的数值：0, 90, 180, 270, 360

        public int interval;                 ///< 在 MG_FPP_DETECTIONMODE_TRACKING 模式下才有效。
        ///< 表示每隔多少帧进行一次全图的人脸检测。
        ///< 其余时间只对原有人脸进行跟踪。

        public int detectionMode;            ///< 人脸检测模式，可见 MG_FPP_DETECTIONMODE 类型。

        /**
         * 一个矩形框，表示只对图像中 roi 所表示的区域做人脸检测。
         * 在特定场景下，此方法可以提高检测速度。
         * 如果人脸在 roi 中被检测到，且移动到了 roi 之外的区域，依然可以被跟踪。
         */
        public int roi_left;               ///< roi的left坐标
        public int roi_top;                ///< roi的top坐标
        public int roi_right;              ///< roi的right坐标
        public int roi_bottom;             ///< roi的bottom坐标

        public float face_confidence_filter;

        public int one_face_tracking;  ///< 是否只识别一张脸 0表示识别多张脸，1表示只识别1张脸
    }
}
