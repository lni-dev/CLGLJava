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

package de.linusdev.cvg4j.nat.glfw3;

@SuppressWarnings("unused")
public interface GLFWValues {

    int GLFW_TRUE = 1;
    int GLFW_FALSE = 0;

    /**
     *
     * @param bool boolean
     * @return {@link #GLFW_TRUE} if given {@code bool} is {@code true}, {@link #GLFW_FALSE} otherwise.
     */
    static int convertBoolean(boolean bool) {
        if(bool) return GLFW_TRUE;
        return GLFW_FALSE;
    }

    /**
     * Key and button actions
     */
    interface Actions {
        /**
         * The key or mouse button was released.
         */
        int GLFW_RELEASE = 0;

        /**
         * The key or mouse button was pressed.
         */
        int GLFW_PRESS = 1;

        /**
         * The key was held down until it repeated.
         */
        int GLFW_REPEAT = 2;
    }

    /**
     * Joystick hat states. Bitfield!
     */
    interface HatState {
        int GLFW_HAT_CENTERED = 0;
        int GLFW_HAT_UP = 1;
        int GLFW_HAT_RIGHT = 2;
        int GLFW_HAT_DOWN = 4;
        int GLFW_HAT_LEFT = 8;
        int GLFW_HAT_RIGHT_UP = (GLFW_HAT_RIGHT | GLFW_HAT_UP);
        int GLFW_HAT_RIGHT_DOWN = (GLFW_HAT_RIGHT | GLFW_HAT_DOWN);
        int GLFW_HAT_LEFT_UP = (GLFW_HAT_LEFT | GLFW_HAT_UP);
        int GLFW_HAT_LEFT_DOWN = (GLFW_HAT_LEFT | GLFW_HAT_DOWN);
    }

    /**
     * These key codes are inspired by the _USB HID Usage Tables v1.12_ (p. 53-60),
     * but re-arranged to map to 7-bit ASCII for printable keys (function keys are
     * put in the 256+ range).
     * <br><br>
     *  The naming of the key codes follow these rules:
     *   - The US keyboard layout is used<br>
     *   - Names of printable alphanumeric characters are used (e.g. "A", "R",
     *     "3", etc.)<br>
     *   - For non-alphanumeric characters, Unicode:ish names are used (e.g.
     *     "COMMA", "LEFT_SQUARE_BRACKET", etc.). Note that some names do not
     *     correspond to the Unicode standard (usually for brevity)<br>
     *   - Keys that lack a clear US mapping are named "WORLD_x"<br>
     *   - For non-printable keys, custom names are used (e.g. "F4",
     *     "BACKSPACE", etc.)<br>
     */
    interface Keys_US {
        /* The unknown key */
        int GLFW_KEY_UNKNOWN = -1;

        /* Printable keys */
        int GLFW_KEY_SPACE = 32;
        int GLFW_KEY_APOSTROPHE = 39;
        int GLFW_KEY_COMMA = 44  /* , */;
        int GLFW_KEY_MINUS = 45  /* - */;
        int GLFW_KEY_PERIOD = 46  /* . */;
        int GLFW_KEY_SLASH = 47  /* / */;
        int GLFW_KEY_0 = 48;
        int GLFW_KEY_1 = 49;
        int GLFW_KEY_2 = 50;
        int GLFW_KEY_3 = 51;
        int GLFW_KEY_4 = 52;
        int GLFW_KEY_5 = 53;
        int GLFW_KEY_6 = 54;
        int GLFW_KEY_7 = 55;
        int GLFW_KEY_8 = 56;
        int GLFW_KEY_9 = 57;
        int GLFW_KEY_SEMICOLON = 59  /* ; */;
        int GLFW_KEY_EQUAL = 61  /* = */;
        int GLFW_KEY_A = 65;
        int GLFW_KEY_B = 66;
        int GLFW_KEY_C = 67;
        int GLFW_KEY_D = 68;
        int GLFW_KEY_E = 69;
        int GLFW_KEY_F = 70;
        int GLFW_KEY_G = 71;
        int GLFW_KEY_H = 72;
        int GLFW_KEY_I = 73;
        int GLFW_KEY_J = 74;
        int GLFW_KEY_K = 75;
        int GLFW_KEY_L = 76;
        int GLFW_KEY_M = 77;
        int GLFW_KEY_N = 78;
        int GLFW_KEY_O = 79;
        int GLFW_KEY_P = 80;
        int GLFW_KEY_Q = 81;
        int GLFW_KEY_R = 82;
        int GLFW_KEY_S = 83;
        int GLFW_KEY_T = 84;
        int GLFW_KEY_U = 85;
        int GLFW_KEY_V = 86;
        int GLFW_KEY_W = 87;
        int GLFW_KEY_X = 88;
        int GLFW_KEY_Y = 89;
        int GLFW_KEY_Z = 90;
        int GLFW_KEY_LEFT_BRACKET = 91  /* [ */;
        int GLFW_KEY_BACKSLASH = 92  /* \ */;
        int GLFW_KEY_RIGHT_BRACKET = 93  /* ] */;
        int GLFW_KEY_GRAVE_ACCENT = 96  /* ` */;
        int GLFW_KEY_WORLD_1 = 161 /* non-US #1 */;
        int GLFW_KEY_WORLD_2 = 162 /* non-US #2 */;

