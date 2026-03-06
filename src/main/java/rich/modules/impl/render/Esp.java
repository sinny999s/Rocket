/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Vector4d
 */
package rich.modules.impl.render;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector4d;
import rich.events.api.EventHandler;
import rich.events.impl.DrawEvent;
import rich.events.impl.TickEvent;
import rich.events.impl.WorldLoadEvent;
import rich.events.impl.WorldRenderEvent;
import rich.modules.impl.combat.AntiBot;
import rich.modules.module.ModuleStructure;
import rich.modules.module.category.ModuleCategory;
import rich.modules.module.setting.implement.BooleanSetting;
import rich.modules.module.setting.implement.ColorSetting;
import rich.modules.module.setting.implement.MultiSelectSetting;
import rich.modules.module.setting.implement.SelectSetting;
import rich.modules.module.setting.implement.SliderSettings;
import rich.util.ColorUtil;
import rich.util.Instance;
import rich.util.math.Projection;
import rich.util.modules.esp.RwPrefix;
import rich.util.network.Network;
import rich.util.render.Render2D;
import rich.util.render.Render3D;
import rich.util.render.font.Fonts;
import rich.util.render.item.ItemRender;
import rich.util.repository.friend.FriendUtils;
import rich.util.string.PlayerInteractionHelper;

