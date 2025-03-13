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

package de.linusdev.ljgel.nat.cl;

@SuppressWarnings("unused")
public class CLException extends RuntimeException {

    private final CLStatus code;

    public CLException(int code) {
        this.code = CLStatus.fromStatus(code);
    }

    public CLException(CLStatus code) {
        this.code = code;
    }

    @Override
    public String getMessage() {
        return code.toString();
    }
}
