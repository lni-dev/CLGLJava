# Setup

## Build

### VCPKG Packages
All packages must be installed with the `--triplet x64-windows` option
- Vulkan
- OpenGl
- OpenCl

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