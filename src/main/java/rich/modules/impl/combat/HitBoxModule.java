
package rich.modules.impl.combat;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import rich.events.api.EventHandler;
import rich.events.impl.BoundingBoxControlEvent;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.SliderSettings;
import rich.util.repository.friend.FriendUtils;

public class HitBoxModule
extends ModuleStructure {
    private final SliderSettings xzExpandSetting = new SliderSettings("XZ expansion", "Allows expanding hitbox on XZ axes").setValue(0.2f).range(0.0f, 3.0f);
    private final SliderSettings yExpandSetting = new SliderSettings("Y expansion", "Allows expanding hitbox on Y axis").setValue(0.0f).range(0.0f, 3.0f);

    public HitBoxModule() {
        super("HitBox", "Hit Box", ModuleCategory.COMBAT);
        this.settings(this.xzExpandSetting, this.yExpandSetting);
    }

    @EventHandler
    public void onBoundingBoxControl(BoundingBoxControlEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof LivingEntity) {
            LivingEntity living = (LivingEntity)entity;
            AABB box = event.getBox();
            float xzExpand = this.xzExpandSetting.getValue();
            float yExpand = this.yExpandSetting.getValue();
            AABB changedBox = new AABB(box.minX - (double)(xzExpand / 2.0f), box.minY - (double)(yExpand / 2.0f), box.minZ - (double)(xzExpand / 2.0f), box.maxX + (double)(xzExpand / 2.0f), box.maxY + (double)(yExpand / 2.0f), box.maxZ + (double)(xzExpand / 2.0f));
            if (living != HitBoxModule.mc.player && !FriendUtils.isFriend(living)) {
                event.setBox(changedBox);
            }
        }
    }
}

