package rich.modules.impl.render;

import java.awt.Color;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.AABB;
import rich.events.api.EventHandler;
import rich.events.impl.WorldRenderEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.modules.module.setting.implement.ColorSetting;
import rich.modules.module.setting.implement.MultiSelectSetting;
import rich.modules.module.setting.implement.SliderSettings;
import rich.util.render.Render3D;

public class StorageEsp extends ModuleStructure {

    public final MultiSelectSetting storageTypes = new MultiSelectSetting("Types", "Storage block types to highlight")
        .value("Chest", "Trapped Chest", "Ender Chest", "Barrel", "Shulker", "Hopper", "Dispenser", "Dropper", "Furnace", "Brewing Stand")
        .selected("Chest", "Trapped Chest", "Ender Chest", "Barrel", "Shulker");
    public final ColorSetting chestColor = new ColorSetting("Chest Color", "Color for chests").setColor(new Color(255, 160, 0, 180).getRGB());
    public final ColorSetting trappedChestColor = new ColorSetting("Trapped Color", "Color for trapped chests").setColor(new Color(255, 0, 0, 180).getRGB());
    public final ColorSetting enderChestColor = new ColorSetting("Ender Color", "Color for ender chests").setColor(new Color(120, 0, 255, 180).getRGB());
    public final ColorSetting barrelColor = new ColorSetting("Barrel Color", "Color for barrels").setColor(new Color(255, 160, 0, 180).getRGB());
    public final ColorSetting shulkerColor = new ColorSetting("Shulker Color", "Color for shulker boxes").setColor(new Color(255, 0, 255, 180).getRGB());
    public final ColorSetting otherColor = new ColorSetting("Other Color", "Color for other storage").setColor(new Color(140, 140, 140, 180).getRGB());
    public final BooleanSetting tracers = new BooleanSetting("Tracers", "Draw lines to storage blocks").setValue(false);
    public final SliderSettings lineWidth = new SliderSettings("Line Width", "Width of outline lines").setValue(1.5f).range(0.5f, 5.0f);

    private int count = 0;

    public StorageEsp() {
        super("StorageEsp", "Storage ESP", ModuleCategory.RENDER);
        this.settings(this.storageTypes, this.chestColor, this.trappedChestColor, this.enderChestColor, this.barrelColor, this.shulkerColor, this.otherColor, this.tracers, this.lineWidth);
    }

    @EventHandler
    public void onWorldRender(WorldRenderEvent event) {
        if (mc.level == null || mc.player == null) return;
        count = 0;

        BlockPos playerPos = mc.player.blockPosition();
        int chunkRadius = 4;
        int playerChunkX = playerPos.getX() >> 4;
        int playerChunkZ = playerPos.getZ() >> 4;
        for (int cx = -chunkRadius; cx <= chunkRadius; cx++) {
            for (int cz = -chunkRadius; cz <= chunkRadius; cz++) {
                int chunkX = playerChunkX + cx;
                int chunkZ = playerChunkZ + cz;
                if (!mc.level.getChunkSource().hasChunk(chunkX, chunkZ)) continue;
                var chunk = mc.level.getChunkSource().getChunkNow(chunkX, chunkZ);
                if (chunk == null) continue;
                for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                    renderBlockEntity(blockEntity);
                }
            }
        }
    }

    private void renderBlockEntity(BlockEntity blockEntity) {
        int color = getColor(blockEntity);
        if (color == 0) return;

        BlockPos pos = blockEntity.getBlockPos();
        double x1 = pos.getX();
        double y1 = pos.getY();
        double z1 = pos.getZ();
        double x2 = pos.getX() + 1.0;
        double y2 = pos.getY() + 1.0;
        double z2 = pos.getZ() + 1.0;

        double inset = 1.0 / 16.0;

        if (blockEntity instanceof ChestBlockEntity chestBE && !(blockEntity instanceof EnderChestBlockEntity)) {
            BlockState state = chestBE.getBlockState();
            if (state.getBlock() instanceof ChestBlock) {
                ChestType chestType = state.getValue(ChestBlock.TYPE);

                if (chestType == ChestType.LEFT) return;

                if (chestType == ChestType.RIGHT) {
                    Direction facing = state.getValue(ChestBlock.FACING);
                    BlockPos otherPos = pos.relative(facing.getCounterClockWise());

                    double ox1 = Math.min(pos.getX(), otherPos.getX());
                    double oz1 = Math.min(pos.getZ(), otherPos.getZ());
                    double ox2 = Math.max(pos.getX(), otherPos.getX()) + 1.0;
                    double oz2 = Math.max(pos.getZ(), otherPos.getZ()) + 1.0;

                    x1 = ox1 + inset;
                    z1 = oz1 + inset;
                    x2 = ox2 - inset;
                    y2 -= inset * 2;
                    z2 = oz2 - inset;
                } else {
                    x1 += inset;
                    z1 += inset;
                    x2 -= inset;
                    y2 -= inset * 2;
                    z2 -= inset;
                }
            }
        } else if (blockEntity instanceof EnderChestBlockEntity) {
            x1 += inset;
            z1 += inset;
            x2 -= inset;
            y2 -= inset * 2;
            z2 -= inset;
        }

        AABB box = new AABB(x1, y1, z1, x2, y2, z2);
        Render3D.drawBox(box, color, lineWidth.getValue());

        if (tracers.isValue()) {
            net.minecraft.world.phys.Vec3 center = new net.minecraft.world.phys.Vec3(
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5
            );
            net.minecraft.world.phys.Vec3 eyes = mc.player.getEyePosition();
            Render3D.drawLine(eyes, center, color | 0xFF000000, 1.0f, true);
        }

        count++;
    }

    private int getColor(BlockEntity blockEntity) {
        if (blockEntity instanceof TrappedChestBlockEntity) {
            return storageTypes.isSelected("Trapped Chest") ? trappedChestColor.getColor() : 0;
        }
        if (blockEntity instanceof ChestBlockEntity) {
            return storageTypes.isSelected("Chest") ? chestColor.getColor() : 0;
        }
        if (blockEntity instanceof EnderChestBlockEntity) {
            return storageTypes.isSelected("Ender Chest") ? enderChestColor.getColor() : 0;
        }
        if (blockEntity instanceof BarrelBlockEntity) {
            return storageTypes.isSelected("Barrel") ? barrelColor.getColor() : 0;
        }
        if (blockEntity instanceof ShulkerBoxBlockEntity) {
            return storageTypes.isSelected("Shulker") ? shulkerColor.getColor() : 0;
        }
        if (blockEntity instanceof HopperBlockEntity) {
            return storageTypes.isSelected("Hopper") ? otherColor.getColor() : 0;
        }
        if (blockEntity instanceof DispenserBlockEntity) {
            if (blockEntity.getBlockState().is(Blocks.DROPPER)) {
                return storageTypes.isSelected("Dropper") ? otherColor.getColor() : 0;
            }
            return storageTypes.isSelected("Dispenser") ? otherColor.getColor() : 0;
        }
        if (blockEntity instanceof AbstractFurnaceBlockEntity) {
            return storageTypes.isSelected("Furnace") ? otherColor.getColor() : 0;
        }
        if (blockEntity instanceof BrewingStandBlockEntity) {
            return storageTypes.isSelected("Brewing Stand") ? otherColor.getColor() : 0;
        }
        return 0;
    }
}
