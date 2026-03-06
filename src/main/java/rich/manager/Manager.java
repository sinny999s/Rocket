
package rich.manager;

import lombok.Generated;
import rich.client.draggables.HudManager;
import rich.command.CommandManager;
import rich.events.api.EventManager;
import rich.modules.impl.combat.aura.attack.StrikerConstructor;
import rich.modules.module.ModuleRepository;
import rich.screens.clickgui.ClickGui;
import rich.util.config.ConfigSystem;
import rich.util.config.impl.bind.BindConfig;
import rich.util.config.impl.blockesp.BlockESPConfig;
import rich.util.config.impl.drag.DragConfig;
import rich.util.config.impl.friend.FriendConfig;
import rich.util.config.impl.prefix.PrefixConfig;
import rich.util.config.impl.proxy.ProxyConfig;
import rich.util.config.impl.staff.StaffConfig;
import rich.util.modules.ModuleProvider;
import rich.util.modules.ModuleSwitcher;
import rich.util.render.font.FontInitializer;
import rich.util.render.shader.RenderCore;
import rich.util.render.shader.Scissor;
import rich.util.repository.macro.MacroRepository;
import rich.util.repository.way.WayRepository;
import rich.util.tps.TPSCalculate;

public class Manager {
    public StrikerConstructor attackPerpetrator = new StrikerConstructor();
    private EventManager eventManager;
    private RenderCore renderCore;
    private Scissor scissor;
    private ModuleProvider moduleProvider;
    private ModuleRepository moduleRepository;
    private ModuleSwitcher moduleSwitcher;
    private ClickGui clickgui;
    private ConfigSystem configSystem;
    private CommandManager commandManager;
    private TPSCalculate tpsCalculate;
    private HudManager hudManager = new HudManager();

    public void init() {
        MacroRepository.getInstance().init();
        WayRepository.getInstance().init();
        BlockESPConfig.getInstance().load();
        FriendConfig.getInstance().load();
        PrefixConfig.getInstance().load();
        StaffConfig.getInstance().load();
        ProxyConfig.getInstance().load();
        DragConfig.getInstance().load();
        BindConfig.getInstance();
        FontInitializer.register();
        this.tpsCalculate = new TPSCalculate();
        this.clickgui = new ClickGui();
        this.eventManager = new EventManager();
        this.renderCore = new RenderCore();
        this.scissor = new Scissor();
        this.hudManager = new HudManager();
        this.hudManager.initElements();
        this.moduleRepository = new ModuleRepository();
        this.moduleRepository.setup();
        this.moduleProvider = new ModuleProvider(this.moduleRepository.modules());
        this.moduleSwitcher = new ModuleSwitcher(this.moduleRepository.modules(), this.eventManager);
        this.configSystem = new ConfigSystem();
        this.configSystem.init();
        this.commandManager = new CommandManager();
        this.commandManager.init();
    }

    @Generated
    public StrikerConstructor getAttackPerpetrator() {
        return this.attackPerpetrator;
    }

    @Generated
    public EventManager getEventManager() {
        return this.eventManager;
    }

    @Generated
    public RenderCore getRenderCore() {
        return this.renderCore;
    }

    @Generated
    public Scissor getScissor() {
        return this.scissor;
    }

    @Generated
    public ModuleProvider getModuleProvider() {
        return this.moduleProvider;
    }

    @Generated
    public ModuleRepository getModuleRepository() {
        return this.moduleRepository;
    }

    @Generated
    public ModuleSwitcher getModuleSwitcher() {
        return this.moduleSwitcher;
    }

    @Generated
    public ClickGui getClickgui() {
        return this.clickgui;
    }

    @Generated
    public ConfigSystem getConfigSystem() {
        return this.configSystem;
    }

    @Generated
    public CommandManager getCommandManager() {
        return this.commandManager;
    }

    @Generated
    public TPSCalculate getTpsCalculate() {
        return this.tpsCalculate;
    }

    @Generated
    public HudManager getHudManager() {
        return this.hudManager;
    }
}

