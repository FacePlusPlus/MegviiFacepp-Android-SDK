#include "include/MG_Facepp.h"
#include <android/log.h>
#include "megvii_facepp_jni.h"
#include <jni.h>

#include <vector>
#include <algorithm>
#include <string>
#include <chrono>
#include <cmath>

#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,"mgf-c",__VA_ARGS__)

template<class point_t>
inline void rotate_point_2d(int w, int h, point_t &x, point_t &y,
                            int orientation) {
    point_t tmp;
    switch (orientation % 360) {
        case 90:
            tmp = x;
            x = h - y;
            y = tmp;
            break;
        case 180:
            x = w - x;
            y = h - y;
            break;
        case 270:
            tmp = x;
            x = y;
            y = w - tmp;
            break;
        default:
            break;
    }
}

class Timer {
    std::chrono::system_clock::time_point start;
    std::string name;

public:
    Timer(std::string name) :
            name(name) {
        start = std::chrono::system_clock::now();
    }

    ~Timer() {
        std::chrono::system_clock::time_point end =
                std::chrono::system_clock::now();
        LOGE("%s, used time = %lld\n", name.c_str(),
             (std::chrono::duration_cast<std::chrono::microseconds
             >(end - start)).count());
    }
};

#define LANDMARK_ST_NR 106

struct ApiHandle {
    MG_FPP_APIHANDLE api;
    MG_FPP_IMAGEHANDLE imghandle;
    int points;
    int w, h, orientation;
};

/*
 * Class: com.megvii.fppapidemo.Api.nativeInit(Context, byte[])
 */
jlong Java_com_megvii_facepp_sdk_jni_NativeFaceppAPI_nativeInit(JNIEnv *env, jobject,
                                                                jobject context, jbyteArray model) {

    jbyte *model_data = env->GetByteArrayElements(model, 0);
    long model_len = env->GetArrayLength(model);

    ApiHandle *h = new ApiHandle();
    int retcode = mg_facepp.CreateApiHandle(env, context,
                                            reinterpret_cast<const MG_BYTE *>(model_data),
                                            model_len, &h->api);
    env->ReleaseByteArrayElements(model, model_data, 0);
    LOGE("nativeInit retcode: %d", retcode);
    if (retcode != 0) {
        return retcode;
    }

    h->imghandle = nullptr;

    return reinterpret_cast<jlong>(h);
}

jintArray Java_com_megvii_facepp_sdk_jni_NativeFaceppAPI_nativeGetFaceppConfig(
        JNIEnv *env, jobject, jlong handle) {
    ApiHandle *h = reinterpret_cast<ApiHandle *>(handle);

    jintArray retArray = env->NewIntArray(9);

    MG_FPP_APICONFIG config;
    mg_facepp.GetDetectConfig(h->api, &config);

    int min_face_size = config.min_face_size;
    int rotation = config.rotation;
    int interval = config.interval;
    int detection_mode = config.detection_mode;

    env->SetIntArrayRegion(retArray, 0, 1, &min_face_size);
    env->SetIntArrayRegion(retArray, 1, 1, &rotation);
    env->SetIntArrayRegion(retArray, 2, 1, &interval);
    env->SetIntArrayRegion(retArray, 3, 1, &detection_mode);

    env->SetIntArrayRegion(retArray, 4, 1, &(config.roi.left));
    env->SetIntArrayRegion(retArray, 5, 1, &(config.roi.top));
    env->SetIntArrayRegion(retArray, 6, 1, &(config.roi.right));
    env->SetIntArrayRegion(retArray, 7, 1, &(config.roi.bottom));
    env->SetIntArrayRegion(retArray, 8, 1, &(config.one_face_tracking));

    return retArray;
}

jint Java_com_megvii_facepp_sdk_jni_NativeFaceppAPI_nativeSetFaceppConfig(JNIEnv *,
                                                                          jobject, jlong handle,
                                                                          jint minFaceSize,
                                                                          jint rotation,
                                                                          jint interval,
                                                                          jint detection_mode,
                                                                          jint left, jint top,
                                                                          jint right, jint bottom,
                                                                          jint one_face_tracking) {
    ApiHandle *h = reinterpret_cast<ApiHandle *>(handle);
    h->orientation = rotation;
    MG_FPP_APICONFIG config;
    mg_facepp.GetDetectConfig(h->api, &config);
    config.min_face_size = minFaceSize;
    config.rotation = rotation;
    config.interval = interval;
    config.detection_mode = (MG_FPP_DETECTIONMODE) detection_mode;
    MG_RECTANGLE _roi;
    _roi.left = left;
    _roi.top = top;
    _roi.right = right;
    _roi.bottom = bottom;
    config.roi = _roi;
    config.one_face_tracking = one_face_tracking;
    int retcode = mg_facepp.SetDetectConfig(h->api, &config);
    return retcode;
}

