cmake_minimum_required(VERSION 3.25)
project(CLGLJava VERSION 1.0.0 DESCRIPTION "OpenCL Rendering Library")

include(FetchContent)
include(UseJava)

set(CMAKE_CXX_STANDARD 20)
option(BUILD_SHARED_LIBS ON)

add_link_options(-static-libgcc -static-libstdc++ -static)

add_subdirectory(libs/glad)

add_library(${PROJECT_NAME} STATIC MODULE
        src/main/cpp/clgl_Load.cpp
        src/main/cpp/clgl_Glad.cpp
        src/main/cpp/clgl_GLFW.cpp
        src/main/cpp/clgl_CL.cpp
        src/main/cpp/clgl_NativeUtils.cpp
        src/main/cpp/clgl_WGL.cpp
        src/main/cpp/clgl_Vulkan.cpp
        src/main/cpp/JniUtils.cpp
)

set_target_properties(${PROJECT_NAME} PROPERTIES
        VERSION ${PROJECT_VERSION}
        SOVERSION 1
        SUFFIX ".jnilib"
        )

#dependencies
find_package(Java REQUIRED)
find_package(JNI REQUIRED)
find_package(OpenCL REQUIRED)
find_package(OpenGL REQUIRED)
find_package(Vulkan REQUIRED)
FetchContent_Declare(
        glfw3
        GIT_REPOSITORY https://github.com/glfw/glfw.git
)
FetchContent_MakeAvailable(glfw3)

target_link_libraries(${PROJECT_NAME}
        PRIVATE ${JNI_LIBRARIES}
        PUBLIC OpenCL::OpenCL
        PRIVATE OpenGL::GL
        PUBLIC glfw
        PUBLIC glad
        PRIVATE Vulkan::Vulkan
)

target_include_directories(${PROJECT_NAME} PUBLIC ${JNI_INCLUDE_DIRS} ${_classDir} ${_stubDir} include)