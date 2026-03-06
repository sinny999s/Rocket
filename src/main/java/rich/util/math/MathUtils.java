/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  org.joml.Matrix3x2fStack
 *  org.joml.Vector3d
 */
package rich.util.math;

import java.util.concurrent.ThreadLocalRandom;
import lombok.Generated;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3x2fStack;
import org.joml.Vector3d;
import rich.IMinecraft;

public final class MathUtils
implements IMinecraft {
    public static double PI2 = Math.PI * 2;
    private static float contextAlpha = 1.0f;

    public static boolean isHovered(double mouseX, double mouseY, double x, double y, double width, double height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public static float clamp(float num, float min, float max) {
        return num < min ? min : Math.min(num, max);
    }

    public static double computeGcd() {
        return Math.pow((Double)MathUtils.mc.options.sensitivity().get() * 0.6 + 0.2, 3.0) * 1.2;
    }

    public static int getRandom(int min, int max) {
        return (int)MathUtils.getRandom((float)min, (float)max + 1.0f);
    }

    public static float getRandom(float min, float max) {
        return (float)MathUtils.getRandom((double)min, (double)max);
    }

    public static double getRandom(double min, double max) {
        if (min == max) {
            return min;
        }
        if (min > max) {
            double d = min;
            min = max;
            max = d;
        }
        return ThreadLocalRandom.current().nextDouble(min, max);
    }

    public static void scale(Matrix3x2fStack stack, float x, float y, float scaleX, float scaleY, Runnable data) {
        float sumScale = scaleX * scaleY;
        if (sumScale != 1.0f && sumScale > 0.0f) {
            float prevAlpha = contextAlpha;
            contextAlpha = sumScale;
            stack.pushMatrix();
            stack.translate(x, y);
            stack.scale(scaleX, scaleY);
            stack.translate(-x, -y);
            data.run();
            stack.popMatrix();
            contextAlpha = prevAlpha;
        } else if (sumScale >= 1.0f) {
            data.run();
        }
    }

    public static float textScrolling(float textWidth) {
        int speed = (int)(textWidth * 75.0f);
        return (float)Mth.clamp((double)((double)(System.currentTimeMillis() % (long)speed) * Math.PI / (double)speed), (double)0.0, (double)1.0) * textWidth;
    }

    public static double round(double num, double increment) {
        double rounded = (double)Math.round(num / increment) * increment;
        return (double)Math.round(rounded * 100.0) / 100.0;
    }

    public static int floorNearestMulN(int x, int n) {
        return n * (int)Math.floor((double)x / (double)n);
    }

    public static int getRed(int hex) {
        return hex >> 16 & 0xFF;
    }

    public static int getGreen(int hex) {
        return hex >> 8 & 0xFF;
    }

    public static int getBlue(int hex) {
        return hex & 0xFF;
    }

    public static int getAlpha(int hex) {
        return hex >> 24 & 0xFF;
    }

    public static int applyOpacity(int color, float opacity) {
        return ARGB.color((int)((int)((float)MathUtils.getAlpha(color) * opacity / 255.0f)), (int)MathUtils.getRed(color), (int)MathUtils.getGreen(color), (int)MathUtils.getBlue(color));
    }

    public static int applyContextAlpha(int color) {
        int a = (int)((float)MathUtils.getAlpha(color) * contextAlpha);
        return ARGB.color((int)a, (int)MathUtils.getRed(color), (int)MathUtils.getGreen(color), (int)MathUtils.getBlue(color));
    }

    public static Vec3 cosSin(int i, int size, double width) {
        int index = Math.min(i, size);
        float cos = (float)(Math.cos((double)index * PI2 / (double)size) * width);
        float sin = (float)(-Math.sin((double)index * PI2 / (double)size) * width);
        return new Vec3(cos, 0.0, sin);
    }

    public static double absSinAnimation(double input) {
        return Math.abs(1.0 + Math.sin(input)) / 2.0;
    }

    public static Vector3d interpolate(Vector3d prevPos, Vector3d pos) {
        return new Vector3d(MathUtils.interpolate(prevPos.x, pos.x), MathUtils.interpolate(prevPos.y, pos.y), MathUtils.interpolate(prevPos.z, pos.z));
    }

    public static float interpolate(float prev, float to, float value) {
        return prev + (to - prev) * value;
    }

    public static Vec3 interpolate(Vec3 prevPos, Vec3 pos) {
        return new Vec3(MathUtils.interpolate(prevPos.x, pos.x), MathUtils.interpolate(prevPos.y, pos.y), MathUtils.interpolate(prevPos.z, pos.z));
    }

    public static Vec3 interpolate(Entity entity) {
        if (entity == null) {
            return Vec3.ZERO;
        }
        return new Vec3(MathUtils.interpolate(entity.xo, entity.getX()), MathUtils.interpolate(entity.yo, entity.getY()), MathUtils.interpolate(entity.zo, entity.getZ()));
    }

    public static float interpolate(float prev, float orig) {
        return Mth.lerp((float)tickCounter.getGameTimeDeltaPartialTick(false), (float)prev, (float)orig);
    }

    public static double interpolate(double prev, double orig) {
        return Mth.lerp((double)tickCounter.getGameTimeDeltaPartialTick(false), (double)prev, (double)orig);
    }

    public static float interpolateSmooth(double smooth, float prev, float orig) {
        return (float)Mth.lerp((double)((double)tickCounter.getRealtimeDeltaTicks() / smooth), (double)prev, (double)orig);
    }

    @Generated
    private MathUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    @Generated
    public static float getContextAlpha() {
        return contextAlpha;
    }
}

