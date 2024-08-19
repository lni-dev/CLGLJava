/*
 * Copyright (c) 2023 Linus Andera
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.linusdev.cvg4j.window;

import de.linusdev.cvg4j.api.misc.annos.CallOnlyFromUIThread;
import de.linusdev.cvg4j.nat.cl.CL;
import de.linusdev.cvg4j.nat.cl.objects.*;
import de.linusdev.cvg4j.nat.cl.structs.CLImageDesc;
import de.linusdev.cvg4j.nat.cl.structs.CLImageFormat;
import de.linusdev.cvg4j.nat.glad.Glad;
import de.linusdev.cvg4j.nat.glad.GladInitException;
import de.linusdev.cvg4j.nat.glad.objects.GLFrameBuffer;
import de.linusdev.cvg4j.nat.glad.objects.GLRenderBuffer;
import de.linusdev.cvg4j.nat.glfw3.custom.FrameInfo;
import de.linusdev.cvg4j.nat.glfw3.custom.UpdateListener;
import de.linusdev.cvg4j.nat.glfw3.exceptions.GLFWException;
import de.linusdev.cvg4j.nat.glfw3.objects.GLFWWindow;
import de.linusdev.cvg4j.nat.glfw3.objects.OpenGLWindow;
import de.linusdev.cvg4j.window.args.AutoUpdateArgManager;
import de.linusdev.cvg4j.window.args.KernelView;
import de.linusdev.cvg4j.window.input.InputManagerImpl;
import de.linusdev.cvg4j.window.input.InputManger;
import de.linusdev.cvg4j.window.queue.UITaskQueue;
import de.linusdev.lutils.async.Future;
import de.linusdev.lutils.async.Nothing;
import de.linusdev.lutils.async.Task;
import de.linusdev.lutils.async.exception.NonBlockingThreadException;
import de.linusdev.lutils.async.manager.AsyncManager;
import de.linusdev.lutils.bitfield.LongBitfieldImpl;
import de.linusdev.lutils.math.vector.buffer.intn.BBInt2;
import de.linusdev.lutils.math.vector.buffer.longn.BBLong2;
import de.linusdev.lutils.nat.abi.DefaultABIOverwrites;
import de.linusdev.lutils.nat.struct.annos.SVWrapper;
import de.linusdev.lutils.nat.struct.array.StructureArray;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static de.linusdev.cvg4j.nat.glad.GLConstants.*;
import static de.linusdev.cvg4j.nat.glad.Glad.glClear;

@SuppressWarnings("unused")
public class CLGLWindow implements UpdateListener<GLFWWindow>, AsyncManager, AutoCloseable {

    public static final int UPDATE_SHARED_FRAMEBUFFER_TASK_ID = UITaskQueue.getUniqueTaskId("UPDATE_SHARED_FRAMEBUFFER");

    protected final @NotNull OpenGLWindow glfwWindow;
    protected final @NotNull InputManger inputManger;

    //Window stuff
    protected final @NotNull BBInt2 size;

    protected @Nullable Thread uiThread = null;
    protected @NotNull Handler handler;

    //Task queue
    protected final @NotNull UITaskQueue uiTaskQueue;

    //OpenCL
    protected final int openClVersion; //1,2 or 3
    protected final @NotNull Context clContext;
    protected final @NotNull Device clDevice;
    protected final @NotNull CommandQueue clQueue;
    protected @Nullable Kernel renderKernel;
    protected @Nullable Kernel uiKernel;
    protected final @NotNull BBLong2 globalWorkSize;
    protected final @NotNull AutoUpdateArgManager argumentManager;

    //Shared framebuffer
    protected GLFrameBuffer glFrameBuffer;
    protected GLRenderBuffer glRenderBuffer;
    protected MemoryObject sharedRenderBuffer;
    protected StructureArray<MemoryObject> glObjects;

    //UI image
    protected final @NotNull CLImageFormat uiImageFormat;
    protected final @NotNull CLImageDesc uiImageDescription;
    protected final @NotNull MemoryObject uiImageBuffer;


    public CLGLWindow(@NotNull Handler handler, long maxQueuedTaskMillisPerFrame) throws GLFWException, GladInitException {
        this.glfwWindow = new OpenGLWindow(null);
        this.glfwWindow.enableGLDebugMessageListener((source, type, id, severity, message, userParam) ->
                System.out.println("OpenGl Debug Message (source=" + source + ", type=" + type + "): " + message));

        this.handler = handler;

        //Task queue
        this.uiTaskQueue = new UITaskQueue(this, maxQueuedTaskMillisPerFrame);

        //Input manager
        this.inputManger = new InputManagerImpl(glfwWindow);

        //Create OpenCL Context
        {
            int ver = 0;
            Platform plat = null;
            for(Platform p : Platform.getPlatforms()) {
                String version = p.getPlatformVersion();

                if(version.contains("OpenCL 2.")) {
                    ver = 2;
                    plat = p;
                } else if (version.contains("OpenCL 3.")) {
                    ver = 3;
                    plat = p;
                    break;
                }
            }

            this.openClVersion = ver;
            List<Device> devs;
            if(plat == null || (devs = plat.getDevices(CL.DeviceType.CL_DEVICE_TYPE_GPU)).isEmpty()) {
                throw new UnsupportedOperationException("This computer does not have a GPU supporting OpenCL 2 or 3.");
            }

            clContext = Context.createCLGLSharedContext(clDevice = devs.get(0));
            clQueue = new CommandQueue(clContext, clDevice);
        }


        //Create gl objects array
        glObjects = StructureArray.newAllocated(
                true,
                SVWrapper.of(1, MemoryObject.class, DefaultABIOverwrites.CVG4J_OPEN_CL),
                null,
                () -> null
        );


        //Read window size
        size = glfwWindow.getFrameBufferSize(null);
        globalWorkSize = BBLong2.newAllocated(null);
        globalWorkSize.xy(size.x(), size.y()); //always holds the same value as size but as long.


        //Create renderbuffer for rendering
        glFrameBuffer = new GLFrameBuffer();
        glRenderBuffer = new GLRenderBuffer(GL_RGBA32F, size.x(), size.y());
        glFrameBuffer.addRenderBuffer(glRenderBuffer, GL_COLOR_ATTACHMENT0, GL_RENDERBUFFER);
        Glad.glFinish();

        sharedRenderBuffer = MemoryObject.newUnallocated();
        glObjects.set(0, sharedRenderBuffer);
        sharedRenderBuffer.fromGLRenderBuffer(
                clContext, new LongBitfieldImpl<>(CL.CLMemFlag.CL_MEM_WRITE_ONLY), glRenderBuffer);

        //Create ui image buffer
        uiImageFormat = new CLImageFormat(CL.CLChannelOrder.CL_RGBA, CL.CLChannelType.CL_FLOAT);
        uiImageDescription = new CLImageDesc(
                CL.CLMemoryObjectType.CL_MEM_OBJECT_IMAGE2D,
                globalWorkSize.x(), globalWorkSize.y(),
                0L, 0L, 0L, 0L, 0, 0, null
        );

        uiImageBuffer = MemoryObject.newAllocated(null);
        uiImageBuffer.newCLImage(clContext,
                new LongBitfieldImpl<>(CL.CLMemFlag.CL_MEM_READ_WRITE, CL.CLMemFlag.CL_MEM_HOST_NO_ACCESS),
                uiImageFormat, uiImageDescription, null
        );

        glfwWindow.listeners().addFramebufferSizeListener((width, height) ->
                uiTaskQueue.queueForExecution(UPDATE_SHARED_FRAMEBUFFER_TASK_ID, () ->
                        {
                            this.updateSharedFramebuffer();
                            return Nothing.INSTANCE;
                        }
                ));

        argumentManager = new AutoUpdateArgManager();
    }

    @Override
    public void update(@NotNull FrameInfo frameInfo) {
        //clear screen
        glClear(GL_COLOR_BUFFER_BIT);

        uiTaskQueue.runQueuedTasks();

        argumentManager.check();

        handler.update0(this, frameInfo);

        if(renderKernel != null) {
            clQueue.enqueueAcquireGLObjects(glObjects, null, null);
            clQueue.enqueueNDRangeKernel(renderKernel, 2, null, globalWorkSize,
                    null, null, null);
            clQueue.enqueueReleaseGLObjects(glObjects, null, null);

            clQueue.finish();

            if(uiKernel != null) {
                clQueue.enqueueNDRangeKernel(uiKernel, 2, null, globalWorkSize,
                        null, null, null);
            }

            glFrameBuffer.blitInto(GLFrameBuffer.DEFAULT_FRAME_BUFFER, glRenderBuffer, glRenderBuffer,
                    GL_COLOR_BUFFER_BIT, GL_NEAREST);

            clQueue.finish();
        }

    }

    @CallOnlyFromUIThread("glfw")
    public void clearKernels() {
        this.renderKernel = null;
        this.uiKernel = null;
        this.argumentManager.reset();
    }

    @CallOnlyFromUIThread("glfw")
    public void setRenderKernel(@Nullable Kernel kernel) {
        if(this.renderKernel != null)
            throw new IllegalStateException("clearKernels must be called before setting the kernels again.");
        if(kernel == null)
            return;

        this.renderKernel = kernel;

        setBaseRenderKernelArgs(kernel);

        handler.setRenderKernelArgs(new KernelView(renderKernel, this, argumentManager));
    }

    protected void setBaseRenderKernelArgs(@NotNull Kernel kernel) {
        kernel.setKernelArg(0, sharedRenderBuffer);
        kernel.setKernelArg(1, size);
        kernel.setKernelArg(2, uiImageBuffer);
    }

    @CallOnlyFromUIThread("glfw")
    public void setUiKernel(@Nullable Kernel kernel) {
        if(this.uiKernel != null)
            throw new IllegalStateException("clearKernels must be called before setting the kernels again.");
        if(kernel == null)
            return;

        this.uiKernel = kernel;

        setBaseUIKernelArgs(kernel);

        handler.setUIKernelArgs(new KernelView(uiKernel, this, argumentManager));
    }

    protected void setBaseUIKernelArgs(@NotNull Kernel kernel) {
        kernel.setKernelArg(0, uiImageBuffer);
        kernel.setKernelArg(1, size);
    }

    @CallOnlyFromUIThread(value = "glfw", creates = true, claims = true)
    public void show() {
        glfwWindow.show(this);
    }

    @CallOnlyFromUIThread("glfw")
    protected void updateSharedFramebuffer() {
        glfwWindow.getFrameBufferSize(size);
        globalWorkSize.xy(size.x(), size.y()); //always holds the same value as size but as long.
        glRenderBuffer.changeSize(size.x(), size.y());

        sharedRenderBuffer.fromGLRenderBuffer(
                clContext, new LongBitfieldImpl<>(CL.CLMemFlag.CL_MEM_WRITE_ONLY), glRenderBuffer);

        uiImageDescription.image_width.set(globalWorkSize.x());
        uiImageDescription.image_height.set(globalWorkSize.y());

        uiImageBuffer.newCLImage(clContext,
                new LongBitfieldImpl<>(CL.CLMemFlag.CL_MEM_READ_WRITE, CL.CLMemFlag.CL_MEM_HOST_NO_ACCESS),
                uiImageFormat, uiImageDescription, null
        );

        if(renderKernel != null)
            setBaseRenderKernelArgs(renderKernel);

        if(uiKernel != null)
            setBaseUIKernelArgs(uiKernel);
    }

    @Override
    public void checkThread() throws NonBlockingThreadException {
        if(Thread.currentThread() == uiThread)
            throw new NonBlockingThreadException();
    }

    @Override
    public void onExceptionInListener(@NotNull Future<?, ?> future, @Nullable Task<?, ?> task, @NotNull Throwable throwable) {
        System.err.println("Exception in a future listener!");
        throwable.printStackTrace();
    }

    @Override
    public void close() {
        clContext.close();
        clQueue.close();
        glRenderBuffer.close();
        glFrameBuffer.close();
        glfwWindow.close();

        uiImageBuffer.close();
        sharedRenderBuffer.close();
    }

    //Getter

    public @NotNull Context getClContext() {
        return clContext;
    }

    public @NotNull Device getClDevice() {
        return clDevice;
    }

    public @NotNull GLFWWindow getGlfwWindow() {
        return glfwWindow;
    }

    public @NotNull CommandQueue getClQueue() {
        return clQueue;
    }

    public @NotNull InputManger getInputManger() {
        return inputManger;
    }

    public @NotNull UITaskQueue getUiTaskQueue() {
        return uiTaskQueue;
    }
}
