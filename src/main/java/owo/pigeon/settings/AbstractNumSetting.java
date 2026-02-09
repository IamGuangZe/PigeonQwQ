package owo.pigeon.settings;


import java.util.function.Predicate;

public class AbstractNumSetting<N extends Number> extends AbstractSetting<N> {
    protected final N minValue;
    protected final N maxValue;
    protected final String unit;

    protected AbstractNumSetting(String name, N defaultValue, N minValue, N maxValue, String unit, Predicate<Boolean> visible) {
        super(name, defaultValue, visible);
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.unit = unit;
    }

    public N getMinValue() {
        return minValue;
    }

    public N getMaxValue() {
        return maxValue;
    }

    public String getUnit() {
        return unit;
    }
}
