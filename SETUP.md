# Setup
This document describes the required programs and how to build this library.

## Requirements
 - [Java JDK](https://openjdk.org/install/). Must be added to PATH and JAVA_HOME must be set.
 - [CMake](https://cmake.org/download/) version 4.0.0 or higher. Must be added to PATH.
 - [VCPKG](https://github.com/microsoft/vcpkg). see [Installing VCPKG](#installing-vcpkg).
 - [Ninja](https://ninja-build.org/). see [Installing Ninja](#installing-ninja).
 - On Debian-based Linux:
   - XCB dev package `sudo apt install libxcb1-dev`
   - X11 dev package `sudo apt install libx11-dev`
   - XRANDR dev package `sudo apt install libxrandr-dev`
   - GL dev package`sudo apt-get install libgl1-mesa-dev` (Required only for OpenGl)
   - [GLFW3 requirements](https://www.glfw.org/docs/latest/compile_guide.html#compile_deps_wayland) `sudo apt install libwayland-dev libxkbcommon-dev xorg-dev`
 - Vulkan validation layers and glslc:
   - [LunarG Vulkan SDK and Vulkan Configurator](https://vulkan.lunarg.com/)
   - glslc is included in the vulkan sdk
   - Validation layers can then be enabled globally in the `Vulkan Configurator`

### Installing VCPKG
First go to the directory where you want to install vcpkg. Then run
```shell
git clone https://github.com/microsoft/vcpkg.git
cd vcpkg

.\bootstrap-vcpkg.bat #on windows

chmod +x bootstrap-vcpkg.sh #on linux
./bootstrap-vcpkg.sh        #on linux
```
Set the following environment variables. On windows using the gui. on linux add it to `.bashrc`:
```shell
export VCPKG_ROOT=C:\path\to\vcpkg
export PATH=$VCPKG_ROOT:$PATH
```
Remember environment variable expansion in windows uses `%PATH%` instead of `$PATH`.

### Installing Ninja
Download the binary from [here](https://github.com/ninja-build/ninja/releases). Extract it and
add it to the PATH environment variables:
```shell
export PATH=/path/to/ninja-dir/:$PATH
```

## Build

### Manual Cmake Build
First you need to run the gradle task `genFromVulkanXML`, which will generate
the native Java mappings including the cpp code:
```shell
gradlew vulkanWrapper
```
Now run the gradle task `compileJava` to generate all c-headers as a by-product
```shell
gradlew compileJava
```
Now you can run the cmake to create the `cmake-build` directory:
```shell
cmake --preset=msvc
```
or on linux:
```shell
cmake --preset=gcc-debug
```
Finally, you can run the build
```shell
cmake --build cmake-build/msvc --config Debug
```
or on linux:
```shell
cmake --build build/cmake/gcc-debug
```