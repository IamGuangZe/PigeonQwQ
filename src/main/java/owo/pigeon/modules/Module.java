package owo.pigeon.modules;

import net.minecraft.client.gui.DrawContext;
import owo.pigeon.Pigeonqwq;
import owo.pigeon.settings.SettingDesigner;
import owo.pigeon.utils.Chat.ChatUtil;

public class Module extends SettingDesigner {
    public final String name;
    public final Category category;
    private int key;
    private boolean hide;
    private boolean enable;

    public Module(String name, Category category, int key) {
        this.name = name;
        this.category = category;
        this.key = key;
    }

    public Module(String name, Category category) {
        this.name = name;
        this.category = category;
        this.key = -1;
    }

    public final void enable() {
        if (!enable) {
            enable = true;
            Pigeonqwq.EVENT_BUS.subscribe(this);
            onEnable();
        }
        ChatUtil.sendIfHudReadyMessage(this.name + " has &aEnabled!");
    }

    public final void disable() {
        if (enable) {
            enable = false;
            Pigeonqwq.EVENT_BUS.unsubscribe(this);
            onDisable();
        }
        ChatUtil.sendIfHudReadyMessage(this.name + " has &4Disabled!");
    }

    public void toggle() {
        if (enable) {
            disable();
        } else {
            enable();
        }
    }

    public boolean isEnable() {
        return enable;
    }

    public boolean isHide() {
        return hide;
    }

    public void setHide(boolean hide) {
        this.hide = hide;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public void onEnable() { }
    public void onDisable() { }
    public void onTickPost() { }
    public void onRender2D(DrawContext context) { }
}

