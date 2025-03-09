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
 - LunarG Vulkan SDK and Vulkan Configurator.
   - Validation Layers can be enabled globally in the Vulkan Configurator.

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
gradlew genFromVulkanXML
```
Now run the gradle task `compileJava` to generate all c-headers as a by-product
```shell
gradlew compileJava
```
Now you can run the cmake to create the `cmake-build` directory:
```shell
cmake --preset=default
```
Finally, you can run the build
```shell
cmake --build cmake-build
```
TODO: cmake relase/debug for linux/windows presets -> CMAKE_BUILD_TYPE




# Old Stuff below

### VCPKG
This Additional Cmake Option must be set:
```
-DCMAKE_TOOLCHAIN_FILE=<path-to-vcpkg>/scripts/buildsystems/vcpkg.cmake
```
This option can be set in
`File` -> `Settings` -> `Build, Execution, Deployment`-> `CMake` -> `Profiles` -> `CMake options`.

### Building the Project
The project must be build in three steps:
- Generate the vulkan related sources from the vk.xml file
- Generate the native jnilib
- Compile Java

The easiest way to accomplish this is:
- Run any existing java test case inside this project (you will likely get an UnsatisfiedLinkError at runtime. 
  This error can be ignored), to generate the vulkan related sources
- Run the CMakeLists.txt and build the native library
- Compile Java
  

## Vulkan

### Vulkan Validation Layers
Install LunarG Vulkan SDK and Vulkan Configurator.
Validation Layers can be enabled globally in the Vulkan Configurator.