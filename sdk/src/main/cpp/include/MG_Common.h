/**
 * @file MG_Common.h
 * @brief 通用接口头文件
 *
 * 包含 Face++ 提供的算法接口的一些通用类型。
 */

#ifndef _MG_COMMON_H_
#define _MG_COMMON_H_

#ifdef __cplusplus
extern "C" {
#endif

/**
 * @brief 函数返回值类型
 *
 * 大部分函数返回的类型，用于表示函数运行是否正常。
 */
typedef enum {
    MG_RETCODE_OK = 0,              ///< 正确运行程序

    MG_RETCODE_INVALID_ARGUMENT,    ///< 传入了非法的参数

    MG_RETCODE_INVALID_HANDLE,      ///< 传入了非法的句柄（handle）

    MG_RETCODE_INDEX_OUT_OF_RANGE,  ///< 传入了非法的索引（index）

    MG_RETCODE_EXPIRE = 101,        ///< SDK已过期，函数无法正常运行

    MG_RETCODE_INVALID_BUNDLEID,    ///< 检测到包名与SDK所限制的包名不符

    MG_RETCODE_INVALID_LICENSE,     ///< 传入了错误的证书（license）

    MG_RETCODE_INVALID_MODEL,       ///< 传入了错误的模型（model）

    MG_RETCODE_FAILED = -1,         ///< 算法内部错误

    MG_RETCODE_GL_CONTEXT = 201,    ///< 不在 OpenGL context 下
} MG_RETCODE;

/**
 * @brief SDK 授权的类型
 */
typedef enum {
    MG_ONLINE_AUTH = 1,             ///< 联网授权

    MG_OFFLINE_AUTH = 2             ///< 非联网授权
} MG_SDKAUTHTYPE;

/**
 * @{
 * @brief SDK 导出符号
 */
#if defined(_WIN32)

#if defined(MGFACEAPI_LIBRARY)
#define MG_EXPORT __declspec(dllexport)
#else
#define MG_EXPORT __declspec(dllimport)
#endif

#elif defined(unix) || defined(__unix__) || defined(__unix) || defined(__GNUC__)

#if defined(MGFACEAPI_LIBRARY)
#define MG_EXPORT __attribute__ ((visibility ("default")))
#else
#define MG_EXPORT
#endif

#endif
/**
 * @}
 */

/**
 * @{
 * @brief 常用基础变量
 */
#ifndef MG_BASIC_TYPES
#if defined(unix) || defined(__unix__) || defined(__unix) || defined (__APPLE__) || defined(__MINGW_GCC) || defined(__MINGW32__)
#include <stdint.h>
typedef int8_t MG_INT8;
typedef int16_t MG_INT16;
typedef int32_t MG_INT32;
typedef int64_t MG_INT64;
typedef uint8_t MG_UINT8;
typedef uint16_t MG_UINT16;
typedef uint32_t MG_UINT32;
typedef uint64_t MG_UINT64;
#elif defined(_WIN32) || defined(WIN32) || defined(_WIN64) || defined(WIN64)
#include <windows.h>
typedef signed __int8      MG_INT8;
typedef signed __int16     MG_INT16;
typedef signed __int32     MG_INT32;
typedef signed __int64     MG_INT64;
typedef unsigned __int8    MG_UINT8;
typedef unsigned __int16   MG_UINT16;
typedef unsigned __int32   MG_UINT32;
typedef unsigned __int64   MG_UINT64;
#else
typedef signed char        MG_INT8;
typedef signed short       MG_INT16;
typedef int                MG_INT32;
typedef long long          MG_INT64;
typedef unsigned char      MG_UINT8;
typedef unsigned short     MG_UINT16;
typedef unsigned int       MG_UINT32;
typedef unsigned long long MG_UINT64;
#endif

typedef float MG_SINGLE;
typedef double MG_DOUBLE;
typedef unsigned char MG_BYTE;
typedef int MG_BOOL;
#ifndef NULL
#define NULL 0
#endif
#define MG_BASIC_TYPES
#endif
/**
 * @}
 */


/**
 * @{
 * @判断 SDK 使用平台
 */
#if __APPLE__
#define MGAPI_BUILD_ON_IPHONE   1

#elif __ANDROID__
#define MGAPI_BUILD_ON_ANDROID    1
#include <jni.h>
#elif __linux
#define MGAPI_BUILD_ON_LINUX    1
#else
#error "unsupported platform"
#endif
/**
 * @}
 */

/**
 * @brief 坐标点类型
 *
 * 表示一个二维平面上的坐标（笛卡尔坐标系）。
 */
typedef struct {
    MG_SINGLE x;                ///< 坐标点x轴的值

    MG_SINGLE y;                ///< 坐标点y轴的值
} MG_POINT;

/**
 * @brief 图像中平行于量坐标轴的矩形框
 *
 * 在图像中表示一个双边平行于坐标轴的矩形框，
 * 用 ( right - left ) 和 ( bottom - top ) 可以计算出矩形的宽和高。
 */
typedef struct {
    MG_INT32 left;              ///< 矩形框最左边的坐标值

    MG_INT32 top;               ///< 矩形框最上边的坐标值

    MG_INT32 right;             ///< 矩形框最右边的坐标值

    MG_INT32 bottom;            ///< 矩形框最下边的坐标值
} MG_RECTANGLE;

/**
 * @brief 图像格式类型
 *
 * 表示图像数据格式的枚举类型，支持几种常见的图像格式。
 */
typedef enum {
    MG_IMAGEMODE_GRAY = 0,      ///< 灰度图像

    MG_IMAGEMODE_BGR,           ///< BGR图像

    MG_IMAGEMODE_NV21,          ///< YUV420（nv21）图像

    MG_IMAGEMODE_RGBA,          ///< RGBA图像

    MG_IMAGEMODE_RGB,           ///< RGB图像

    MG_IMAGEMODE_COUNT          ///< 支持图像总数
} MG_IMAGEMODE;

/**
 * @{
 * @brief 81 点人脸关键点坐标定义
 */
#define MG_LEFT_EYE_PUPIL 0
#define MG_LEFT_EYE_LEFT_CORNER 1
#define MG_LEFT_EYE_RIGHT_CORNER 2
#define MG_LEFT_EYE_TOP 3
#define MG_LEFT_EYE_BOTTOM 4
#define MG_LEFT_EYE_UPPER_LEFT_QUARTER 5
#define MG_LEFT_EYE_LOWER_LEFT_QUARTER 6
#define MG_LEFT_EYE_UPPER_RIGHT_QUARTER 7
#define MG_LEFT_EYE_LOWER_RIGHT_QUARTER 8
#define MG_RIGHT_EYE_PUPIL 9
#define MG_RIGHT_EYE_LEFT_CORNER 10
#define MG_RIGHT_EYE_RIGHT_CORNER 11
#define MG_RIGHT_EYE_TOP 12
#define MG_RIGHT_EYE_BOTTOM 13
#define MG_RIGHT_EYE_UPPER_LEFT_QUARTER 14
#define MG_RIGHT_EYE_LOWER_LEFT_QUARTER 15
#define MG_RIGHT_EYE_UPPER_RIGHT_QUARTER 16
#define MG_RIGHT_EYE_LOWER_RIGHT_QUARTER 17
#define MG_LEFT_EYEBROW_LEFT_CORNER 18
#define MG_LEFT_EYEBROW_RIGHT_CORNER 19
#define MG_LEFT_EYEBROW_UPPER_MIDDLE 20
#define MG_LEFT_EYEBROW_LOWER_MIDDLE 21
#define MG_LEFT_EYEBROW_UPPER_LEFT_QUARTER 22
#define MG_LEFT_EYEBROW_LOWER_LEFT_QUARTER 23
#define MG_LEFT_EYEBROW_UPPER_RIGHT_QUARTER 24
#define MG_LEFT_EYEBROW_LOWER_RIGHT_QUARTER 25
#define MG_RIGHT_EYEBROW_LEFT_CORNER 26
#define MG_RIGHT_EYEBROW_RIGHT_CORNER 27
#define MG_RIGHT_EYEBROW_UPPER_MIDDLE 28
#define MG_RIGHT_EYEBROW_LOWER_MIDDLE 29
#define MG_RIGHT_EYEBROW_UPPER_LEFT_QUARTER 30
#define MG_RIGHT_EYEBROW_LOWER_LEFT_QUARTER 31
#define MG_RIGHT_EYEBROW_UPPER_RIGHT_QUARTER 32
#define MG_RIGHT_EYEBROW_LOWER_RIGHT_QUARTER 33
#define MG_NOSE_TIP 34
#define MG_NOSE_CONTOUR_LOWER_MIDDLE 35
#define MG_NOSE_CONTOUR_LEFT1 36
#define MG_NOSE_CONTOUR_RIGHT1 37
#define MG_NOSE_CONTOUR_LEFT2 38
#define MG_NOSE_CONTOUR_RIGHT2 39
#define MG_NOSE_LEFT 40
#define MG_NOSE_RIGHT 41
#define MG_NOSE_CONTOUR_LEFT3 42
#define MG_NOSE_CONTOUR_RIGHT3 43
#define MG_MOUTH_LEFT_CORNER 44
#define MG_MOUTH_RIGHT_CORNER 45
#define MG_MOUTH_UPPER_LIP_TOP 46
#define MG_MOUTH_UPPER_LIP_BOTTOM 47
#define MG_MOUTH_UPPER_LIP_LEFT_CONTOUR1 48
#define MG_MOUTH_UPPER_LIP_RIGHT_CONTOUR1 49
#define MG_MOUTH_UPPER_LIP_LEFT_CONTOUR2 50
#define MG_MOUTH_UPPER_LIP_RIGHT_CONTOUR2 51
#define MG_MOUTH_UPPER_LIP_LEFT_CONTOUR3 52
#define MG_MOUTH_UPPER_LIP_RIGHT_CONTOUR3 53
#define MG_MOUTH_LOWER_LIP_TOP 54
#define MG_MOUTH_LOWER_LIP_BOTTOM 55
#define MG_MOUTH_LOWER_LIP_LEFT_CONTOUR1 56
#define MG_MOUTH_LOWER_LIP_RIGHT_CONTOUR1 57
#define MG_MOUTH_LOWER_LIP_LEFT_CONTOUR2 58
#define MG_MOUTH_LOWER_LIP_LEFT_CONTOUR3 59
#define MG_MOUTH_LOWER_LIP_RIGHT_CONTOUR3 60
#define MG_MOUTH_LOWER_LIP_RIGHT_CONTOUR2 61
#define MG_CONTOUR_LEFT1 62
#define MG_CONTOUR_RIGHT1 63
#define MG_CONTOUR_CHIN 64
#define MG_CONTOUR_LEFT2 65
#define MG_CONTOUR_LEFT3 66
#define MG_CONTOUR_LEFT4 67
#define MG_CONTOUR_LEFT5 68
#define MG_CONTOUR_LEFT6 69
#define MG_CONTOUR_LEFT7 70
#define MG_CONTOUR_LEFT8 71
#define MG_CONTOUR_LEFT9 72
#define MG_CONTOUR_RIGHT2 73
#define MG_CONTOUR_RIGHT3 74
#define MG_CONTOUR_RIGHT4 75
#define MG_CONTOUR_RIGHT5 76
#define MG_CONTOUR_RIGHT6 77
#define MG_CONTOUR_RIGHT7 78
#define MG_CONTOUR_RIGHT8 79
#define MG_CONTOUR_RIGHT9 80

#define MG_LANDMARK_NR 81                       ///< 默认的人脸关键点总数
/**
 * @}
 */

/**
 * @brief 人脸关键点类型
 *
 * 默认关键点个数为 81 点，此类型记录了所有关键点。
 */
typedef struct {
    MG_POINT point[MG_LANDMARK_NR];             ///< 记录关键点的数组
} MG_FACELANDMARKS;

/**
 * @brief 人脸 3D 角度类型
 *
 * 记录三维属性
 */
typedef struct {
    MG_SINGLE pitch;                            ///< 一个弧度，表示物体顺时针饶x轴旋转的弧度。

    MG_SINGLE yaw;                              ///< 一个弧度，表示物体顺时针饶y轴旋转的弧度。

    MG_SINGLE roll;                             ///< 一个弧度，表示物体顺时针饶z轴旋转的弧度。
} MG_3DPOSE;

/**
 * @brief 男女属性
 *
 * 记录人脸男女属性的类型，男女概率之和为 1。
 */
typedef struct {
    MG_SINGLE female;                           ///< 是女性人脸的概率

    MG_SINGLE male;                             ///< 是男性人脸的概率
} MG_GENDER;

/**
 * @brief 人眼睛状态的类型
 *
 * @warning 目前此类型尚未定型
 */
typedef enum {
    MG_EYESTATUS_NOGLASSES_EYEOPEN = 0,         ///< 不带眼镜，并且睁着眼

    MG_EYESTATUS_NOGLASSES_EYECLOSE = 1,        ///< 不戴眼镜，并且闭着眼

    MG_EYESTATUS_NORMALGLASSES_EYEOPEN = 2,     ///< 带着普通眼镜，并且睁着眼

    MG_EYESTATUS_NORMALGLASSES_EYECLOSE = 3,    ///< 带着普通眼镜，并且闭着眼

    MG_EYESTATUS_DARKGLASSES = 4,               ///< 带着墨镜

    MG_EYESTATUS_OTHER_OCCLUSION = 5,           ///< 眼镜被遮挡

    MG_EYESTATUS_COUNT                          ///< 眼睛状态总数
} MG_EYESTATUS;

/**
 * @brief 人嘴状态的类型
 *
 * @warning 魔泉此状态尚未定型
 */
typedef enum {
    MG_MOUTHSTATUS_OPEN = 0,                    ///< 处于张嘴状态

    MG_MOUTHSTATUS_CLOSE = 1,                   ///< 处于闭嘴状态

    MG_MOUTHSTATUS_MASK_OR_RESPIRATOR = 2,      ///< 带着面具或者带着口罩

    MG_MOUTHSTATUS_OTHER_OCCLUSION = 3,         ///< 被其他东西遮挡着嘴巴

    MG_MOUTHSTATUS_COUNT                        ///< 嘴巴状态总数
} MG_MOUTHSTATUS;

/**
 * @brief 记录人脸信息的类型
 *
 * 记录了人脸所有属性信息，关键点信息的类型。
 */
typedef struct {
    MG_INT32 track_id;                              ///< 人脸的跟踪标记。
    ///< 如果只对单张图做人脸检测则固定返回 -1，
    ///< 否则在不同帧中相同的 track_id 表示同一个人脸。
    ///< 每次初始化后 track_id 的值为从 0 开始依此递增。

    MG_RECTANGLE rect;                              ///< 人脸在图像中的位置，以一个矩形框来刻画。

    MG_FACELANDMARKS points;                        ///< 人脸关键点信息。

    MG_SINGLE confidence;                           ///< 人脸置信度，为一个 0 ~ 1 之间的浮点数。
    ///< 超过 0.5 表示这确实是一个人脸。

    MG_3DPOSE pose;                                 ///< 人脸三维旋转角度。

    MG_SINGLE left_eyestatus[MG_EYESTATUS_COUNT];   ///< 人左眼状态，每个数值表示概率，总和为 1

    MG_SINGLE right_eyestatus[MG_EYESTATUS_COUNT];  ///< 人右眼状态，每个数值表示概率，总和为 1

    MG_SINGLE age;                                  ///< 年龄，为浮点数

    MG_GENDER gender;                               ///< 性别

    MG_SINGLE blurness;                             ///< 模糊程度，数值越小表示越清晰，0 ~ 1

    MG_SINGLE minority;                             ///< 是否是少数民族（对于汉族而言）

    MG_SINGLE moutstatus[MG_MOUTHSTATUS_COUNT];     ///< 嘴部状态
} MG_FACE;

/**
 * @brief 算法相关的信息
 *
 * 记录了算法相关信息的类型
 */
typedef struct {
    MG_UINT64 expire_time;                          ///< 一个时间戳，表示过期时间

    MG_SDKAUTHTYPE auth_type;                       ///< SDK 的授权类型（联网授权或者非联网授权）

    MG_UINT64 ability;                              ///< 提供人脸算法的能力
    ///< 这是一些属性值的 bit 值的或和，
    ///< 可以参考以 MG_FPP_ATTR_ 开头的宏定义名。

} MG_ALGORITHMINFO;

typedef enum {
    MG_ROTATION_0 = 0,                              ///< 不旋转

    MG_ROTATION_90 = 90,                            ///< 图像右时针旋转 90 度

    MG_ROTATION_180 = 180,                          ///< 图像右时针旋转 180 度

    MG_ROTATION_270 = 270,                          ///< 图像右时针旋转 270 度
} MG_ROTATION;

#ifdef __cplusplus
}
#endif

#endif // _MG_COMMON_H_