        /* Function keys */
        int GLFW_KEY_ESCAPE = 256;
        int GLFW_KEY_ENTER = 257;
        int GLFW_KEY_TAB = 258;
        int GLFW_KEY_BACKSPACE = 259;
        int GLFW_KEY_INSERT = 260;
        int GLFW_KEY_DELETE = 261;
        int GLFW_KEY_RIGHT = 262;
        int GLFW_KEY_LEFT = 263;
        int GLFW_KEY_DOWN = 264;
        int GLFW_KEY_UP = 265;
        int GLFW_KEY_PAGE_UP = 266;
        int GLFW_KEY_PAGE_DOWN = 267;
        int GLFW_KEY_HOME = 268;
        int GLFW_KEY_END = 269;
        int GLFW_KEY_CAPS_LOCK = 280;
        int GLFW_KEY_SCROLL_LOCK = 281;
        int GLFW_KEY_NUM_LOCK = 282;
        int GLFW_KEY_PRINT_SCREEN = 283;
        int GLFW_KEY_PAUSE = 284;
        int GLFW_KEY_F1 = 290;
        int GLFW_KEY_F2 = 291;
        int GLFW_KEY_F3 = 292;
        int GLFW_KEY_F4 = 293;
        int GLFW_KEY_F5 = 294;
        int GLFW_KEY_F6 = 295;
        int GLFW_KEY_F7 = 296;
        int GLFW_KEY_F8 = 297;
        int GLFW_KEY_F9 = 298;
        int GLFW_KEY_F10 = 299;
        int GLFW_KEY_F11 = 300;
        int GLFW_KEY_F12 = 301;
        int GLFW_KEY_F13 = 302;
        int GLFW_KEY_F14 = 303;
        int GLFW_KEY_F15 = 304;
        int GLFW_KEY_F16 = 305;
        int GLFW_KEY_F17 = 306;
        int GLFW_KEY_F18 = 307;
        int GLFW_KEY_F19 = 308;
        int GLFW_KEY_F20 = 309;
        int GLFW_KEY_F21 = 310;
        int GLFW_KEY_F22 = 311;
        int GLFW_KEY_F23 = 312;
        int GLFW_KEY_F24 = 313;
        int GLFW_KEY_F25 = 314;
        int GLFW_KEY_KP_0 = 320;
        int GLFW_KEY_KP_1 = 321;
        int GLFW_KEY_KP_2 = 322;
        int GLFW_KEY_KP_3 = 323;
        int GLFW_KEY_KP_4 = 324;
        int GLFW_KEY_KP_5 = 325;
        int GLFW_KEY_KP_6 = 326;
        int GLFW_KEY_KP_7 = 327;
        int GLFW_KEY_KP_8 = 328;
        int GLFW_KEY_KP_9 = 329;
        int GLFW_KEY_KP_DECIMAL = 330;
        int GLFW_KEY_KP_DIVIDE = 331;
        int GLFW_KEY_KP_MULTIPLY = 332;
        int GLFW_KEY_KP_SUBTRACT = 333;
        int GLFW_KEY_KP_ADD = 334;
        int GLFW_KEY_KP_ENTER = 335;
        int GLFW_KEY_KP_EQUAL = 336;
        int GLFW_KEY_LEFT_SHIFT = 340;
        int GLFW_KEY_LEFT_CONTROL = 341;
        int GLFW_KEY_LEFT_ALT = 342;
        int GLFW_KEY_LEFT_SUPER = 343;
        int GLFW_KEY_RIGHT_SHIFT = 344;
        int GLFW_KEY_RIGHT_CONTROL = 345;
        int GLFW_KEY_RIGHT_ALT = 346;
        int GLFW_KEY_RIGHT_SUPER = 347;
        int GLFW_KEY_MENU = 348;

        int GLFW_KEY_LAST = GLFW_KEY_MENU;
    }


    /**
     * Modifier key flags. Bitfield!
     */
    interface Modifiers {

