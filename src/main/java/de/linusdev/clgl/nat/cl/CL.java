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

package de.linusdev.clgl.nat.cl;

import de.linusdev.clgl.api.structs.PrimitiveTypeArray;
import de.linusdev.clgl.api.types.bytebuffer.BBInt1;
import de.linusdev.clgl.api.utils.BufferUtils;
import de.linusdev.clgl.nat.cl.objects.Context;
import de.linusdev.lutils.bitfield.IntBitFieldValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static de.linusdev.clgl.nat.cl.CLStatus.check;

@SuppressWarnings("unused")
public class CL {

    public static Iterable<Long> getPlatformIDs() {
        BBInt1 size = new BBInt1(true);
        check(clGetPlatformIDs(null, size));

        PrimitiveTypeArray<Long> ids = new PrimitiveTypeArray<>(Long.class, size.get(), true);
        check(clGetPlatformIDs(ids, null));

        return ids;
    }

    public static int clGetPlatformIDs(
            @Nullable PrimitiveTypeArray<Long> array,
            @Nullable BBInt1 platformCount
    ) {
        return _clGetPlatformIDs(
                array == null ? 0 : array.size(),
                array == null ? null : array.getByteBuf(),
                platformCount == null ? null : platformCount.getByteBuf()
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
        ByteBuffer longBuffer = BufferUtils.createAlignedByteBuffer(Long.BYTES, 8);
        check(clGetPlatformInfo(platform, paramName, longBuffer, null));
        return longBuffer.getLong();
    }

    public static String getPlatformInfoString(long platform, @NotNull PlatformInfo paramName) {
        BBInt1 size = new BBInt1(true);
        check(clGetPlatformInfo(platform, paramName, null, size));

        ByteBuffer info = BufferUtils.createAlignedByteBuffer(size.get(), 8);
        check(clGetPlatformInfo(platform, paramName, info, null));

        info.limit(info.capacity() - 1); //remove '\0' from c-string
        return StandardCharsets.UTF_8.decode(info).toString();
    }

    public static int clGetPlatformInfo(
            long platform,
            @NotNull PlatformInfo paramName,
            @Nullable ByteBuffer paramValue,
            @Nullable BBInt1 paramValueSizeRet
    ) {
        return _clGetPlatformInfo(
                platform,
                paramName.getValue(),
                paramValue == null ? 0 : paramValue.capacity(),
                paramValue,
                paramValueSizeRet == null ? null : paramValueSizeRet.getByteBuf()
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

        BBInt1 size = new BBInt1(true);
        check(clGetDeviceIDs(platform, deviceType, null, size));

        PrimitiveTypeArray<Long> ids = new PrimitiveTypeArray<>(Long.class, size.get(), true);
        check(clGetDeviceIDs(platform, deviceType, ids, null));

        return ids;
    }

    public static int clGetDeviceIDs(
            long platform,
            @NotNull DeviceType deviceType,
            @Nullable PrimitiveTypeArray<Long> array,
            @Nullable BBInt1 deviceCount
    ) {
        return _clGetDeviceIDs(
                platform,
                deviceType.getValue(),
                array == null ? 0 : array.size(),
                array == null ? null : array.getByteBuf(),
                deviceCount == null ? null : deviceCount.getByteBuf()
        );
    }

    private static native int _clGetDeviceIDs(
            long platform,
            int device_type,
            int num_entries,
            @Nullable ByteBuffer p_devices,
            @Nullable ByteBuffer p_num_devices
    );

    public enum CLContextProperties implements IntBitFieldValue {
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
        public int getValue() {
            return value;
        }
    }


    public static long clCreateContext(
            @Nullable PrimitiveTypeArray<Long> properties,
            @NotNull PrimitiveTypeArray<Long> devices,
            long userData,
            @Nullable BBInt1 errCodeRet
    ) {
        return _clCreateContext(
                properties == null ? null : properties.getByteBuf(),
                devices.size(),
                devices.getByteBuf(),
                Context.class,
                userData,
                errCodeRet == null ? null : errCodeRet.getByteBuf()
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

    public static void clReleaseContext(@NotNull Context context) {
        check(_clReleaseContext(context.getPointer()));
    }

    private static native int _clReleaseContext(
            long context
    );

}
