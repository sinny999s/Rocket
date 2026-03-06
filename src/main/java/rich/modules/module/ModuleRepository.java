
package rich.modules.module;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import rich.modules.impl.combat.AntiBot;
import rich.modules.impl.combat.Criticals;
import rich.modules.impl.combat.Aura;
import rich.modules.impl.combat.CrystalAura;
import rich.modules.impl.combat.Surround;
import rich.modules.impl.combat.AutoGApple;
import rich.modules.impl.combat.AutoSwap;
import rich.modules.impl.combat.AutoTotem;
import rich.modules.impl.combat.BowSpammer;
import rich.modules.impl.combat.HitBoxModule;
import rich.modules.impl.combat.HitSound;
import rich.modules.impl.combat.MaceTarget;
import rich.modules.impl.combat.NoFriendDamage;
import rich.modules.impl.combat.NoInteract;
import rich.modules.impl.combat.ProjectileHelper;
import rich.modules.impl.combat.ShiftTap;
import rich.modules.impl.combat.TapeMouse;
import rich.modules.impl.combat.TriggerBot;
import rich.modules.impl.combat.Velocity;
import rich.modules.impl.misc.AutoBuy;
import rich.modules.impl.misc.AutoDuel;
import rich.modules.impl.misc.AutoLeave;
import rich.modules.impl.misc.AutoTpAccept;
import rich.modules.impl.misc.ClickFriend;
import rich.modules.impl.misc.ClickPearl;
import rich.modules.impl.misc.ClientSounds;
import rich.modules.impl.misc.ElytraHelper;
import rich.modules.impl.misc.RegionExploit;
import rich.modules.impl.misc.ServerHelper;
import rich.modules.impl.misc.ServerRPSpoofer;
import rich.modules.impl.misc.BetterChat;
import rich.modules.impl.misc.WindJump;
import rich.modules.impl.misc.autoparser.AutoParser;
import rich.modules.impl.movement.AutoSprint;
import rich.modules.impl.movement.ReverseStep;
import rich.modules.impl.movement.Step;
import rich.modules.impl.movement.ElytraMotion;
import rich.modules.impl.movement.ElytraTarget;
import rich.modules.impl.movement.Fly;
import rich.modules.impl.movement.InventoryMove;
import rich.modules.impl.movement.Jesus;
import rich.modules.impl.movement.LongJump;
import rich.modules.impl.movement.NoSlow;
import rich.modules.impl.movement.NoWeb;
import rich.modules.impl.movement.Speed;
import rich.modules.impl.movement.Spider;
import rich.modules.impl.movement.Strafe;
import rich.modules.impl.movement.SuperFireWork;
import rich.modules.impl.movement.TargetStrafe;
import rich.modules.impl.movement.WaterSpeed;
import rich.modules.impl.player.AutoEat;
import rich.modules.impl.player.AutoPilot;
import rich.modules.impl.player.AutoPotion;
import rich.modules.impl.player.AutoRespawn;
import rich.modules.impl.player.AutoTool;
import rich.modules.impl.player.ChestStealer;
import rich.modules.impl.player.FreeCam;
import rich.modules.impl.player.FreeLook;
import rich.modules.impl.player.ItemScroller;
import rich.modules.impl.player.NameProtect;
import rich.modules.impl.player.NoDelay;
import rich.modules.impl.player.NoEntityTrace;
import rich.modules.impl.player.NoFallDamage;
import rich.modules.impl.player.NoPush;
import rich.modules.impl.render.Ambience;
import rich.modules.impl.render.BetterTooltips;
import rich.modules.impl.render.Arrows;
import rich.modules.impl.render.AuctionHelper;
import rich.modules.impl.render.BlockESP;
import rich.modules.impl.render.BlockOverlay;
import rich.modules.impl.render.CameraSettings;
import rich.modules.impl.render.ChinaHat;
import rich.modules.impl.render.ChunkAnimator;
import rich.modules.impl.render.Esp;
import rich.modules.impl.render.StorageEsp;
import rich.modules.impl.render.Waypoints;
import rich.modules.impl.render.FullBright;
import rich.modules.impl.render.GlassHands;
import rich.modules.impl.render.HitEffect;
import rich.modules.impl.render.Hud;
import rich.modules.impl.render.ItemPhysic;
import rich.modules.impl.render.JumpCircle;
import rich.modules.impl.render.NoRender;
import rich.modules.impl.render.Particles;
import rich.modules.impl.render.SeeInvisible;
import rich.modules.impl.render.SwingAnimation;
import rich.modules.impl.render.TargetESP;
import rich.modules.impl.render.ViewModel;
import rich.modules.impl.render.WorldParticles;
import rich.modules.impl.world.PacketMine;
import rich.modules.impl.world.Scaffold;
import rich.modules.module.DuplicateModuleException;
import rich.modules.module.ModuleBuilder;
import rich.modules.module.ModuleStructure;

