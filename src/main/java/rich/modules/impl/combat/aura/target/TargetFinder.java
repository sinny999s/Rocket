
package rich.modules.impl.combat.aura.target;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import lombok.Generated;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import rich.IMinecraft;
import rich.modules.impl.combat.AntiBot;
import rich.modules.impl.combat.aura.AngleConnection;
import rich.modules.impl.combat.aura.MathAngle;
import rich.modules.impl.combat.aura.impl.LinearConstructor;
import rich.modules.impl.combat.aura.target.MultiPoint;
import rich.modules.impl.combat.aura.target.RaycastAngle;
import rich.util.repository.friend.FriendUtils;

public class TargetFinder
implements IMinecraft {
    private final MultiPoint pointFinder = new MultiPoint();
    private LivingEntity currentTarget = null;
    private Stream<LivingEntity> potentialTargets;

    public void lockTarget(LivingEntity target) {
        if (this.currentTarget == null) {
            this.currentTarget = target;
        }
    }

    public void releaseTarget() {
        this.currentTarget = null;
    }

    public void validateTarget(Predicate<LivingEntity> predicate) {
        this.findFirstMatch(predicate).ifPresent(this::lockTarget);
        if (this.currentTarget != null && !predicate.test(this.currentTarget)) {
            this.releaseTarget();
        }
    }

    public void searchTargets(Iterable<Entity> entities, float maxDistance, float maxFov, boolean ignoreWalls) {
        if (this.currentTarget != null && (!this.pointFinder.hasValidPoint(this.currentTarget, maxDistance, ignoreWalls) || this.getFov(this.currentTarget, maxDistance, ignoreWalls) > (double)maxFov)) {
            this.releaseTarget();
        }
        this.potentialTargets = this.createStreamFromEntities(entities, maxDistance, maxFov, ignoreWalls);
    }

    private double getFov(LivingEntity entity, float maxDistance, boolean ignoreWalls) {
        Vec3 attackVector = (Vec3)this.pointFinder.computeVector(entity, maxDistance, AngleConnection.INSTANCE.getRotation(), new LinearConstructor().randomValue(), ignoreWalls).getA();
        return RaycastAngle.rayTrace(maxDistance, entity.getBoundingBox()) ? 0.0 : AngleConnection.computeRotationDifference(MathAngle.cameraAngle(), MathAngle.calculateAngle(attackVector));
    }

    private Stream<LivingEntity> createStreamFromEntities(Iterable<Entity> entities, float maxDistance, float maxFov, boolean ignoreWalls) {
        return StreamSupport.stream(entities.spliterator(), false).filter(LivingEntity.class::isInstance).map(LivingEntity.class::cast).filter(entity -> this.pointFinder.hasValidPoint((LivingEntity)entity, maxDistance, ignoreWalls) && this.getFov((LivingEntity)entity, maxDistance, ignoreWalls) < (double)maxFov).sorted(Comparator.comparingDouble(entity -> entity.distanceTo(TargetFinder.mc.player)));
    }

    private Optional<LivingEntity> findFirstMatch(Predicate<LivingEntity> predicate) {
        return this.potentialTargets.filter(predicate).findFirst();
    }

    @Generated
    public MultiPoint getPointFinder() {
        return this.pointFinder;
    }

    @Generated
    public LivingEntity getCurrentTarget() {
        return this.currentTarget;
    }

    @Generated
    public Stream<LivingEntity> getPotentialTargets() {
        return this.potentialTargets;
    }

    public static class EntityFilter {
        private final List<String> targetSettings;

        public boolean isValid(LivingEntity entity) {
            if (this.isLocalPlayer(entity)) {
                return false;
            }
            if (this.isInvalidHealth(entity)) {
                return false;
            }
            if (this.isBotPlayer(entity)) {
                return false;
            }
            return this.isValidEntityType(entity);
        }

        private boolean isLocalPlayer(LivingEntity entity) {
            return entity == IMinecraft.mc.player;
        }

        private boolean isInvalidHealth(LivingEntity entity) {
            return !entity.isAlive() || entity.getHealth() <= 0.0f;
        }

        /*
         * Enabled force condition propagation
         * Lifted jumps to return sites
         */
        private boolean isBotPlayer(LivingEntity entity) {
            if (!(entity instanceof Player)) return false;
            Player player = (Player)entity;
            if (!AntiBot.getInstance().isBot(player)) return false;
            return true;
        }

        private boolean isFriendPlayer(LivingEntity entity) {
            return entity instanceof Player && FriendUtils.isFriend(entity);
        }

        private boolean isValidEntityType(LivingEntity entity) {
            if (entity instanceof Player) {
                Player player = (Player)entity;
                if (FriendUtils.isFriend(player)) {
                    return this.targetSettings.contains("Friends");
                }
                return this.targetSettings.contains("Players");
            }
            if (entity instanceof Animal) {
                return this.targetSettings.contains("Animals");
            }
            if (entity instanceof Mob) {
                return this.targetSettings.contains("Mobs");
            }
            if (entity instanceof ArmorStand) {
                return this.targetSettings.contains("Armor stands");
            }
            return false;
        }

        @Generated
        public EntityFilter(List<String> targetSettings) {
            this.targetSettings = targetSettings;
        }
    }
}

