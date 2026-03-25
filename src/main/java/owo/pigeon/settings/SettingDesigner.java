package owo.pigeon.settings;

import net.minecraft.block.Block;

import java.awt.*;
import java.util.ArrayList;
import java.util.function.Predicate;

public abstract class SettingDesigner {
    private final ArrayList<AbstractSetting<?>> settings = new ArrayList<>();

    public BlockSetting setting(String name, Block value, Predicate<Boolean> visible) {
        BlockSetting setting = new BlockSetting(name, value, visible);
        settings.add(setting);
        return setting;
    }

    public CharSetting setting(String name, Character value, Predicate<Boolean> visible) {
        CharSetting setting = new CharSetting(name, value, visible);
        settings.add(setting);
        return setting;
    }

    public ColorSetting setting(String name, Color value, Predicate<Boolean> visible){
        ColorSetting setting = new ColorSetting(name, value, visible);
        settings.add(setting);
        return setting;
    }

    public EnableSetting setting(String name, Boolean value, Predicate<Boolean> visible) {
        EnableSetting setting = new EnableSetting(name, value, visible);
        settings.add(setting);
        return setting;
    }

    public FloatSetting setting(String name, Float value, Float minValue, Float maxValue, Predicate<Boolean> visible) {
        FloatSetting setting = new FloatSetting(name, value, minValue, maxValue, null, visible);
        settings.add(setting);
        return setting;
    }

    public FloatSetting setting(String name, Float value, Float minValue, Float maxValue, String unit, Predicate<Boolean> visible) {
        FloatSetting setting = new FloatSetting(name, value, minValue, maxValue, unit, visible);
        settings.add(setting);
        return setting;
    }

    public IntSetting setting(String name, Integer value, Integer minValue, Integer maxValue, Predicate<Boolean> visible) {
        IntSetting setting = new IntSetting(name, value, minValue, maxValue, null,visible);
        settings.add(setting);
        return setting;
    }

    public IntSetting setting(String name, Integer value, Integer minValue, Integer maxValue, String unit, Predicate<Boolean> visible) {
        IntSetting setting = new IntSetting(name, value, minValue, maxValue, unit, visible);
        settings.add(setting);
        return setting;
    }

    public KeySetting setting(String name, Integer value, Predicate<Boolean> visible) {
        KeySetting setting = new KeySetting(name, value, visible);
        settings.add(setting);
        return setting;
    }

    public StringSetting setting(String name, String value, Predicate<Boolean> visible) {
        StringSetting setting = new StringSetting(name, value, visible);
        settings.add(setting);
        return setting;
    }

    public <T extends Enum<T>> ModeSetting<T> setting(String name, T value, Predicate<Boolean> visible) {
        ModeSetting<T> setting = new ModeSetting<>(name, value, visible);
        settings.add(setting);
        return setting;
    }

    public ArrayList<AbstractSetting<?>> getSettings() {
        return settings;
    }
}