public class ModuleRepository {
    private final List<ModuleStructure> moduleStructures = new ArrayList<ModuleStructure>();
    private final List<ModuleStructure> hiddenModules = new ArrayList<ModuleStructure>();
    private final Set<Class<? extends ModuleStructure>> registeredClasses = new HashSet<Class<? extends ModuleStructure>>();

    public void setup() {
        this.builder().add(new Hud()).add(new Aura()).add(new CrystalAura()).add(new Surround()).add(new HitEffect()).add(new Esp()).add(new BlockESP()).add(new StorageEsp()).add(new Waypoints()).add(new AutoTool()).add(new RegionExploit()).add(new WorldParticles()).add(new Arrows()).add(new Particles()).add(new AuctionHelper()).add(new GlassHands()).add(new ChunkAnimator()).add(new MaceTarget()).add(new TriggerBot()).add(new BowSpammer()).add(new Ambience()).add(new AutoTotem()).add(new TapeMouse()).add(new ElytraHelper()).add(new ChinaHat()).add(new AutoPotion()).add(new Jesus()).add(new ClientSounds()).add(new AutoGApple()).add(new ServerHelper()).add(new WindJump()).add(new TargetESP()).add(new BlockOverlay()).add(new HitSound()).add(new ClickPearl()).add(new JumpCircle()).add(new ItemScroller()).add(new TargetStrafe()).add(new AutoLeave()).add(new Strafe()).add(new AutoDuel()).add(new NoWeb()).add(new AutoTpAccept()).add(new Spider()).add(new ClickFriend()).add(new FreeLook()).add(new Fly()).add(new ElytraMotion()).add(new FullBright()).add(new CameraSettings()).add(new ItemPhysic()).add(new NoDelay()).add(new ServerRPSpoofer()).add(new SeeInvisible()).add(new AutoPilot()).add(new NoFallDamage()).add(new NoRender()).add(new ShiftTap()).add(new HitBoxModule()).add(new WaterSpeed()).add(new NameProtect()).add(new NoFriendDamage()).add(new ProjectileHelper()).add(new InventoryMove()).add(new ChestStealer()).add(new NoInteract()).add(new AntiBot()).add(new ViewModel()).add(new SuperFireWork()).add(new LongJump()).add(new ElytraTarget()).add(new FreeCam()).add(new Speed()).add(new NoEntityTrace()).add(new AutoRespawn()).add(new AutoSwap()).add(new NoPush()).add(new NoSlow()).add(new Velocity()).add(new SwingAnimation()).add(new AutoSprint()).add(new AutoBuy()).add(new Scaffold()).add(new PacketMine()).add(new BetterChat()).add(new Criticals()).add(new Step()).add(new ReverseStep()).add(new AutoEat()).add(new BetterTooltips()).hidden(new AutoParser());
    }

    public ModuleBuilder builder() {
        return new ModuleBuilder(this);
    }

    void registerModule(ModuleStructure module, boolean hidden) {
        Class<? extends ModuleStructure> clazz = (Class<? extends ModuleStructure>)(Class<?>)module.getClass();
        if (this.registeredClasses.contains(clazz)) {
            throw new DuplicateModuleException(clazz.getSimpleName());
        }
        this.registeredClasses.add(clazz);
        if (hidden) {
            this.hiddenModules.add(module);
            module.setState(true);
        } else {
            this.moduleStructures.add(module);
        }
    }

    public List<ModuleStructure> modules() {
        return this.moduleStructures;
    }

    public List<ModuleStructure> hiddenModules() {
        return this.hiddenModules;
    }

    public List<ModuleStructure> allModules() {
        ArrayList<ModuleStructure> all = new ArrayList<ModuleStructure>(this.moduleStructures);
        all.addAll(this.hiddenModules);
        return all;
    }
}