        /**
         *  If this bit is set one or more Shift keys were held down.
         */
        int GLFW_MOD_SHIFT = 0x1;

        /**
         * If this bit is set one or more Control keys were held down.
         */
        int GLFW_MOD_CONTROL = 0x2;

        /**
         * If this bit is set one or more Alt keys were held down.
         */
        int GLFW_MOD_ALT = 0x4;

        /**
         * If this bit is set one or more Super keys were held down.
         */
        int GLFW_MOD_SUPER = 0x8;

        /**
         * If this bit is set the Caps Lock key is enabled.
         */
        int GLFW_MOD_CAPS_LOCK = 0x10;

        /**
         * If this bit is set the Num Lock key is enabled.
         */
        int GLFW_MOD_NUM_LOCK = 0x20;

    }

    /**
     * Mouse Buttons
     */
    interface Buttons {
        int GLFW_MOUSE_BUTTON_1 = 0;
        int GLFW_MOUSE_BUTTON_2 = 1;
        int GLFW_MOUSE_BUTTON_3 = 2;
        int GLFW_MOUSE_BUTTON_4 = 3;
        int GLFW_MOUSE_BUTTON_5 = 4;
        int GLFW_MOUSE_BUTTON_6 = 5;
        int GLFW_MOUSE_BUTTON_7 = 6;
        int GLFW_MOUSE_BUTTON_8 = 7;
        int GLFW_MOUSE_BUTTON_LAST = GLFW_MOUSE_BUTTON_8;
        int GLFW_MOUSE_BUTTON_LEFT = GLFW_MOUSE_BUTTON_1;
        int GLFW_MOUSE_BUTTON_RIGHT = GLFW_MOUSE_BUTTON_2;
        int GLFW_MOUSE_BUTTON_MIDDLE = GLFW_MOUSE_BUTTON_3;
    }

    /**
     * Joysticks
     */
    interface Joysticks {
        int GLFW_JOYSTICK_1 = 0;
        int GLFW_JOYSTICK_2 = 1;
        int GLFW_JOYSTICK_3 = 2;
        int GLFW_JOYSTICK_4 = 3;
        int GLFW_JOYSTICK_5 = 4;
        int GLFW_JOYSTICK_6 = 5;
        int GLFW_JOYSTICK_7 = 6;
        int GLFW_JOYSTICK_8 = 7;
        int GLFW_JOYSTICK_9 = 8;
        int GLFW_JOYSTICK_10 = 9;
        int GLFW_JOYSTICK_11 = 10;
        int GLFW_JOYSTICK_12 = 11;
        int GLFW_JOYSTICK_13 = 12;
        int GLFW_JOYSTICK_14 = 13;
        int GLFW_JOYSTICK_15 = 14;
        int GLFW_JOYSTICK_16 = 15;
        int GLFW_JOYSTICK_LAST = GLFW_JOYSTICK_16;
   }


    /**
     * Gamepad buttons
     */
   interface GamepadButtons {
        int GLFW_GAMEPAD_BUTTON_A = 0;
        int GLFW_GAMEPAD_BUTTON_B = 1;
        int GLFW_GAMEPAD_BUTTON_X = 2;
        int GLFW_GAMEPAD_BUTTON_Y = 3;
        int GLFW_GAMEPAD_BUTTON_LEFT_BUMPER = 4;
        int GLFW_GAMEPAD_BUTTON_RIGHT_BUMPER = 5;
        int GLFW_GAMEPAD_BUTTON_BACK = 6;
        int GLFW_GAMEPAD_BUTTON_START = 7;
        int GLFW_GAMEPAD_BUTTON_GUIDE = 8;
        int GLFW_GAMEPAD_BUTTON_LEFT_THUMB = 9;
        int GLFW_GAMEPAD_BUTTON_RIGHT_THUMB = 10;
        int GLFW_GAMEPAD_BUTTON_DPAD_UP = 11;
        int GLFW_GAMEPAD_BUTTON_DPAD_RIGHT = 12;
        int GLFW_GAMEPAD_BUTTON_DPAD_DOWN = 13;
        int GLFW_GAMEPAD_BUTTON_DPAD_LEFT = 14;
        int GLFW_GAMEPAD_BUTTON_LAST = GLFW_GAMEPAD_BUTTON_DPAD_LEFT;

        int GLFW_GAMEPAD_BUTTON_CROSS = GLFW_GAMEPAD_BUTTON_A;
        int GLFW_GAMEPAD_BUTTON_CIRCLE = GLFW_GAMEPAD_BUTTON_B;
        int GLFW_GAMEPAD_BUTTON_SQUARE = GLFW_GAMEPAD_BUTTON_X;
        int GLFW_GAMEPAD_BUTTON_TRIANGLE = GLFW_GAMEPAD_BUTTON_Y;
   }

