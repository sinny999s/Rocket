/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  net.fabricmc.api.ClientModInitializer
 */
package rich;

import lombok.Generated;
import net.fabricmc.api.ClientModInitializer;
import rich.manager.Manager;

public class Initialization
implements ClientModInitializer {
    private static Initialization instance;
    private Manager manager;

    public void onInitializeClient() {
    }

    public void init() {
        instance = this;
        this.manager = new Manager();
        this.manager.init();
    }

    @Generated
    public static Initialization getInstance() {
        return instance;
    }

    @Generated
    public Manager getManager() {
        return this.manager;
    }
}

