/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 */
package rich.screens.hud;

import com.mojang.authlib.GameProfile;
import java.awt.Color;
import java.lang.invoke.LambdaMetafactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Pattern;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import rich.client.draggables.AbstractHudElement;
import rich.util.ColorUtil;
import rich.util.animations.Direction;
import rich.util.render.Render2D;
import rich.util.render.font.Fonts;
import rich.util.render.shader.Scissor;

public class Staff
extends AbstractHudElement {
    private static final Pattern NAME_PATTERN = Pattern.compile("^\\w{3,16}$");
    private static final Pattern DIGIT_ONLY_PATTERN = Pattern.compile("^\\d{1,4}$");
    private static final Identifier STEVE_SKIN = Identifier.fromNamespaceAndPath((String)"rich", (String)"textures/entity/player/wide/steve.png");
    private Map<String, StaffInfo> staffMap = new LinkedHashMap<String, StaffInfo>();
    private Map<String, Float> staffAnimations = new LinkedHashMap<String, Float>();
    private Set<String> activeStaffIds = new HashSet<String>();
    private Identifier cachedRandomSkin = null;
    private boolean skinCached = false;
    private float animatedWidth = 80.0f;
    private float animatedHeight = 23.0f;
    private long lastUpdateTime = System.currentTimeMillis();
    private static final float ANIMATION_SPEED = 8.0f;
    private static final float FACE_SIZE = 8.0f;
    private static final float CIRCLE_SIZE = 5.0f;

    public Staff() {
        super("Staff", 300, 150, 80, 23, true);
        this.stopAnimation();
    }

    @Override
    public boolean visible() {
        return !this.scaleAnimation.isFinished(Direction.BACKWARDS);
    }

    private Identifier getSkinFromEntry(PlayerInfo entry) {
        try {
            Identifier texturePath;
            PlayerSkin skinTextures = entry.getSkin();
            if (skinTextures != null && skinTextures.body() != null && (texturePath = skinTextures.body().texturePath()) != null) {
                return texturePath;
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
        return null;
    }

    private Identifier findRandomOnlineSkin() {
        if (this.mc.player == null || this.mc.player.connection == null) {
            return STEVE_SKIN;
        }
        ArrayList<PlayerInfo> players = new ArrayList<>(this.mc.player.connection.getOnlinePlayers());
        Collections.shuffle(players);
        for (PlayerInfo entry : players) {
            Identifier skin = this.getSkinFromEntry(entry);
            if (skin == null) continue;
            return skin;
        }
        return STEVE_SKIN;
    }

    private Identifier getCachedSkin() {
        if (!this.skinCached || this.cachedRandomSkin == null) {
            if (this.mc.player != null && this.mc.player.connection != null) {
                Identifier found = this.findRandomOnlineSkin();
                if (found != null && !found.equals(STEVE_SKIN)) {
                    this.cachedRandomSkin = found;
                    this.skinCached = true;
                } else if (this.cachedRandomSkin == null) {
                    this.cachedRandomSkin = STEVE_SKIN;
                }
            } else {
                this.cachedRandomSkin = STEVE_SKIN;
            }
        }
        return this.cachedRandomSkin;
    }

    @Override
    public void tick() {
        if (this.mc.player == null || this.mc.level == null) {
            this.staffMap.clear();
            this.activeStaffIds.clear();
            this.cachedRandomSkin = null;
            this.skinCached = false;
            this.stopAnimation();
            return;
        }
        String myName = this.mc.player.getName().getString();
        this.activeStaffIds.clear();
        Scoreboard scoreboard = this.mc.level.getScoreboard();
        ArrayList<PlayerTeam> teams = new ArrayList<PlayerTeam>(scoreboard.getPlayerTeams());
        teams.sort(Comparator.comparing(PlayerTeam::getName));
        Collection<PlayerInfo> online = this.mc.player.connection.getOnlinePlayers();
        HashSet<String> onlineNames = new HashSet<String>();
        for (Object _e : online) { PlayerInfo entry = (PlayerInfo)_e;
            if (entry.getProfile() == null || entry.getProfile().name() == null) continue;
            onlineNames.add(entry.getProfile().name());
        }
        for (PlayerTeam team : teams) {
            boolean isOnline;
            String name;
            Collection members = team.getPlayers();
            if (members.size() != 1 || !NAME_PATTERN.matcher(name = (String)members.iterator().next()).matches() || name.equals(myName) || DIGIT_ONLY_PATTERN.matcher(name).matches() || (isOnline = onlineNames.contains(name))) continue;
            this.activeStaffIds.add(name);
            if (!this.staffMap.containsKey(name)) {
                GameProfile fakeProfile = new GameProfile(UUID.randomUUID(), name);
                Identifier randomSkin = this.findRandomOnlineSkin();
                this.staffMap.put(name, new StaffInfo(name, fakeProfile, randomSkin));
            }
            if (this.staffAnimations.containsKey(name)) continue;
            this.staffAnimations.put(name, Float.valueOf(0.0f));
        }
        boolean hasActiveStaff = !this.activeStaffIds.isEmpty() || !this.staffAnimations.isEmpty();
        boolean inChat = this.isChat(this.mc.screen);
        if (hasActiveStaff || inChat) {
            this.startAnimation();
        } else {
            this.stopAnimation();
        }
    }

    private float lerp(float current, float target, float deltaTime) {
        float factor = (float)(1.0 - Math.pow(0.001, deltaTime * 8.0f));
        return current + (target - current) * factor;
    }

    @Override
    public void drawDraggable(GuiGraphics context, int alpha) {
        float f;
        if (alpha <= 0) {
            return;
        }
        float alphaFactor = (float)alpha / 255.0f;
        long currentTime = System.currentTimeMillis();
        float deltaTime = (float)(currentTime - this.lastUpdateTime) / 1000.0f;
        this.lastUpdateTime = currentTime;
        deltaTime = Math.min(deltaTime, 0.1f);
        ArrayList<String> toRemove = new ArrayList<String>();
        for (Map.Entry<String, Float> entry : this.staffAnimations.entrySet()) {
            float targetAnim;
            String id = entry.getKey();
            float currentAnim = entry.getValue().floatValue();
            float newAnim = this.lerp(currentAnim, targetAnim = this.activeStaffIds.contains(id) ? 1.0f : 0.0f, deltaTime);
            if (Math.abs(newAnim - targetAnim) < 0.01f) {
                newAnim = targetAnim;
            }
            if (newAnim <= 0.01f && targetAnim == 0.0f) {
                toRemove.add(id);
                continue;
            }
            this.staffAnimations.put(id, Float.valueOf(newAnim));
        }
        for (String id : toRemove) {
            this.staffAnimations.remove(id);
            this.staffMap.remove(id);
        }
        float x = this.getX();
        float y = this.getY();
        boolean hasAnimatingStaff = !this.staffAnimations.isEmpty();
        boolean showExample = !hasAnimatingStaff && this.isChat(this.mc.screen);
        int offset = 23;
        float targetWidth = 80.0f;
        if (showExample) {
            offset += 11;
            String name = "ExampleStaff";
            f = Fonts.BOLD.getWidth(name, 6.0f);
            targetWidth = Math.max(f + 55.0f, targetWidth);
        } else if (hasAnimatingStaff) {
            for (Map.Entry entry : this.staffAnimations.entrySet()) {
                StaffInfo info;
                String id = (String)entry.getKey();
                float animation = ((Float)entry.getValue()).floatValue();
                if (animation <= 0.0f || (info = this.staffMap.get(id)) == null) continue;
                offset += (int)(animation * 11.0f);
                float nameWidth = Fonts.BOLD.getWidth(info.name, 6.0f);
                targetWidth = Math.max(nameWidth + 55.0f, targetWidth);
            }
        }
        float targetHeight = offset + 2;
        this.animatedWidth = this.lerp(this.animatedWidth, targetWidth, deltaTime);
        this.animatedHeight = this.lerp(this.animatedHeight, targetHeight, deltaTime);
        if (Math.abs(this.animatedWidth - targetWidth) < 0.3f) {
            this.animatedWidth = targetWidth;
        }
        if (Math.abs(this.animatedHeight - targetHeight) < 0.3f) {
            this.animatedHeight = targetHeight;
        }
        this.setWidth((int)Math.ceil(this.animatedWidth));
        this.setHeight((int)Math.ceil(this.animatedHeight));
        f = this.animatedHeight;
        int bgAlpha = (int)(255.0f * alphaFactor);
        if (f > 0.0f) {
            Render2D.gradientRect(x, y, this.getWidth(), f, new int[]{new Color(52, 52, 52, bgAlpha).getRGB(), new Color(32, 32, 32, bgAlpha).getRGB(), new Color(52, 52, 52, bgAlpha).getRGB(), new Color(32, 32, 32, bgAlpha).getRGB()}, 5.0f);
            Render2D.outline(x, y, this.getWidth(), f, 0.35f, new Color(90, 90, 90, bgAlpha).getRGB(), 5.0f);
        }
        Scissor.enable(x, y, this.getWidth(), f, 2.0f);
        Render2D.gradientRect(x + (float)this.getWidth() - 18.5f, y + 5.0f, 14.0f, 12.0f, new int[]{new Color(52, 52, 52, bgAlpha).getRGB(), new Color(52, 52, 52, bgAlpha).getRGB(), new Color(52, 52, 52, bgAlpha).getRGB(), new Color(52, 52, 52, bgAlpha).getRGB()}, 3.0f);
        Fonts.ICONS.draw("E", x + (float)this.getWidth() - 15.5f, y + 7.5f, 8.0f, new Color(165, 165, 165, bgAlpha).getRGB());
        Fonts.BOLD.draw("Staff", x + 8.0f, y + 6.5f, 6.0f, new Color(255, 255, 255, bgAlpha).getRGB());
        int moduleOffset = 23;
        Identifier exampleSkin = this.getCachedSkin();
        if (showExample) {
            String name = "ExampleStaff";
            float faceX = x + 8.0f;
            float faceY = y + (float)moduleOffset - 2.0f;
            this.drawFace(exampleSkin, faceX, faceY, bgAlpha);
            float nameX = x + 8.0f + 8.0f + 4.0f;
            Fonts.BOLD.draw(name, nameX, y + (float)moduleOffset - 1.5f, 6.0f, new Color(255, 255, 255, bgAlpha).getRGB());
            float circleX = x + (float)this.getWidth() - 14.0f;
            float circleY = y + (float)moduleOffset - 0.5f;
            this.drawStatusCircle(circleX, circleY, bgAlpha);
        } else if (hasAnimatingStaff) {
            for (Map.Entry<String, Float> entry : this.staffAnimations.entrySet()) {
                StaffInfo info;
                String id = entry.getKey();
                float animation = entry.getValue().floatValue();
                if (animation <= 0.0f || (info = this.staffMap.get(id)) == null) continue;
                Identifier skinToUse = info.skin != null ? info.skin : STEVE_SKIN;
                int textAlpha = (int)(255.0f * animation * alphaFactor);
                float faceX = x + 8.0f;
                float faceY = y + (float)moduleOffset - 2.0f;
                this.drawFace(skinToUse, faceX, faceY, textAlpha);
                float nameX = faceX + 8.0f + 4.0f;
                Fonts.BOLD.draw(info.name, nameX, y + (float)moduleOffset - 1.5f, 6.0f, new Color(255, 255, 255, textAlpha).getRGB());
                float circleX = x + (float)this.getWidth() - 14.0f;
                float circleY = y + (float)moduleOffset - 0.5f;
                this.drawStatusCircle(circleX, circleY, textAlpha);
                moduleOffset += (int)(animation * 11.0f);
            }
        }
        Scissor.disable();
    }

    private void drawFace(Identifier skin, float faceX, float faceY, int alpha) {
        int color = new Color(255, 255, 255, alpha).getRGB();
        Render2D.texture(skin, faceX, faceY, 8.0f, 8.0f, 0.125f, 0.125f, 0.25f, 0.25f, color, 0.0f, 2.0f);
        float hatScale = 1.15f;
        float hatSize = 8.0f * hatScale;
        float hatOffset = (hatSize - 8.0f) / 2.0f;
        Render2D.texture(skin, faceX - hatOffset, faceY - hatOffset, hatSize, hatSize, 0.625f, 0.125f, 0.75f, 0.25f, color, 0.0f, 2.0f);
        Render2D.blur(faceX, faceY, 1.0f, 1.0f, 0.0f, 0.0f, ColorUtil.rgba(0, 0, 0, 0));
    }

    private void drawStatusCircle(float circleX, float circleY, int alpha) {
        Render2D.gradientRect(circleX - 3.0f, circleY - 2.0f, 11.0f, 9.0f, new int[]{new Color(52, 52, 52, alpha).getRGB(), new Color(52, 52, 52, alpha).getRGB(), new Color(52, 52, 52, alpha).getRGB(), new Color(52, 52, 52, alpha).getRGB()}, 3.0f);
        Render2D.outline(circleX - 3.0f, circleY - 2.0f, 11.0f, 9.0f, 0.35f, new Color(90, 90, 90, alpha).getRGB(), 3.0f);
        Render2D.rect(circleX, circleY, 5.0f, 5.0f, new Color(255, 80, 80, alpha).getRGB(), 2.5f);
    }

    private static class StaffInfo {
        String name;
        GameProfile profile;
        Identifier skin;

        StaffInfo(String name, GameProfile profile, Identifier skin) {
            this.name = name;
            this.profile = profile;
            this.skin = skin;
        }
    }
}

