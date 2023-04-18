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

import org.jetbrains.annotations.NotNull;

/**
 * Enum of OpenCL error codes. Based on <a href="https://streamhpc.com/blog/2013-04-28/opencl-error-codes/">streamhpc.com</a>
 */
@SuppressWarnings("unused")
public enum CLStatus {

    CL_SUCCESS(0, "The sweet spot."),
    CL_DEVICE_NOT_FOUND(-1, "if no OpenCL devices that matched device_type were found.", "clGetDeviceIDs"),
    CL_DEVICE_NOT_AVAILABLE(-2, "if a device in devices is currently not available even though the device was returned by clGetDeviceIDs.", "clCreateContext"),
    CL_COMPILER_NOT_AVAILABLE(-3, "if program is created with clCreateProgramWithSource and a compiler is not available i.e. CL_DEVICE_COMPILER_AVAILABLE specified in the table of OpenCL Device Queries for clGetDeviceInfo is set to CL_FALSE.", "clBuildProgram"),
    CL_MEM_OBJECT_ALLOCATION_FAILURE(-4, "if there is a failure to allocate memory for buffer object."),
    CL_OUT_OF_RESOURCES(-5, "if there is a failure to allocate resources required by the OpenCL implementation on the device."),
    CL_OUT_OF_HOST_MEMORY(-6, "if there is a failure to allocate resources required by the OpenCL implementation on the host."),
    CL_PROFILING_INFO_NOT_AVAILABLE(-7, "if the CL_QUEUE_PROFILING_ENABLE flag is not set for the command-queue, if the execution status of the command identified by event is not CL_COMPLETE or if event is a user event object.", "clGetEventProfilingInfo"),
    CL_MEM_COPY_OVERLAP(-8, "if src_buffer and dst_buffer are the same buffer or subbuffer object and the source and destination regions overlap or if src_buffer and dst_buffer are different sub-buffers of the same associated buffer object and they overlap. The regions overlap if src_offset ? to dst_offset ? to src_offset + size � 1, or if dst_offset ? to src_offset ? to dst_offset + size � 1.", "clEnqueueCopyBuffer", "clEnqueueCopyBufferRect", "clEnqueueCopyImage"),
    CL_IMAGE_FORMAT_MISMATCH(-9, "if src_image and dst_image do not use the same image format.", "clEnqueueCopyImage"),
    CL_IMAGE_FORMAT_NOT_SUPPORTED(-10, "if the image_format is not supported.", "clCreateImage"),
    CL_BUILD_PROGRAM_FAILURE(-11, "if there is a failure to build the program executable. This error will be returned if clBuildProgram does not return until the build has completed.", "clBuildProgram"),
    CL_MAP_FAILURE(-12, "if there is a failure to map the requested region into the host address space. This error cannot occur for image objects created with CL_MEM_USE_HOST_PTR or CL_MEM_ALLOC_HOST_PTR.", "clEnqueueMapBuffer", "clEnqueueMapImage"),
    CL_MISALIGNED_SUB_BUFFER_OFFSET(-13, "if a sub-buffer object is specified as the value for an argument that is a buffer object and the offset specified when the sub-buffer object is created is not aligned to CL_DEVICE_MEM_BASE_ADDR_ALIGN value for device associated with queue."),
    CL_EXEC_STATUS_ERROR_FOR_EVENTS_IN_WAIT_LIST(-14, "if the execution status of any of the events in event_list is a negative integer value."),
    CL_COMPILE_PROGRAM_FAILURE(-15, "if there is a failure to compile the program source. This error will be returned if clCompileProgram does not return until the compile has completed.", "clCompileProgram"),
    CL_LINKER_NOT_AVAILABLE(-16, "if a linker is not available i.e. CL_DEVICE_LINKER_AVAILABLE specified in the table of allowed values for param_name for clGetDeviceInfo is set to CL_FALSE.", "clLinkProgram"),
    CL_LINK_PROGRAM_FAILURE(-17, "if there is a failure to link the compiled binaries and/or libraries.", "clLinkProgram"),
    CL_DEVICE_PARTITION_FAILED(-18, "if the partition name is supported by the implementation but in_device could not be further partitioned.", "clCreateSubDevices"),
    CL_KERNEL_ARG_INFO_NOT_AVAILABLE(-19, "if the argument information is not available for kernel.", "clGetKernelArgInfo"),

