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

## Vulkan

### Vulkan Validation Layers
The environment variable `VK_ADD_LAYER_PATH` must contain the path
to the validation layer installation: `vulkan-validationlayers_x64-windows/bin`