    /**
     * Gamepad axes
     */
   interface GamepadAxes {
        int GLFW_GAMEPAD_AXIS_LEFT_X = 0;
        int GLFW_GAMEPAD_AXIS_LEFT_Y = 1;
        int GLFW_GAMEPAD_AXIS_RIGHT_X = 2;
        int GLFW_GAMEPAD_AXIS_RIGHT_Y = 3;
        int GLFW_GAMEPAD_AXIS_LEFT_TRIGGER = 4;
        int GLFW_GAMEPAD_AXIS_RIGHT_TRIGGER = 5;
        int GLFW_GAMEPAD_AXIS_LAST = GLFW_GAMEPAD_AXIS_RIGHT_TRIGGER;
   }


   interface InputMode {
       interface Mode {
           int GLFW_CURSOR = 0x00033001;
           int GLFW_STICKY_KEYS = 0x00033002;
           int GLFW_STICKY_MOUSE_BUTTONS = 0x00033003;
           int GLFW_LOCK_KEY_MODS = 0x00033004;
           int GLFW_RAW_MOUSE_MOTION = 0x00033005;
       }

       interface Value {
           int GLFW_CURSOR_NORMAL = 0x00034001;
           int GLFW_CURSOR_HIDDEN = 0x00034002;
           int GLFW_CURSOR_DISABLED = 0x00034003;
           int GLFW_CURSOR_CAPTURED = 0x00034004;
       }
   }

