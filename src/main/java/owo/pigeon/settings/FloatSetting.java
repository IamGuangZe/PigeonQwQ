package owo.pigeon.settings;

import java.util.function.Predicate;

public class FloatSetting extends AbstractNumSetting<Float> {
    protected FloatSetting(String name, Float defaultValue, Float minValue, Float maxValue, String unit, Predicate<Boolean> visible) {
        super(name, defaultValue, minValue, maxValue, unit, visible);
    }
}
