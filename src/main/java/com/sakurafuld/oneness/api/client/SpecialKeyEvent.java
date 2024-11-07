package com.sakurafuld.oneness.api.client;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWKeyCallback;

@OnlyIn(Dist.CLIENT)
@Cancelable
public class SpecialKeyEvent extends Event {
    private int key;
    private int scancode;
    private int action;
    private int mods;

    public SpecialKeyEvent(int key, int scancode, int action, int mods) {
        this.key = key;
        this.scancode = scancode;
        this.action = action;
        this.mods = mods;
    }
    public SpecialKeyEvent() {}

    public int getKey() {
        return this.key;
    }
    public void setKey(int key) {
        this.key = key;
    }
    public int getScancode() {
        return this.scancode;
    }
    public int getAction() {
        return action;
    }
    public int getMods() {
        return mods;
    }
    public void setScancode(int scancode) {
        this.scancode = scancode;
    }
    public void setAction(int action) {
        this.action = action;
    }

    public void setMods(int mods) {
        this.mods = mods;
    }


    private GLFWKeyCallback defaultCallback;
    public void setup(Minecraft mc) {
        this.defaultCallback = GLFW.glfwSetKeyCallback(mc.getWindow().getWindow(), this::callback);
    }
    private void callback(long window, int key, int scancode, int action, int mods) {
        SpecialKeyEvent event = new SpecialKeyEvent(key, scancode, action, mods);
        MinecraftForge.EVENT_BUS.post(event);

        if (event.isCanceled())
            return;

        if (this.defaultCallback != null)
            this.defaultCallback.invoke(window, event.getKey(), event.getScancode(), event.getAction(), event.getMods());
    }
}
