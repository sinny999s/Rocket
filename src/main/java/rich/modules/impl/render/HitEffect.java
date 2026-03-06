
package rich.modules.impl.render;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import rich.IMinecraft;
import rich.events.api.EventHandler;
import rich.events.impl.AttackEvent;
import rich.events.impl.WorldRenderEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.ColorSetting;
import rich.util.ColorUtil;
import rich.util.Instance;
import rich.util.render.Render3D;

public class HitEffect
extends ModuleStructure {
    private final List<WaveEffect> waveEffects = Collections.synchronizedList(new ArrayList());
    public ColorSetting colorSetting = new ColorSetting("Color", "Select effect color").setColor(new Color(137, 97, 72, 255).getRGB());

    public static HitEffect getInstance() {
        return Instance.get(HitEffect.class);
    }

    public HitEffect() {
        super("HitEffect", "Hit Effect", ModuleCategory.RENDER);
        this.settings(this.colorSetting);
    }

    public void addWave(BlockPos pos) {
        BlockPos groundPos;
        if (HitEffect.mc.level != null && pos != null && (groundPos = this.findGround(pos)) != null) {
            this.waveEffects.add(new WaveEffect(groundPos, System.currentTimeMillis()));
        }
    }

    private BlockPos findGround(BlockPos pos) {
        for (int y = 0; y <= 10; ++y) {
            BlockPos down = pos.below(y);
            if (!HitEffect.mc.level.isInWorldBounds(down) || HitEffect.mc.level.getBlockState(down).isAir()) continue;
            return down;
        }
        return pos;
    }

    @EventHandler
    public void onAttack(AttackEvent e) {
        if (!this.isState()) {
            return;
        }
        if (e.getTarget() != null) {
            this.addWave(e.getTarget().blockPosition());
        }
    }

    @EventHandler
    public void onWorldRender(WorldRenderEvent e) {
        if (this.waveEffects.isEmpty() || HitEffect.mc.level == null) {
            return;
        }
        Iterator<WaveEffect> iterator = this.waveEffects.iterator();
        while (iterator.hasNext()) {
            WaveEffect wave = iterator.next();
            if (wave.isExpired()) {
                iterator.remove();
                continue;
            }
            wave.render();
        }
    }

    private class WaveEffect {
        private final BlockPos centerPos;
        private final long startTime;
        private final long duration = 475L;
        private final int maxRadius = 8;
        private Map<Long, Integer> reachableBlocks;
        private boolean calculated = false;

        public WaveEffect(BlockPos centerPos, long startTime) {
            this.centerPos = centerPos;
            this.startTime = startTime;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() - this.startTime > 475L;
        }

        private void calculateReachableBlocks() {
            if (this.calculated) {
                return;
            }
            this.calculated = true;
            this.reachableBlocks = new HashMap<Long, Integer>();
            LinkedList<BlockPos> queue = new LinkedList<BlockPos>();
            HashMap<Long, Integer> visited = new HashMap<Long, Integer>();
            BlockPos startPos = this.centerPos;
            if (IMinecraft.mc.level.getBlockState(startPos).isAir()) {
                for (int y = 1; y <= 5; ++y) {
                    BlockPos down = startPos.below(y);
                    if (IMinecraft.mc.level.getBlockState(down).isAir()) continue;
                    startPos = down;
                    break;
                }
            }
            queue.add(startPos);
            visited.put(startPos.asLong(), 0);
            while (!queue.isEmpty()) {
                VoxelShape shape;
                BlockPos current = (BlockPos)queue.poll();
                int currentDistance = (Integer)visited.get(current.asLong());
                if (currentDistance > 8) continue;
                BlockState state = IMinecraft.mc.level.getBlockState(current);
                if (!state.isAir() && !(shape = state.getShape(IMinecraft.mc.level, current)).isEmpty()) {
                    this.reachableBlocks.put(current.asLong(), currentDistance);
                }
                for (Direction dir : Direction.values()) {
                    long aboveLong;
                    BlockPos above;
                    long belowLong;
                    BlockPos neighbor = current.relative(dir);
                    if (!IMinecraft.mc.level.isInWorldBounds(neighbor)) continue;
                    long neighborLong = neighbor.asLong();
                    int newDistance = currentDistance + 1;
                    if (visited.containsKey(neighborLong) && (Integer)visited.get(neighborLong) <= newDistance || newDistance > 8) continue;
                    BlockState neighborState = IMinecraft.mc.level.getBlockState(neighbor);
                    if (!neighborState.isAir()) {
                        visited.put(neighborLong, newDistance);
                        queue.add(neighbor);
                        continue;
                    }
                    BlockPos below = neighbor.below();
                    if (!(!IMinecraft.mc.level.isInWorldBounds(below) || IMinecraft.mc.level.getBlockState(below).isAir() || visited.containsKey(belowLong = below.asLong()) && (Integer)visited.get(belowLong) <= newDistance)) {
                        visited.put(belowLong, newDistance);
                        queue.add(below);
                    }
                    if (!IMinecraft.mc.level.isInWorldBounds(above = neighbor.above()) || IMinecraft.mc.level.getBlockState(above).isAir() || visited.containsKey(aboveLong = above.asLong()) && (Integer)visited.get(aboveLong) <= newDistance) continue;
                    visited.put(aboveLong, newDistance);
                    queue.add(above);
                }
            }
        }

        public void render() {
            if (IMinecraft.mc.level == null) {
                return;
            }
            this.calculateReachableBlocks();
            if (this.reachableBlocks == null || this.reachableBlocks.isEmpty()) {
                return;
            }
            long elapsed = System.currentTimeMillis() - this.startTime;
            float progress = (float)elapsed / 475.0f;
            float currentRadius = progress * 8.0f;
            float waveWidth = 2.5f;
            float globalAlpha = 1.0f - progress;
            globalAlpha = (float)Math.pow(globalAlpha, 0.5);
            int rendered = 0;
            int maxPerFrame = 500;
            for (Map.Entry<Long, Integer> entry : this.reachableBlocks.entrySet()) {
                VoxelShape shape;
                BlockPos pos;
                BlockState state;
                if (rendered >= maxPerFrame) break;
                int blockDistance = entry.getValue();
                if ((float)blockDistance < currentRadius - waveWidth || (float)blockDistance > currentRadius + 0.5f || (state = IMinecraft.mc.level.getBlockState(pos = BlockPos.of((long)entry.getKey()))).isAir() || (shape = state.getShape(IMinecraft.mc.level, pos)).isEmpty()) continue;
                ++rendered;
                float localAlpha = 1.0f - Math.abs((float)blockDistance - currentRadius) / waveWidth;
                localAlpha = Math.max(0.0f, Math.min(1.0f, localAlpha));
                if (!((localAlpha *= globalAlpha) > 0.02f)) continue;
                int baseColor = HitEffect.this.colorSetting.getColor();
                int color = ColorUtil.setAlpha(baseColor, (int)(localAlpha * 75.0f));
                try {
                    Render3D.drawShapeAlternative(pos, shape, color, 1.0f, true, true);
                }
                catch (Exception exception) {}
            }
        }
    }
}

