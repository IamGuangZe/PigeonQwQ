package owo.pigeon.settings;

import owo.pigeon.utils.ColorUtil;

import java.awt.*;
import java.util.function.Predicate;

public class ColorSetting extends AbstractSetting<Color> {
    protected ColorSetting(String name, Color defaultValue, Predicate<Boolean> visible) {
        super(name, defaultValue, visible);
    }

    public void setRed(int value) {
        Color color = this.getValue();
        this.setValue(new Color(clamp(value), color.getGreen(), color.getBlue(), color.getAlpha()));
    }

    public int getRed() {
        return this.getValue().getRed();
    }

    public void setGreen(int value) {
        Color color = this.getValue();
        this.setValue(new Color(color.getRed(), clamp(value), color.getBlue(), color.getAlpha()));
    }

    public int getGreen() {
        return this.getValue().getGreen();
    }

    public void setBlue(int value) {
        Color color = this.getValue();
        this.setValue(new Color(color.getRed(), color.getGreen(), clamp(value), color.getAlpha()));
    }

    public int getBlue() {
        return this.getValue().getBlue();
    }

    public void setAlpha(int value) {
        Color color = this.getValue();
        this.setValue(new Color(color.getRed(), color.getGreen(), color.getBlue(), clamp(value)));
    }

    public int getAlpha() {
        return this.getValue().getAlpha();
    }

    public void setRGB(int rgb) {
        this.setValue(new Color(rgb, true));
    }

    public int getRGB() {
        return this.getValue().getRGB();
    }

    private int clamp(int value) {
        return ColorUtil.colorClamp(value);
    }
}
