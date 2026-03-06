
package rich.modules.impl.combat.aura.target;

import java.security.SecureRandom;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;
import lombok.Generated;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import rich.IMinecraft;
import rich.modules.impl.combat.Aura;
import rich.modules.impl.combat.aura.Angle;
import rich.modules.impl.combat.aura.MathAngle;
import rich.modules.impl.combat.aura.target.RaycastAngle;

public class MultiPoint
implements IMinecraft {
    private final Random random = new SecureRandom();
    private Vec3 offset = Vec3.ZERO;

    public Tuple<Vec3, AABB> computeVector(LivingEntity entity, float maxDistance, Angle initialAngle, Vec3 velocity, boolean ignoreWalls) {
        Tuple<List<Vec3>, AABB> candidatePoints = this.generateCandidatePoints(entity, maxDistance, ignoreWalls);
        Vec3 bestVector = this.findBestVector((List)candidatePoints.getA(), initialAngle);
        if (Aura.getInstance().options.isSelected("Height interpolation") && entity != null) {
            double wHalf = entity.getBbWidth() / 6.0f;
            double yExpand = net.minecraft.util.Mth.clamp((double)(entity.getEyeY() - entity.getY()), (double)0.0, (double)entity.getBbHeight());
            double xExpand = net.minecraft.util.Mth.clamp((double)(MultiPoint.mc.player.getX() - entity.getX()), (double)(-wHalf), (double)wHalf);
            double zExpand = net.minecraft.util.Mth.clamp((double)(MultiPoint.mc.player.getZ() - entity.getZ()), (double)(-wHalf), (double)wHalf);
            bestVector = new Vec3(entity.getX() + xExpand / 1.0, entity.getY() + yExpand / 1.1, entity.getZ() + zExpand / 1.0);
        }
        return new Tuple<Vec3, AABB>((bestVector == null ? entity.getEyePosition() : bestVector).add(this.offset), (AABB)candidatePoints.getB());
    }

    public Tuple<List<Vec3>, AABB> generateCandidatePoints(LivingEntity entity, float maxDistance, boolean ignoreWalls) {
        AABB entityBox = entity.getBoundingBox();
        double stepY = entityBox.getYsize() / 10.0;
        List list = Stream.iterate(entityBox.minY, y -> y <= entityBox.maxY, y -> y + stepY).map(y -> new Vec3(entityBox.getCenter().x, (double)y, entityBox.getCenter().z)).filter(point -> this.isValidPoint(MultiPoint.mc.player.getEyePosition(), (Vec3)point, maxDistance, ignoreWalls)).toList();
        return new Tuple<List<Vec3>, AABB>(list, entityBox);
    }

    public boolean hasValidPoint(LivingEntity entity, float maxDistance, boolean ignoreWalls) {
        AABB entityBox = entity.getBoundingBox();
        double stepY = entityBox.getYsize() / 10.0;
        return Stream.iterate(entityBox.minY, y -> y < entityBox.maxY, y -> y + stepY).map(y -> new Vec3(entityBox.getCenter().x, (double)y, entityBox.getCenter().z)).anyMatch(point -> this.isValidPoint(MultiPoint.mc.player.getEyePosition(), (Vec3)point, maxDistance, ignoreWalls));
    }

    private boolean isValidPoint(Vec3 startPoint, Vec3 endPoint, float maxDistance, boolean ignoreWalls) {
        return startPoint.distanceTo(endPoint) <= (double)maxDistance && (ignoreWalls || !RaycastAngle.raycast(startPoint, endPoint, ClipContext.Block.COLLIDER).getType().equals((Object)HitResult.Type.BLOCK));
    }

    private Vec3 findBestVector(List<Vec3> candidatePoints, Angle initialAngle) {
        return candidatePoints.stream().min(Comparator.comparing(point -> this.calculateRotationDifference(MultiPoint.mc.player.getEyePosition(), (Vec3)point, initialAngle))).orElse(null);
    }

    private double calculateRotationDifference(Vec3 startPoint, Vec3 endPoint, Angle initialAngle) {
        Angle targetAngle = MathAngle.fromVec3d(endPoint.subtract(startPoint));
        Angle delta = MathAngle.calculateDelta(initialAngle, targetAngle);
        return Math.hypot(delta.getYaw(), delta.getPitch());
    }

    private void updateOffset(Vec3 velocity) {
        this.offset = this.offset.add(this.random.nextGaussian(), this.random.nextGaussian(), this.random.nextGaussian()).multiply(velocity);
    }

    @Generated
    public Random getRandom() {
        return this.random;
    }

    @Generated
    public Vec3 getOffset() {
        return this.offset;
    }
}

