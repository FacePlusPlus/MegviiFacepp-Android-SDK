/**
 * @file MG_Facepp.h
 * @brief 人脸算法头文件
 *
 * 包含 Face++ 的人脸算法相关的接口，根据不同模型可以实现人脸检测、
 * 跟踪、人脸属性计算以及人脸比对。
 */

#ifndef _MG_FACEPP_H_
#define _MG_FACEPP_H_

#include "MG_Common.h"

#define _OUT

#ifdef __cplusplus
extern "C" {
#endif

#define MG_FPP_GET_LANDMARK106 106      ///< 计算 106 个关键点
#define MG_FPP_GET_LANDMARK101 101      ///< 计算 101 个关键点
#define MG_FPP_GET_LANDMARK81 81        ///< 计算 81 个关键点

#define MG_FPP_ATTR_POSE3D 0x01             ///< 3dpose 的标识位
#define MG_FPP_ATTR_EYESTATUS 0x02          ///< 眼睛状态的标识位
#define MG_FPP_ATTR_MOUTHSTATUS 0x04        ///< 嘴巴状态的标识位
#define MG_FPP_ATTR_MINORITY 0x08           ///< 少数民族的标识位
#define MG_FPP_ATTR_BLURNESS 0x10           ///< 人脸模糊度的标识位
#define MG_FPP_ATTR_AGE_GENDER 0x20         ///< 年龄性别的标识位
#define MG_FPP_ATTR_EXTRACT_FEATURE 0x40    ///< 人脸比对的标识位

#define MG_FPP_DETECT 0x1000                ///< 人脸检测的标识位
#define MG_FPP_TRACK 0x2000                 ///< 人脸跟踪的标识位

/**
 * @brief 人脸检测模式类型
 * 
 * 支持对单张图片做人脸检测，也支持对视频流做人脸检测。
 */
typedef enum {
    MG_FPP_DETECTIONMODE_NORMAL = 0,        ///< 单张图片人脸检测模式

    MG_FPP_DETECTIONMODE_TRACKING,          ///< 视频人脸跟踪模式

    MG_FPP_DETECTIONMODE_TRACKING_SMOOTH,    ///< 特殊的视频人脸跟踪模式。
    ///< 此模式下人脸检测与跟踪会更平均的使用 CPU 计算资源。
            MG_FPP_DETECTIONMODE_TRACKING_FAST,     ///< 牺牲了人脸关键点的贴合度，提升了人脸跟踪的速度
    MG_FPP_DETECTIONMODE_TRACKING_ROBUST    ///< 提高了人脸关键点的贴合度，降低了人脸跟踪的速度
} MG_FPP_DETECTIONMODE;                        ///< 检测人脸时只跟踪单张人脸

struct _MG_FPP_API;
/**
 * @brief 人脸算法句柄
 */
typedef struct _MG_FPP_API *MG_FPP_APIHANDLE;

struct _MG_FPP_IMAGE;
/**
 * @brief 人脸算法使用的图像句柄
 */
typedef struct _MG_FPP_IMAGE *MG_FPP_IMAGEHANDLE;

/**
 * @brief 人脸检测算法配置类型 
 * 
 * 可以对人脸检测算法进行配置。
 */
typedef struct {
    MG_UINT32 min_face_size;                ///< 最小检测人脸的尺寸（人脸尺寸一般是指人脸脸颊的宽度）。
    ///< 数值越大检测用的耗时越少。

    MG_UINT32 rotation;                     ///< 输入图像的重力方向，必须是 90 的倍数。
    ///< 表示输入图像顺时针旋转 rotation 度之后为正常的重力方向。
    ///< 推荐使用的数值：0, 90, 180, 270, 360

    MG_UINT32 interval;                     ///< 在 MG_FPP_DETECTIONMODE_TRACKING 模式下才有效。
    ///< 表示每隔多少帧进行一次全图的人脸检测。
    ///< 其余时间只对原有人脸进行跟踪。

    MG_FPP_DETECTIONMODE detection_mode;    ///< 人脸检测模式，可见 MG_FPP_DETECTIONMODE 类型。

    MG_RECTANGLE roi;                       ///< 一个矩形框，表示只对图像中 roi 所表示的区域做人脸检测。
    ///< 在特定场景下，此方法可以提高检测速度。
    ///< 如果人脸在 roi 中被检测到，且移动到了 roi 之外的区域，依然可以被跟踪。
    MG_BOOL one_face_tracking;
} MG_FPP_APICONFIG;

/**
 * @brief 人脸算法函数集合
 *
 * 所有的算法函数都表示为该类型的一个变量，可以用形如：
 *   mg_facepp.Function(...)
 * 的形式进行调用。
 */
typedef struct {
    /**
     * @brief 创建人脸算法句柄（handle）
     *
     * 传入算法模型数据，创建一个算法句柄。
     *
     * @param[in] env               Android jni 的环境变量，仅在 Android SDK 中使用
     * @param[in] jobj              Android 调用的上下文，仅在 Android SDK 中使用
     * @param[in] model_data        算法模型的二进制数据
     * @param[in] model_length      算法模型的字节长度
     *
     * @param[out] api_handle_ptr   算法句柄的指针，成功创建后会修改其值
     *
     * @return 成功则返回 MG_RETCODE_OK
     */
    MG_RETCODE (*CreateApiHandle)(
#if MGAPI_BUILD_ON_ANDROID
            JNIEnv *env,
            jobject jobj,
#endif
            const MG_BYTE *model_data,
            MG_INT32 model_length,
            MG_FPP_APIHANDLE _OUT *api_handle_ptr);

    /**
     * @brief 释放人脸算法句柄（handle）
     *
     * 释放一个算法句柄。
     *
     * @param[in] api_handle 人脸算法句柄
     *
     * @return 成功则返回 MG_RETCODE_OK
     */
    MG_RETCODE (*ReleaseApiHandle)(
            MG_FPP_APIHANDLE api_handle);

    /**
     * @brief 获取算法版本信息
     *
     * @return 返回一个字符串，表示算法版本号及相关信息
     */
    const char *(*GetApiVersion)();

    /**
     * @brief 查看算法授权的过期时间
     *
     * @warning 此接口已经废弃，可以用 GetAlgorithmInfo 函数代替。
     * 在初次使用 SDK 时，需要先调用 CreateApiHandle 方法才能正确返回过期时间。
     *
     * @param[in] env               Android jni 的环境变量，仅在 Android SDK 中使用
     * @param[in] jobj              Android 调用的上下文，仅在 Android SDK 中使用
     *
     * @return 成功则返回 MG_RETCODE_OK
     */
    MG_UINT64 (*GetApiExpiration)(
#if MGAPI_BUILD_ON_ANDROID
            JNIEnv *env,
            jobject jobj
#endif
    );

    /**
     * @brief 获取当前算法的配置信息
     *
     * 获取算法句柄对应的配置信息。
     *
     * @param[in] api_handle 算法句柄
     *
     * @param[out] config 算法配置信息
     *
     * @return 成功则返回 MG_RETCODE_OK
     */
    MG_RETCODE (*GetDetectConfig)(
            MG_FPP_APIHANDLE api_handle,
            MG_FPP_APICONFIG _OUT *config);

    /**
     * @brief 设置算法配置信息
     *
     * 将算法的配置信息设置到算法句柄中。
     *
     * @param[in] api_handle 算法句柄
     * @param[in] config 算法配置信息
     *
     * @return 成功则返回 MG_RETCODE_OK
     */
    MG_RETCODE (*SetDetectConfig)(
            MG_FPP_APIHANDLE api_handle,
            const MG_FPP_APICONFIG *config);

    /**
     * @brief 检测图像中的人脸
     * 
     * 检测一张图像，并返回检测到的人脸个数。
     * 人脸检测使用的是灰度图，传入灰度的图像数据可以减少算法做图像格式转换的时间。
     *
     * @param[in] api_handle 算法句柄
     * @param[in] image_handle 图像句柄
     *
     * @param[out] face_nr 检测到的人脸个数，人脸以0~face_nr-1编号。
     *
     * @return 成功则返回 MG_RETCODE_OK
     */
    MG_RETCODE (*Detect)(
            MG_FPP_APIHANDLE api_handle,
            MG_FPP_IMAGEHANDLE image_handle,
            MG_INT32 _OUT *face_nr);

    /**
     * @brief 获取人脸信息
     *
     * 通过人脸标号获取人脸信息。
     *
     * @param[in] api_handle 算法句柄
     * @param[in] idx 人脸编号（人脸以0~face_nr-1编号）
     * @param[out] face 人脸信息
     *
     * @return 成功则返回 MG_RETCODE_OK
     */
    MG_RETCODE (*GetFaceInfo)(
            MG_FPP_APIHANDLE api_handle,
            MG_INT32 idx,
            MG_FACE _OUT *face);


    /**
     * @brief 获取人脸关键点信息
     *
     * 可以通过参数控制，获取不同个数的关键点，也可以获取平滑过的关键点。
     *
     * @param[in] api_handle 算法句柄
     * @param[in] idx 人脸编号（人脸以0~face_nr-1编号）
     * @param[in] is_smooth 是否需要进行平滑处理。选择平滑处理可以让前后帧关键点相对比较稳定。
     * @param[in] nr 获取的关键点个数，目前只有3种数值是合理的，分别是81点、101点和106点。
     *
     * @param[out] points 获取的人脸关键点
     *
     * @return 成功则返回 MG_RETCODE_OK
     */
    MG_RETCODE (*GetLandmark)(
            MG_FPP_APIHANDLE api_handle,
            MG_INT32 idx,
            MG_BOOL is_smooth,
            MG_INT32 nr,
            MG_POINT _OUT *points);

    /**
     * @brief 计算一张人脸的属性
     *
     * 通过attribute_mode控制计算哪些人脸属性，将计算的结果写入到face的具体字段当中。
     *
     * @param[in] api_handle 算法句柄
     * @param[in] image_handle 图像句柄
     * @param[in] idx 人脸编号（人脸以0~face_nr-1编号）
     * @param[in] attribute_mode 需要计算的属性类型，将需要计算的属性用或（"|"）符号链接，传入即可
     *
     * @param[out] face 人脸信息，根据 attribute_mode 的不同，会计算对应人脸属性。
     *
     * @return 成功则返回 MG_RETCODE_OK
     */
    MG_RETCODE (*GetAttribute)(
            MG_FPP_APIHANDLE api_handle,
            MG_FPP_IMAGEHANDLE image_handle,
            MG_INT32 idx,
            MG_INT32 attribute_mode,
            MG_FACE _OUT *face);

    /**
     * @brief 创建图像句柄（handle）
     *
     * 图像句柄是表示的是一张图像，创建时需要确定其宽和高，且后续不能修改。
     * 如果是对连续的视频图像进行图像检测，可以使用同一个图像句柄，而不用反复释放。
     *
     * @param[in] width 图像的宽
     * @param[in] height 图像的高
     *
     * @param[out] image_handle_ptr 图像句柄的指针，成功创建后会修改其值
     *
     * @return 成功则返回 MG_RETCODE_OK
     */
    MG_RETCODE (*CreateImageHandle)(
            MG_INT32 width,
            MG_INT32 height,
            MG_FPP_IMAGEHANDLE _OUT *image_handle_ptr);

    /**
     * @brief 设置图像数据
     *
     * 传图的图像数据的内存，需要保证在图像句柄被释放前都是有效的。
     * 如果一个图像句柄被反复调用该函数，则以最后一次设置的图像数据为准，且不会再用到上一次设置的数据。
     *
     * @param[in] image_handle 图像句柄
     * @param[in] image_data 图像数据，其大小应该有图像大小及格式决定。
     *                      （如：一张YUV的图，其数据大小应该为witdh*height*1.5，且witdh和height必须都是2的倍数）
     * @param[in] image_mode 图像格式
     *
     * @return 成功则返回 MG_RETCODE_OK
     */
    MG_RETCODE (*SetImageData)(
            MG_FPP_IMAGEHANDLE image_handle,
            const MG_BYTE *image_data,
            MG_IMAGEMODE image_mode);

    /**
     * @brief 释放图像句柄（handle）
     *
     * @param[in] image_handle 图像句柄
     *
     * @return 成功则返回 MG_RETCODE_OK
     */
    MG_RETCODE (*ReleaseImageHandle)(
            MG_FPP_IMAGEHANDLE image_handle);

    /**
     * @brief 获取 SDK 的授权类型
     *
     * @warning 此接口已经废弃，可以用 GetAlgorithmInfo 函数代替。
     *
     * @return 只有联网授权和非联网授权两种类型
     */
    MG_SDKAUTHTYPE (*GetSDKAuthType)();

    /**
     * @brief 获取算法相关信息
     * 
     * 读取模型中相关参数，返回当前SDK的所使用的算法的相关信息。
     *
     * @param[in] model_data 算法模型的二进制数据
     * @param[in] model_length 算法模型的字节长度
     *
     * @param[out] algorithm_info 算法相关信息
     *
     * @return 成功则返回 MG_RETCODE_OK
     */
    MG_RETCODE (*GetAlgorithmInfo)(
            const MG_BYTE *model_data,
            MG_INT32 model_length,
            MG_ALGORITHMINFO *algorithm_info);

    /**
     * @brief 抽取人脸特征
     *
     * 抽取图像中特定人脸的特征，特征以单精度浮点数存储
     * 
     * @param[in] api_handle 算法句柄
     * @param[in] image_handle 图像句柄
     * @param[in] idx 人脸编号（人脸以0~face_nr-1编号）
     *
     * @param[out] feature_length_ptr 存储特征需要的单精度浮点数长度
     *
     * @return 成功则返回 MG_RETCODE_OK
     */
    MG_RETCODE (*ExtractFeature)(
            MG_FPP_APIHANDLE api_handle,
            MG_FPP_IMAGEHANDLE image_handle,
            MG_INT32 idx,
            MG_INT32 *_OUT feature_length_ptr);

    /**
     * @brief 获取人脸特征数据
     *
     * 在调用 ExtractFeature 后，调用此函数获取特征数据
     *
     * @param[in] api_handle 算法句柄
     * @param[in] feature_length 特征的长度，通过 ExtractFeature 函数获得
     *
     * @param[out] feature_data 人脸特征数据，务必保证其内存大小不低于 feature_length
     *
     * @return 成功则返回 MG_RETCODE_OK
     */
    MG_RETCODE (*GetFeatureData)(
            MG_FPP_APIHANDLE api_handle,
            void _OUT *feature_data,
            MG_INT32 feature_length);

    /**
     * @brief 获取人脸比对的分数
     *
     * 传入两个特征，比对两个特征后产生对应分数。传入的特征顺序课交换。
     * 阈值如下：
     *   - 1e-2: 63.07
     *   - 1e-3: 73.43
     *   - 1e-4: 79.79
     *   - 1e-5: 84.02
     *
     * @param[in] api_handle 算法句柄
     * @param[in] feature_data1 参与比对的特征1
     * @param[in] feature_data2 参与比对的特征2
     * @param[in] feature_length 特征的长度，通过 ExtractFeature 函数获得
     *
     * @param[out] score_ptr 人脸比对的分数
     *
     * @return 成功则返回 MG_RETCODE_OK
     */
    MG_RETCODE (*FaceCompare)(
            MG_FPP_APIHANDLE api_handle,
            const void *feature_data1,
            const void *feature_data2,
            MG_INT32 feature_length,
            MG_DOUBLE _OUT *score_ptr);

} MG_FACEPP_API_FUNCTIONS_TYPE;

/**
 * @brief 人脸算法域
 *
 * Example:
 *      mg_facepp.CreateApiHandle(...
 *      mg_facepp.Detect(...
 */
extern MG_EXPORT MG_FACEPP_API_FUNCTIONS_TYPE mg_facepp;

#ifdef __cplusplus
}
#endif

#undef _OUT
#endif // _MG_FACEPP_H_