/*
 * Class: com.megvii.fppapidemo.Api.nativeGetFacesPoint(jlong handle, img_data, img_width, img_hegiht)
 * Return: [[left, top, right, bottom], [(x,y) x 81]] * face_nr
 */
jint Java_com_megvii_facepp_sdk_jni_NativeFaceppAPI_nativeDetect(JNIEnv *env, jobject,
                                                                 jlong handle, jbyteArray img,
                                                                 jint width, jint height,
                                                                 jint imageMode) {

    ApiHandle *h = reinterpret_cast<ApiHandle *>(handle);
    jbyte *img_data = env->GetByteArrayElements(img, 0);
    //LOGE("nativeDetect length: %d", 0);

    if (h->imghandle != nullptr && (h->w != width || h->h != height)) {
        mg_facepp.ReleaseImageHandle(h->imghandle);
        h->imghandle = nullptr;
    }
    if (h->imghandle == nullptr) {
        mg_facepp.CreateImageHandle(width, height, &h->imghandle);
        h->w = width;
        h->h = height;
    }

    MG_FPP_IMAGEHANDLE imageHandle = h->imghandle;
    //LOGE("nativeDetect length: %d, imageMode: %d,", 1, imageMode);
    mg_facepp.SetImageData(imageHandle, (unsigned char *) img_data, (MG_IMAGEMODE) imageMode);
    //LOGE("nativeDetect length: %d", 2);
    int faceCount = 0;
    mg_facepp.Detect(h->api, imageHandle, &faceCount);
    env->ReleaseByteArrayElements(img, img_data, 0); //release javabytearray
    return faceCount;
}

jfloatArray Java_com_megvii_facepp_sdk_jni_NativeFaceppAPI_nativeFaceInfo(JNIEnv *env,
                                                                          jobject, jlong handle,
                                                                          jint index) {
    ApiHandle *h = reinterpret_cast<ApiHandle *>(handle);
    jfloatArray retArray = env->NewFloatArray(MG_LANDMARK_NR * 2 + 10);
    MG_FACE face;
    mg_facepp.GetFaceInfo(h->api, index, &face);

    float track_id = face.track_id;
    env->SetFloatArrayRegion(retArray, 0, 1, &(track_id));
    float idx = index;
    env->SetFloatArrayRegion(retArray, 1, 1, &(idx));

    env->SetFloatArrayRegion(retArray, 2, 1, &(face.confidence));
    MG_RECTANGLE rect = face.rect;
    float left = rect.left;
    float top = rect.top;
    float right = rect.right;
    float bottom = rect.bottom;
    env->SetFloatArrayRegion(retArray, 3, 1, &left);
    env->SetFloatArrayRegion(retArray, 4, 1, &top);
    env->SetFloatArrayRegion(retArray, 5, 1, &right);
    env->SetFloatArrayRegion(retArray, 6, 1, &bottom);
    float pitch = face.pose.pitch;
    float yaw = face.pose.yaw;
    float roll = face.pose.roll;
    env->SetFloatArrayRegion(retArray, 7, 1, &pitch);
    env->SetFloatArrayRegion(retArray, 8, 1, &yaw);
    env->SetFloatArrayRegion(retArray, 9, 1, &roll);

    MG_FACELANDMARKS facelandmark = face.points;
    MG_POINT *points = facelandmark.point;
    for (int j = 0; j < MG_LANDMARK_NR; ++j) {
        float point[2];
        point[0] = points[j].x;
        point[1] = points[j].y;

        rotate_point_2d(h->w, h->h, point[0], point[1], h->orientation);

        env->SetFloatArrayRegion(retArray, 10 + j * 2, 2, point);
    }

    return retArray;
}