    CL_INVALID_VALUE(-30, "This depends on the function: two or more coupled parameters had errors.", "clGetDeviceIDs", "clCreateContext"),
    CL_INVALID_DEVICE_TYPE(-31, "if an invalid device_type is given", "clGetDeviceIDs"),
    CL_INVALID_PLATFORM(-32, "if an invalid platform was given", "clGetDeviceIDs"),
    CL_INVALID_DEVICE(-33, "if devices contains an invalid device or are not associated with the specified platform.", "clCreateContext", "clBuildProgram"),
    CL_INVALID_CONTEXT(-34, "if context is not a valid context."),
    CL_INVALID_QUEUE_PROPERTIES(-35, "if specified command-queue-properties are valid but are not supported by the device.", "clCreateCommandQueue"),
    CL_INVALID_COMMAND_QUEUE(-36, "if command_queue is not a valid command-queue."),
    CL_INVALID_HOST_PTR(-37, "This flag is valid only if host_ptr is not NULL. If specified, it indicates that the application wants the OpenCL implementation to allocate memory for the memory object and copy the data from memory referenced by host_ptr.CL_MEM_COPY_HOST_PTR and CL_MEM_USE_HOST_PTR are mutually exclusive.CL_MEM_COPY_HOST_PTR can be used with CL_MEM_ALLOC_HOST_PTR to initialize the contents of the cl_mem object allocated using host-accessible (e.g. PCIe) memory.", "clCreateImage", "clCreateBuffer"),
    CL_INVALID_MEM_OBJECT(-38, "if memobj is not a valid OpenCL memory object."),
    CL_INVALID_IMAGE_FORMAT_DESCRIPTOR(-39, "if the OpenGL/DirectX texture internal format does not map to a supported OpenCL image format."),
    CL_INVALID_IMAGE_SIZE(-40, "if an image object is specified as an argument value and the image dimensions (image width, height, specified or compute row and/or slice pitch) are not supported by device associated with queue."),
    CL_INVALID_SAMPLER(-41, "if sampler is not a valid sampler object.", "clGetSamplerInfo", "clReleaseSampler", "clRetainSampler", "clSetKernelArg"),
    CL_INVALID_BINARY(-42, "The provided binary is unfit for the selected device. if program is created with clCreateProgramWithBinary and devices listed in device_list do not have a valid program binary loaded.", "clCreateProgramWithBinary", "clBuildProgram"),
    CL_INVALID_BUILD_OPTIONS(-43, "if the build options specified by options are invalid.", "clBuildProgram"),
    CL_INVALID_PROGRAM(-44, "if program is a not a valid program object."),
    CL_INVALID_PROGRAM_EXECUTABLE(-45, "if there is no successfully built program executable available for device associated with command_queue."),
    CL_INVALID_KERNEL_NAME(-46, "if kernel_name is not found in program.", "clCreateKernel"),
    CL_INVALID_KERNEL_DEFINITION(-47, "if the function definition for __kernel function given by kernel_name such as the number of arguments, the argument types are not the same for all devices for which the program executable has been built.", "clCreateKernel"),
    CL_INVALID_KERNEL(-48, "if kernel is not a valid kernel object."),
    CL_INVALID_ARG_INDEX(-49, "if arg_index is not a valid argument index.", "clSetKernelArg", "clGetKernelArgInfo"),
    CL_INVALID_ARG_VALUE(-50, "if arg_value specified is not a valid value.", "clSetKernelArg", "clGetKernelArgInfo"),
    CL_INVALID_ARG_SIZE(-51, "if arg_size does not match the size of the data type for an argument that is not a memory object or if the argument is a memory object and arg_size != sizeof(cl_mem) or if arg_size is zero and the argument is declared with the __local qualifier or if the argument is a sampler and arg_size != sizeof(cl_sampler).", "clSetKernelArg"),
    CL_INVALID_KERNEL_ARGS(-52, "if the kernel argument values have not been specified."),
    CL_INVALID_WORK_DIMENSION(-53, "if work_dim is not a valid value (i.e. a value between 1 and 3)."),
    CL_INVALID_WORK_GROUP_SIZE(-54, "if local_work_size is specified and number of work-items specified by global_work_size is not evenly divisable by size of work-group given by local_work_size or does not match the work-group size specified for kernel using the __attribute__ ((reqd_work_group_size(X, Y, Z))) qualifier in program source.if local_work_size is specified and the total number of work-items in the work-group computed as local_work_size[0] *� local_work_size[work_dim � 1] is greater than the value specified by CL_DEVICE_MAX_WORK_GROUP_SIZE in the table of OpenCL Device Queries for clGetDeviceInfo.if local_work_size is NULL and the __attribute__ ((reqd_work_group_size(X, Y, Z))) qualifier is used to declare the work-group size for kernel in the program source."),
    CL_INVALID_WORK_ITEM_SIZE(-55, "if the number of work-items specified in any of local_work_size[0], � local_work_size[work_dim � 1] is greater than the corresponding values specified by CL_DEVICE_MAX_WORK_ITEM_SIZES[0], �. CL_DEVICE_MAX_WORK_ITEM_SIZES[work_dim � 1]."),
    CL_INVALID_GLOBAL_OFFSET(-56, "if the value specified in global_work_size + the corresponding values in global_work_offset for any dimensions is greater than the sizeof(size_t) for the device on which the kernel execution will be enqueued."),
    CL_INVALID_EVENT_WAIT_LIST(-57, "if event_wait_list is NULL and num_events_in_wait_list > 0, or event_wait_list is not NULL and num_events_in_wait_list is 0, or if event objects in event_wait_list are not valid events."),
    CL_INVALID_EVENT(-58, "if event objects specified in event_list are not valid event objects."),
    CL_INVALID_OPERATION(-59, "if interoperability is specified by setting CL_CONTEXT_ADAPTER_D3D9_KHR, CL_CONTEXT_ADAPTER_D3D9EX_KHR or CL_CONTEXT_ADAPTER_DXVA_KHR to a non-NULL value, and interoperability with another graphics API is also specified. (only if the cl_khr_dx9_media_sharing extension is supported)."),
    CL_INVALID_GL_OBJECT(-60, "if texture is not a GL texture object whose type matches texture_target, if the specified miplevel of texture is not defined, or if the width or height of the specified miplevel is zero."),
    CL_INVALID_BUFFER_SIZE(-61, "if size is 0.Implementations may return CL_INVALID_BUFFER_SIZE if size is greater than the CL_DEVICE_MAX_MEM_ALLOC_SIZE value specified in the table of allowed values for param_name for clGetDeviceInfo for all devices in context.", "clCreateBuffer", "clCreateSubBuffer"),
    CL_INVALID_MIP_LEVEL(-62, "if miplevel is greater than zero and the OpenGL implementation does not support creating from non-zero mipmap levels.", "OpenGL-functions"),
    CL_INVALID_GLOBAL_WORK_SIZE(-63, "if global_work_size is NULL, or if any of the values specified in global_work_size[0], �global_work_size [work_dim � 1] are 0 or exceed the range given by the sizeof(size_t) for the device on which the kernel execution will be enqueued."),
    CL_INVALID_PROPERTY(-64, "Vague error, depends on the function", "clCreateContext"),
    CL_INVALID_IMAGE_DESCRIPTOR(-65, "if values specified in image_desc are not valid or if image_desc is NULL.", "clCreateImage"),
    CL_INVALID_COMPILER_OPTIONS(-66, "if the compiler options specified by options are invalid.", "clCompileProgram"),
    CL_INVALID_LINKER_OPTIONS(-67, "if the linker options specified by options are invalid.", "clLinkProgram"),
    CL_INVALID_DEVICE_PARTITION_COUNT(-68, "if the partition name specified in properties is CL_DEVICE_PARTITION_BY_COUNTS and the number of sub-devices requested exceeds CL_DEVICE_PARTITION_MAX_SUB_DEVICES or the total number of compute units requested exceeds CL_DEVICE_PARTITION_MAX_COMPUTE_UNITS for in_device, or the number of compute units requested for one or more sub-devices is less than zero or the number of sub-devices requested exceeds CL_DEVICE_PARTITION_MAX_COMPUTE_UNITS for in_device.", "clCreateSubDevices"),
    CL_INVALID_PIPE_SIZE(-69, "if pipe_packet_size is 0 or the pipe_packet_size exceeds CL_DEVICE_PIPE_MAX_PACKET_SIZE value for all devices in context or if pipe_max_packets is 0.", "clCreatePipe"),
    CL_INVALID_DEVICE_QUEUE(-70, "when an argument is of type queue_t when it�s not a valid device queue object.", "clSetKernelArg"),