    @SuppressWarnings("JavadocDeclaration")
    interface ErrorCodes {
        /**
         * No error has occurred.
         */
        int GLFW_NO_ERROR = 0;
        /**
         * This occurs if a GLFW function was called that must not be called unless the
         * library is [initialized](@ref intro_init).
         *
         * @analysis Application programmer error.  Initialize GLFW before calling any
         * function that requires initialization.
         */
        int GLFW_NOT_INITIALIZED = 0x00010001;
        /**
         * This occurs if a GLFW function was called that needs and operates on the
         * current OpenGL or OpenGL ES context but no context is current on the calling
         * thread.  One such function is @ref glfwSwapInterval.
         *
         * @analysis Application programmer error.  Ensure a context is current before
         * calling functions that require a current context.
         */
        int GLFW_NO_CURRENT_CONTEXT = 0x00010002;
        /**
         * One of the arguments to the function was an invalid enum value, for example
         * requesting @ref GLFW_RED_BITS with @ref glfwGetWindowAttrib.
         *
         * @analysis Application programmer error.  Fix the offending call.
         */
        int GLFW_INVALID_ENUM = 0x00010003;
        /**
         * One of the arguments to the function was an invalid value, for example
         * requesting a non-existent OpenGL or OpenGL ES version like 2.7.
         * <br>
         * Requesting a valid but unavailable OpenGL or OpenGL ES version will instead
         * result in a @ref GLFW_VERSION_UNAVAILABLE error.
         *
         * @analysis Application programmer error.  Fix the offending call.
         */
        int GLFW_INVALID_VALUE = 0x00010004;
        /**
         * A memory allocation failed.
         *
         * @analysis A bug in GLFW or the underlying operating system.  Report the bug
         * to our <a href="https://github.com/glfw/glfw/issues">issue tracker</a>.
         */
        int GLFW_OUT_OF_MEMORY = 0x00010005;
        /**
         * GLFW could not find support for the requested API on the system.
         *
         * @analysis The installed graphics driver does not support the requested
         * API, or does not support it via the chosen context creation API.
         * Below are a few examples.
         * @par Some pre-installed Windows graphics drivers do not support OpenGL.  AMD only
         * supports OpenGL ES via EGL, while Nvidia and Intel only support it via
         * a WGL or GLX extension.  macOS does not provide OpenGL ES at all.  The Mesa
         * EGL, OpenGL and OpenGL ES libraries do not interface with the Nvidia binary
         * driver.  Older graphics drivers do not support Vulkan.
         */
        int GLFW_API_UNAVAILABLE = 0x00010006;
        /**
         * The requested OpenGL or OpenGL ES version (including any requested context
         * or framebuffer hints) is not available on this machine.
         *
         * @analysis The machine does not support your requirements.  If your
         * application is sufficiently flexible, downgrade your requirements and try
         * again.  Otherwise, inform the user that their machine does not match your
         * requirements.
         * @par Future invalid OpenGL and OpenGL ES versions, for example OpenGL 4.8 if 5.0
         * comes out before the 4.x series gets that far, also fail with this error and
         * not @ref GLFW_INVALID_VALUE, because GLFW cannot know what future versions
         * will exist.
         */
        int GLFW_VERSION_UNAVAILABLE = 0x00010007;
        /**
         * A platform-specific error occurred that does not match any of the more
         * specific categories.
         *
         * @analysis A bug or configuration error in GLFW, the underlying operating
         * system or its drivers, or a lack of required resources.  Report the issue to
         * our <a href="https://github.com/glfw/glfw/issues">issue tracker</a>.
         */
        int GLFW_PLATFORM_ERROR = 0x00010008;
        /**
         * If emitted during window creation, the requested pixel format is not
         * supported.
         * <br>
         * If emitted when querying the clipboard, the contents of the clipboard could
         * not be converted to the requested format.
         *
         * @analysis If emitted during window creation, one or more
         * [hard constraints](@ref window_hints_hard) did not match any of the
         * available pixel formats.  If your application is sufficiently flexible,
         * downgrade your requirements and try again.  Otherwise, inform the user that
         * their machine does not match your requirements.
         * @par If emitted when querying the clipboard, ignore the error or report it to
         * the user, as appropriate.
         */
        int GLFW_FORMAT_UNAVAILABLE = 0x00010009;
        /**
         * A window that does not have an OpenGL or OpenGL ES context was passed to
         * a function that requires it to have one.
         *
         * @analysis Application programmer error.  Fix the offending call.
         */
        int GLFW_NO_WINDOW_CONTEXT = 0x0001000A;
        /**
         * The specified standard cursor shape is not available, either because the
         * current platform cursor theme does not provide it or because it is not
         * available on the platform.
         *
         * @analysis Platform or system settings limitation.  Pick another
         * [standard cursor shape](@ref shapes) or create a
         * [custom cursor](@ref cursor_custom).
         */
        int GLFW_CURSOR_UNAVAILABLE = 0x0001000B;
        /**
         * The requested feature is not provided by the platform, so GLFW is unable to
         * implement it.  The documentation for each function notes if it could emit
         * this error.
         *
         * @analysis Platform or platform version limitation.  The error can be ignored
         * unless the feature is critical to the application.
         * @par A function call that emits this error has no effect other than the error and
         * updating any existing out parameters.
         */
        int GLFW_FEATURE_UNAVAILABLE = 0x0001000C;
        /**
         * The requested feature has not yet been implemented in GLFW for this platform.
         * <br>
         *
         * @analysis An incomplete implementation of GLFW for this platform, hopefully
         * fixed in a future release.  The error can be ignored unless the feature is
         * critical to the application.
         * <br>
         * @par A function call that emits this error has no effect other than the error and
         * updating any existing out parameters.
         */
        int GLFW_FEATURE_UNIMPLEMENTED = 0x0001000D;
        /**
         * If emitted during initialization, no matching platform was found.  If the @ref
         * GLFW_PLATFORM init hint was set to `GLFW_ANY_PLATFORM`, GLFW could not detect any of
         * the platforms supported by this library binary, except for the Null platform.  If the
         * init hint was set to a specific platform, it is either not supported by this library
         * binary or GLFW was not able to detect it.
         * <br>
         * If emitted by a native access function, GLFW was initialized for a different platform
         * than the function is for.
         * <br>
         *
         * @analysis Failure to detect any platform usually only happens on non-macOS Unix
         * systems, either when no window system is running or the program was run from
         * a terminal that does not have the necessary environment variables.  Fall back to
         * a different platform if possible or notify the user that no usable platform was
         * detected.
         * <br>
         * Failure to detect a specific platform may have the same cause as above or be because
         * support for that platform was not compiled in.  Call @ref glfwPlatformSupported to
         * check whether a specific platform is supported by a library binary.
         */
        int GLFW_PLATFORM_UNAVAILABLE = 0x0001000E;
    }
    
