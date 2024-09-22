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

## Scene loading
Allow loading of a new scene after the first has been loaded.

## Scene

### State
A scene can be in different states, each state different listeners will be called. When a scene changes its state, a scene state change event will be transmitted.

- CREATED
  - This is the initial state a scene is in, when it is created using its clazz constructor. No scene state change event will be transmitted, when this state is entered.
  - No data should be loaded in this state except what is required to start loading the scene.
- LOADING
  - This state is entered, when `Engine.loadScene()` is called.
  - This state should be used for loading memory, assets required when rendering the scene.
  - Each scene has a function `loader()` which returns a `Loader` object ready to load this scene.
  - During loading, the returned loader will be ticked by the `Ticker`.
  - During loading the loader should update its current loading progress.
- LOADED
  - The loader will still be ticked.
  - The scene is ready to be rendered.
- RENDERING
  - The scene will be ticked.
  - The scene will be rendered. This means its `render` method will called when rendering is executed.
- RELEASING
  - The scene will not be used anymore and will can now release its resources.
  - The `releaser()` method will be called and should return a `Loader` to release this scene
  - The scene will not be ticked any longer.
  - The releaser will be ticked.
  - The releaser may update its current releasing progress.
- CLOSED
  - This scene is closed and can safely be discarded.

### Loader
Each scene must have a `loader()` method, which supplies a `Loader` to load the scene.<br>
Public Methods:
- `load()`: Start loading.
- `tick()`: Tick method
- `progress()`: Method that returns the current loading progress, must be thread safe.

## TODO: Quality of life changes to generated code
- none