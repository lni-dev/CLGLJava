
#include "clgl_CL.h"

#include "CL/cl.h"

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

