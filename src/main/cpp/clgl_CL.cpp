
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

    if(!contextOnErrorClassGlobalRef) {
        contextOnErrorClassGlobalRef = (jclass)(env->NewGlobalRef(callback));
        contextOnErrorMethodID = env->GetStaticMethodID(callback, "onErrorStatic", "(Ljava/lang/String;Ljava/nio/ByteBuffer;J)V");
    }

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

/*
 * Class:     de_linusdev_clgl_nat_cl_CL
 * Method:    _clCreateCommandQueueWithProperties
 * Signature: (JJLjava/nio/ByteBuffer;Ljava/nio/ByteBuffer;)J
 */
JNIEXPORT jlong JNICALL Java_de_linusdev_clgl_nat_cl_CL__1clCreateCommandQueueWithProperties
        (JNIEnv* env, jclass clazz, jlong context, jlong device, jobject p_properties, jobject p_errcode_ret) {
    auto* properties = p_properties == nullptr ? nullptr : env->GetDirectBufferAddress(p_properties);
    auto* errcode_ret = p_errcode_ret == nullptr ? nullptr : env->GetDirectBufferAddress(p_errcode_ret);

    cl_command_queue queue = clCreateCommandQueueWithProperties(
            reinterpret_cast<cl_context>(context),
            reinterpret_cast<cl_device_id>(device),
            reinterpret_cast<cl_queue_properties*>(properties),
            reinterpret_cast<cl_int*>(errcode_ret)
    );

    return reinterpret_cast<jlong>(queue);
}

/*
 * Class:     de_linusdev_clgl_nat_cl_CL
 * Method:    _clCreateCommandQueue
 * Signature: (JJJLjava/nio/ByteBuffer;)J
 */
JNIEXPORT jlong JNICALL Java_de_linusdev_clgl_nat_cl_CL__1clCreateCommandQueue
        (JNIEnv* env, jclass clazz, jlong context, jlong device, jlong properties, jobject p_errcode_ret) {
    auto* errcode_ret = p_errcode_ret == nullptr ? nullptr : env->GetDirectBufferAddress(p_errcode_ret);

    cl_command_queue queue = clCreateCommandQueue(
            reinterpret_cast<cl_context>(context),
            reinterpret_cast<cl_device_id>(device),
            properties,
            reinterpret_cast<cl_int*>(errcode_ret)
            );

    return reinterpret_cast<jlong>(queue);
}

/*
 * Class:     de_linusdev_clgl_nat_cl_CL
 * Method:    _clReleaseCommandQueue
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_de_linusdev_clgl_nat_cl_CL__1clReleaseCommandQueue
        (JNIEnv* env, jclass clazz, jlong queue) {
    return clReleaseCommandQueue(reinterpret_cast<cl_command_queue>(queue));
}

/*
 * Class:     de_linusdev_clgl_nat_cl_CL
 * Method:    _clCreateBuffer
 * Signature: (JJJLjava/nio/ByteBuffer;Ljava/nio/ByteBuffer;)J
 */
JNIEXPORT jlong JNICALL Java_de_linusdev_clgl_nat_cl_CL__1clCreateBuffer
        (JNIEnv* env, jclass clazz, jlong context, jlong cl_mem_flags, jlong size, jobject p_host_ptr, jobject p_errcode_ret) {

    void* host_ptr =  p_host_ptr == nullptr ? nullptr : env->GetDirectBufferAddress(p_host_ptr);
    void* errcode_ret = GET_BUF_ADDRESS_NULLABLE(p_errcode_ret);

    auto buffer = clCreateBuffer(
            reinterpret_cast<cl_context>(context),
            cl_mem_flags,
            size,
            host_ptr,
            reinterpret_cast<cl_int*>(errcode_ret)
            );

    return reinterpret_cast<jlong>(buffer);
}

/*
 * Class:     de_linusdev_clgl_nat_cl_CL
 * Method:    _clReleaseMemObject
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_de_linusdev_clgl_nat_cl_CL__1clReleaseMemObject
        (JNIEnv *, jclass, jlong memobj) {
    return clReleaseMemObject(reinterpret_cast<cl_mem>(memobj));
}

/*
 * Class:     de_linusdev_clgl_nat_cl_CL
 * Method:    _clEnqueueReadBuffer
 * Signature: (JJZJJLjava/nio/ByteBuffer;ILjava/nio/ByteBuffer;Ljava/nio/ByteBuffer;)I
 */
JNIEXPORT jint JNICALL Java_de_linusdev_clgl_nat_cl_CL__1clEnqueueReadBuffer(
        JNIEnv* env, jclass clazz,
        jlong command_queue,
        jlong buffer,
        jboolean blocking_read,
        jlong offset,
        jlong size,
        jobject p_ptr,
        jint num_events_in_wait_list,
        jobject p_event_wait_list,
        jobject p_event
) {

    void* ptr = env->GetDirectBufferAddress(p_ptr);
    void* event_wait_list = GET_BUF_ADDRESS_NULLABLE(p_event_wait_list);
    void* event = GET_BUF_ADDRESS_NULLABLE(p_event);

    return clEnqueueReadBuffer(
            reinterpret_cast<cl_command_queue>(command_queue),
            reinterpret_cast<cl_mem>(buffer),
            blocking_read == JNI_TRUE ? CL_TRUE : CL_FALSE,
            offset,
            size,
            ptr,
            num_events_in_wait_list,
            reinterpret_cast<cl_event*>(event_wait_list),
            reinterpret_cast<cl_event*>(event)
    );
}

