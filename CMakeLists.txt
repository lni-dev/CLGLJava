cmake_minimum_required(VERSION 4.0.0)
project(ljgel VERSION 1.0.0 DESCRIPTION "LinusDev's Java Game Engine Library")

include(FetchContent)
include(UseJava)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Dependencies

# External dependencies
find_package(Java REQUIRED)
find_package(JNI REQUIRED)
find_package(OpenCL REQUIRED)
find_package(OpenGL REQUIRED)
find_package(Vulkan REQUIRED)
FetchContent_Declare(glfw3 GIT_REPOSITORY https://github.com/glfw/glfw.git)
FetchContent_MakeAvailable(glfw3)

# Glad
add_subdirectory(libs/glad)

# Define source code
add_library(${PROJECT_NAME} SHARED
        src/main/cpp/Load.cpp
        src/main/cpp/Glad.cpp
        src/main/cpp/GLFW.cpp
        src/main/cpp/CL.cpp
        src/main/cpp/NativeUtils.cpp

        src/main/cpp/JniUtils.cpp
        build/generated/sources/vulkan/cpp/NativeFunctions.cpp
        src/main/cpp/VulkanNatDebugUtilsMessageCallback.cpp
)

if (WIN32)
    # Windows only sources:
    target_sources(${PROJECT_NAME} PRIVATE src/main/cpp/WGL.cpp)
endif()

# Define properties
set_target_properties(${PROJECT_NAME} PROPERTIES VERSION ${PROJECT_VERSION})
set_target_properties(${PROJECT_NAME} PROPERTIES SOVERSION 1)

if (APPLE)
    set_target_properties(${PROJECT_NAME} PROPERTIES SUFFIX ".jnilib")
elseif (WIN32)
    set_target_properties(${PROJECT_NAME} PROPERTIES SUFFIX ".dll")
else()
    set_target_properties(${PROJECT_NAME} PROPERTIES SUFFIX ".so")
endif()

# Include directories
target_include_directories(${PROJECT_NAME} PUBLIC
        ${JNI_INCLUDE_DIRS}
        ${_classDir}
        ${_stubDir}
        include/
        build/generated/sources/headers/java/main
)

# Link Libraries
target_link_libraries(${PROJECT_NAME}
        PUBLIC ${JNI_LIBRARIES}
        PRIVATE OpenCL::OpenCL
        PRIVATE OpenGL::GL
        PRIVATE Vulkan::Vulkan
        PUBLIC glfw
        PUBLIC glad
)