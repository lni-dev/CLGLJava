
#include "clgl_CL.h"

#include "CL/cl.h"

#include "JniUtils.h"

/*
 * Class:     de_linusdev_clgl_nat_cl_CL
 * Method:    _clGetPlatformIDs
 * Signature: (ILjava/nio/ByteBuffer;Ljava/nio/ByteBuffer;)I
 */
JNIEXPORT jint JNICALL Java_de_linusdev_clgl_nat_cl_CL__1clGetPlatformIDs
  (JNIEnv* env, jclass clazz, jint num_entries, jobject p_platforms, jobject p_num_platforms) {

    auto* platforms = reinterpret_cast<cl_platform_id *>(
            p_platforms == nullptr ? nullptr : env->GetDirectBufferAddress(p_platforms));

    auto* num_platforms = reinterpret_cast<cl_uint *>(
            p_num_platforms == nullptr ? nullptr : env->GetDirectBufferAddress(p_num_platforms));

    return clGetPlatformIDs(num_entries, platforms, num_platforms);
}

/*
 * Class:     de_linusdev_clgl_nat_cl_CL
 * Method:    _clGetPlatformInfo
 * Signature: (IIILjava/nio/ByteBuffer;Ljava/nio/ByteBuffer;)I
 */
JNIEXPORT jint JNICALL Java_de_linusdev_clgl_nat_cl_CL__1clGetPlatformInfo(
        JNIEnv* env, jclass clazz, jlong platform, jint param_name,
        jint param_value_size, jobject p_param_value, jobject p_param_value_size_ret
   ) {

    auto* param_value = p_param_value == nullptr ? nullptr : env->GetDirectBufferAddress(p_param_value);
    auto* param_value_size_ret = p_param_value_size_ret == nullptr ? nullptr : env->GetDirectBufferAddress(p_param_value_size_ret);

    return clGetPlatformInfo(
            reinterpret_cast<cl_platform_id>(platform),
            param_name,
            param_value_size,
            param_value,
            reinterpret_cast<size_t*>(param_value_size_ret));
}

/*
 * Class:     de_linusdev_clgl_nat_cl_CL
 * Method:    _clGetDeviceIDs
 * Signature: (IIILjava/nio/ByteBuffer;Ljava/nio/ByteBuffer;)I
 */
JNIEXPORT jint JNICALL Java_de_linusdev_clgl_nat_cl_CL__1clGetDeviceIDs(JNIEnv* env, jclass clazz,
   jlong platform,
   jint device_type,
   jint num_entries,
   jobject p_devices,
   jobject p_num_devices
) {
    auto* devices = p_devices == nullptr ? nullptr : env->GetDirectBufferAddress(p_devices);
    auto* num_devices = p_num_devices == nullptr ? nullptr : env->GetDirectBufferAddress(p_num_devices);
    
    return clGetDeviceIDs(
            reinterpret_cast<cl_platform_id>(platform),
            device_type,
            num_entries,
            reinterpret_cast<cl_device_id *>(devices),
            reinterpret_cast<cl_uint *>(num_devices)
            );
}

static jclass contextOnErrorClassGlobalRef = nullptr;
static jmethodID contextOnErrorMethodID = nullptr;

/*
 * Class:     de_linusdev_clgl_nat_cl_CL
 * Method:    _clCreateContext
 * Signature: (Ljava/nio/ByteBuffer;ILjava/nio/ByteBuffer;Ljava/lang/Class;JLjava/nio/ByteBuffer;)J
 */
JNIEXPORT jlong JNICALL Java_de_linusdev_clgl_nat_cl_CL__1clCreateContext(JNIEnv* env, jclass clazz,
        jobject p_properties,
        jint num_devices,
        jobject p_devices,
        jclass callback,
        jlong user_data,
        jobject p_errcode_ret
) {

    if(contextOnErrorClassGlobalRef)
        env->DeleteGlobalRef(contextOnErrorClassGlobalRef);

    contextOnErrorClassGlobalRef = (jclass)(env->NewGlobalRef(callback));
    contextOnErrorMethodID = env->GetStaticMethodID(callback, "onErrorStatic", "(Ljava/lang/String;Ljava/nio/ByteBuffer;J)V");

    void* properties = env->GetDirectBufferAddress(p_properties);
    void* devices = env->GetDirectBufferAddress(p_devices);
    void* errcode_ret = env->GetDirectBufferAddress(p_errcode_ret);

    cl_context context = clCreateContext(
            reinterpret_cast<cl_context_properties*>(properties),
            num_devices,
            reinterpret_cast<cl_device_id*>(devices),
            [](const char* errinfo, const void* private_data, size_t cb, void* user_data) {
                JNIEnv* env;
                JNI_UTILS->getEnv(&env);

                jstring jerrinfo = env->NewStringUTF(errinfo);
                jobject jprivate_data = env->NewDirectByteBuffer(const_cast<void*>(private_data), cb);

                env->CallStaticVoidMethod(contextOnErrorClassGlobalRef, contextOnErrorMethodID, jerrinfo, jprivate_data, reinterpret_cast<jlong>(user_data));
                env->DeleteLocalRef(jprivate_data);
                env->DeleteLocalRef(jerrinfo);
                },
            reinterpret_cast<void*>(user_data),
            reinterpret_cast<cl_int*>(errcode_ret)
            );

    return reinterpret_cast<jlong>(context);
}

/*
 * Class:     de_linusdev_clgl_nat_cl_CL
 * Method:    _clReleaseContext
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_de_linusdev_clgl_nat_cl_CL__1clReleaseContext
        (JNIEnv* env, jclass clazz, jlong context) {
    return clReleaseContext(reinterpret_cast<cl_context>(context));
}

