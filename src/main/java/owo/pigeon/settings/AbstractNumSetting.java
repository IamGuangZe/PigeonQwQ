package owo.pigeon.settings;

import java.util.function.Predicate;

public class AbstractNumSetting<N extends Number & Comparable<N>> extends AbstractSetting<N> {
    protected final N minValue;
    protected final N maxValue;
    protected final String unit;

    protected AbstractNumSetting(String name, N defaultValue, N minValue, N maxValue, String unit, Predicate<Boolean> visible) {
        super(name, defaultValue, visible);
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.unit = unit;
    }

    @Override
    public void setValue(N value) {
        if (value.compareTo(minValue) < 0) {
            super.setValue(minValue);
        } else if (value.compareTo(maxValue) > 0) {
            super.setValue(maxValue);
        } else {
            super.setValue(value);
        }
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