    interface WindowHints {
        /**
         * Input focus window hint and attribute
         * <p>
         * Input focus [window hint](@ref GLFW_FOCUSED_hint) or
         * [window attribute](@ref GLFW_FOCUSED_attrib).
         */
        int GLFW_FOCUSED = 0x00020001;
        /**
         * Window iconification window attribute
         * <p>
         * Window iconification [window attribute](@ref GLFW_ICONIFIED_attrib).
         */
        int GLFW_ICONIFIED = 0x00020002;
        /**
         * Window resize-ability window hint and attribute
         * <p>
         * Window resize-ability [window hint](@ref GLFW_RESIZABLE_hint) and
         * [window attribute](@ref GLFW_RESIZABLE_attrib).
         */
        int GLFW_RESIZABLE = 0x00020003;
        /**
         * Window visibility window hint and attribute
         * <p>
         * Window visibility [window hint](@ref GLFW_VISIBLE_hint) and
         * [window attribute](@ref GLFW_VISIBLE_attrib).
         */
        int GLFW_VISIBLE = 0x00020004;
        /**
         * Window decoration window hint and attribute
         * <p>
         * Window decoration [window hint](@ref GLFW_DECORATED_hint) and
         * [window attribute](@ref GLFW_DECORATED_attrib).
         */
        int GLFW_DECORATED = 0x00020005;
        /**
         * Window auto-iconification window hint and attribute
         * <p>
         * Window auto-iconification [window hint](@ref GLFW_AUTO_ICONIFY_hint) and
         * [window attribute](@ref GLFW_AUTO_ICONIFY_attrib).
         */
        int GLFW_AUTO_ICONIFY = 0x00020006;
        /**
         * Window decoration window hint and attribute
         * <p>
         * Window decoration [window hint](@ref GLFW_FLOATING_hint) and
         * [window attribute](@ref GLFW_FLOATING_attrib).
         */
        int GLFW_FLOATING = 0x00020007;
        /**
         * Window maximization window hint and attribute
         * <p>
         * Window maximization [window hint](@ref GLFW_MAXIMIZED_hint) and
         * [window attribute](@ref GLFW_MAXIMIZED_attrib).
         */
        int GLFW_MAXIMIZED = 0x00020008;
        /**
         * Cursor centering window hint
         * <p>
         * Cursor centering [window hint](@ref GLFW_CENTER_CURSOR_hint).
         */
        int GLFW_CENTER_CURSOR = 0x00020009;
        /**
         * Window framebuffer transparency hint and attribute
         * <p>
         * Window framebuffer transparency
         * [window hint](@ref GLFW_TRANSPARENT_FRAMEBUFFER_hint) and
         * [window attribute](@ref GLFW_TRANSPARENT_FRAMEBUFFER_attrib).
         */
        int GLFW_TRANSPARENT_FRAMEBUFFER = 0x0002000A;
        /**
         * Mouse cursor hover window attribute.
         * <p>
         * Mouse cursor hover [window attribute](@ref GLFW_HOVERED_attrib).
         */
        int GLFW_HOVERED = 0x0002000B;
        /**
         * Input focus on calling show window hint and attribute
         * <p>
         * Input focus [window hint](@ref GLFW_FOCUS_ON_SHOW_hint) or
         * [window attribute](@ref GLFW_FOCUS_ON_SHOW_attrib).
         */
        int GLFW_FOCUS_ON_SHOW = 0x0002000C;

        /**
         * Mouse input transparency window hint and attribute
         * <p>
         * Mouse input transparency [window hint](@ref GLFW_MOUSE_PASSTHROUGH_hint) or
         * [window attribute](@ref GLFW_MOUSE_PASSTHROUGH_attrib).
         */
        int GLFW_MOUSE_PASSTHROUGH = 0x0002000D;

        /**
         * Initial position x-coordinate window hint.
         * <p>
         * Initial position x-coordinate [window hint](@ref GLFW_POSITION_X).
         */
        int GLFW_POSITION_X = 0x0002000E;

        /**
         * Initial position y-coordinate window hint.
         * <p>
         * Initial position y-coordinate [window hint](@ref GLFW_POSITION_Y).
         */
        int GLFW_POSITION_Y = 0x0002000F;

