/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.glfw.GLFW
 */
package rich.screens.clickgui.impl.module.handler;

import org.lwjgl.glfw.GLFW;

public class ModuleBindHandler {
    public String getBindDisplayName(int key, int type) {
        if (key == -1) {
            return "";
        }
        if (key == 1000) {
            return "Up";
        }
        if (key == 1001) {
            return "Dn";
        }
        if (key == 1002) {
            return "M3";
        }
        if (type == 0) {
            return switch (key) {
                case 0 -> "LMB";
                case 1 -> "RMB";
                case 2 -> "MMB";
                case 3 -> "M4";
                case 4 -> "M5";
                case 5 -> "M6";
                case 6 -> "M7";
                case 7 -> "M8";
                default -> "M" + key;
            };
        }
        String keyName = GLFW.glfwGetKeyName((int)key, (int)0);
        if (keyName != null) {
            return keyName.toUpperCase();
        }
        return switch (key) {
            case 340 -> "LS";
            case 344 -> "RS";
            case 341 -> "LC";
            case 345 -> "RC";
            case 342 -> "LA";
            case 346 -> "RA";
            case 32 -> "Sp";
            case 258 -> "Tab";
            case 280 -> "Cap";
            case 257 -> "Ent";
            case 259 -> "Bk";
            case 260 -> "Ins";
            case 261 -> "Del";
            case 268 -> "Hm";
            case 269 -> "End";
            case 266 -> "PU";
            case 267 -> "PD";
            case 265 -> "Up";
            case 264 -> "Dn";
            case 263 -> "Lt";
            case 262 -> "Rt";
            case 290 -> "F1";
            case 291 -> "F2";
            case 292 -> "F3";
            case 293 -> "F4";
            case 294 -> "F5";
            case 295 -> "F6";
            case 296 -> "F7";
            case 297 -> "F8";
            case 298 -> "F9";
            case 299 -> "F10";
            case 300 -> "F11";
            case 301 -> "F12";
            case 256 -> "Esc";
            default -> "K" + key;
        };
    }
}

