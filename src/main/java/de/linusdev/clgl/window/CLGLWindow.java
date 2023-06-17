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

package de.linusdev.clgl.window;

import de.linusdev.clgl.api.misc.annos.CallOnlyFromUIThread;
import de.linusdev.clgl.api.structs.StructureArray;
import de.linusdev.clgl.api.types.bytebuffer.BBInt2;
import de.linusdev.clgl.api.types.bytebuffer.BBLong2;
import de.linusdev.clgl.nat.cl.CL;
import de.linusdev.clgl.nat.cl.objects.*;
import de.linusdev.clgl.nat.cl.structs.CLImageDesc;
import de.linusdev.clgl.nat.cl.structs.CLImageFormat;
import de.linusdev.clgl.nat.glad.Glad;
import de.linusdev.clgl.nat.glad.objects.GLFrameBuffer;
import de.linusdev.clgl.nat.glad.objects.GLRenderBuffer;
import de.linusdev.clgl.nat.glfw3.custom.FrameInfo;
import de.linusdev.clgl.nat.glfw3.custom.UpdateListener;
import de.linusdev.clgl.nat.glfw3.objects.GLFWWindow;
import de.linusdev.clgl.window.input.InputManagerImpl;
import de.linusdev.clgl.window.input.InputManger;
import de.linusdev.clgl.window.queue.UITaskQueue;
import de.linusdev.lutils.async.Future;
import de.linusdev.lutils.async.Nothing;
import de.linusdev.lutils.async.Task;
import de.linusdev.lutils.async.exception.NonBlockingThreadException;
import de.linusdev.lutils.async.manager.AsyncManager;
import de.linusdev.lutils.bitfield.LongBitfield;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static de.linusdev.clgl.nat.glad.GLConstants.*;

@SuppressWarnings("unused")
public class CLGLWindow implements UpdateListener, AsyncManager, AutoCloseable {

    public static final int UPDATE_SHARED_FRAMEBUFFER_TASK_ID = UITaskQueue.getUniqueTaskId("UPDATE_SHARED_FRAMEBUFFER");

    protected final @NotNull GLFWWindow glfwWindow;
    protected final @NotNull InputManger inputManger;

    //Window stuff
    protected final @NotNull BBInt2 size;

    protected @Nullable Thread uiThread = null;

    //Task queue
    protected final @NotNull UITaskQueue uiTaskQueue;

    //OpenCL
    protected final int openClVersion; //1,2 or 3
    protected final Context clContext;
    protected final Device clDevice;
    protected final CommandQueue clQueue;
    protected @Nullable Kernel renderKernel;
    protected @Nullable Kernel uiKernel;
    protected final @NotNull BBLong2 globalWorkSize;

    //Shared framebuffer
    protected GLFrameBuffer glFrameBuffer;
    protected GLRenderBuffer glRenderBuffer;
    protected MemoryObject sharedRenderBuffer;
    protected StructureArray<MemoryObject> glObjects;

    //UI image
    protected final @NotNull CLImageFormat uiImageFormat;
    protected final @NotNull CLImageDesc uiImageDescription;
    protected final @NotNull MemoryObject uiImageBuffer;


    public CLGLWindow(long maxQueuedTaskMillisPerFrame) {
        this.glfwWindow = new GLFWWindow();
        this.glfwWindow.enableGLDebugMessageListener((source, type, id, severity, message, userParam) ->
                System.out.println("OpenGl Debug Message: " + message));

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
            if(plat == null || (devs = plat.getDevices(CL.DeviceType.CL_DEVICE_TYPE_GPU)).size() == 0) {
                throw new UnsupportedOperationException("This computer does not have a GPU supporting OpenCL 2 or 3.");
            }

            clContext = Context.createCLGLSharedContext(clDevice = devs.get(0));
            clQueue = new CommandQueue(clContext, clDevice);
        }


        //Create gl objects array
        glObjects = new StructureArray<>(false, MemoryObject.INFO, 1, (parent, offset) -> null);


        //Read window size
        size = glfwWindow.getFrameBufferSize(null);
        globalWorkSize = new BBLong2(true);
        globalWorkSize.xy(size.x(), size.y()); //always holds the same value as size but as long.


        //Create renderbuffer for rendering
        glFrameBuffer = new GLFrameBuffer();
        glRenderBuffer = new GLRenderBuffer(GL_RGBA32F, size.x(), size.y());
        glFrameBuffer.addRenderBuffer(glRenderBuffer, GL_COLOR_ATTACHMENT0, GL_RENDERBUFFER);
        Glad.glFinish();

        sharedRenderBuffer = new MemoryObject(false);
        glObjects.set(0, sharedRenderBuffer);
        sharedRenderBuffer.fromGLRenderBuffer(
                clContext, new LongBitfield<>(CL.CLMemFlag.CL_MEM_WRITE_ONLY), glRenderBuffer);

        //Create ui image buffer
        uiImageFormat = new CLImageFormat(CL.CLChannelOrder.CL_RGBA, CL.CLChannelType.CL_FLOAT);
        uiImageDescription = new CLImageDesc(
                CL.CLMemoryObjectType.CL_MEM_OBJECT_IMAGE2D,
                globalWorkSize.x(), globalWorkSize.y(),
                0L, 0L, 0L, 0L, 0, 0, null
        );

        uiImageBuffer = new MemoryObject(true);
        uiImageBuffer.newCLImage(clContext,
                new LongBitfield<>(CL.CLMemFlag.CL_MEM_READ_WRITE, CL.CLMemFlag.CL_MEM_HOST_NO_ACCESS),
                uiImageFormat, uiImageDescription, null
        );

        glfwWindow.addFramebufferSizeListener((window, width, height) ->
                uiTaskQueue.queueForExecution(UPDATE_SHARED_FRAMEBUFFER_TASK_ID, window1 ->
                        {
                            window1.updateSharedFramebuffer();
                            return Nothing.INSTANCE;
                        }
                ));
    }

    @Override
    public void update(@NotNull GLFWWindow window, @NotNull FrameInfo frameInfo) {
        uiTaskQueue.runQueuedTasks();

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

    public void setRenderKernel(@NotNull Kernel kernel) {
        this.renderKernel = kernel;

        kernel.setKernelArg(0, sharedRenderBuffer);
        kernel.setKernelArg(1, size);
        kernel.setKernelArg(2, uiImageBuffer);
    }

    public void setUiKernel(@NotNull Kernel kernel) {
        this.uiKernel = kernel;

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
                clContext, new LongBitfield<>(CL.CLMemFlag.CL_MEM_WRITE_ONLY), glRenderBuffer);

        uiImageDescription.image_width.set(globalWorkSize.x());
        uiImageDescription.image_height.set(globalWorkSize.y());

        uiImageBuffer.newCLImage(clContext,
                new LongBitfield<>(CL.CLMemFlag.CL_MEM_READ_WRITE, CL.CLMemFlag.CL_MEM_HOST_NO_ACCESS),
                uiImageFormat, uiImageDescription, null
        );

        if(renderKernel != null)
            setRenderKernel(renderKernel);

        if(uiKernel != null)
            setUiKernel(uiKernel);
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

    public Context getClContext() {
        return clContext;
    }

    public Device getClDevice() {
        return clDevice;
    }

    public @NotNull GLFWWindow getGlfwWindow() {
        return glfwWindow;
    }

    public @NotNull InputManger getInputManger() {
        return inputManger;
    }

    public @NotNull UITaskQueue getUiTaskQueue() {
        return uiTaskQueue;
    }
}
