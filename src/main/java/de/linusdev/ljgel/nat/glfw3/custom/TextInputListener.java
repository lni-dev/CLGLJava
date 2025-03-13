/*
 * Copyright (c) 2023-2025 Linus Andera
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

package de.linusdev.ljgel.nat.glfw3.custom;

public interface TextInputListener {

    /**
     * The array {@code chars} may not be modified or stored.<br><br>
     * If {@code supplementaryChar} is {@code false}, the contents of {@code chars[1]} are undefined.
     * @param chars the character as utf-16. Array length is always 2
     * @param supplementaryChar whether only the first index of the array is the character ot both.
     */
    void onTextInput(char[] chars, boolean supplementaryChar);

}
