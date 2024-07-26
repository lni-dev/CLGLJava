/*
 * Copyright (c) 2023 Linus Andera
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.linusdev.cvg4j.nat.cl;

import de.linusdev.cvg4j.nat.cl.objects.Context;
import de.linusdev.cvg4j.nat.cl.objects.Event;
import de.linusdev.cvg4j.nat.cl.objects.MemoryObject;
import de.linusdev.cvg4j.nat.cl.objects.Program;
import de.linusdev.cvg4j.nat.cl.structs.CLImageDesc;
import de.linusdev.cvg4j.nat.cl.structs.CLImageFormat;
import de.linusdev.lutils.bitfield.IntBitFieldValue;
import de.linusdev.lutils.bitfield.LongBitFieldValue;
import de.linusdev.lutils.bitfield.LongBitfield;
import de.linusdev.lutils.math.vector.buffer.intn.BBInt1;
import de.linusdev.lutils.math.vector.buffer.longn.BBLong1;
import de.linusdev.lutils.math.vector.buffer.longn.BBLongN;
import de.linusdev.lutils.nat.array.NativeInt64Array;
import de.linusdev.lutils.nat.struct.abstracts.Structure;
import de.linusdev.lutils.nat.array.NativeArray;
import de.linusdev.lutils.nat.struct.annos.SVWrapper;
import de.linusdev.lutils.nat.struct.utils.BufferUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static de.linusdev.cvg4j.nat.NativeUtils.SIZE_OF_CL_MEM;
import static de.linusdev.cvg4j.nat.cl.CLStatus.check;

@SuppressWarnings("unused")
public class CL {

    public static Iterable<Long> getPlatformIDs() {
        BBInt1 size = BBInt1.newAllocated(null);
        check(clGetPlatformIDs(null, size));

        NativeInt64Array ids = NativeInt64Array.newAllocated(SVWrapper.length(size.get()));
        check(clGetPlatformIDs(ids, null));

        return ids;
    }

    public static int clGetPlatformIDs(
            @Nullable NativeArray<Long> array,
            @Nullable BBInt1 platformCount
    ) {
        return _clGetPlatformIDs(
                array == null ? 0 : array.length(),
                array == null ? null : array.getByteBuffer(),
                platformCount == null ? null : platformCount.getByteBuffer()
        );
    }

    private static native int _clGetPlatformIDs(
            int num_entries,
            @Nullable ByteBuffer p_platforms,
            @Nullable ByteBuffer p_num_platforms
    );


    public enum PlatformInfo implements IntBitFieldValue {
        /**
         * OpenCL profile string. Returns the profile name supported by the implementation. The profile name returned can be one of the following strings:<br>
         * <br>FULL_PROFILE - if the implementation supports the OpenCL specification
         * (functionality defined as part of the core specification and does not require any extensions to be supported).<br>
         * <br>EMBEDDED_PROFILE - if the implementation supports the OpenCL embedded profile.
         * The embedded profile is defined to be a subset for each version of OpenCL.
         *
         * <br><br>
         * Return type: {@link String}. see {@link #getPlatformInfoString(long, PlatformInfo)}
         */
        CL_PLATFORM_PROFILE(0x0900),

        /**
         * OpenCL version string. Returns the OpenCL version supported by the implementation.
         * This version string has the following format:<br>
         * OpenCL{@code <space><major_version.minor_version><space><platform-specific information>}<br>
         * The major_version.minor_version value returned will be 1.0.
         *
         * <br><br>
         * Return type: {@link String}. see {@link #getPlatformInfoString(long, PlatformInfo)}
         */
        CL_PLATFORM_VERSION(0x0901),

        /**
         * Platform name string.
         *
         * <br><br>
         * Return type: {@link String}. see {@link #getPlatformInfoString(long, PlatformInfo)}
         */
        CL_PLATFORM_NAME(0x0902),

        /**
         * Platform vendor string.
         *
         * <br><br>
         *          * Return type: {@link String}. see {@link #getPlatformInfoString(long, PlatformInfo)}
         */
        CL_PLATFORM_VENDOR(0x0903),

        /**
         * Returns a space-separated list of extension names (the extension names themselves do not contain any spaces)
         * supported by the platform. Extensions defined here must be supported by all devices associated with this platform.
         *
         * <br><br>
         * Return type: {@link String}. see {@link #getPlatformInfoString(long, PlatformInfo)}
         */
        CL_PLATFORM_EXTENSIONS(0x0904),

        /**
         * Returns the resolution of the host timer in nanoseconds as used by clGetDeviceAndHostTimer.<br>
         * Support for device and host timer synchronization is required for platforms supporting OpenCL 2.1 or 2.2. This value must be 0 for devices that do not support device and host timer synchronization.
         * <br><br>
         * Return type: long. see {@link #getPlatformInfoLong(long, PlatformInfo)}
         * @since OpenCL 2.1
         */
        CL_PLATFORM_HOST_TIMER_RESOLUTION(0x0905),

        /**
         * Returns the detailed (major, minor, patch) version supported by the platform.<br>
         * The major and minor version numbers returned must match those returned via CL_PLATFORM_VERSION.
         * <br><br>
         * Return type: cl_version. see {@link #getPlatformInfoString(long, PlatformInfo)}<br>
         * TODO: https://registry.khronos.org/OpenCL/specs/3.0-unified/html/OpenCL_API.html#_querying_platform_info
         *
         * @since OpenCL 3.0
         */
        CL_PLATFORM_NUMERIC_VERSION(0x0906),

        /**
         * Returns an array of description (name and version) structures that lists all the extensions supported by
         * the platform. The same extension name must not be reported more than once. The list of extensions reported
         * must match the list reported via CL_PLATFORM_EXTENSIONS.
         * <br><br>
         * Return type: cl_name_version[]. see {@link #getPlatformInfoString(long, PlatformInfo)}<br>
         * TODO: https://registry.khronos.org/OpenCL/specs/3.0-unified/html/OpenCL_API.html#cl_name_version
         *
         * @since OpenCL 3.0
         */
        CL_PLATFORM_EXTENSIONS_WITH_VERSION(0x0907),
        ;
        private final int value;

        PlatformInfo(int value) {
            this.value = value;
        }

        @Override
        public int getValue() {
            return value;
        }
    }

    public static long getPlatformInfoLong(long platform, @NotNull PlatformInfo paramName) {
        ByteBuffer longBuffer = BufferUtils.createAligned(Long.BYTES, 8);
        check(clGetPlatformInfo(platform, paramName, longBuffer, null));
        return longBuffer.getLong();
    }

    public static String getPlatformInfoString(long platform, @NotNull PlatformInfo paramName) {
        BBLong1 size = BBLong1.newAllocated(null);
        check(clGetPlatformInfo(platform, paramName, null, size));

        ByteBuffer info = BufferUtils.createAligned((int) size.get(), 8);
        check(clGetPlatformInfo(platform, paramName, info, null));

        info.limit(info.capacity() - 1); //remove '\0' from c-string
        return StandardCharsets.UTF_8.decode(info).toString();
    }

    public static int clGetPlatformInfo(
            long platform,
            @NotNull PlatformInfo paramName,
            @Nullable ByteBuffer paramValue,
            @Nullable BBLong1 paramValueSizeRet
    ) {
        return _clGetPlatformInfo(
                platform,
                paramName.getValue(),
                paramValue == null ? 0 : paramValue.capacity(),
                paramValue,
                paramValueSizeRet == null ? null : paramValueSizeRet.getByteBuffer()
        );
    }

    private static native int _clGetPlatformInfo(
            long platform,
            int param_name,
            int param_value_size,
            @Nullable ByteBuffer p_param_value,
            @Nullable ByteBuffer p_param_value_size_ret
    );


    @SuppressWarnings("PointlessBitwiseExpression")
    public enum DeviceType implements IntBitFieldValue {
        CL_DEVICE_TYPE_DEFAULT(1 << 0),
        CL_DEVICE_TYPE_CPU(1 << 1),
        CL_DEVICE_TYPE_GPU(1 << 2),
        CL_DEVICE_TYPE_ACCELERATOR(1 << 3),
        CL_DEVICE_TYPE_CUSTOM(1 << 4),
        CL_DEVICE_TYPE_ALL(0xFFFFFFFF),
        ;
        private final int value;

        DeviceType(int value) {
            this.value = value;
        }

        @Override
        public int getValue() {
            return value;
        }
    }


    public static Iterable<Long> getDeviceIDs(long platform, @NotNull DeviceType deviceType) {
        int status;

        BBInt1 size = BBInt1.newAllocated(null);
        check(clGetDeviceIDs(platform, deviceType, null, size));

        NativeInt64Array ids = NativeInt64Array.newAllocated(SVWrapper.length(size.get()));
        check(clGetDeviceIDs(platform, deviceType, ids, null));

        return ids;
    }

    public static int clGetDeviceIDs(
            long platform,
            @NotNull DeviceType deviceType,
            @Nullable NativeArray<Long> array,
            @Nullable BBInt1 deviceCount
    ) {
        return _clGetDeviceIDs(
                platform,
                deviceType.getValue(),
                array == null ? 0 : array.length(),
                array == null ? null : array.getByteBuffer(),
                deviceCount == null ? null : deviceCount.getByteBuffer()
        );
    }

    private static native int _clGetDeviceIDs(
            long platform,
            int device_type,
            int num_entries,
            @Nullable ByteBuffer p_devices,
            @Nullable ByteBuffer p_num_devices
    );

    public enum CLContextProperties implements LongBitFieldValue {
        CL_CURRENT_DEVICE_FOR_GL_CONTEXT_KHR(0x2006),
        CL_DEVICES_FOR_GL_CONTEXT_KHR(0x2007),
        CL_GL_CONTEXT_KHR(0x2008),
        CL_EGL_DISPLAY_KHR(0x2009),
        CL_GLX_DISPLAY_KHR(0x200A),
        CL_WGL_HDC_KHR(0x200B),
        CL_CGL_SHAREGROUP_KHR(0x200C),
        ;
        private final int value;

        CLContextProperties(int value) {
            this.value = value;
        }

        @Override
        public long getValue() {
            return value;
        }
    }


    public static long clCreateContext(
            @Nullable NativeArray<Long> properties,
            @NotNull NativeArray<Long> devices,
            long userData,
            @Nullable BBInt1 errCodeRet
    ) {
        return _clCreateContext(
                properties == null ? null : properties.getByteBuffer(),
                devices.length(),
                devices.getByteBuffer(),
                Context.class,
                userData,
                errCodeRet == null ? null : errCodeRet.getByteBuffer()
        );
    }

    private static native long _clCreateContext(
            @Nullable ByteBuffer p_properties,
            int num_devices,
            @NotNull ByteBuffer p_devices,
            @NotNull Class<Context> callback,
            long user_data,
            @Nullable ByteBuffer p_errcode_ret
            );

    public static void clReleaseContext(long context) {
        check(_clReleaseContext(context));
    }

    private static native int _clReleaseContext(
            long context
    );

    public enum QueueProperty implements LongBitFieldValue {
        CL_QUEUE_CONTEXT(0x1090),
        CL_QUEUE_DEVICE(0x1091),
        CL_QUEUE_REFERENCE_COUNT(0x1092),
        CL_QUEUE_PROPERTIES(0x1093),
        CL_QUEUE_SIZE(0x1094),
        CL_QUEUE_DEVICE_DEFAULT(0x1095),
        CL_QUEUE_PROPERTIES_ARRAY(0x1098),
        ;

        private final long value;

        QueueProperty(long value) {
            this.value = value;
        }

        @Override
        public long getValue() {
            return value;
        }
    }

    @SuppressWarnings("PointlessBitwiseExpression")
    public enum CLQueuePropertiesValue implements LongBitFieldValue {
        CL_QUEUE_OUT_OF_ORDER_EXEC_MODE_ENABLE(1 << 0),
        CL_QUEUE_PROFILING_ENABLE(1 << 1),
        CL_QUEUE_ON_DEVICE(1 << 2),
        CL_QUEUE_ON_DEVICE_DEFAULT(1 << 3),
        ;

        private final long value;

        CLQueuePropertiesValue(long value) {
            this.value = value;
        }

        @Override
        public long getValue() {
            return value;
        }
    }

    public static long clCreateCommandQueueWithProperties(
            long context,
            long device,
            @Nullable NativeArray<Long> properties
    ) {
        BBInt1 errCodeRet = BBInt1.newAllocated(null);
        long pointer = _clCreateCommandQueueWithProperties(
                context,
                device,
                properties == null ? null : properties.getByteBuffer(),
                errCodeRet.getByteBuffer()
        );

        check(errCodeRet.get());
        return pointer;
    }

    private static native long _clCreateCommandQueueWithProperties(
      long context,
      long device,
      @Nullable ByteBuffer p_properties,
      @Nullable ByteBuffer p_errcode_ret
    );

    @Deprecated(since = "OpenCL 2.0")
    public static long clCreateCommandQueue(
            long context,
            long device,
            @Nullable LongBitfield<CLQueuePropertiesValue> properties
    ) {
        BBInt1 errCodeRet = BBInt1.newAllocated(null);
        long pointer = _clCreateCommandQueue(
                context,
                device,
                properties == null ? 0 : properties.getValue(),
                errCodeRet.getByteBuffer()
        );

        check(errCodeRet.get());
        return pointer;
    }

    @Deprecated(since = "OpenCL 2.0")
    private static native long _clCreateCommandQueue(
      long context,
      long device,
      long properties,
      @Nullable ByteBuffer p_errcode_ret
    );

    public static void clReleaseCommandQueue(
            long queue
    ) {
        check(_clReleaseCommandQueue(queue));
    }

    private static native int _clReleaseCommandQueue(
            long queue
    );

    @SuppressWarnings("PointlessBitwiseExpression")
    public enum CLMemFlag implements LongBitFieldValue {
        CL_MEM_READ_WRITE(1 << 0),
        CL_MEM_WRITE_ONLY(1 << 1),
        CL_MEM_READ_ONLY(1 << 2),
        CL_MEM_USE_HOST_PTR(1 << 3),
        CL_MEM_ALLOC_HOST_PTR(1 << 4),
        CL_MEM_COPY_HOST_PTR(1 << 5),
        CL_MEM_HOST_WRITE_ONLY(1 << 7),
        CL_MEM_HOST_READ_ONLY(1 << 8),
        CL_MEM_HOST_NO_ACCESS(1 << 9),
        CL_MEM_SVM_FINE_GRAIN_BUFFER(1 << 10),
        CL_MEM_SVM_ATOMICS(1 << 11),
        CL_MEM_KERNEL_READ_AND_WRITE(1 << 12),
        ;

        private final long value;

        CLMemFlag(long value) {
            this.value = value;
        }

        @Override
        public long getValue() {
            return value;
        }

    }



    public static long clCreateBuffer(
            long context,
            @NotNull LongBitfield<CLMemFlag> clMemFlags,
            long size
    ) {
        BBInt1 errCodeRet = BBInt1.newAllocated(null);
        long pointer = _clCreateBuffer(
                context,
                clMemFlags.getValue(),
                size,
                null,
                errCodeRet.getByteBuffer()
        );
        check(errCodeRet.get());
        return pointer;
    }

    public static long clCreateBuffer(
            long context,
            @NotNull LongBitfield<CLMemFlag> clMemFlags,
            @NotNull Structure hostPtr
    ) {
        BBInt1 errCodeRet = BBInt1.newAllocated(null);
        long pointer = _clCreateBuffer(
                context,
                clMemFlags.getValue(),
                hostPtr.getRequiredSize(),
                hostPtr.getByteBuffer(),
                errCodeRet.getByteBuffer()
        );
        check(errCodeRet.get());
        return pointer;
    }

    private static native long _clCreateBuffer(
            long context,
            long cl_mem_flags,
            long size,
            @Nullable ByteBuffer p_host_ptr,
            @Nullable ByteBuffer p_errcode_ret
    );



    public static void clReleaseMemObject(
            long memObj
    ) {
        check(_clReleaseMemObject(memObj));
    }

    private static native int _clReleaseMemObject(
            long memobj
    );

    public static void clEnqueueReadBuffer(
            long command_queue,
            long buffer,
            boolean blocking,
            long offset,
            long size,
            long ptr,
            @Nullable NativeArray<Event> eventWaitList,
            @Nullable BBLong1 event
    ) {
        //TODO: size is actually wrong. offset is only used for the reading from the cl mem
        //TODO: Just use a long instead for ptr
        check(_clEnqueueReadBuffer(
                command_queue,
                buffer,
                blocking,
                offset,
                size,
                ptr,
                eventWaitList == null ? 0 : eventWaitList.length(),
                eventWaitList == null ? null : eventWaitList.getByteBuffer(),
                event == null ? null : event.getByteBuffer()
        ));
    }

    private static native int _clEnqueueReadBuffer(
            long command_queue,
            long buffer,
            boolean blocking_read,
            long offset,
            long size,
            long ptr,
            int num_events_in_wait_list,
            @Nullable ByteBuffer p_event_wait_list,
            @Nullable ByteBuffer p_event
    );

    public static void clEnqueueWriteBuffer(
            long command_queue,
            long buffer,
            boolean blocking,
            long offset,
            long size,
            long ptr,
            @Nullable NativeArray<Event> eventWaitList,
            @Nullable BBLong1 event
    ) {
        //TODO: size is actually wrong. offset is only used for the writing to the cl mem
        check(_clEnqueueWriteBuffer(
                command_queue,
                buffer,
                blocking,
                offset,
                size,
                ptr,
                eventWaitList == null ? 0 : eventWaitList.length(),
                eventWaitList == null ? null : eventWaitList.getByteBuffer(),
                event == null ? null : event.getByteBuffer()
        ));
    }

    private static native int _clEnqueueWriteBuffer(
            long command_queue,
            long buffer,
            boolean blocking_write,
            long offset,
            long size,
            long ptr,
            int num_events_in_wait_list,
            @Nullable ByteBuffer p_event_wait_list,
            @Nullable ByteBuffer p_event
    );

    public static long clCreateProgramWithSource(
            long context,
            @NotNull String src
    ) {
        BBInt1 errCodeRet = BBInt1.newAllocated(null);
        long pointer = _clCreateProgramWithSource(
                context,
                src,
                errCodeRet.getByteBuffer()
        );
        check(errCodeRet.get());
        return pointer;
    }

    private static native long _clCreateProgramWithSource(
            long context,
            @NotNull String src,
            @Nullable ByteBuffer p_errcode_ret
    );

    public static void clReleaseProgram(
            long program
    ) {
        check(_clReleaseProgram(program));
    }

    private static native int _clReleaseProgram(
            long program
    );

    public static void clBuildProgram(
            long program,
            @NotNull NativeArray<Long> deviceList,
            @Nullable String options,
            long user_data
    ) {
        check(_clBuildProgram(
                program,
                deviceList.length(),
                deviceList.getByteBuffer(),
                options,
                Program.class,
                user_data
        ));
    }

    private static native int _clBuildProgram(
            long program,
            int num_devices,
            @NotNull ByteBuffer p_device_list,
            @Nullable String options,
            @Nullable Class<Program> callback,
            long user_data
    );

    public enum ProgramBuildInfo implements IntBitFieldValue {
        CL_PROGRAM_BUILD_STATUS(0x1181),
        CL_PROGRAM_BUILD_OPTIONS(0x1182),
        CL_PROGRAM_BUILD_LOG(0x1183),

        /**
         * @since OpenCl 1.2
         */
        CL_PROGRAM_BINARY_TYPE(0x1184),

        /**
         * @since OpenCL 2.0
         */
        CL_PROGRAM_BUILD_GLOBAL_VARIABLE_TOTAL_SIZE(0x1185),
        ;

        private final int value;

        ProgramBuildInfo(int value) {
            this.value = value;
        }

        @Override
        public int getValue() {
            return value;
        }
    }

    public enum ProgramBinaryType implements IntBitFieldValue {
        CL_PROGRAM_BINARY_TYPE_NONE(0x0),
        CL_PROGRAM_BINARY_TYPE_COMPILED_OBJECT(0x1),
        CL_PROGRAM_BINARY_TYPE_LIBRARY(0x2),
        CL_PROGRAM_BINARY_TYPE_EXECUTABLE(0x4),
        ;

        private final int value;

        ProgramBinaryType(int value) {
            this.value = value;
        }

        @Override
        public int getValue() {
            return value;
        }
    }

    public enum BuildStatus implements IntBitFieldValue {
        CL_BUILD_SUCCESS(0),
        CL_BUILD_NONE(-1),
        CL_BUILD_ERROR(-2),
        CL_BUILD_IN_PROGRESS(-3),
        ;
        private final int value;

        BuildStatus(int value) {
            this.value = value;
        }

        public static @NotNull BuildStatus fromValue(int value) {
            for(BuildStatus status : values()) {
                if(status.value == value) return status;
            }

            throw new IllegalArgumentException("Unknown build status (" + value + ").");
        }

        @Override
        public int getValue() {
            return value;
        }
    }

    public static int clGetProgramBuildInfoInt(
            long program,
            long device,
            @NotNull ProgramBuildInfo param_name
    ) {
        BBInt1 paramValue = BBInt1.newAllocated(null);

        clGetProgramBuildInfo(
                program,
                device,
                param_name,
                paramValue.getByteBuffer(),
                null
        );

        return paramValue.get();
    }

    public static long clGetProgramBuildInfoLong(
            long program,
            long device,
            @NotNull ProgramBuildInfo param_name
    ) {
        BBLong1 paramValue = BBLong1.newAllocated(null);

        clGetProgramBuildInfo(
                program,
                device,
                param_name,
                paramValue.getByteBuffer(),
                null
        );

        return paramValue.get();
    }

    public static String clGetProgramBuildInfoString(
            long program,
            long device,
            @NotNull ProgramBuildInfo param_name
    ) {
        BBLong1 paramValueSizeRet = BBLong1.newAllocated(null);

        clGetProgramBuildInfo(
                program,
                device,
                param_name,
                null,
                paramValueSizeRet
        );

        ByteBuffer paramValue = BufferUtils.createAligned((int) paramValueSizeRet.get(), 8);

        clGetProgramBuildInfo(
                program,
                device,
                param_name,
                paramValue,
                null
        );

        paramValue.limit(paramValue.capacity() - 1); //remove '\0' from c-string
        return StandardCharsets.UTF_8.decode(paramValue).toString();
    }

    public static void clGetProgramBuildInfo(
            long program,
            long device,
            @NotNull ProgramBuildInfo param_name,
            @Nullable ByteBuffer paramValue,
            @Nullable BBLong1 paramValueSizeRet
    ) {
        check(_clGetProgramBuildInfo(
                program,
                device,
                param_name.getValue(),
                paramValue == null ? 0 : paramValue.capacity(),
                paramValue,
                paramValueSizeRet == null ? null : paramValueSizeRet.getByteBuffer()
        ));
    }

    private static native int _clGetProgramBuildInfo(
            long program,
            long device,
            int param_name,
            long param_value_size,
            @Nullable ByteBuffer p_param_value,
            @Nullable ByteBuffer p_param_value_size_ret
    );

    public enum CLDeviceInfo implements IntBitFieldValue {
        CL_DEVICE_TYPE(0x1000),
        CL_DEVICE_VENDOR_ID(0x1001),
        CL_DEVICE_MAX_COMPUTE_UNITS(0x1002),
        CL_DEVICE_MAX_WORK_ITEM_DIMENSIONS(0x1003),
        CL_DEVICE_MAX_WORK_GROUP_SIZE(0x1004),
        CL_DEVICE_MAX_WORK_ITEM_SIZES(0x1005),
        CL_DEVICE_PREFERRED_VECTOR_WIDTH_CHAR(0x1006),
        CL_DEVICE_PREFERRED_VECTOR_WIDTH_SHORT(0x1007),
        CL_DEVICE_PREFERRED_VECTOR_WIDTH_INT(0x1008),
        CL_DEVICE_PREFERRED_VECTOR_WIDTH_LONG(0x1009),
        CL_DEVICE_PREFERRED_VECTOR_WIDTH_FLOAT(0x100A),
        CL_DEVICE_PREFERRED_VECTOR_WIDTH_DOUBLE(0x100B),
        CL_DEVICE_MAX_CLOCK_FREQUENCY(0x100C),
        CL_DEVICE_ADDRESS_BITS(0x100D),
        CL_DEVICE_MAX_READ_IMAGE_ARGS(0x100E),
        CL_DEVICE_MAX_WRITE_IMAGE_ARGS(0x100F),
        CL_DEVICE_MAX_MEM_ALLOC_SIZE(0x1010),
        CL_DEVICE_IMAGE2D_MAX_WIDTH(0x1011),
        CL_DEVICE_IMAGE2D_MAX_HEIGHT(0x1012),
        CL_DEVICE_IMAGE3D_MAX_WIDTH(0x1013),
        CL_DEVICE_IMAGE3D_MAX_HEIGHT(0x1014),
        CL_DEVICE_IMAGE3D_MAX_DEPTH(0x1015),
        CL_DEVICE_IMAGE_SUPPORT(0x1016),
        CL_DEVICE_MAX_PARAMETER_SIZE(0x1017),
        CL_DEVICE_MAX_SAMPLERS(0x1018),
        CL_DEVICE_MEM_BASE_ADDR_ALIGN(0x1019),
        CL_DEVICE_MIN_DATA_TYPE_ALIGN_SIZE(0x101A),
        CL_DEVICE_SINGLE_FP_CONFIG(0x101B),
        CL_DEVICE_GLOBAL_MEM_CACHE_TYPE(0x101C),
        CL_DEVICE_GLOBAL_MEM_CACHELINE_SIZE(0x101D),
        CL_DEVICE_GLOBAL_MEM_CACHE_SIZE(0x101E),
        CL_DEVICE_GLOBAL_MEM_SIZE(0x101F),
        CL_DEVICE_MAX_CONSTANT_BUFFER_SIZE(0x1020),
        CL_DEVICE_MAX_CONSTANT_ARGS(0x1021),
        CL_DEVICE_LOCAL_MEM_TYPE(0x1022),
        CL_DEVICE_LOCAL_MEM_SIZE(0x1023),
        CL_DEVICE_ERROR_CORRECTION_SUPPORT(0x1024),
        CL_DEVICE_PROFILING_TIMER_RESOLUTION(0x1025),
        CL_DEVICE_ENDIAN_LITTLE(0x1026),
        CL_DEVICE_AVAILABLE(0x1027),
        CL_DEVICE_COMPILER_AVAILABLE(0x1028),
        CL_DEVICE_EXECUTION_CAPABILITIES(0x1029),
        @Deprecated
        CL_DEVICE_QUEUE_PROPERTIES(0x102A),
        CL_DEVICE_QUEUE_ON_HOST_PROPERTIES(0x102A),
        CL_DEVICE_NAME(0x102B),
        CL_DEVICE_VENDOR(0x102C),
        CL_DRIVER_VERSION(0x102D),
        CL_DEVICE_PROFILE(0x102E),
        CL_DEVICE_VERSION(0x102F),
        CL_DEVICE_EXTENSIONS(0x1030),
        CL_DEVICE_PLATFORM(0x1031),
        CL_DEVICE_DOUBLE_FP_CONFIG(0x1032),
        CL_DEVICE_PREFERRED_VECTOR_WIDTH_HALF(0x1034),
        @Deprecated
        CL_DEVICE_HOST_UNIFIED_MEMORY(0x1035),
        CL_DEVICE_NATIVE_VECTOR_WIDTH_CHAR(0x1036),
        CL_DEVICE_NATIVE_VECTOR_WIDTH_SHORT(0x1037),
        CL_DEVICE_NATIVE_VECTOR_WIDTH_INT(0x1038),
        CL_DEVICE_NATIVE_VECTOR_WIDTH_LONG(0x1039),
        CL_DEVICE_NATIVE_VECTOR_WIDTH_FLOAT(0x103A),
        CL_DEVICE_NATIVE_VECTOR_WIDTH_DOUBLE(0x103B),
        CL_DEVICE_NATIVE_VECTOR_WIDTH_HALF(0x103C),
        CL_DEVICE_OPENCL_C_VERSION(0x103D),
        CL_DEVICE_LINKER_AVAILABLE(0x103E),
        CL_DEVICE_BUILT_IN_KERNELS(0x103F),
        CL_DEVICE_IMAGE_MAX_BUFFER_SIZE(0x1040),
        CL_DEVICE_IMAGE_MAX_ARRAY_SIZE(0x1041),
        CL_DEVICE_PARENT_DEVICE(0x1042),
        CL_DEVICE_PARTITION_MAX_SUB_DEVICES(0x1043),
        CL_DEVICE_PARTITION_PROPERTIES(0x1044),
        CL_DEVICE_PARTITION_AFFINITY_DOMAIN(0x1045),
        CL_DEVICE_PARTITION_TYPE(0x1046),
        CL_DEVICE_REFERENCE_COUNT(0x1047),
        CL_DEVICE_PREFERRED_INTEROP_USER_SYNC(0x1048),
        CL_DEVICE_PRINTF_BUFFER_SIZE(0x1049),
        CL_DEVICE_IMAGE_PITCH_ALIGNMENT(0x104A),
        CL_DEVICE_IMAGE_BASE_ADDRESS_ALIGNMENT(0x104B),
        CL_DEVICE_MAX_READ_WRITE_IMAGE_ARGS(0x104C),
        CL_DEVICE_MAX_GLOBAL_VARIABLE_SIZE(0x104D),
        CL_DEVICE_QUEUE_ON_DEVICE_PROPERTIES(0x104E),
        CL_DEVICE_QUEUE_ON_DEVICE_PREFERRED_SIZE(0x104F),
        CL_DEVICE_QUEUE_ON_DEVICE_MAX_SIZE(0x1050),
        CL_DEVICE_MAX_ON_DEVICE_QUEUES(0x1051),
        CL_DEVICE_MAX_ON_DEVICE_EVENTS(0x1052),
        CL_DEVICE_SVM_CAPABILITIES(0x1053),
        CL_DEVICE_GLOBAL_VARIABLE_PREFERRED_TOTAL_SIZE(0x1054),
        CL_DEVICE_MAX_PIPE_ARGS(0x1055),
        CL_DEVICE_PIPE_MAX_ACTIVE_RESERVATIONS(0x1056),
        CL_DEVICE_PIPE_MAX_PACKET_SIZE(0x1057),
        CL_DEVICE_PREFERRED_PLATFORM_ATOMIC_ALIGNMENT(0x1058),
        CL_DEVICE_PREFERRED_GLOBAL_ATOMIC_ALIGNMENT(0x1059),
        CL_DEVICE_PREFERRED_LOCAL_ATOMIC_ALIGNMENT(0x105A),
        CL_DEVICE_IL_VERSION(0x105B),
        CL_DEVICE_MAX_NUM_SUB_GROUPS(0x105C),
        CL_DEVICE_SUB_GROUP_INDEPENDENT_FORWARD_PROGRESS(0x105D),
        CL_DEVICE_NUMERIC_VERSION(0x105E),
        CL_DEVICE_EXTENSIONS_WITH_VERSION(0x1060),
        CL_DEVICE_ILS_WITH_VERSION(0x1061),
        CL_DEVICE_BUILT_IN_KERNELS_WITH_VERSION(0x1062),
        CL_DEVICE_ATOMIC_MEMORY_CAPABILITIES(0x1063),
        CL_DEVICE_ATOMIC_FENCE_CAPABILITIES(0x1064),
        CL_DEVICE_NON_UNIFORM_WORK_GROUP_SUPPORT(0x1065),
        CL_DEVICE_OPENCL_C_ALL_VERSIONS(0x1066),
        CL_DEVICE_PREFERRED_WORK_GROUP_SIZE_MULTIPLE(0x1067),
        CL_DEVICE_WORK_GROUP_COLLECTIVE_FUNCTIONS_SUPPORT(0x1068),
        CL_DEVICE_GENERIC_ADDRESS_SPACE_SUPPORT(0x1069),
        CL_DEVICE_OPENCL_C_FEATURES(0x106F),
        CL_DEVICE_DEVICE_ENQUEUE_CAPABILITIES(0x1070),
        CL_DEVICE_PIPE_SUPPORT(0x1071),
        CL_DEVICE_LATEST_CONFORMANCE_VERSION_PASSED(0x1072),
        ;

        private final int value;

        CLDeviceInfo(int value) {
            this.value = value;
        }

        @Override
        public int getValue() {
            return value;
        }
    }

    public static String getDeviceInfoString(
            long device,
            @NotNull CLDeviceInfo paramName
    ) {
        BBLong1 sizeRet = BBLong1.newAllocated(null);

        clGetDeviceInfo(
                device,
                paramName,
                null,
                sizeRet
        );

        ByteBuffer paramValue = BufferUtils.createAligned((int) sizeRet.get(), 8);

        clGetDeviceInfo(
                device,
                paramName,
                paramValue,
                null
        );

        paramValue.limit(paramValue.capacity() - 1); //remove '\0' from c-string
        return StandardCharsets.UTF_8.decode(paramValue).toString();
    }

    public static void clGetDeviceInfo(
            long device,
            @NotNull CLDeviceInfo paramName,
            @Nullable ByteBuffer paramValue,
            @Nullable BBLong1 paramValueSizeRet
    ) {
        check(_clGetDeviceInfo(
                device,
                paramName.getValue(),
                paramValue == null ? 0 : paramValue.capacity(),
                paramValue,
                paramValueSizeRet == null ? null : paramValueSizeRet.getByteBuffer()
        ));
    }

    private static native int _clGetDeviceInfo(
            long device,
            int param_name,
            long param_value_size,
            @Nullable ByteBuffer p_param_value,
            @Nullable ByteBuffer p_param_value_size_ret
    );

    public static long clCreateKernel(
            long program,
            @NotNull String kernelName
    ) {
        BBInt1 errCodeRet = BBInt1.newAllocated(null);

        long pointer = _clCreateKernel(
                program,
                kernelName,
                errCodeRet.getByteBuffer()
        );

        check(errCodeRet.get());
        return pointer;
    }

    private static native long _clCreateKernel(
            long program,
            @NotNull String kernel_name,
            @Nullable ByteBuffer p_errcode_ret
    );

    public static void clReleaseKernel(
            long kernel
    ) {
        check(_clReleaseKernel(kernel));
    }

    private static native int _clReleaseKernel(
            long kernel
    );

    public static void clSetKernelArg(
            long kernel,
            int argIndex,
            @NotNull MemoryObject memoryObject
    ) {
        check(_clSetKernelArg(
                kernel,
                argIndex,
                SIZE_OF_CL_MEM,
                memoryObject.getOpenCLObjectPointer(),
                true
        ));
    }

    public static void clSetKernelArg(
            long kernel,
            int argIndex,
            @NotNull Structure value
    ) {
        check(_clSetKernelArg(
                kernel,
                argIndex,
                value.getRequiredSize(),
                value.getPointer(),
                false
        ));
    }

    private static native int _clSetKernelArg(
            long kernel,
            int arg_index,
            long arg_size,
            long p_arg_value,
            boolean passPointerToPointer
    );

    public enum KernelInfo implements IntBitFieldValue {
        CL_KERNEL_FUNCTION_NAME(0x1190),
        CL_KERNEL_NUM_ARGS(0x1191),
        CL_KERNEL_REFERENCE_COUNT(0x1192),
        CL_KERNEL_CONTEXT(0x1193),
        CL_KERNEL_PROGRAM(0x1194),
        CL_KERNEL_ATTRIBUTES(0x1195),
        ;

        private final int value;

        KernelInfo(int value) {
            this.value = value;
        }

        @Override
        public int getValue() {
            return value;
        }
    }

    public static int GetKernelInfoInt(
            long kernel,
            @NotNull KernelInfo paramName
    ) {
        BBInt1 value = BBInt1.newAllocated(null);
        clGetKernelInfo(
                kernel,
                paramName,
                value.getByteBuffer(),
                null
        );

        return value.get();
    }

    public static @NotNull String GetKernelInfoString(
            long kernel,
            @NotNull KernelInfo paramName
    ) {
        BBLong1 sizeRet = BBLong1.newAllocated(null);

        clGetKernelInfo(
                kernel,
                paramName,
                null,
                sizeRet
        );

        ByteBuffer paramValue = BufferUtils.createAligned((int) sizeRet.get(), 8);

        clGetKernelInfo(
                kernel,
                paramName,
                paramValue,
                null
        );

        paramValue.limit(paramValue.capacity() - 1); //remove '\0' from c-string
        return StandardCharsets.UTF_8.decode(paramValue).toString();
    }

    public static void clGetKernelInfo(
            long kernel,
            @NotNull KernelInfo paramName,
            @Nullable ByteBuffer paramValue,
            @Nullable BBLong1 paramValueSizeRet
    ) {
        check(_clGetKernelInfo(
                kernel,
                paramName.getValue(),
                paramValue == null ? 0 : paramValue.capacity(),
                paramValue,
                paramValueSizeRet == null ? null : paramValueSizeRet.getByteBuffer()
        ));
    }

    private static native int _clGetKernelInfo(
            long kernel,
            int param_name,
            long param_value_size,
            @Nullable ByteBuffer p_param_value,
            @Nullable ByteBuffer p_param_value_size_ret
    );

    public static void clEnqueueNDRangeKernel(
            long command_queue,
            long kernel,
            int work_dim,
            @Nullable BBLongN globalWorkOffset,
            @Nullable BBLongN globalWorkSize,
            @Nullable BBLongN localWorkSize,
            @Nullable NativeArray<Event> event_wait_list,
            @Nullable BBLong1 event
    ) {
        check(_clEnqueueNDRangeKernel(
                command_queue,
                kernel,
                work_dim,
                globalWorkOffset == null ? null : globalWorkOffset.getByteBuffer(),
                globalWorkSize == null ? null : globalWorkSize.getByteBuffer(),
                localWorkSize == null ? null : localWorkSize.getByteBuffer(),
                event_wait_list == null ? 0 : event_wait_list.length(),
                event_wait_list == null ? null : event_wait_list.getByteBuffer(),
                event == null ? null : event.getByteBuffer()
        ));
    }

    private static native int _clEnqueueNDRangeKernel(
            long command_queue,
            long kernel,
            int work_dim,
            @Nullable ByteBuffer p_global_work_offset,
            @Nullable ByteBuffer p_global_work_size,
            @Nullable ByteBuffer p_local_work_size,
            int num_events_in_wait_list,
            @Nullable ByteBuffer p_event_wait_list,
            @Nullable ByteBuffer p_event
    );

    public static long clCreateFromGLRenderbuffer(
            long context,
            @NotNull LongBitfield<CLMemFlag> memFlags,
            int renderbuffer
    ) {
        BBInt1 errCodeRet = BBInt1.newAllocated(null);

        long pointer = _clCreateFromGLRenderbuffer(
                context,
                memFlags.getValue(),
                renderbuffer,
                errCodeRet.getByteBuffer()
        );

        check(errCodeRet.get());
        return pointer;
    }

    private static native long _clCreateFromGLRenderbuffer(
            long context,
            long cl_mem_flags,
            int renderbuffer,
            @Nullable ByteBuffer p_errcode_ret
    );

    public static void clEnqueueAcquireGLObjects(
            long command_queue,
            @NotNull NativeArray<MemoryObject> memObjects,
            @Nullable NativeArray<Event> eventWaitList,
            @Nullable BBLong1 event
    ) {
        check(_clEnqueueAcquireGLObjects(
                command_queue,
                memObjects.length(),
                memObjects.getByteBuffer(),
                eventWaitList == null ? 0 : eventWaitList.length(),
                eventWaitList == null ? null : eventWaitList.getByteBuffer(),
                event == null ? null : event.getByteBuffer()
        ));
    }

    private static native int _clEnqueueAcquireGLObjects(
            long command_queue,
            int num_objects,
            @NotNull ByteBuffer p_mem_objects,
            int num_events_in_wait_list,
            @Nullable ByteBuffer p_event_wait_list,
            @Nullable ByteBuffer p_event
    );

    public static void clEnqueueReleaseGLObjects(
            long command_queue,
            @NotNull NativeArray<MemoryObject> memObjects,
            @Nullable NativeArray<Event> eventWaitList,
            @Nullable BBLong1 event
    ) {
        check(_clEnqueueReleaseGLObjects(
                command_queue,
                memObjects.length(),
                memObjects.getByteBuffer(),
                eventWaitList == null ? 0 : eventWaitList.length(),
                eventWaitList == null ? null : eventWaitList.getByteBuffer(),
                event == null ? null : event.getByteBuffer()
        ));
    }

    private static native int _clEnqueueReleaseGLObjects(
            long command_queue,
            int num_objects,
            @NotNull ByteBuffer p_mem_objects,
            int num_events_in_wait_list,
            @Nullable ByteBuffer p_event_wait_list,
            @Nullable ByteBuffer p_event
    );

    public static void clFinish(long command_queue) {
        check(_clFinish(command_queue));
    }

    private static native int _clFinish(long command_queue);

    public static void clFlush(long command_queue) {
        check(_clFlush(command_queue));
    }

    private static native int _clFlush(long command_queue);

    public enum CLChannelOrder implements IntBitFieldValue {
        CL_R(0x10B0),
        CL_A(0x10B1),
        CL_RG(0x10B2),
        CL_RA(0x10B3),
        CL_RGB(0x10B4),
        CL_RGBA(0x10B5),
        CL_BGRA(0x10B6),
        CL_ARGB(0x10B7),
        CL_INTENSITY(0x10B8),
        CL_LUMINANCE(0x10B9),
        CL_Rx(0x10BA),
        CL_RGx(0x10BB),
        CL_RGBx(0x10BC),
        CL_DEPTH(0x10BD),
        CL_DEPTH_STENCIL(0x10BE),
        CL_sRGB(0x10BF),
        CL_sRGBx(0x10C0),
        CL_sRGBA(0x10C1),
        CL_sBGRA(0x10C2),
        CL_ABGR(0x10C3),
        ;

        private final int value;

        CLChannelOrder(int value) {
            this.value = value;
        }

        @Override
        public int getValue() {
            return value;
        }
    }

    public enum CLChannelType implements IntBitFieldValue {
        CL_SNORM_INT8(0x10D0),
        CL_SNORM_INT16(0x10D1),
        CL_UNORM_INT8(0x10D2),
        CL_UNORM_INT16(0x10D3),
        CL_UNORM_SHORT_565(0x10D4),
        CL_UNORM_SHORT_555(0x10D5),
        CL_UNORM_INT_101010(0x10D6),
        CL_SIGNED_INT8(0x10D7),
        CL_SIGNED_INT16(0x10D8),
        CL_SIGNED_INT32(0x10D9),
        CL_UNSIGNED_INT8(0x10DA),
        CL_UNSIGNED_INT16(0x10DB),
        CL_UNSIGNED_INT32(0x10DC),
        CL_HALF_FLOAT(0x10DD),
        CL_FLOAT(0x10DE),
        CL_UNORM_INT24(0x10DF),
        CL_UNORM_INT_101010_2(0x10E0),
        ;

        private final int value;

        CLChannelType(int value) {
            this.value = value;
        }

        @Override
        public int getValue() {
            return value;
        }
    }

    public enum CLMemoryObjectType implements IntBitFieldValue {
        CL_MEM_OBJECT_BUFFER(0x10F0),
        CL_MEM_OBJECT_IMAGE2D(0x10F1),
        CL_MEM_OBJECT_IMAGE3D(0x10F2),

        CL_MEM_OBJECT_IMAGE2D_ARRAY(0x10F3),
        CL_MEM_OBJECT_IMAGE1D(0x10F4),
        CL_MEM_OBJECT_IMAGE1D_ARRAY(0x10F5),
        CL_MEM_OBJECT_IMAGE1D_BUFFER(0x10F6),
        CL_MEM_OBJECT_PIPE(0x10F7),
        ;

        private final int value;

        CLMemoryObjectType(int value) {
            this.value = value;
        }

        @Override
        public int getValue() {
            return value;
        }
    }

    public static long clCreateImage(
            long context,
            @NotNull LongBitfield<CLMemFlag> memFlags,
            @NotNull CLImageFormat imageFormat,
            @NotNull CLImageDesc imageDesc,
            long hostPointer
    ) {
        BBInt1 errCodeRet = BBInt1.newAllocated(null);

        long pointer = _clCreateImage(
                context,
                memFlags.getValue(),
                imageFormat.getPointer(),
                imageDesc.getPointer(),
                hostPointer,
                errCodeRet.getByteBuffer()
        );

        check(errCodeRet.get());

        return pointer;
    }

    protected static native long _clCreateImage(
            long context,
            long cl_mem_flags,
            long p_image_format,
            long p_image_desc,
            long host_ptr,
            @Nullable ByteBuffer p_errcode_ret
    );

    public static void clWaitForEvents(
            NativeArray<Event> eventList
    ) {
        check(_clWaitForEvents(eventList.length(), eventList.getPointer()));
    }

    public static void clWaitForEvent(
            Event event
    ) {
        check(_clWaitForEvents(1, event.getPointer()));
    }

    protected static native int _clWaitForEvents(
            int num_events,
            long ptr_event_list
    );
}
