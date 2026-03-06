/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
 */
package rich.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerSkin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={AbstractClientPlayer.class})
public class CustomCapeMixin {
    @Unique
    private static final Identifier CAPE_ID = Identifier.fromNamespaceAndPath((String)"rich", (String)"capes/cape");
    @Unique
    private static final ClientAsset.ResourceTexture CAPE_ASSET = new ClientAsset.ResourceTexture(CAPE_ID);

    @Inject(method={"getSkin"}, at={@At(value="RETURN")}, cancellable=true)
    private void replaceCape(CallbackInfoReturnable<PlayerSkin> cir) {
        AbstractClientPlayer player = (AbstractClientPlayer)((Object)this);
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || !player.getUUID().equals(client.player.getUUID())) {
            return;
        }
        PlayerSkin old = (PlayerSkin)((Object)cir.getReturnValue());
        cir.setReturnValue(new PlayerSkin(old.body(), CAPE_ASSET, CAPE_ASSET, old.model(), old.secure()));
    }
}