        /**
         * Framebuffer bit depth hint.
         * <p>
         * Framebuffer bit depth [hint](@ref GLFW_RED_BITS).
         */
        int GLFW_RED_BITS = 0x00021001;
        /**
         * Framebuffer bit depth hint.
         * <p>
         * Framebuffer bit depth [hint](@ref GLFW_GREEN_BITS).
         */
        int GLFW_GREEN_BITS = 0x00021002;
        /**
         * Framebuffer bit depth hint.
         * <p>
         * Framebuffer bit depth [hint](@ref GLFW_BLUE_BITS).
         */
        int GLFW_BLUE_BITS = 0x00021003;
        /**
         * Framebuffer bit depth hint.
         * <p>
         * Framebuffer bit depth [hint](@ref GLFW_ALPHA_BITS).
         */
        int GLFW_ALPHA_BITS = 0x00021004;
        /**
         * Framebuffer bit depth hint.
         * <p>
         * Framebuffer bit depth [hint](@ref GLFW_DEPTH_BITS).
         */
        int GLFW_DEPTH_BITS = 0x00021005;
        /**
         * Framebuffer bit depth hint.
         * <p>
         * Framebuffer bit depth [hint](@ref GLFW_STENCIL_BITS).
         */
        int GLFW_STENCIL_BITS = 0x00021006;
        /**
         * Framebuffer bit depth hint.
         * <p>
         * Framebuffer bit depth [hint](@ref GLFW_ACCUM_RED_BITS).
         */
        int GLFW_ACCUM_RED_BITS = 0x00021007;
        /**
         * Framebuffer bit depth hint.
         * <p>
         * Framebuffer bit depth [hint](@ref GLFW_ACCUM_GREEN_BITS).
         */
        int GLFW_ACCUM_GREEN_BITS = 0x00021008;
        /**
         * Framebuffer bit depth hint.
         * <p>
         * Framebuffer bit depth [hint](@ref GLFW_ACCUM_BLUE_BITS).
         */
        int GLFW_ACCUM_BLUE_BITS = 0x00021009;
        /**
         * Framebuffer bit depth hint.
         * <p>
         * Framebuffer bit depth [hint](@ref GLFW_ACCUM_ALPHA_BITS).
         */
        int GLFW_ACCUM_ALPHA_BITS = 0x0002100A;
        /**
         * Framebuffer auxiliary buffer hint.
         * <p>
         * Framebuffer auxiliary buffer [hint](@ref GLFW_AUX_BUFFERS).
         */
        int GLFW_AUX_BUFFERS = 0x0002100B;
        /**
         * OpenGL stereoscopic rendering hint.
         * <p>
         * OpenGL stereoscopic rendering [hint](@ref GLFW_STEREO).
         */
        int GLFW_STEREO = 0x0002100C;
        /**
         * Framebuffer MSAA samples hint.
         * <p>
         * Framebuffer MSAA samples [hint](@ref GLFW_SAMPLES).
         */
        int GLFW_SAMPLES = 0x0002100D;
        /**
         * Framebuffer sRGB hint.
         * <p>
         * Framebuffer sRGB [hint](@ref GLFW_SRGB_CAPABLE).
         */
        int GLFW_SRGB_CAPABLE = 0x0002100E;
        /**
         * Monitor refresh rate hint.
         * <p>
         * Monitor refresh rate [hint](@ref GLFW_REFRESH_RATE).
         */
        int GLFW_REFRESH_RATE = 0x0002100F;
        /**
         * Framebuffer double buffering hint and attribute.
         * <p>
         * Framebuffer double buffering [hint](@ref GLFW_DOUBLEBUFFER_hint) and
         * [attribute](@ref GLFW_DOUBLEBUFFER_attrib).
         */
        int GLFW_DOUBLEBUFFER = 0x00021010;