jfloatArray Java_com_megvii_facepp_sdk_jni_NativeFaceppAPI_nativeLandMark(JNIEnv *env,
                                                                          jobject, jlong handle,
                                                                          jint index,
                                                                          jint point_nr) {
    ApiHandle *h = reinterpret_cast<ApiHandle *>(handle);
    jfloatArray retArray = env->NewFloatArray(LANDMARK_ST_NR * 2);
    MG_POINT buff[LANDMARK_ST_NR];
    mg_facepp.GetLandmark(h->api, index, true, point_nr, buff);
    for (int j = 0; j < point_nr; ++j) {
        float point[2];
        point[0] = buff[j].x;
        point[1] = buff[j].y;

        rotate_point_2d(h->w, h->h, point[0], point[1], h->orientation);

        env->SetFloatArrayRegion(retArray, j * 2, 2, point);
    }

    return retArray;
}

jfloatArray Java_com_megvii_facepp_sdk_jni_NativeFaceppAPI_nativeAttribute(JNIEnv *env,
                                                                           jobject, jlong handle,
                                                                           jint index) {
    ApiHandle *h = reinterpret_cast<ApiHandle *>(handle);
    jfloatArray retArray = env->NewFloatArray(LANDMARK_ST_NR * 2);
    MG_FACE mgFace;
    mg_facepp.GetAttribute(h->api, h->imghandle, index,
                           MG_FPP_ATTR_POSE3D | MG_FPP_ATTR_EYESTATUS | MG_FPP_ATTR_MOUTHSTATUS
                           | MG_FPP_ATTR_MINORITY | MG_FPP_ATTR_BLURNESS
                           | MG_FPP_ATTR_AGE_GENDER, &mgFace);
    float pitch = mgFace.pose.pitch;
    float yaw = mgFace.pose.yaw;
    float roll = mgFace.pose.roll;
    env->SetFloatArrayRegion(retArray, 0, 1, &pitch);
    env->SetFloatArrayRegion(retArray, 1, 1, &yaw);
    env->SetFloatArrayRegion(retArray, 2, 1, &roll);

    int offset = 3;

    for (int i = 0; i < MG_EYESTATUS_COUNT; ++i) {
        float left_eyestatu = mgFace.left_eyestatus[i];
        env->SetFloatArrayRegion(retArray, i + offset, 1, &left_eyestatu);
    }
    offset += MG_EYESTATUS_COUNT;
    for (int i = 0; i < MG_EYESTATUS_COUNT; ++i) {
        float right_eyestatu = mgFace.right_eyestatus[i];
        env->SetFloatArrayRegion(retArray, i + offset, 1, &right_eyestatu);
    }
    offset += MG_EYESTATUS_COUNT;

    for (int i = 0; i < MG_MOUTHSTATUS_COUNT; ++i) {
        float moutstatu = mgFace.moutstatus[i];
        env->SetFloatArrayRegion(retArray, i + offset, 1, &moutstatu);
    }
    offset += MG_MOUTHSTATUS_COUNT;

    float minority = mgFace.minority;
    env->SetFloatArrayRegion(retArray, offset, 1, &minority);
    float blurness = mgFace.blurness;
    env->SetFloatArrayRegion(retArray, offset + 1, 1, &blurness);
    float age = mgFace.age;
    env->SetFloatArrayRegion(retArray, offset + 2, 1, &age);
    MG_GENDER gender = mgFace.gender;
    float female = gender.female;
    float male = gender.male;
    env->SetFloatArrayRegion(retArray, offset + 3, 1, &female);
    env->SetFloatArrayRegion(retArray, offset + 4, 1, &male);

    return retArray;
}

jfloatArray Java_com_megvii_facepp_sdk_jni_NativeFaceppAPI_nativePose3D(JNIEnv *env,
                                                                        jobject, jlong handle,
                                                                        jint index) {
    ApiHandle *h = reinterpret_cast<ApiHandle *>(handle);
    jfloatArray retArray = env->NewFloatArray(3);
    MG_FACE mgFace;
    mg_facepp.GetAttribute(h->api, h->imghandle, index, MG_FPP_ATTR_POSE3D,
                           &mgFace);
    float pitch = mgFace.pose.pitch;
    float yaw = mgFace.pose.yaw;
    float roll = mgFace.pose.roll;
    env->SetFloatArrayRegion(retArray, 0, 1, &pitch);
    env->SetFloatArrayRegion(retArray, 1, 1, &yaw);
    env->SetFloatArrayRegion(retArray, 2, 1, &roll);

    return retArray;
}