/*
 * Class:     de_linusdev_clgl_nat_cl_CL
 * Method:    _clEnqueueWriteBuffer
 * Signature: (JJZJJLjava/nio/ByteBuffer;ILjava/nio/ByteBuffer;Ljava/nio/ByteBuffer;)I
 */
JNIEXPORT jint JNICALL Java_de_linusdev_clgl_nat_cl_CL__1clEnqueueWriteBuffer(JNIEnv* env, jclass clazz,
         jlong command_queue,
         jlong buffer,
         jboolean blocking_write,
         jlong offset,
         jlong size,
         jobject p_ptr,
         jint num_events_in_wait_list,
         jobject p_event_wait_list,
         jobject p_event
) {

    void* ptr = env->GetDirectBufferAddress(p_ptr);
    void* event_wait_list = GET_BUF_ADDRESS_NULLABLE(p_event_wait_list);
    void* event = GET_BUF_ADDRESS_NULLABLE(p_event);

    return clEnqueueWriteBuffer(
            reinterpret_cast<cl_command_queue>(command_queue),
            reinterpret_cast<cl_mem>(buffer),
            blocking_write == JNI_TRUE ? CL_TRUE : CL_FALSE,
            offset,
            size,
            ptr,
            num_events_in_wait_list,
            reinterpret_cast<cl_event*>(event_wait_list),
            reinterpret_cast<cl_event*>(event)
    );
}

/*
 * Class:     de_linusdev_clgl_nat_cl_CL
 * Method:    _clCreateProgramWithSource
 * Signature: (JLjava/lang/String;Ljava/nio/ByteBuffer;)J
 */
JNIEXPORT jlong JNICALL Java_de_linusdev_clgl_nat_cl_CL__1clCreateProgramWithSource
        (JNIEnv* env, jclass clazz, jlong context, jstring src, jobject p_errcode_ret) {

    const char* cSrc = env->GetStringUTFChars(src, nullptr);
    void* errcode_ret = GET_BUF_ADDRESS_NULLABLE(p_errcode_ret);

    cl_program program = clCreateProgramWithSource(
            reinterpret_cast<cl_context>(context),
            1,
            &cSrc,
            nullptr,
            reinterpret_cast<cl_int*>(errcode_ret)
    );

    env->ReleaseStringUTFChars(src, cSrc);

    return reinterpret_cast<jlong>(program);
}

/*
 * Class:     de_linusdev_clgl_nat_cl_CL
 * Method:    _clReleaseProgram
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_de_linusdev_clgl_nat_cl_CL__1clReleaseProgram
        (JNIEnv* env, jclass clazz, jlong program) {
    return clReleaseProgram(reinterpret_cast<cl_program>(program));
}

static jclass programBuildFinishedClassGlobalRef = nullptr;
static jmethodID programBuildFinishedMethodID = nullptr;

/*
 * Class:     de_linusdev_clgl_nat_cl_CL
 * Method:    _clBuildProgram
 * Signature: (JILjava/nio/ByteBuffer;Ljava/lang/String;Ljava/lang/Class;J)I
 */
JNIEXPORT jint JNICALL Java_de_linusdev_clgl_nat_cl_CL__1clBuildProgram
        (JNIEnv* env, jclass clazz,
         jlong program,
         jint num_devices,
         jobject p_device_list,
         jstring options,
         jclass callback,
         jlong user_data
) {

    void* device_list = GET_BUF_ADDRESS_NULLABLE(p_device_list);
    auto* cOptions = options == nullptr ? nullptr : env->GetStringUTFChars(options, nullptr);

    if(callback != nullptr && programBuildFinishedClassGlobalRef == nullptr) {
        programBuildFinishedClassGlobalRef = (jclass) (env->NewGlobalRef(callback));
        programBuildFinishedMethodID = env->GetStaticMethodID(callback, "onProgramBuildFinishedStatic", "(JJ)V");
    }

    cl_int ret = clBuildProgram(
            reinterpret_cast<cl_program>(program),
            num_devices,
            reinterpret_cast<cl_device_id*>(device_list),
            cOptions,
            [](cl_program program, void* user_data) {
                JNIEnv* env;
                JNI_UTILS->getEnv(&env);

                env->CallStaticVoidMethod(
                        programBuildFinishedClassGlobalRef,programBuildFinishedMethodID,
                        reinterpret_cast<jlong>(program), reinterpret_cast<jlong>(user_data));
            },
            reinterpret_cast<void*>(user_data)
    );

    if(options != nullptr)
        env->ReleaseStringUTFChars(options, cOptions);

    return ret;
}

/*
 * Class:     de_linusdev_clgl_nat_cl_CL
 * Method:    _clGetProgramBuildInfo
 * Signature: (JJIJLjava/nio/ByteBuffer;Ljava/nio/ByteBuffer;)I
 */
JNIEXPORT jint JNICALL Java_de_linusdev_clgl_nat_cl_CL__1clGetProgramBuildInfo
        (JNIEnv* env, jclass clazz,
         jlong program,
         jlong device,
         jint param_name,
         jlong param_value_size,
         jobject p_param_value,
         jobject p_param_value_size_ret
) {

    void* param_value = GET_BUF_ADDRESS_NULLABLE(p_param_value);
    void* param_value_size_ret = GET_BUF_ADDRESS_NULLABLE(p_param_value_size_ret);

    return clGetProgramBuildInfo(
            reinterpret_cast<cl_program>(program),
            reinterpret_cast<cl_device_id>(device),
            param_name,
            param_value_size,
            param_value,
            reinterpret_cast<size_t*>(param_value_size_ret)
    );

}