        /**
         * Context client API hint and attribute.
         * <p>
         * Context client API [hint](@ref GLFW_CLIENT_API_hint) and
         * [attribute](@ref GLFW_CLIENT_API_attrib).
         */
        int GLFW_CLIENT_API = 0x00022001;
        /**
         * Context client API major version hint and attribute.
         * <p>
         * Context client API major version [hint](@ref GLFW_CONTEXT_VERSION_MAJOR_hint)
         * and [attribute](@ref GLFW_CONTEXT_VERSION_MAJOR_attrib).
         */
        int GLFW_CONTEXT_VERSION_MAJOR = 0x00022002;
        /**
         * Context client API minor version hint and attribute.
         * <p>
         * Context client API minor version [hint](@ref GLFW_CONTEXT_VERSION_MINOR_hint)
         * and [attribute](@ref GLFW_CONTEXT_VERSION_MINOR_attrib).
         */
        int GLFW_CONTEXT_VERSION_MINOR = 0x00022003;
        /**
         * Context client API revision number attribute.
         * <p>
         * Context client API revision number
         * [attribute](@ref GLFW_CONTEXT_REVISION_attrib).
         */
        int GLFW_CONTEXT_REVISION = 0x00022004;
        /**
         * Context robustness hint and attribute.
         * <p>
         * Context client API revision number [hint](@ref GLFW_CONTEXT_ROBUSTNESS_hint)
         * and [attribute](@ref GLFW_CONTEXT_ROBUSTNESS_attrib).
         */
        int GLFW_CONTEXT_ROBUSTNESS = 0x00022005;
        /**
         * OpenGL forward-compatibility hint and attribute.
         * <p>
         * OpenGL forward-compatibility [hint](@ref GLFW_OPENGL_FORWARD_COMPAT_hint)
         * and [attribute](@ref GLFW_OPENGL_FORWARD_COMPAT_attrib).
         */
        int GLFW_OPENGL_FORWARD_COMPAT = 0x00022006;
        /**
         * Debug mode context hint and attribute.
         * <p>
         * Debug mode context [hint](@ref GLFW_CONTEXT_DEBUG_hint) and
         * [attribute](@ref GLFW_CONTEXT_DEBUG_attrib).
         */
        int GLFW_CONTEXT_DEBUG = 0x00022007;
        /**
         * Legacy name for compatibility.
         * <p>
         * This is an alias for compatibility with earlier versions.
         */
        int GLFW_OPENGL_DEBUG_CONTEXT = GLFW_CONTEXT_DEBUG;
        /**
         * OpenGL profile hint and attribute.
         * <p>
         * OpenGL profile [hint](@ref GLFW_OPENGL_PROFILE_hint) and
         * [attribute](@ref GLFW_OPENGL_PROFILE_attrib).
         */
        int GLFW_OPENGL_PROFILE = 0x00022008;
        /**
         * Context flush-on-release hint and attribute.
         * <p>
         * Context flush-on-release [hint](@ref GLFW_CONTEXT_RELEASE_BEHAVIOR_hint) and
         * [attribute](@ref GLFW_CONTEXT_RELEASE_BEHAVIOR_attrib).
         */
        int GLFW_CONTEXT_RELEASE_BEHAVIOR = 0x00022009;
        /**
         * Context error suppression hint and attribute.
         * <p>
         * Context error suppression [hint](@ref GLFW_CONTEXT_NO_ERROR_hint) and
         * [attribute](@ref GLFW_CONTEXT_NO_ERROR_attrib).
         */
        int GLFW_CONTEXT_NO_ERROR = 0x0002200A;
        /**
         * Context creation API hint and attribute.
         * <p>
         * Context creation API [hint](@ref GLFW_CONTEXT_CREATION_API_hint) and
         * [attribute](@ref GLFW_CONTEXT_CREATION_API_attrib).
         */
        int GLFW_CONTEXT_CREATION_API = 0x0002200B;
        /**
         * Window content area scaling window
         * [window hint](@ref GLFW_SCALE_TO_MONITOR).
         */
        int GLFW_SCALE_TO_MONITOR = 0x0002200C;
        /**
         * Window framebuffer scaling
         * [window hint](@ref GLFW_SCALE_FRAMEBUFFER_hint).
         */
        int GLFW_SCALE_FRAMEBUFFER = 0x0002200D;
        /**
         * Legacy name for compatibility.
         * <p>
         * This is an alias for the
         * [GLFW_SCALE_FRAMEBUFFER](@ref GLFW_SCALE_FRAMEBUFFER_hint) window hint for
         * compatibility with earlier versions.
         */
        int GLFW_COCOA_RETINA_FRAMEBUFFER = 0x00023001;
        /**
         * macOS specific
         * [window hint](@ref GLFW_COCOA_FRAME_NAME_hint).
         */
        int GLFW_COCOA_FRAME_NAME = 0x00023002;
        /**
         * macOS specific
         * [window hint](@ref GLFW_COCOA_GRAPHICS_SWITCHING_hint).
         */
        int GLFW_COCOA_GRAPHICS_SWITCHING = 0x00023003;
        /**
         * X11 specific
         * [window hint](@ref GLFW_X11_CLASS_NAME_hint).
         */
        int GLFW_X11_CLASS_NAME = 0x00024001;
        /**
         * X11 specific
         * [window hint](@ref GLFW_X11_CLASS_NAME_hint).
         */
        int GLFW_X11_INSTANCE_NAME = 0x00024002;
        int GLFW_WIN32_KEYBOARD_MENU = 0x00025001;
        /**
         * Win32 specific [window hint](@ref GLFW_WIN32_SHOWDEFAULT_hint).
         */
        int GLFW_WIN32_SHOWDEFAULT = 0x00025002;
        /**
         * Wayland specific
         * [window hint](@ref GLFW_WAYLAND_APP_ID_hint).
         * <p>
         * Allows specification of the Wayland app_id.
         */
        int GLFW_WAYLAND_APP_ID = 0x00026001;
    }

    enum OpenGlProfiles {
        GLFW_OPENGL_ANY_PROFILE(0),
        GLFW_OPENGL_CORE_PROFILE(0x00032001),
        GLFW_OPENGL_COMPAT_PROFILE(0x00032002),
        ;
        public final int value;

        OpenGlProfiles(int value) {
            this.value = value;
        }
    }

    enum ClientApis {
        GLFW_NO_API(0),
        GLFW_OPENGL_API(0x00030001),
        GLFW_OPENGL_ES_API(0x00030002),
        ;
        public final int value;

        ClientApis(int value) {
            this.value = value;
        }
    }
    
}
