
package rich.util.network;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import lombok.Generated;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.prediction.BlockStatePredictionHandler;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerInputPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.BlockHitResult;
import rich.IMinecraft;
import rich.mixin.ClientConnectionAccessor;
import rich.mixin.IClientWorld;
import rich.modules.impl.combat.aura.Angle;
import rich.util.timer.TimerUtil;

public final class NetworkUtility
implements IMinecraft {
    private static boolean shouldTriggerEvent = true;
    private static boolean serverSprinting = false;
    private static float tpsFactor = 0.0f;
    private static int received = 0;
    private static long lastReceive = 0L;
    private static TimerUtil tpsTimer = new TimerUtil();

    public static void pauseEvents() {
        shouldTriggerEvent = false;
    }

    public static void resumeEvents() {
        shouldTriggerEvent = true;
    }

    public static boolean shouldTriggerEvent() {
        return shouldTriggerEvent;
    }

    public static void updateServerSprint(boolean sprint) {
        serverSprinting = sprint;
    }

    public static boolean serverSprinting() {
        return serverSprinting;
    }

    public static void sendWithoutEvent(Runnable runnable) {
        NetworkUtility.pauseEvents();
        runnable.run();
        NetworkUtility.resumeEvents();
    }

    public static void sendWithoutEvent(Packet<?> packet) {
        NetworkUtility.pauseEvents();
        NetworkUtility.send(packet);
        NetworkUtility.resumeEvents();
    }

    public static void send(Packet<?> packet) {
        if (mc.getConnection() == null) {
            return;
        }
        if (packet instanceof ServerboundContainerClickPacket) {
            ServerboundContainerClickPacket click = (ServerboundContainerClickPacket)packet;
            NetworkUtility.mc.gameMode.handleInventoryMouseClick(click.containerId(), click.slotNum(), click.buttonNum(), click.clickType(), NetworkUtility.mc.player);
        } else {
            mc.getConnection().send(packet);
        }
    }

    public static void sendInputPacket(boolean forward, boolean backward, boolean left, boolean right, boolean jump, boolean sneak, boolean sprint) {
        Input input = new Input(forward, backward, left, right, jump, sneak, sprint);
        mc.getConnection().send(new ServerboundPlayerInputPacket(input));
    }

    public static void sendOnlySneak(boolean sneak) {
        Input playerInput = NetworkUtility.mc.player.input.keyPresses;
        NetworkUtility.sendInputPacket(playerInput.forward(), playerInput.backward(), playerInput.left(), playerInput.right(), playerInput.jump(), sneak, playerInput.sprint());
    }

    public static void sendUse(InteractionHand hand) {
        NetworkUtility.sendUse(hand, new Angle(NetworkUtility.mc.player.getYRot(), NetworkUtility.mc.player.getXRot()));
    }

    public static void sendUse(InteractionHand hand, Angle angle) {
        try (BlockStatePredictionHandler pendingUpdateManager = ((IClientWorld)((Object)NetworkUtility.mc.level)).client$pending().startPredicting();){
            int i = pendingUpdateManager.currentSequence();
            ServerboundUseItemPacket packet = new ServerboundUseItemPacket(hand, i, angle.getYaw(), angle.getPitch());
            NetworkUtility.send(packet);
        }
    }

    public static void sendUse(InteractionHand hand, BlockHitResult hitResult) {
        try (BlockStatePredictionHandler pendingUpdateManager = ((IClientWorld)((Object)NetworkUtility.mc.level)).client$pending().startPredicting();){
            int i = pendingUpdateManager.currentSequence();
            ServerboundUseItemOnPacket packet = new ServerboundUseItemOnPacket(hand, hitResult, i);
            NetworkUtility.send(packet);
        }
    }

    public static boolean is(String server) {
        return mc.getConnection() != null && mc.getConnection().getServerData() != null && NetworkUtility.mc.getConnection().getServerData().ip.contains(server);
    }

    public static void handleCPacket(Packet<?> packet) {
        if (packet instanceof ServerboundMovePlayerPacket) {
            ServerboundMovePlayerPacket e = (ServerboundMovePlayerPacket)packet;
            PlayerState.lastGround = e.isOnGround();
            PlayerState.lastVertical = NetworkUtility.mc.player.verticalCollision;
        }
    }

    public static void handleSPacket(Packet<?> packet) {
        if (packet instanceof ClientboundSetTimePacket) {
            ClientboundSetTimePacket e = (ClientboundSetTimePacket)packet;
            lastReceive = System.currentTimeMillis();
        }
    }

    public static void handlePacket(Packet<?> packet) {
        ClientPacketListener clientPlayNetworkHandler = mc.getConnection();
        if (!(clientPlayNetworkHandler instanceof ClientPacketListener)) {
            return;
        }
        ClientPacketListener net = clientPlayNetworkHandler;
        if (mc.isSameThread()) {
            ClientConnectionAccessor.handlePacket(packet, net);
        } else {
            mc.execute(() -> ClientConnectionAccessor.handlePacket(packet, net));
        }
    }

    public static UUID offlineUUID(String name) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
    }

    @Generated
    private NetworkUtility() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    @Generated
    public static float getTpsFactor() {
        return tpsFactor;
    }

    public static final class PlayerState {
        public static boolean lastGround = false;
        public static boolean lastVertical = false;
        public static int lastTp = 0;

        @Generated
        private PlayerState() {
            throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
        }
    }
}

