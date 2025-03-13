# TODO
this file contains todos and notes about this project.

## cmake-presets release/debug for linux

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

## Glfw-Stuff
- Get Monitor(s) (size, which is main)

## TODO: Quality of life changes to generated code
- auto escaped generated java doc.
- none