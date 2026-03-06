/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.sun.jna.Memory
 *  com.sun.jna.Native
 *  com.sun.jna.Pointer
 *  com.sun.jna.platform.win32.WinDef$HWND
 *  com.sun.jna.platform.win32.WinNT$HRESULT
 *  com.sun.jna.win32.StdCallLibrary
 *  org.lwjgl.glfw.GLFWNativeWin32
 */
package rich.util.window;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.win32.StdCallLibrary;
import org.lwjgl.glfw.GLFWNativeWin32;

public class WindowStyle {
    public static void setDarkMode(long windowHandle) {
        long hwnd = GLFWNativeWin32.glfwGetWin32Window((long)windowHandle);
        WinDef.HWND hwndJna = new WinDef.HWND(new Pointer(hwnd));
        int DWMWA_USE_IMMERSIVE_DARK_MODE = 20;
        Memory darkModeEnabled = new Memory(4L);
        darkModeEnabled.setInt(0L, 1);
        DwmApi.INSTANCE.DwmSetWindowAttribute(hwndJna, DWMWA_USE_IMMERSIVE_DARK_MODE, (Pointer)darkModeEnabled, 4);
    }

    public static interface DwmApi
    extends StdCallLibrary {
        public static final DwmApi INSTANCE = (DwmApi)Native.load((String)"dwmapi", DwmApi.class);

        public WinNT.HRESULT DwmSetWindowAttribute(WinDef.HWND var1, int var2, Pointer var3, int var4);
    }
}

