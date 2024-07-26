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

package de.linusdev.cvg4j.nat.glfw3.custom;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class FrameInfoImpl implements FrameInfo {
    private final int averageOverFrames;
    private @Nullable UpdateListener listener;

    private volatile double averageMillisBetweenFrames;

    long frameMillisSum = 0;

    int frameCount = 0;

    private volatile double deltaTime = 1d / 60d;

    public FrameInfoImpl(int averageOverFrames) {
        this.averageOverFrames = averageOverFrames;
    }

    public void setListener(@Nullable UpdateListener listener) {
        this.listener = listener;
    }

    public void submitFrame(long millis) {

        deltaTime = (deltaTime + ((double) millis) / 1000d) / 2d;

        frameMillisSum += millis;

        if(frameCount++ >= averageOverFrames) {
            averageMillisBetweenFrames = frameMillisSum / (double) frameCount;

            frameCount = 0;
            frameMillisSum = 0;

            if(listener != null)
                listener.onUpdate(this);
        }
    }

    @Override
    public double getFPS() {
        return 1000d / averageMillisBetweenFrames;
    }

    @Override
    public double getDeltaTime() {
        return deltaTime;
    }

    @Override
    public double getAverageMillisBetweenFrames() {
        return averageMillisBetweenFrames;
    }

    public interface UpdateListener {
        void onUpdate(@NotNull FrameInfo info);
    }
}
