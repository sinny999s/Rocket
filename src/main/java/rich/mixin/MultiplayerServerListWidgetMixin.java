/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.util.concurrent.ThreadFactoryBuilder
 *  org.spongepowered.asm.mixin.Final
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Mutable
 *  org.spongepowered.asm.mixin.Shadow
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package rich.mixin;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ServerSelectionList.class})
public class MultiplayerServerListWidgetMixin {
    @Mutable
    @Final
    @Shadow
    static ThreadPoolExecutor THREAD_POOL;
    @Unique
    private static final int PINGER_THREAD_COUNT_OVERHEAD = 5;
    @Final
    @Shadow
    private List<ServerSelectionList.OnlineServerEntry> onlineServers;
    @Unique
    private static boolean threadpoolInitialized;

    @Inject(method={"refreshEntries"}, at={@At(value="HEAD")})
    private void updateEntriesInject(CallbackInfo ci) {
        if (!threadpoolInitialized) {
            threadpoolInitialized = true;
            this.clearServerPingerThreadPool();
        }
        if (THREAD_POOL.getActiveCount() >= 5) {
            this.clearServerPingerThreadPool();
        }
    }

    @Unique
    private void clearServerPingerThreadPool() {
        THREAD_POOL.shutdownNow();
        THREAD_POOL = new ScheduledThreadPoolExecutor(this.onlineServers.size() + 5, new ThreadFactoryBuilder().setNameFormat("Server Pinger #%d").setDaemon(true).build());
    }

    static {
        threadpoolInitialized = false;
    }
}

