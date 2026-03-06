
package rich.modules.impl.render.worldparticles;

import net.minecraft.world.phys.Vec3;
import rich.modules.impl.render.worldparticles.Particle;

public class ParticleSpawner {
    private static final double MIN_RADIUS = 3.0;
    private static final double MAX_RADIUS = 60.0;
    private static final double MAX_HEIGHT = 25.0;
    private static final double DESPAWN_DISTANCE = 65.0;

    public static Particle createParticle(Vec3 playerPos, Vec3 playerVelocity, double playerSpeed, long lifeTimeMs, Particle.ParticleType type) {
        double radius = 3.0 + Math.random() * 57.0;
        double angle = Math.random() * Math.PI * 2.0;
        double spawnX = playerPos.x;
        double spawnZ = playerPos.z;
        if (playerSpeed > 0.05 && playerVelocity.horizontalDistance() > 0.01) {
            Vec3 normalizedVelocity = playerVelocity.normalize();
            double forwardAngle = Math.atan2(normalizedVelocity.z, normalizedVelocity.x);
            double angleSpread = 1.2566370614359172;
            angle = forwardAngle + (Math.random() - 0.5) * angleSpread * 2.0;
            double forwardOffset = radius * 0.7 * Math.min(playerSpeed * 8.0, 1.0);
            spawnX += normalizedVelocity.x * forwardOffset;
            spawnZ += normalizedVelocity.z * forwardOffset;
        }
        double finalX = spawnX + Math.cos(angle) * radius;
        double finalZ = spawnZ + Math.sin(angle) * radius;
        double finalY = playerPos.y - 5.0 + Math.random() * 25.0;
        double mx = (Math.random() - 0.5) * 0.08;
        double my = (Math.random() - 0.5) * 0.02;
        double mz = (Math.random() - 0.5) * 0.08;
        return new Particle(finalX, finalY, finalZ, mx, my, mz, lifeTimeMs).setType(type);
    }

    public static Particle createParticle(Vec3 playerPos, Vec3 playerVelocity, double playerSpeed, long lifeTimeMs) {
        return ParticleSpawner.createParticle(playerPos, playerVelocity, playerSpeed, lifeTimeMs, Particle.ParticleType.CUBE_3D);
    }

    public static int calculateSpawnDelay(double playerSpeed) {
        int baseDelay;
        int actualDelay = baseDelay = 40;
        if (playerSpeed > 0.05) {
            double speedFactor = Math.min(playerSpeed * 5.0, 4.0);
            actualDelay = (int)((double)baseDelay / (1.0 + speedFactor));
            actualDelay = Math.max(actualDelay, 8);
        }
        return actualDelay;
    }

    public static int calculateSpawnCount(double playerSpeed, int currentCount, int maxCount) {
        int spawnCount = 1;
        if (playerSpeed > 0.1) {
            spawnCount = Math.min(8, maxCount - currentCount);
            spawnCount = Math.max(1, (int)((double)spawnCount * Math.min(playerSpeed * 5.0, 1.0)));
        }
        return spawnCount;
    }

    public static double getDespawnDistance() {
        return 65.0;
    }

    public static double getDespawnDistanceSquared() {
        return 4225.0;
    }
}

