# CVG4J
CLGLJava is a library to create opencl-rendering windows in Java.
Each Window has two Kernels, one for the UI and one for the actual rendering.

## Getting Started

### Getting an Engine Instance
The  `Engine` is the main class which manages the scene, window, kernels, etc.
Getting an `Engine` instance is the first step:
```java
TestGame yourGame = new TestGame();
Engine<TestGame> engine = Engine.getInstance(yourGame);
```
Note that the engine has a generic G, which should be a class containing core information of your Game or Application.
It must implement the `Game` interface. In this example the class is called `TestGame`. The instance of this class will always be easily accessable through the `Engine` or `Scene`.

## Scene

### State
A scene can be in different states, each state different listeners will be called.

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
    - This state will be entered, when `LoadedScene.activate()` is called.
    - The scene will be ticked.
    - The scene will be rendered. This means its `render()` method will be called when rendering is executed.
- RELEASING
    - The scene will not be used anymore and can now release its resources.
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
