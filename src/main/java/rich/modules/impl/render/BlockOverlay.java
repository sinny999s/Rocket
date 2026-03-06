
package rich.modules.impl.render;

import java.awt.Color;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import rich.events.api.EventHandler;
import rich.events.impl.WorldRenderEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.util.Instance;
import rich.util.render.Render3D;

public class BlockOverlay
extends ModuleStructure {
    public static BlockOverlay getInstance() {
        return Instance.get(BlockOverlay.class);
    }

    public BlockOverlay() {
        super("BlockOverlay", "Block Overlay", ModuleCategory.RENDER);
    }

    @EventHandler
    public void onWorldRender(WorldRenderEvent e) {
        BlockHitResult result;
        HitResult hitResult = BlockOverlay.mc.hitResult;
        if (hitResult instanceof BlockHitResult && (result = (BlockHitResult)hitResult).getType().equals((Object)HitResult.Type.BLOCK)) {
            BlockPos pos = result.getBlockPos();
            Render3D.drawShapeAlternative(pos, BlockOverlay.mc.level.getBlockState(pos).getShape(BlockOverlay.mc.level, pos), new Color(109, 252, 255, 230).getRGB(), 1.5f, true, true);
        }
    }
}

