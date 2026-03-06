/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  lombok.Generated
 *  org.lwjgl.glfw.GLFW
 */
package rich.screens.clickgui.impl;

import lombok.Generated;
import org.lwjgl.glfw.GLFW;
import rich.IMinecraft;

public class DragHandler
implements IMinecraft {
    private float offsetX = 0.0f;
    private float offsetY = 0.0f;
    private float targetOffsetX = 0.0f;
    private float targetOffsetY = 0.0f;
    private boolean dragging = false;
    private double dragStartX = 0.0;
    private double dragStartY = 0.0;
    private float dragStartOffsetX = 0.0f;
    private float dragStartOffsetY = 0.0f;
    private static final float ANIMATION_SPEED = 10.0f;
    private long lastUpdateTime = System.currentTimeMillis();

    public void update(double mouseX, double mouseY) {
        long currentTime = System.currentTimeMillis();
        float deltaTime = Math.min((float)(currentTime - this.lastUpdateTime) / 1000.0f, 0.1f);
        this.lastUpdateTime = currentTime;
        if (this.dragging) {
            if (GLFW.glfwGetMouseButton((long)mc.getWindow().handle(), (int)2) != 1) {
                this.dragging = false;
            } else {
                this.targetOffsetX = this.dragStartOffsetX + (float)(mouseX - this.dragStartX);
                this.targetOffsetY = this.dragStartOffsetY + (float)(mouseY - this.dragStartY);
                this.offsetX = this.targetOffsetX;
                this.offsetY = this.targetOffsetY;
            }
        }
        float diffX = this.targetOffsetX - this.offsetX;
        float diffY = this.targetOffsetY - this.offsetY;
        this.offsetX = Math.abs(diffX) > 0.01f ? (this.offsetX += diffX * 10.0f * deltaTime) : this.targetOffsetX;
        this.offsetY = Math.abs(diffY) > 0.01f ? (this.offsetY += diffY * 10.0f * deltaTime) : this.targetOffsetY;
    }

    public boolean startDrag(double mouseX, double mouseY, float bgX, float bgY, int bgWidth, int bgHeight) {
        if (mouseX >= (double)bgX && mouseX <= (double)(bgX + (float)bgWidth) && mouseY >= (double)bgY && mouseY <= (double)(bgY + (float)bgHeight)) {
            this.dragging = true;
            this.dragStartX = mouseX;
            this.dragStartY = mouseY;
            this.dragStartOffsetX = this.targetOffsetX;
            this.dragStartOffsetY = this.targetOffsetY;
            return true;
        }
        return false;
    }

    public void reset() {
        this.targetOffsetX = 0.0f;
        this.targetOffsetY = 0.0f;
    }

    public void stopDrag() {
        this.dragging = false;
    }

    public boolean isResetNeeded(int key, int mods) {
        boolean ctrlMod = (mods & 2) != 0;
        boolean altMod = (mods & 4) != 0;
        boolean isCtrlKey = key == 341 || key == 345;
        boolean isAltKey = key == 342 || key == 346;
        return isCtrlKey && altMod || isAltKey && ctrlMod;
    }

    @Generated
    public float getOffsetX() {
        return this.offsetX;
    }

    @Generated
    public float getOffsetY() {
        return this.offsetY;
    }

    @Generated
    public float getTargetOffsetX() {
        return this.targetOffsetX;
    }

    @Generated
    public float getTargetOffsetY() {
        return this.targetOffsetY;
    }

    @Generated
    public boolean isDragging() {
        return this.dragging;
    }

    @Generated
    public double getDragStartX() {
        return this.dragStartX;
    }

    @Generated
    public double getDragStartY() {
        return this.dragStartY;
    }

    @Generated
    public float getDragStartOffsetX() {
        return this.dragStartOffsetX;
    }

    @Generated
    public float getDragStartOffsetY() {
        return this.dragStartOffsetY;
    }

    @Generated
    public long getLastUpdateTime() {
        return this.lastUpdateTime;
    }

    @Generated
    public void setOffsetX(float offsetX) {
        this.offsetX = offsetX;
    }

    @Generated
    public void setOffsetY(float offsetY) {
        this.offsetY = offsetY;
    }

    @Generated
    public void setTargetOffsetX(float targetOffsetX) {
        this.targetOffsetX = targetOffsetX;
    }

    @Generated
    public void setTargetOffsetY(float targetOffsetY) {
        this.targetOffsetY = targetOffsetY;
    }

    @Generated
    public void setDragging(boolean dragging) {
        this.dragging = dragging;
    }

    @Generated
    public void setDragStartX(double dragStartX) {
        this.dragStartX = dragStartX;
    }

    @Generated
    public void setDragStartY(double dragStartY) {
        this.dragStartY = dragStartY;
    }

    @Generated
    public void setDragStartOffsetX(float dragStartOffsetX) {
        this.dragStartOffsetX = dragStartOffsetX;
    }

    @Generated
    public void setDragStartOffsetY(float dragStartOffsetY) {
        this.dragStartOffsetY = dragStartOffsetY;
    }

    @Generated
    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }
}