    CL_INVALID_GL_SHAREGROUP_REFERENCE_KHR(-1000, "CL and GL not on the same device (only when using a GPU).", "clGetGLContextInfoKHR", "clCreateContext"),
    CL_PLATFORM_NOT_FOUND_KHR(-1001, "No valid ICDs found", "clGetPlatform"),
    CL_INVALID_D3D10_DEVICE_KHR(-1002, "if the Direct3D 10 device specified for interoperability is not compatible with the devices against which the context is to be created.", "clCreateContext", "clCreateContextFromType"),
    CL_INVALID_D3D10_RESOURCE_KHR(-1003, "If the resource is not a Direct3D 10 buffer or texture object", "clCreateFromD3D10BufferKHR", "clCreateFromD3D10Texture2DKHR", "clCreateFromD3D10Texture3DKHR"),
    CL_D3D10_RESOURCE_ALREADY_ACQUIRED_KHR(-1004, "If a mem_object is already acquired by OpenCL", "clEnqueueAcquireD3D10ObjectsKHR"),
    CL_D3D10_RESOURCE_NOT_ACQUIRED_KHR(-1005, "If a mem_object is not acquired by OpenCL", "clEnqueueReleaseD3D10ObjectsKHR"),
    CL_INVALID_D3D11_DEVICE_KHR(-1006, "if the Direct3D 11 device specified for interoperability is not compatible with the devices against which the context is to be created.", "clCreateContext", "clCreateContextFromType"),
    CL_INVALID_D3D11_RESOURCE_KHR(-1007, "If the resource is not a Direct3D 11 buffer or texture object", "clCreateFromD3D11BufferKHR", "clCreateFromD3D11Texture2DKHR", "clCreateFromD3D11Texture3DKHR"),
    CL_D3D11_RESOURCE_ALREADY_ACQUIRED_KHR(-1008, "If a mem_object is already acquired by OpenCL", "clEnqueueAcquireD3D11ObjectsKHR"),
    CL_D3D11_RESOURCE_NOT_ACQUIRED_KHR(-1009, "If a �mem_object� is not acquired by OpenCL", "clEnqueueReleaseD3D11ObjectsKHR"),
    CL_INVALID_D3D9_DEVICE_NVCL_INVALID_DX9_DEVICE_INTEL(-1010, "If the Direct3D 9 device specified for interoperability is not compatible with the devices against which the context is to be created", "clCreateContext", "clCreateContextFromType"),
    CL_INVALID_D3D9_RESOURCE_NVCL_INVALID_DX9_RESOURCE_INTEL(-1011, "If a �mem_object� is not a Direct3D 9 resource of the required type", "clCreateFromD3D9VertexBufferNV", "clCreateFromD3D9IndexBufferNV", "clCreateFromD3D9SurfaceNV", "clCreateFromD3D9TextureNV", "clCreateFromD3D9CubeTextureNV", "clCreateFromD3D9VolumeTextureNV"),
    CL_D3D9_RESOURCE_ALREADY_ACQUIRED_NVCL_DX9_RESOURCE_ALREADY_ACQUIRED_INTEL(-1012, "If any of the �mem_objects� is currently already acquired by OpenCL", "clEnqueueAcquireD3D9ObjectsNV"),
    CL_D3D9_RESOURCE_NOT_ACQUIRED_NVCL_DX9_RESOURCE_NOT_ACQUIRED_INTEL(-1013, "If any of the �mem_objects� is currently not acquired by OpenCL", "clEnqueueReleaseD3D9ObjectsNV"),
    CL_EGL_RESOURCE_NOT_ACQUIRED_KHR(-1092, "If a �mem_object� is not acquired by OpenCL", "clEnqueueReleaseEGLObjectsKHR"),
    CL_INVALID_EGL_OBJECT_KHR(-1093, "If a �mem_object� is not a EGL resource of the required type", "clCreateFromEGLImageKHR", "clEnqueueAcquireEGLObjectsKHR"),
    CL_INVALID_ACCELERATOR_INTEL(-1094, "when �arg_value� is not a valid accelerator object, and by clRetainAccelerator, clReleaseAccelerator, and clGetAcceleratorInfo when �accelerator� is not a valid accelerator object", "clSetKernelArg"),
    CL_INVALID_ACCELERATOR_TYPE_INTEL(-1095, "when �arg_value� is not an accelerator object of the correct type, or when �accelerator_type� is not a valid accelerator type", "clSetKernelArg", "clCreateAccelerator"),
    CL_INVALID_ACCELERATOR_DESCRIPTOR_INTEL(-1096, "when values described by �descriptor� are not valid, or if a combination of values is not valid", "clCreateAccelerator"),
    CL_ACCELERATOR_TYPE_NOT_SUPPORTED_INTEL(-1097, "when �accelerator_type� is a valid accelerator type, but it not supported by any device in �context�", "clCreateAccelerator"),
    CL_INVALID_VA_API_MEDIA_ADAPTER_INTEL(-1098, "If the VA API display specified for interoperability is not compatible with the devices against which the context is to be created", "clCreateContext", "clCreateContextFromType"),
    CL_INVALID_VA_API_MEDIA_SURFACE_INTEL(-1099, "If �surface� is not a VA API surface of the required type, by clGetMemObjectInfo when �param_name� is CL_MEM_VA_API_MEDIA_SURFACE_INTEL when was not created from a VA API surface, and from clGetImageInfo when �param_name� is CL_IMAGE_VA_API_PLANE_INTEL and �image� was not created from a VA API surface", "clEnqueueReleaseVA_APIMediaSurfacesINTEL"),
    CL_VA_API_MEDIA_SURFACE_ALREADY_ACQUIRED_INTEL(-1100, "If any of the �mem_objects� is already acquired by OpenCL", "clEnqueueReleaseVA_APIMediaSurfacesINTEL"),
    CL_VA_API_MEDIA_SURFACE_NOT_ACQUIRED_INTEL(-1101, "If any of the �mem_objects� are not currently acquired by OpenCL", "clEnqueueReleaseVA_APIMediaSurfacesINTEL"),