public class Esp
extends ModuleStructure {
    private final Identifier TEXTURE = Identifier.fromNamespaceAndPath((String)"rich", (String)"textures/features/esp/container.png");
    private final List<Player> players = new ArrayList<Player>();
    public final MultiSelectSetting entityType = new MultiSelectSetting("Entity type", "Entities that will be displayed").value("Player", "Item", "Hostile", "Peaceful", "Crystal", "Chest", "Shulker").selected("Player", "Item");
    private final MultiSelectSetting playerSetting = new MultiSelectSetting("Settings player", "Player settings").value("Box", "Armor", "NameTags", "Hand Items").selected("Box", "Armor", "NameTags", "Hand Items").visible(() -> this.entityType.isSelected("Player"));
    public final SelectSetting boxType = new SelectSetting("Type", "Type").value("Corner", "3D Box", "Glow").selected("Glow");
    public final ColorSetting boxColor = new ColorSetting("Color box", "Color for box display").value(-22016).visible(() -> this.playerSetting.isSelected("Box"));
    public final ColorSetting friendColor = new ColorSetting("Color friend", "Color for friend display").value(-16711936).visible(() -> this.playerSetting.isSelected("Box"));
    public final BooleanSetting flatBoxOutline = new BooleanSetting("Outline", "Outline for flat boxes").visible(() -> this.playerSetting.isSelected("Box") && this.boxType.isSelected("Corner"));
    public final SliderSettings boxAlpha = new SliderSettings("Transparency", "Transparency box").setValue(1.0f).range(0.1f, 1.0f).visible(() -> this.boxType.isSelected("3D Box"));
    public final SliderSettings outlineDistance = new SliderSettings("Distance", "Max glow distance").setValue(52.0f).range(15, 256).visible(() -> this.boxType.isSelected("Glow"));
    public final BooleanSetting showMobSpawns = new BooleanSetting("MobSpawn", "Highlight mob spawn locations").setValue(false);
    public final SliderSettings mobSpawnLight = new SliderSettings("SpawnLight", "Light level threshold").setValue(7.0f).range(0, 15).visible(() -> this.showMobSpawns.isValue());
    private static final float DISTANCE = 128.0f;
    private static final int GRAY_COLOR = -7829368;
    private static final int WHITE_COLOR = -1;

    public static Esp getInstance() {
        return Instance.get(Esp.class);
    }

    public Esp() {
        super("Esp", "Esp", ModuleCategory.RENDER);
        this.settings(this.entityType, this.boxType, this.playerSetting, this.boxColor, this.friendColor, this.flatBoxOutline, this.boxAlpha, this.outlineDistance, this.showMobSpawns, this.mobSpawnLight);
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent e) {
        this.players.clear();
    }

    @EventHandler
    public void onTick(TickEvent e) {
        this.players.clear();
        if (Esp.mc.level != null) {
            Esp.mc.level.players().stream().filter(player -> player != Esp.mc.player).filter(player -> player.getCustomName() == null || !player.getCustomName().getString().startsWith("Ghost_")).forEach(this.players::add);
        }
    }

    @EventHandler
    public void onWorldRender(WorldRenderEvent e) {
        if (!this.entityType.isSelected("Player")) {
            return;
        }
        float tickDelta = e.getPartialTicks();
        for (Player player : this.players) {
            if (player == null || player == Esp.mc.player || player.getCustomName() != null && player.getCustomName().getString().startsWith("Ghost_")) continue;
            double interpX = Mth.lerp((double)tickDelta, (double)player.xo, (double)player.getX());
            double interpY = Mth.lerp((double)tickDelta, (double)player.yo, (double)player.getY());
            double interpZ = Mth.lerp((double)tickDelta, (double)player.zo, (double)player.getZ());
            Vec3 interpCenter = new Vec3(interpX, interpY, interpZ);
            float distance = (float)Esp.mc.gameRenderer.getMainCamera().position().distanceTo(interpCenter);
            if (distance < 1.0f) continue;
            boolean friend = FriendUtils.isFriend(player);
            int baseColor = friend ? this.getFriendColor() : this.getClientColor();
            int alpha = (int)(this.boxAlpha.getValue() * 255.0f);
            int fillColor = baseColor & 0xFFFFFF | alpha << 24;
            int outlineColor = baseColor | 0xFF000000;
            if (!this.boxType.isSelected("3D Box") || !this.playerSetting.isSelected("Box")) continue;
            AABB interpBox = player.getDimensions(player.getPose()).makeBoundingBox(interpX, interpY, interpZ);
            Render3D.drawBox(interpBox, fillColor, 2.0f, true, true, false);
            Render3D.drawBox(interpBox, outlineColor, 2.0f, true, false, false);
        }
    }

    @EventHandler
    public void onDraw(DrawEvent e) {
        GuiGraphics context = e.getDrawContext();
        float tickDelta = e.getPartialTicks();
        float size = 5.5f;
        if (this.entityType.isSelected("Player")) {
            for (Player player : this.players) {
                if (player == null || player == Esp.mc.player || player.getCustomName() != null && player.getCustomName().getString().startsWith("Ghost_")) continue;
                Vector4d vec4d = Projection.getVector4D(player, tickDelta);
                float distance = (float)Esp.mc.gameRenderer.getMainCamera().position().distanceTo(player.getBoundingBox().getCenter());
                boolean friend = FriendUtils.isFriend(player);
                if (distance < 1.0f || Projection.cantSee(vec4d)) continue;
                if (this.playerSetting.isSelected("Box") && !this.boxType.isSelected("3D Box")) {
                    this.drawBox(friend, vec4d, player);
                }
                if (this.playerSetting.isSelected("Armor")) {
                    this.drawArmor(context, player, vec4d);
                }
                if (this.playerSetting.isSelected("Hand Items")) {
                    this.drawHands(context, player, vec4d, size);
                }
                this.drawPlayerName(context, player, friend, Projection.centerX(vec4d), vec4d.y - 2.0, size);
            }
        }
        List<Entity> entities = PlayerInteractionHelper.streamEntities().sorted(Comparator.comparing(ent -> {
            ItemEntity item;
            return ent instanceof ItemEntity && (item = (ItemEntity)ent).getItem().getHoverName().getContents().toString().equals("empty");
        })).toList();
        for (Entity entity : entities) {
            List list;
            if (!(entity instanceof ItemEntity)) continue;
            ItemEntity item = (ItemEntity)entity;
            if (!this.entityType.isSelected("Item")) continue;
            Vector4d vec4d = Projection.getVector4D(entity, tickDelta);
            ItemStack stack = item.getItem();
            ItemContainerContents compoundTag = (ItemContainerContents)stack.get(DataComponents.CONTAINER);
            List list2 = list = compoundTag != null ? compoundTag.stream().toList() : List.of();
            if (Projection.cantSee(vec4d)) continue;
            String text = item.getItem().getHoverName().getString();
            if (!list.isEmpty()) {
                this.drawShulkerBox(context, stack, list, vec4d);
                continue;
            }
            this.drawText(context, text, Projection.centerX(vec4d), vec4d.y, size);
        }
    }

    private void drawPlayerName(GuiGraphics context, Player player, boolean friend, double centerX, double startY, float size) {
        StringBuilder extraInfo = new StringBuilder();
        if (friend) {
            extraInfo.append("[Friend] ");
        }
        if (AntiBot.getInstance() != null && AntiBot.getInstance().isState() && AntiBot.getInstance().isBot(player)) {
            extraInfo.append("[BOT] ");
        }
        String displayName = this.playerSetting.isSelected("NameTags") ? player.getDisplayName().getString() : player.getName().getString();
        RwPrefix.ParsedName parsed = RwPrefix.parseDisplayName(displayName);
        String sphere = "";
        ItemStack offHand = player.getItemInHand(InteractionHand.OFF_HAND);
        if (offHand.getItem().equals(Items.PLAYER_HEAD) || offHand.getItem().equals(Items.TOTEM_OF_UNDYING)) {
            sphere = this.getSphere(offHand);
        }
        Object prefixPart = "";
        if (!parsed.prefix.isEmpty()) {
            prefixPart = parsed.prefix + " ";
        }
        String namePart = parsed.name;
        String clanPart = !parsed.clan.isEmpty() ? " " + parsed.clan : "";
        String spherePart = sphere;
        String extraPart = extraInfo.toString();
        float extraWidth = extraPart.isEmpty() ? 0.0f : Fonts.TEST.getWidth(extraPart, size);
        float prefixWidth = ((String)prefixPart).isEmpty() ? 0.0f : Fonts.TEST.getWidth((String)prefixPart, size);
        float nameWidth = Fonts.TEST.getWidth(namePart, size);
        float clanWidth = clanPart.isEmpty() ? 0.0f : Fonts.TEST.getWidth(clanPart, size);
        float sphereWidth = spherePart.isEmpty() ? 0.0f : Fonts.TEST.getWidth(spherePart, size);
        float totalWidth = extraWidth + prefixWidth + nameWidth + clanWidth + sphereWidth;
        float height = Fonts.TEST.getHeight(size);
        float posX = (float)centerX - totalWidth / 2.0f;
        float posY = (float)startY - height;
        Render2D.rect(posX - 4.0f, posY - 1.25f, totalWidth + 8.0f, height + 2.0f, Integer.MIN_VALUE, 2.0f);
        float drawX = posX;
        if (!extraPart.isEmpty()) {
            Fonts.TEST.draw(extraPart, drawX, posY, size, friend ? this.getFriendColor() : -43691);
            drawX += extraWidth;
        }
        if (!((String)prefixPart).isEmpty()) {
            Fonts.TEST.draw((String)prefixPart, drawX, posY, size, -7829368);
            drawX += prefixWidth;
        }
        Fonts.TEST.draw(namePart, drawX, posY, size, -1);
        drawX += nameWidth;
        if (!clanPart.isEmpty()) {
            Fonts.TEST.draw(clanPart, drawX, posY, size, -7829368);
            drawX += clanWidth;
        }
        if (!spherePart.isEmpty()) {
            Fonts.TEST.draw(spherePart, drawX, posY, size, -7829368);
        }
    }

    private void drawBox(boolean friend, Vector4d vec, Player player) {
        int client = friend ? this.getFriendColor() : this.getClientColor();
        int black = Integer.MIN_VALUE;
        float posX = (float)vec.x;
        float posY = (float)vec.y;
        float endPosX = (float)vec.z;
        float endPosY = (float)vec.w;
        float size = (endPosX - posX) / 3.0f;
        if (this.boxType.isSelected("Corner")) {
            Render2D.rect(posX - 0.5f, posY - 0.5f, size, 0.5f, client);
            Render2D.rect(posX - 0.5f, posY, 0.5f, size + 0.5f, client);
            Render2D.rect(posX - 0.5f, endPosY - size - 0.5f, 0.5f, size, client);
            Render2D.rect(posX - 0.5f, endPosY - 0.5f, size, 0.5f, client);
            Render2D.rect(endPosX - size + 1.0f, posY - 0.5f, size, 0.5f, client);
            Render2D.rect(endPosX + 0.5f, posY, 0.5f, size + 0.5f, client);
            Render2D.rect(endPosX + 0.5f, endPosY - size - 0.5f, 0.5f, size, client);
            Render2D.rect(endPosX - size + 1.0f, endPosY - 0.5f, size, 0.5f, client);
            if (this.flatBoxOutline.isValue()) {
                Render2D.rect(posX - 1.0f, posY - 1.0f, size + 1.0f, 1.5f, black);
                Render2D.rect(posX - 1.0f, posY + 0.5f, 1.5f, size + 0.5f, black);
                Render2D.rect(posX - 1.0f, endPosY - size - 1.0f, 1.5f, size, black);
                Render2D.rect(posX - 1.0f, endPosY - 1.0f, size + 1.0f, 1.5f, black);
                Render2D.rect(endPosX - size + 0.5f, posY - 1.0f, size + 1.0f, 1.5f, black);
                Render2D.rect(endPosX, posY + 0.5f, 1.5f, size + 0.5f, black);
                Render2D.rect(endPosX, endPosY - size - 1.0f, 1.5f, size, black);
                Render2D.rect(endPosX - size + 0.5f, endPosY - 1.0f, size + 1.0f, 1.5f, black);
            }
        }
    }

    private void drawArmor(GuiGraphics context, Player player, Vector4d vec) {
        ArrayList<ItemStack> items = new ArrayList<ItemStack>();
        for (EquipmentSlot slot : EquipmentSlot.VALUES) {
            ItemStack stack = player.getItemBySlot(slot);
            if (stack.isEmpty()) continue;
            items.add(stack);
        }
        float posX = (float)(Projection.centerX(vec) - (double)((float)items.size() * 4.5f));
        float posY = (float)(vec.y - 20.0);
        float offset = 0.0f;
        for (ItemStack stack : items) {
            ItemRender.drawItemWithContext(context, stack, posX + offset, posY, 0.5f, 1.0f);
            offset += 11.0f;
        }
    }

    private void drawHands(GuiGraphics context, Player player, Vector4d vec, float size) {
        double posY = vec.w;
        ItemStack mainHand = player.getItemInHand(InteractionHand.MAIN_HAND);
        ItemStack offHand = player.getItemInHand(InteractionHand.OFF_HAND);
        for (ItemStack stack : new ItemStack[]{mainHand, offHand}) {
            if (stack.isEmpty()) continue;
            String text = stack.getHoverName().getString();
            this.drawText(context, text, Projection.centerX(vec), posY += (double)Fonts.TEST.getHeight(size) / 2.0 + 6.0, size);
        }
    }

    private void drawShulkerBox(GuiGraphics context, ItemStack itemStack, List<ItemStack> stacks, Vector4d vec) {
        int width = 176;
        int height = 67;
        int color = ((BlockItem)itemStack.getItem()).getBlock().defaultMapColor().col | 0xFF000000;
        float scale = 0.5f;
        float scaledWidth = (float)width * scale;
        float scaledHeight = (float)height * scale;
        float drawX = (float)Projection.centerX(vec) - scaledWidth / 2.0f;
        float drawY = (float)vec.y - scaledHeight - 2.0f;
        Render2D.texture(this.TEXTURE, drawX, drawY, scaledWidth, scaledHeight, color);
        Render2D.blur(drawX, drawY, 1.0f, 1.0f, 0.0f, 0.0f, ColorUtil.rgba(0, 0, 0, 0));
        float itemScale = scale;
        float itemStartX = drawX + 7.0f * scale;
        float itemStartY = drawY + 6.0f * scale;
        float itemSize = 18.0f * scale;
        int col = 0;
        int row = 0;
        for (ItemStack stack : stacks) {
            float itemX = itemStartX + (float)col * itemSize;
            float itemY = itemStartY + (float)row * itemSize;
            ItemRender.drawItemWithContext(context, stack, itemX, itemY, itemScale, 1.0f);
            if (++col < 9) continue;
            ++row;
            col = 0;
        }
    }

    private void drawText(GuiGraphics context, String text, double startX, double startY, float size) {
        String cleanText = RwPrefix.stripFormatting(text);
        float width = Fonts.TEST.getWidth(cleanText, size);
        float height = Fonts.TEST.getHeight(size);
        float posX = (float)(startX - (double)(width / 2.0f));
        float posY = (float)startY - height;
        Render2D.rect(posX - 4.0f, posY - 1.0f, width + 8.0f, height + 2.0f, Integer.MIN_VALUE, 2.0f);
        Fonts.TEST.draw(cleanText, posX, posY, size, -1);
    }

    private String getSphere(ItemStack stack) {
        CompoundTag compound;
        int tslevel;
        CustomData component = (CustomData)stack.get(DataComponents.CUSTOM_DATA);
        if (Network.isFunTime() && component != null && (tslevel = (compound = component.copyTag()).getInt("tslevel").orElse(0).intValue()) != 0) {
            String donItem = compound.getString("don-item").orElse("");
            return " [" + donItem.replace("sphere-", "").toUpperCase() + "]";
        }
        return "";
    }

    private float getHealth(Player player) {
        return player.getHealth() + player.getAbsorptionAmount();
    }

    private String getHealthString(float hp) {
        return String.format("%.1f", Float.valueOf(hp)).replace(",", ".").replace(".0", "");
    }

    private int getFriendColor() {
        return this.friendColor.getColorNoAlpha();
    }

    private int getClientColor() {
        return this.boxColor.getColorNoAlpha();
    }

    public boolean isGlowMode() {
        return this.isState() && this.boxType.isSelected("Glow");
    }

    public boolean shouldGlow(Entity entity) {
        if (!isGlowMode()) return false;
        if (entity == Esp.mc.player) return false;
        double maxDist = this.outlineDistance.getValue();
        if (Esp.mc.gameRenderer.getMainCamera().position().distanceTo(entity.position()) > maxDist) return false;
        if (entity instanceof Player && this.entityType.isSelected("Player")) return true;
        if (entity instanceof net.minecraft.world.entity.monster.Monster && this.entityType.isSelected("Hostile")) return true;
        if (entity instanceof net.minecraft.world.entity.animal.Animal && this.entityType.isSelected("Peaceful")) return true;
        if (entity instanceof ItemEntity && this.entityType.isSelected("Item")) return true;
        if (entity instanceof net.minecraft.world.entity.boss.enderdragon.EndCrystal && this.entityType.isSelected("Crystal")) return true;
        return false;
    }

    public java.awt.Color getGlowColor(Entity entity) {
        if (entity instanceof Player) {
            boolean friend = FriendUtils.isFriend((Player) entity);
            return new java.awt.Color(friend ? this.getFriendColor() : this.getClientColor(), true);
        }
        if (entity instanceof net.minecraft.world.entity.monster.Monster) return new java.awt.Color(255, 80, 80);
        if (entity instanceof net.minecraft.world.entity.animal.Animal) return new java.awt.Color(80, 255, 80);
        if (entity instanceof ItemEntity) return new java.awt.Color(80, 80, 255);
        if (entity instanceof net.minecraft.world.entity.boss.enderdragon.EndCrystal) return new java.awt.Color(255, 0, 255);
        return java.awt.Color.WHITE;
    }
}

