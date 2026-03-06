
package rich.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.Generated;
import rich.Initialization;
import rich.modules.module.ModuleStructure;

public final class Instance {
    private static final ConcurrentMap<Class<? extends ModuleStructure>, ModuleStructure> instanceModules = new ConcurrentHashMap<Class<? extends ModuleStructure>, ModuleStructure>();

    public static <T extends ModuleStructure> T get(Class<T> clazz) {
        return (T)((ModuleStructure)clazz.cast(instanceModules.computeIfAbsent(clazz, instance -> Initialization.getInstance().getManager().getModuleProvider().get(instance))));
    }

    public static <T extends ModuleStructure> T get(String module) {
        return Initialization.getInstance().getManager().getModuleProvider().get(module);
    }

    @Generated
    private Instance() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