    ;

    public static final int SUCCESS = CL_SUCCESS.code;

    private final int code;
    private final @NotNull String description;
    private final @NotNull String @NotNull [] functions;

    CLStatus(int code, @NotNull String description, @NotNull String @NotNull ... functions) {
        this.code = code;
        this.description = description;
        this.functions = functions;
    }

    public int getCode() {
        return code;
    }

    public @NotNull String getDescription() {
        return description;
    }

    public @NotNull String @NotNull [] getFunctions() {
        return functions;
    }

    public static CLStatus fromStatus(int code) {
        for(CLStatus c : CLStatus.values()) {
            if(c.code == code) return c;
        }

        throw new IllegalArgumentException("Unknown OpenCL Error Code: " + code);
    }

    public static void check(int code) {
        if(code != SUCCESS)
            throw new CLException(code);
    }

    @Override
    public String toString() {
        return name() + " " + code + ": " + getDescription();
    }

    /**
     * main to generate enum
     */
    public static void main(String[] args) {
        String source =
                """
Copy Paste Table Here ...
                        """;

        String[] lines = source.split("\n");

        for(String line : lines) {
            String[] fields = line.split("\t");

            String code = fields[0].trim();
            String name = fields[1].trim();
            String functionsString = fields[2].trim();
            String description = fields[3].trim();

            String[] functions = functionsString.split(", ");

            StringBuilder sb = new StringBuilder();
            sb.append(name.replaceAll(" ", "")).append("(").append(code).append(", ");
            sb.append("\"").append(description).append("\"");

            for(String func : functions) {
                if(func.isBlank()) continue;
                sb.append(", \"").append(func).append("\"");
            }

            sb.append("),");
            System.out.println(sb);
        }

    }
}
