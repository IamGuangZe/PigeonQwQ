package owo.pigeon.settings;

import java.util.function.Predicate;

public class KeySetting extends AbstractSetting<Integer> {
    protected KeySetting(String name, Integer defaultValue, Predicate<Boolean> visible) {
        super(name, defaultValue, visible);
    }
}
