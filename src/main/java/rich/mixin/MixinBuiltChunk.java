/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 */
package rich.mixin;

import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import rich.util.interfaces.IBuiltChunkAnimator;

@Mixin(value={SectionRenderDispatcher.RenderSection.class})
public class MixinBuiltChunk
implements IBuiltChunkAnimator {
    @Unique
    private float animation = 100.0f;

    @Override
    public float getAnimation() {
        return this.animation;
    }

    @Override
    public void setAnimation(float value) {
        this.animation = value;
    }
}