jfloatArray Java_com_megvii_facepp_sdk_jni_NativeFaceppAPI_nativeEyeStatus(JNIEnv *env,
                                                                           jobject, jlong handle,
                                                                           jint index) {
    ApiHandle *h = reinterpret_cast<ApiHandle *>(handle);
    jfloatArray retArray = env->NewFloatArray(LANDMARK_ST_NR * 2);
    MG_FACE mgFace;
    mg_facepp.GetAttribute(h->api, h->imghandle, index, MG_FPP_ATTR_EYESTATUS,
                           &mgFace);
    for (int i = 0; i < MG_EYESTATUS_COUNT; ++i) {
        float left_eyestatu = mgFace.left_eyestatus[i];
        env->SetFloatArrayRegion(retArray, i, 1, &left_eyestatu);
    }
    for (int i = 0; i < MG_EYESTATUS_COUNT; ++i) {
        float right_eyestatu = mgFace.right_eyestatus[i];
        env->SetFloatArrayRegion(retArray, MG_EYESTATUS_COUNT + i, 1,
                                 &right_eyestatu);
    }
    return retArray;
}

jfloatArray Java_com_megvii_facepp_sdk_jni_NativeFaceppAPI_nativeMouthStatus(
        JNIEnv *env, jobject, jlong handle, jint index) {
    ApiHandle *h = reinterpret_cast<ApiHandle *>(handle);
    jfloatArray retArray = env->NewFloatArray(LANDMARK_ST_NR * 2);
    MG_FACE mgFace;
    mg_facepp.GetAttribute(h->api, h->imghandle, index, MG_FPP_ATTR_MOUTHSTATUS,
                           &mgFace);
    for (int i = 0; i < MG_MOUTHSTATUS_COUNT; ++i) {
        float moutstatu = mgFace.moutstatus[i];
        env->SetFloatArrayRegion(retArray, i, 1, &moutstatu);
    }

    return retArray;
}

jfloatArray Java_com_megvii_facepp_sdk_jni_NativeFaceppAPI_nativeMinority(JNIEnv *env,
                                                                          jobject, jlong handle,
                                                                          jint index) {
    ApiHandle *h = reinterpret_cast<ApiHandle *>(handle);
    jfloatArray retArray = env->NewFloatArray(1);
    MG_FACE mgFace;
    mg_facepp.GetAttribute(h->api, h->imghandle, index, MG_FPP_ATTR_MINORITY,
                           &mgFace);
    float minority = mgFace.minority;
    env->SetFloatArrayRegion(retArray, 0, 1, &minority);

    return retArray;
}

jfloatArray Java_com_megvii_facepp_sdk_jni_NativeFaceppAPI_nativeBlurness(JNIEnv *env,
                                                                          jobject, jlong handle,
                                                                          jint index) {
    ApiHandle *h = reinterpret_cast<ApiHandle *>(handle);
    jfloatArray retArray = env->NewFloatArray(1);
    MG_FACE mgFace;
    mg_facepp.GetAttribute(h->api, h->imghandle, index, MG_FPP_ATTR_BLURNESS,
                           &mgFace);
    float blurness = mgFace.blurness;
    env->SetFloatArrayRegion(retArray, 0, 1, &blurness);

    return retArray;
}

jfloatArray Java_com_megvii_facepp_sdk_jni_NativeFaceppAPI_nativeAgeGender(JNIEnv *env,
                                                                           jobject, jlong handle,
                                                                           jint index) {
    ApiHandle *h = reinterpret_cast<ApiHandle *>(handle);
    jfloatArray retArray = env->NewFloatArray(3);
    MG_FACE mgFace;
    mg_facepp.GetAttribute(h->api, h->imghandle, index, MG_FPP_ATTR_AGE_GENDER,
                           &mgFace);
    float age = mgFace.age;
    MG_GENDER gender = mgFace.gender;
    float female = gender.female;
    float male = gender.male;
    env->SetFloatArrayRegion(retArray, 0, 1, &age);
    env->SetFloatArrayRegion(retArray, 1, 1, &female);
    env->SetFloatArrayRegion(retArray, 2, 1, &male);

    return retArray;
}

jlongArray Java_com_megvii_facepp_sdk_jni_NativeFaceppAPI_nativeGetAlgorithmInfo(
        JNIEnv *env, jobject, jbyteArray model) {
    jbyte *model_data = env->GetByteArrayElements(model, 0);
    long model_len = env->GetArrayLength(model);

    MG_ALGORITHMINFO algorithm_info;
    mg_facepp.GetAlgorithmInfo(reinterpret_cast<const MG_BYTE *>(model_data), model_len,
                               &algorithm_info);
    env->ReleaseByteArrayElements(model, model_data, 0);

    jlong expire_time = algorithm_info.expire_time;
    jlong auth_type = algorithm_info.auth_type;
    jlong ability = algorithm_info.ability;
    jlongArray retArray = env->NewLongArray(3);
    env->SetLongArrayRegion(retArray, 0, 1, &expire_time);
    env->SetLongArrayRegion(retArray, 1, 1, &auth_type);
    env->SetLongArrayRegion(retArray, 2, 1, &ability);

    return retArray;
}

