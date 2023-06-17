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

package de.linusdev.clgl.nat.glfw3;

@SuppressWarnings("unused")
public interface GLFWValues {

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

           int GLFW_TRUE = 1;
           int GLFW_FALSE = 0;
       }
   }



    
}
