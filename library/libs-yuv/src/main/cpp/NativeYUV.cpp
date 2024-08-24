#include <jni.h>
#include <string>
#include "libyuv.h"

/** implement YUV2_TO_ARGB() */
extern "C"
JNIEXPORT jboolean JNICALL
Java_com_youaji_libs_yuv_NativeLibYUV_uyvy2argb(
        JNIEnv *env,
        jobject  /* this */,
        jbyteArray uyvy_buffer,
        jbyteArray argb32_buffer,
        jint width,
        jint height
) {

    if (uyvy_buffer == NULL ||
        argb32_buffer == NULL ||
        (width * height) <= 0) {
        return JNI_FALSE;
    }

    // 缓存空间错误
    if (env->GetArrayLength(uyvy_buffer) < (width * height * 2) ||
        env->GetArrayLength(argb32_buffer) < (width * height * 4)) {
        return JNI_FALSE;
    }

    jbyte *yuv_buffer = env->GetByteArrayElements(uyvy_buffer, JNI_FALSE);
    jbyte *argb_buffer = env->GetByteArrayElements(argb32_buffer, JNI_FALSE);

    uint8_t *agbr_buffer = (uint8_t *) malloc(sizeof(uint8_t) * env->GetArrayLength(argb32_buffer));
    int code = libyuv::UYVYToARGB(
            (uint8_t *) yuv_buffer,
            width * 2,
            agbr_buffer,
            width * 4,
            width,
            height
    );
    libyuv::ARGBToABGR(
            agbr_buffer,
            width * 4,
            (uint8_t *) argb_buffer,
            width * 4, width, height
    );
    env->ReleaseByteArrayElements(uyvy_buffer, yuv_buffer, 0);
    env->ReleaseByteArrayElements(argb32_buffer, argb_buffer, 0);
    free(agbr_buffer);
    return code == 0;
}

/** implement YUV2_TO_ARGB() */
extern "C"
JNIEXPORT jboolean JNICALL
Java_com_youaji_libs_yuv_NativeLibYUV_yuv22argb(
        JNIEnv *env,
        jobject  /* this */,
        jbyteArray yuy2_buffer,
        jbyteArray argb32_buffer,
        jint width,
        jint height
) {

    if (yuy2_buffer == NULL ||
        argb32_buffer == NULL ||
        (width * height) <= 0) {
        return JNI_FALSE;
    }

    // 缓存空间错误
    if (env->GetArrayLength(yuy2_buffer) < (width * height * 2) ||
        env->GetArrayLength(argb32_buffer) < (width * height * 4)) {
        return JNI_FALSE;
    }

    jbyte *yuv_buffer = env->GetByteArrayElements(yuy2_buffer, JNI_FALSE);
    jbyte *argb_buffer = env->GetByteArrayElements(argb32_buffer, JNI_FALSE);

    uint8_t *agbr_buffer = (uint8_t *) malloc(sizeof(uint8_t) * env->GetArrayLength(argb32_buffer));
    int code = libyuv::YUY2ToARGB(
                    (uint8_t *) yuv_buffer,
                    width * 2,
                    agbr_buffer,
                    width * 4,
                    width,
                    height
            );
    libyuv::ARGBToABGR(
            agbr_buffer,
            width * 4,
            (uint8_t *) argb_buffer,
            width * 4, width, height
    );
    env->ReleaseByteArrayElements(yuy2_buffer, yuv_buffer, 0);
    env->ReleaseByteArrayElements(argb32_buffer, argb_buffer, 0);
    free(agbr_buffer);
    return code == 0;
}

/** implement ARGB_TO_NV21() */
extern "C"
JNIEXPORT jboolean JNICALL
Java_com_youaji_libs_yuv_NativeLibYUV_argb2nv21(
        JNIEnv *env,
        jobject  /* this */,
        jbyteArray src_argb,
        jbyteArray dst_yuv,
        jint w, jint h
) {

    if (dst_yuv == NULL ||
        src_argb == NULL ||
        (w * h) <= 0) {
        return JNI_FALSE;
    }

    jbyte *src_argb_data = env->GetByteArrayElements(src_argb, JNI_FALSE);
    uint8_t *dst_yuv_data = (uint8_t *) env->GetByteArrayElements(dst_yuv, JNI_FALSE);

    libyuv::ARGBToNV21(
            (const uint8_t *) src_argb_data,
            w * 4,
            &dst_yuv_data[0], w,
            &dst_yuv_data[w * h],
            ((w + 1) / 2) * 2,
            w,
            h
    );

    env->ReleaseByteArrayElements(src_argb, src_argb_data, 0);
    env->ReleaseByteArrayElements(dst_yuv, (jbyte *) dst_yuv_data, 0);
    return JNI_TRUE;
}

/** implement ARGB_TO_I420() */
extern "C"
JNIEXPORT jboolean JNICALL
Java_com_youaji_libs_yuv_NativeLibYUV_argb2i420(
        JNIEnv *env,
        jobject  /* this */,
        jbyteArray src_argb,
        jbyteArray dst_yuv,
        jint w, jint h
) {

    if (dst_yuv == NULL ||
        src_argb == NULL ||
        (w * h) <= 0) {
        return JNI_FALSE;
    }

    jbyte *src_argb_data = env->GetByteArrayElements(src_argb, JNI_FALSE);
    uint8_t *dst_yuv_data = (uint8_t *) env->GetByteArrayElements(dst_yuv, JNI_FALSE);

    libyuv::ARGBToI420(
            (const uint8_t *) src_argb_data,
            w * 4,
            &dst_yuv_data[0],
            w,
            &dst_yuv_data[w * h + (w * h) / 4],
            (w + 1) / 2,
            &dst_yuv_data[w * h],
            (w + 1) / 2,
            w,
            h
    );

    env->ReleaseByteArrayElements(src_argb, src_argb_data, 0);
    env->ReleaseByteArrayElements(dst_yuv, (jbyte *) dst_yuv_data, 0);
    return JNI_TRUE;
}