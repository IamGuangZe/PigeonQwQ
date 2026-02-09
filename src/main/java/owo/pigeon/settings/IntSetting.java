package owo.pigeon.settings;

import java.util.function.Predicate;

public class IntSetting extends AbstractNumSetting<Integer> {
    protected IntSetting(String name, Integer defaultValue, Integer minValue, Integer maxValue, String unit, Predicate<Boolean> visible) {
        super(name, defaultValue, minValue, maxValue, unit, visible);
    }
}
