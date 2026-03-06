/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Quaternionfc
 */
package rich.modules.impl.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import org.joml.Quaternionfc;
import rich.events.api.EventHandler;
import rich.events.impl.HandAnimationEvent;
import rich.events.impl.SwingDurationEvent;
import rich.modules.impl.combat.Aura;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.modules.module.setting.implement.SelectSetting;
import rich.modules.module.setting.implement.SliderSettings;

public class SwingAnimation
extends ModuleStructure {
    private final SelectSetting swingType = new SelectSetting("Swing type", "Select swing type").value("Chop", "Swipe", "Down", "Smooth", "Smooth 2", "Power", "Feast", "Twist", "Default");
    private final SliderSettings hitStrengthSetting = new SliderSettings("Swing strength", "Swing animation strength").range(0.5f, 3.0f).setValue(1.0f);
    private final SliderSettings swingSpeedSetting = new SliderSettings("Swing duration", "Attack animation duration").range(0.5f, 4.0f).setValue(1.0f);
    private final BooleanSetting onlySwing = new BooleanSetting("Only on swing", "Show animation only on swing").setValue(false);
    private final BooleanSetting onlyAura = new BooleanSetting("Only with KillAura enabled", "Show animation only with killaura enabled").setValue(false);
    private float spinAngle = 0.0f;
    private float spinBackTimer = 0.0f;
    private boolean wasSwinging = false;

    public SwingAnimation() {
        super("SwingAnimation", "Swing Animation", ModuleCategory.RENDER);
        this.settings(this.swingType, this.hitStrengthSetting, this.swingSpeedSetting, this.onlySwing, this.onlyAura);
    }

    @EventHandler
    public void onSwingDuration(SwingDurationEvent e) {
        block3: {
            block2: {
                if (!this.onlyAura.isValue()) break block2;
                if (!Aura.getInstance().isState()) break block3;
                Aura.getInstance();
                if (Aura.target == null) break block3;
            }
            e.setAnimation(this.swingSpeedSetting.getValue());
            e.cancel();
        }
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @EventHandler
    public void onHandAnimation(HandAnimationEvent e) {
        boolean isMainHand = e.getHand().equals((Object)InteractionHand.MAIN_HAND);
        if (!isMainHand) return;
        PoseStack matrix = e.getMatrices();
        float swingProgress = e.getSwingProgress();
        int i = SwingAnimation.mc.player.getMainArm().equals(HumanoidArm.RIGHT) ? 1 : -1;
        float sin1 = Mth.sin((double)(swingProgress * swingProgress * (float)Math.PI));
        float sin2 = Mth.sin((double)(Mth.sqrt((float)swingProgress) * (float)Math.PI));
        float sinSmooth = (float)(Math.sin((double)swingProgress * Math.PI) * 0.5);
        float strength = this.hitStrengthSetting.getValue();
        if (this.onlyAura.isValue()) {
            if (!Aura.getInstance().isState()) return;
            Aura.getInstance();
            if (Aura.target == null) return;
        }
        if (!this.onlySwing.isValue() || SwingAnimation.mc.player.swingTime != 0) {
            switch (this.swingType.getSelected()) {
                case "Chop": {
                    matrix.translate(0.56f * (float)i, -0.44f, -0.72f);
                    matrix.translate(0.0f, -0.19800001f, 0.0f);
                    matrix.mulPose((Quaternionfc)Axis.YP.rotationDegrees(45.0f * (float)i));
                    float f = Mth.sin((double)(swingProgress * swingProgress * (float)Math.PI));
                    float f2 = Mth.sin((double)(Mth.sqrt((float)swingProgress) * (float)Math.PI));
                    matrix.mulPose((Quaternionfc)Axis.ZP.rotationDegrees(f2 * -20.0f * (float)i * strength));
                    matrix.mulPose((Quaternionfc)Axis.XP.rotationDegrees(f2 * -80.0f * strength));
                    matrix.translate(0.4f, 0.2f, 0.2f);
                    matrix.translate(-0.5f, 0.08f, 0.0f);
                    matrix.mulPose((Quaternionfc)Axis.YP.rotationDegrees(20.0f));
                    matrix.mulPose((Quaternionfc)Axis.XP.rotationDegrees(-80.0f));
                    matrix.mulPose((Quaternionfc)Axis.YP.rotationDegrees(20.0f));
                    break;
                }
                case "Twist": {
                    matrix.translate((float)i * 0.56f, -0.36f, -0.72f);
                    matrix.mulPose((Quaternionfc)Axis.YP.rotationDegrees(80 * i));
                    matrix.mulPose((Quaternionfc)Axis.XP.rotationDegrees(sin2 * -90.0f * strength));
                    matrix.mulPose((Quaternionfc)Axis.ZP.rotationDegrees((sin1 - sin2) * 60.0f * (float)i * strength));
                    matrix.mulPose((Quaternionfc)Axis.XP.rotationDegrees(-30.0f));
                    matrix.translate(0.0f, -0.1f, 0.05f);
                    break;
                }
                case "Swipe": {
                    matrix.translate(0.56f * (float)i, -0.32f, -0.72f);
                    matrix.mulPose((Quaternionfc)Axis.YP.rotationDegrees(70 * i));
                    matrix.mulPose((Quaternionfc)Axis.ZP.rotationDegrees(-20 * i));
                    matrix.mulPose((Quaternionfc)Axis.YP.rotationDegrees(sin2 * sin1 * -5.0f * strength));
                    matrix.mulPose((Quaternionfc)Axis.XP.rotationDegrees(sin2 * sin1 * -120.0f * strength));
                    matrix.mulPose((Quaternionfc)Axis.XP.rotationDegrees(-70.0f));
                    break;
                }
                case "Default": {
                    matrix.translate((float)i * 0.56f, -0.52f - sin2 * 0.5f * strength, -0.72f);
                    matrix.mulPose((Quaternionfc)Axis.YP.rotationDegrees(45 * i));
                    matrix.mulPose((Quaternionfc)Axis.YP.rotationDegrees(-45 * i));
                    break;
                }
                case "Down": {
                    matrix.translate((float)i * 0.56f, -0.32f, -0.72f);
                    matrix.mulPose((Quaternionfc)Axis.YP.rotationDegrees(76 * i));
                    matrix.mulPose((Quaternionfc)Axis.YP.rotationDegrees(sin2 * -5.0f * strength));
                    matrix.mulPose((Quaternionfc)Axis.XN.rotationDegrees(sin2 * -100.0f * strength));
                    matrix.mulPose((Quaternionfc)Axis.XP.rotationDegrees(sin2 * -155.0f * strength));
                    matrix.mulPose((Quaternionfc)Axis.XP.rotationDegrees(-100.0f));
                    break;
                }
                case "Smooth": {
                    matrix.translate((float)i * 0.56f, -0.42f, -0.72f);
                    matrix.mulPose((Quaternionfc)Axis.YP.rotationDegrees((float)i * (45.0f + sin1 * -20.0f * strength)));
                    matrix.mulPose((Quaternionfc)Axis.ZP.rotationDegrees((float)i * sin2 * -20.0f * strength));
                    matrix.mulPose((Quaternionfc)Axis.XP.rotationDegrees(sin2 * -80.0f * strength));
                    matrix.mulPose((Quaternionfc)Axis.YP.rotationDegrees((float)i * -45.0f));
                    matrix.translate(0.0, -0.1, 0.0);
                    break;
                }
                case "Smooth 2": {
                    matrix.translate((float)i * 0.56f, -0.42f, -0.72f);
                    matrix.mulPose((Quaternionfc)Axis.XP.rotationDegrees(sin2 * -80.0f * strength));
                    matrix.translate(0.0, -0.1, 0.0);
                    break;
                }
                case "Power": {
                    matrix.translate((float)i * 0.56f, -0.32f, -0.72f);
                    matrix.translate(-sinSmooth * sinSmooth * sin1 * (float)i * strength, 0.0f, 0.0f);
                    matrix.mulPose((Quaternionfc)Axis.YP.rotationDegrees(61 * i));
                    matrix.mulPose((Quaternionfc)Axis.ZP.rotationDegrees(sin2 * strength));
                    matrix.mulPose((Quaternionfc)Axis.YP.rotationDegrees(sin2 * sin1 * -5.0f * strength));
                    matrix.mulPose((Quaternionfc)Axis.XP.rotationDegrees(sin2 * sin1 * -30.0f * strength));
                    matrix.mulPose((Quaternionfc)Axis.XP.rotationDegrees(-60.0f));
                    matrix.mulPose((Quaternionfc)Axis.XP.rotationDegrees(sinSmooth * -60.0f * strength));
                    break;
                }
                case "Feast": {
                    matrix.translate((float)i * 0.56f, -0.32f, -0.72f);
                    matrix.mulPose((Quaternionfc)Axis.YP.rotationDegrees(30 * i));
                    matrix.mulPose((Quaternionfc)Axis.YP.rotationDegrees(sin2 * 75.0f * (float)i * strength));
                    matrix.mulPose((Quaternionfc)Axis.XP.rotationDegrees(sin2 * -45.0f * strength));
                    matrix.mulPose((Quaternionfc)Axis.YP.rotationDegrees(30 * i));
                    matrix.mulPose((Quaternionfc)Axis.XP.rotationDegrees(-80.0f));
                    matrix.mulPose((Quaternionfc)Axis.YP.rotationDegrees(35 * i));
                }
            }
        } else {
            matrix.translate((float)i * 0.56f, -0.52f, -0.72f);
        }
        e.cancel();
    }
}

