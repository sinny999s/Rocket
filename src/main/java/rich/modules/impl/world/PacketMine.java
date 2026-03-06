package rich.modules.impl.world;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import rich.events.api.EventHandler;
import rich.events.impl.StartBlockBreakEvent;
import rich.events.impl.TickEvent;
import rich.events.impl.WorldRenderEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.modules.module.setting.implement.SliderSettings;
import rich.util.render.Render3D;

public class PacketMine extends ModuleStructure {

    private final SliderSettings delay = new SliderSettings("Delay", "Delay in ticks before mining starts").setValue(1.0f).range(0, 10);
    private final BooleanSetting autoSwitch = new BooleanSetting("AutoSwitch", "Auto switch to best tool when block is ready").setValue(false);
    private final BooleanSetting notOnUse = new BooleanSetting("NotOnUse", "Don't auto switch while using an item").setValue(true);
    private final BooleanSetting rotate = new BooleanSetting("Rotate", "Send rotation packets when mining").setValue(true);
    private final BooleanSetting render = new BooleanSetting("Render", "Render breaking indicator on target block").setValue(true);

    private final List<MineBlock> blocks = new ArrayList<>();
    private boolean swapped = false;
    private boolean shouldUpdateSlot = false;

    private static PacketMine instance;

    public PacketMine() {
        super("PacketMine", "Sends packets to mine blocks without the mining animation.", ModuleCategory.WORLD);
        this.settings(this.delay, this.autoSwitch, this.notOnUse, this.rotate, this.render);
        instance = this;
    }

    public static PacketMine getInstance() {
        return instance;
    }

    @Override
    public void activate() {
        this.swapped = false;
        this.shouldUpdateSlot = false;
    }

    @Override
    public void deactivate() {
        this.blocks.clear();
        if (this.shouldUpdateSlot && mc.player != null) {
            mc.player.connection.send(new ServerboundSetCarriedItemPacket(mc.player.getInventory().getSelectedSlot()));
            this.shouldUpdateSlot = false;
        }
    }

    @EventHandler
    public void onStartBlockBreak(StartBlockBreakEvent event) {
        if (mc.player == null || mc.level == null) return;

        BlockPos pos = event.getBlockPos();
        Direction dir = event.getDirection();
        BlockState state = mc.level.getBlockState(pos);

        if (state.isAir()) return;
        if (state.getDestroySpeed(mc.level, pos) < 0) return;

        event.cancel();
        this.swapped = false;

        if (!this.isMining(pos)) {
            this.blocks.add(new MineBlock(pos, dir, state, (int) this.delay.getValue()));
        }
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (mc.player == null || mc.level == null) return;

        this.blocks.removeIf(MineBlock::shouldRemove);

        if (this.shouldUpdateSlot) {
            mc.player.connection.send(new ServerboundSetCarriedItemPacket(mc.player.getInventory().getSelectedSlot()));
            this.shouldUpdateSlot = false;
            this.swapped = false;
        }

        if (!this.blocks.isEmpty()) {
            MineBlock block = this.blocks.get(0);
            block.mine();

            if (block.isReady() && !this.swapped && this.autoSwitch.isValue() && (!mc.player.isUsingItem() || !this.notOnUse.isValue())) {
                int bestSlot = this.findBestToolSlot(block.blockState);
                if (bestSlot != -1 && bestSlot != mc.player.getInventory().getSelectedSlot()) {
                    mc.player.connection.send(new ServerboundSetCarriedItemPacket(bestSlot));
                    this.swapped = true;
                    this.shouldUpdateSlot = true;
                }
            }
        }
    }

    @EventHandler
    public void onRender3D(WorldRenderEvent event) {
        if (mc.player == null || mc.level == null || !this.render.isValue()) return;

        for (MineBlock block : this.blocks) {
            if (!block.mining) continue;
            double progress = Math.min(block.progress(), 1.0);
            int red = (int)(255 * (1.0 - progress));
            int green = (int)(255 * progress);
            int color = (255 << 24) | (red << 16) | (green << 8);
            AABB box = new AABB(block.blockPos);
            Render3D.drawBox(box, color, 1.5f);
        }
    }

    public boolean isMining(BlockPos pos) {
        for (MineBlock block : this.blocks) {
            if (block.blockPos.equals(pos)) return true;
        }
        return false;
    }

    private int findBestToolSlot(BlockState state) {
        float bestSpeed = 1.0f;
        int bestSlot = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getItem(i);
            float speed = stack.getDestroySpeed(state);
            if (speed > bestSpeed) {
                bestSpeed = speed;
                bestSlot = i;
            }
        }
        return bestSlot;
    }

    private float getBlockBreakDelta(int slot, BlockState state) {
        float hardness = state.getDestroySpeed(mc.level, BlockPos.ZERO);
        if (hardness < 0) return 0.0f;
        ItemStack stack = mc.player.getInventory().getItem(slot);
        float speed = stack.getDestroySpeed(state);
        return speed / hardness / 30.0f;
    }

    public class MineBlock {
        public final BlockPos blockPos;
        public final Direction direction;
        public final BlockState blockState;
        public final Block block;
        public int timer;
        public int startTime;
        public boolean mining;

        public MineBlock(BlockPos pos, Direction dir, BlockState state, int delay) {
            this.blockPos = pos;
            this.direction = dir;
            this.blockState = state;
            this.block = state.getBlock();
            this.timer = delay;
            this.mining = false;
            this.startTime = 0;
        }

        public boolean shouldRemove() {
            if (mc.level == null || mc.player == null) return true;
            boolean broken = mc.level.getBlockState(this.blockPos).getBlock() != this.block;
            boolean timeout = this.progress() > 2.0 && (mc.player.tickCount - this.startTime > 50);
            double dx = mc.player.getX() - (this.blockPos.getX() + 0.5);
            double dy = mc.player.getEyeY() - (this.blockPos.getY() + 0.5);
            double dz = mc.player.getZ() - (this.blockPos.getZ() + 0.5);
            boolean distance = Math.sqrt(dx * dx + dy * dy + dz * dz) > mc.player.blockInteractionRange();
            return broken || timeout || distance;
        }

        public boolean isReady() {
            return this.progress() >= 1.0;
        }

        public double progress() {
            if (!this.mining) return 0.0;
            int bestSlot = PacketMine.this.findBestToolSlot(this.blockState);
            int slot = bestSlot != -1 ? bestSlot : mc.player.getInventory().getSelectedSlot();
            float delta = PacketMine.this.getBlockBreakDelta(slot, this.blockState);
            return delta * ((mc.player.tickCount - this.startTime) + 1);
        }

        public void mine() {
            if (this.timer > 0) {
                this.timer--;
                return;
            }
            if (!this.mining) {
                mc.player.connection.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, this.blockPos, this.direction));
                mc.player.connection.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, this.blockPos, this.direction));
                this.mining = true;
                this.startTime = mc.player.tickCount;
            }
        }
    }
}