jint Java_com_megvii_facepp_sdk_jni_NativeFaceppAPI_nativeExtractFeature(JNIEnv *,
                                                                         jobject, jlong handle,
                                                                         jint index) {
    ApiHandle *h = reinterpret_cast<ApiHandle *>(handle);
    MG_INT32 feature_length;
    mg_facepp.ExtractFeature(h->api, h->imghandle, index, &feature_length);

    return (int) feature_length;
}

jbyteArray Java_com_megvii_facepp_sdk_jni_NativeFaceppAPI_nativeGetFeatureData(
        JNIEnv *env, jobject, jlong handle, jint featureLength) {
    ApiHandle *h = reinterpret_cast<ApiHandle *>(handle);
    jbyteArray featureData = env->NewByteArray(featureLength * sizeof(float));
//	jfloat* feature_data = env->GetFloatArrayElements(featureData, 0);

    jfloat feature_data[featureLength];
    mg_facepp.GetFeatureData(h->api, &feature_data[0], featureLength);
//
//	env->ReleaseFloatArrayElements(featureData, feature_data, 0); //release javabytearray
//
    env->SetByteArrayRegion(featureData, 0, featureLength * sizeof(float),
                            reinterpret_cast<jbyte *>(&feature_data[0]));

//	for (int i = 0; i < featureLength; ++i) {
//		float feature = feature_data[i];
//		env->SetFloatArrayRegion(featureData, i, 1, &feature);
//	}

    return featureData;
}

jdouble Java_com_megvii_facepp_sdk_jni_NativeFaceppAPI_nativeFaceCompare(JNIEnv *env,
                                                                         jobject, jlong handle,
                                                                         jbyteArray featureData1,
                                                                         jbyteArray featureData2,
                                                                         jint featureLength) {
    ApiHandle *h = reinterpret_cast<ApiHandle *>(handle);
    jbyte *feature_data1 = env->GetByteArrayElements(featureData1, 0);
    jbyte *feature_data2 = env->GetByteArrayElements(featureData2, 0);
    MG_DOUBLE score;
    mg_facepp.FaceCompare(h->api, reinterpret_cast<jfloat *>(feature_data1),
                          reinterpret_cast<jfloat *>(feature_data2), featureLength, &score);
    env->ReleaseByteArrayElements(featureData1, feature_data1, 0); //release javabytearray
    env->ReleaseByteArrayElements(featureData2, feature_data2, 0); //release javabytearray
    return (double) score;
}

/*
 * Class: com.megvii.fppapidemo.Api.NativeRelease(jlong handle)
 */
void Java_com_megvii_facepp_sdk_jni_NativeFaceppAPI_nativeRelease(JNIEnv *, jobject,
                                                                  jlong handle) {

    ApiHandle *h = reinterpret_cast<ApiHandle *>(handle);
    if (h->imghandle != nullptr)
        mg_facepp.ReleaseImageHandle(h->imghandle);

    mg_facepp.ReleaseApiHandle(h->api);
    delete h;
}

jlong Java_com_megvii_facepp_sdk_jni_NativeFaceppAPI_nativeGetApiExpication(JNIEnv *env,
                                                                            jobject, jobject ctx) {

    return (long) mg_facepp.GetApiExpiration(env, ctx);
}

jstring Java_com_megvii_facepp_sdk_jni_NativeFaceppAPI_nativeGetVersion(JNIEnv *env,
                                                                        jobject) {
    const char *version = mg_facepp.GetApiVersion();
    return env->NewStringUTF(version);
}

jlong Java_com_megvii_facepp_sdk_jni_NativeFaceppAPI_nativeGetApiName(JNIEnv *,
                                                                      jobject) {
    return (jlong) (mg_facepp.GetApiVersion);
}

jint Java_com_megvii_facepp_sdk_jni_NativeFaceppAPI_nativeGetSDKAuthType(JNIEnv *,
                                                                         jobject) {
    return (jint) mg_facepp.GetSDKAuthType();
}
