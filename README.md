# Engine

## Scene

### State
A scene can be in different states, each state different listeners will be called.

#### CREATED
- This is the initial state a scene is in, when it is created using its clazz constructor. No scene state change event will be transmitted, when this state is entered.
- No data should be loaded in this state except what is required to start loading the scene.
#### LOADING
- This state is entered, when `Engine.loadScene()` is called.
- This state should be used for loading memory, assets required when rendering the scene.
- Each scene has a function `loader()` which returns a `Loader` object ready to load this scene.
- During loading, the returned loader will be ticked by the `Ticker`.
- During loading the loader should update its current loading progress.
#### LOADED
- The loader will still be ticked.
- The scene is ready to be rendered.
#### RENDERING
- This state will be entered, when `LoadedScene.activate()` is called.
- The scene will be ticked.
- The scene will be rendered. This means its `render()` method will be called when rendering is executed.
#### RELEASING
- The scene will not be used anymore and can now release its resources.
- The `releaser()` method will be called and should return a `Loader` to release this scene
- The scene will not be ticked any longer.
- The releaser will be ticked.
- The releaser may update its current releasing progress.
#### CLOSED
- This scene is closed and can safely be discarded.

### Loader
Each scene must have a `loader()` method, which supplies a `Loader` to load the scene.<br>
Public Methods:
- `load()`: Start loading.
- `tick()`: Tick method
- `progress()`: Method that returns the current loading progress, must be thread safe.

## VulkanEngine

### Threads
The engine has two main threads, a window-thread for glfw related event polling and a render-thread for the actual
rendering.

### Death
When the engine is supposed to die (e.g. user quits), the window-thread will transmit a close event. The render-thread 
will listen to this event and delay the window close, until the already submitted render operations are completed and
the renderer has successfully closed. The engine will wait until the render-thread has died and then close its
resources. During this close operation, the engine will also wait the render-thread until the window thread has died,
if it did not already die. Thus the engine death is the same as the death of the render thread.