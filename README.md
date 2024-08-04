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

### Your first Scene
