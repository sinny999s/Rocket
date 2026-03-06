
package rich.modules.impl.render;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import rich.events.api.EventHandler;
import rich.events.impl.WorldRenderEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.modules.module.setting.implement.ColorSetting;
import rich.modules.module.setting.implement.SliderSettings;
import rich.util.ColorUtil;
import rich.util.config.impl.blockesp.BlockESPConfig;
import rich.util.render.Render3D;
import rich.util.string.chat.ChatMessage;

public class BlockESP
extends ModuleStructure {
    ColorSetting color = new ColorSetting("Color", "Color block highlight").value(ColorUtil.getColor(255, 0, 0, 255));
    SliderSettings range = new SliderSettings("Radius", "Block search radius").range(1, 128).setValue(32.0f);
    BooleanSetting notifyInChat = new BooleanSetting("Notifications", "Show found block coordinates in chat").setValue(false);
    Set<String> blocksToHighlight = new CopyOnWriteArraySet<String>();
    Map<BlockPos, BlockState> renderBlocks = new HashMap<BlockPos, BlockState>();
    Set<BlockPos> notifiedBlocks = new CopyOnWriteArraySet<BlockPos>();
    long lastScanTime = 0L;
    int checkCounter = 0;

    public BlockESP() {
        super("BlockESP", "Block ESP", ModuleCategory.RENDER);
        this.settings(this.color, this.range, this.notifyInChat);
    }

    public Set<String> getBlocksToHighlight() {
        return this.blocksToHighlight;
    }

    @Override
    public void activate() {
        this.blocksToHighlight.clear();
        this.blocksToHighlight.addAll(BlockESPConfig.getInstance().getBlocks());
        this.notifiedBlocks.clear();
    }

    @Override
    public void deactivate() {
        this.renderBlocks.clear();
        this.notifiedBlocks.clear();
    }

    @EventHandler
    public void onRender3D(WorldRenderEvent event) {
        if (!this.state || BlockESP.mc.level == null || BlockESP.mc.player == null) {
            this.renderBlocks.clear();
            return;
        }
        if (this.blocksToHighlight.isEmpty()) {
            this.renderBlocks.clear();
            return;
        }
        BlockPos playerPos = BlockESP.mc.player.blockPosition();
        long currentTime = System.nanoTime() / 1000000L;
        if (currentTime - this.lastScanTime >= 2000L) {
            this.renderBlocks.clear();
            int chunkRange = 2;
            int yRange = 48;
            for (int x = -chunkRange; x <= chunkRange; ++x) {
                for (int z = -chunkRange; z <= chunkRange; ++z) {
                    LevelChunk chunk;
                    int chunkX = (playerPos.getX() >> 4) + x;
                    int chunkZ = (playerPos.getZ() >> 4) + z;
                    if (!BlockESP.mc.level.getChunkSource().hasChunk(chunkX, chunkZ) || (chunk = BlockESP.mc.level.getChunkSource().getChunkNow(chunkX, chunkZ)) == null) continue;
                    int cx = chunk.getPos().x << 4;
                    int cz = chunk.getPos().z << 4;
                    for (int bx = 0; bx < 16; ++bx) {
                        for (int bz = 0; bz < 16; ++bz) {
                            int minY = Math.max(BlockESP.mc.level.getMinY(), playerPos.getY() - yRange);
                            int maxY = Math.min(BlockESP.mc.level.getHeight(Heightmap.Types.WORLD_SURFACE, cx + bx, cz + bz), playerPos.getY() + yRange);
                            for (int by = minY; by <= maxY; ++by) {
                                Block block;
                                String blockName;
                                BlockPos pos2 = new BlockPos(cx + bx, by, cz + bz);
                                double dist = BlockESP.mc.player.distanceToSqr((double)pos2.getX() + 0.5, (double)pos2.getY() + 0.5, (double)pos2.getZ() + 0.5);
                                if (dist > (double)(this.range.getValue() * this.range.getValue()) || !this.blocksToHighlight.contains(blockName = BuiltInRegistries.BLOCK.getKey(block = BlockESP.mc.level.getBlockState(pos2).getBlock()).toString())) continue;
                                this.renderBlocks.put(pos2.immutable(), BlockESP.mc.level.getBlockState(pos2));
                                if (!this.notifyInChat.isValue() || this.notifiedBlocks.contains(pos2)) continue;
                                this.notifyBlockFound(pos2, blockName);
                                this.notifiedBlocks.add(pos2);
                            }
                        }
                    }
                }
            }
            this.lastScanTime = currentTime;
            this.checkCounter = 0;
        }
        if (this.checkCounter % 5 == 0) {
            int nearChunkRange = 1;
            for (int x = -nearChunkRange; x <= nearChunkRange; ++x) {
                for (int z = -nearChunkRange; z <= nearChunkRange; ++z) {
                    LevelChunk chunk;
                    int chunkX = (playerPos.getX() >> 4) + x;
                    int chunkZ = (playerPos.getZ() >> 4) + z;
                    if (!BlockESP.mc.level.getChunkSource().hasChunk(chunkX, chunkZ) || (chunk = BlockESP.mc.level.getChunkSource().getChunkNow(chunkX, chunkZ)) == null) continue;
                    int cx = chunk.getPos().x << 4;
                    int cz = chunk.getPos().z << 4;
                    for (int bx = 0; bx < 16; ++bx) {
                        for (int bz = 0; bz < 16; ++bz) {
                            int minY = Math.max(BlockESP.mc.level.getMinY(), playerPos.getY() - 24);
                            int maxY = Math.min(BlockESP.mc.level.getHeight(Heightmap.Types.WORLD_SURFACE, cx + bx, cz + bz), playerPos.getY() + 24);
                            for (int by = minY; by <= maxY; ++by) {
                                Block block;
                                String blockName;
                                BlockPos pos3 = new BlockPos(cx + bx, by, cz + bz);
                                double dist = BlockESP.mc.player.distanceToSqr((double)pos3.getX() + 0.5, (double)pos3.getY() + 0.5, (double)pos3.getZ() + 0.5);
                                if (dist > 16.0 || !this.blocksToHighlight.contains(blockName = BuiltInRegistries.BLOCK.getKey(block = BlockESP.mc.level.getBlockState(pos3).getBlock()).toString()) || this.renderBlocks.containsKey(pos3)) continue;
                                this.renderBlocks.put(pos3.immutable(), BlockESP.mc.level.getBlockState(pos3));
                                if (!this.notifyInChat.isValue() || this.notifiedBlocks.contains(pos3)) continue;
                                this.notifyBlockFound(pos3, blockName);
                                this.notifiedBlocks.add(pos3);
                            }
                        }
                    }
                }
            }
        }
        if (this.checkCounter % 60 == 0) {
            this.renderBlocks.entrySet().removeIf(entry -> {
                boolean shouldRemove;
                BlockPos pos = (BlockPos)entry.getKey();
                Block block = BlockESP.mc.level.getBlockState(pos).getBlock();
                String blockName = BuiltInRegistries.BLOCK.getKey(block).toString();
                boolean bl = shouldRemove = !this.blocksToHighlight.contains(blockName);
                if (shouldRemove) {
                    this.notifiedBlocks.remove(pos);
                }
                return shouldRemove;
            });
        }
        ++this.checkCounter;
        this.renderBlocks.forEach((pos, blockState) -> Render3D.drawBox(new AABB((BlockPos)pos), this.color.getColor(), 1.0f));
    }

    private void notifyBlockFound(BlockPos pos, String blockName) {
        if (BlockESP.mc.player != null) {
            ChatMessage.brandmessage("Found block " + blockName + " -> " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ());
        }
    }
}

