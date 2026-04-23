package owo.pigeon.modules;

import owo.pigeon.Pigeon;
import owo.pigeon.settings.SettingDesigner;
import owo.pigeon.utils.chat.ChatUtil;

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
        this(name, category, -1);
    }

    public void enable() {
        if (!enable) {
            enable = true;
            onEnable();
            Pigeon.EVENT_BUS.subscribe(this);
            ChatUtil.sendIfHudReadyMessage(this.name + " has &aEnabled!");
        }
    }

    public void disable() {
        if (enable) {
            enable = false;
            onDisable();
            Pigeon.EVENT_BUS.unsubscribe(this);
            ChatUtil.sendIfHudReadyMessage(this.name + " has &4Disabled!");
        }
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

    public String getSuffix() {
        return "";
    }

    public void onEnable() { }
    public void onDisable() { }
}

