# TODO
this file contains todos and notes about this project.

## Building the Project
currently the Project has to be built in several steps. Ideally, these would be automatically run
through gradle. (That means cmake build should be executed by the gradle build script). Native libs
should not be copied to resources, but instead into generated sources by that cmake task and then included
like we are doing with the spir-v shader binary.

## Project Name
Rename the cmake project to CVG4J.

## Native Objects
Allocate one big chunk of memory for permanent objects instead of using Structure.allocate().

## Structure
enable closing of a struct e.g. setting its bytebuf to null

## VkMemoryAllocator
As described in the bottom of https://vulkan-tutorial.com/Vertex_buffers/Staging_buffer.
It is bad to allocate memory for every buffer individually. Instead, allocate one buffer for all
buffers with the same memory type and use the offset when binding the memory to the buffers.
Even use a single VkBuffer!
https://developer.nvidia.com/vulkan-memory-management

## Autocloseable fix 
All objects implementing auto closable should check if they actually need closing and what has to be closed.

## TODO: Quality of life changes to generated code
- none