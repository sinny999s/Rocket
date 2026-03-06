
package rich.modules.impl.combat.aura.target;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import rich.IMinecraft;
import rich.util.timer.StopWatch;

public class Vector {
    private static final Random random = new Random();
    private static final StopWatch pointTimer = new StopWatch();
    private static final StopWatch updateTimer = new StopWatch();
    private static List<Vec3> cachedPoints = new ArrayList<Vec3>();
    private static int currentPointIndex = 0;

    public static Vec3 hitbox(Entity entity, float X, float Y, float Z, float WIDTH) {
        double wHalf = entity.getBbWidth() / WIDTH;
        double yExpand = Mth.clamp((double)(entity.getEyeY() - entity.getY()), (double)0.0, (double)entity.getBbHeight());
        double xExpand = Mth.clamp((double)(IMinecraft.mc.player.getX() - entity.getX()), (double)(-wHalf), (double)wHalf);
        double zExpand = Mth.clamp((double)(IMinecraft.mc.player.getZ() - entity.getZ()), (double)(-wHalf), (double)wHalf);
        return new Vec3(entity.getX() + xExpand / (double)X, entity.getY() + yExpand / (double)Y, entity.getZ() + zExpand / (double)Z);
    }

    public static Vec3 brain(Entity entity, float min, float max) {
        double normalizedDistance;
        double distance = IMinecraft.mc.player.position().distanceTo(entity.position());
        double heightFactor = normalizedDistance = Mth.clamp((double)((distance - (double)min) / (double)(max - min)), (double)0.0, (double)1.0);
        double minHeight = 0.2;
        double maxHeight = 0.8;
        double targetHeight = minHeight + (maxHeight - minHeight) * heightFactor;
        double targetY = entity.getY() + (double)entity.getBbHeight() * targetHeight;
        return new Vec3(entity.getX(), targetY, entity.getZ());
    }

    public static Vec3 custom(Entity entity, int pointCount, float switchDelay) {
        if (updateTimer.every(1000.0) || cachedPoints.isEmpty()) {
            Vector.generateRandomPoints(entity, pointCount);
            currentPointIndex = 0;
            pointTimer.reset();
        }
        if (pointTimer.finished(switchDelay)) {
            currentPointIndex = (currentPointIndex + 1) % cachedPoints.size();
            pointTimer.reset();
        }
        if (cachedPoints.isEmpty()) {
            return entity.position();
        }
        return cachedPoints.get(currentPointIndex);
    }

    private static void generateRandomPoints(Entity entity, int pointCount) {
        cachedPoints.clear();
        double width = entity.getBbWidth();
        double height = entity.getBbHeight();
        Vec3 entityPos = entity.position();
        for (int i = 0; i < pointCount; ++i) {
            double x = entityPos.x + (random.nextDouble() - 0.5) * width;
            double y = entityPos.y + random.nextDouble() * height;
            double z = entityPos.z + (random.nextDouble() - 0.5) * width;
            cachedPoints.add(new Vec3(x, y, z));
        }
    }

    public static List<Vec3> getAllCachedPoints() {
        return new ArrayList<Vec3>(cachedPoints);
    }

    public static int getCurrentPointIndex() {
        return currentPointIndex;
    }

    public static long getTimeSinceLastSwitch() {
        return pointTimer.elapsedTime();
    }

    public static void clearCache() {
        cachedPoints.clear();
        currentPointIndex = 0;
        pointTimer.reset();
        updateTimer.reset();
    }

    public static void forceUpdate(Entity entity, int pointCount) {
        Vector.generateRandomPoints(entity, pointCount);
        currentPointIndex = 0;
        pointTimer.reset();
        updateTimer.reset();
    }
}

