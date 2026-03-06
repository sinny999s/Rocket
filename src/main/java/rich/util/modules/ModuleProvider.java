
package rich.util.modules;

import java.util.List;
import lombok.Generated;
import rich.modules.module.ModuleStructure;

public class ModuleProvider {
    private final List<ModuleStructure> moduleStructures;

    public <T extends ModuleStructure> T get(String name) {
        return (T)((ModuleStructure)this.moduleStructures.stream().filter(module -> module.getName().equalsIgnoreCase(name)).map(module -> module).findFirst().orElse(null));
    }

    public <T extends ModuleStructure> T get(Class<T> clazz) {
        return (T)((ModuleStructure)this.moduleStructures.stream().filter(module -> clazz.isAssignableFrom(module.getClass())).map(clazz::cast).findFirst().orElse(null));
    }

    @Generated
    public List<ModuleStructure> getModuleStructures() {
        return this.moduleStructures;
    }

    @Generated
    public ModuleProvider(List<ModuleStructure> moduleStructures) {
        this.moduleStructures = moduleStructures;
    }
}

