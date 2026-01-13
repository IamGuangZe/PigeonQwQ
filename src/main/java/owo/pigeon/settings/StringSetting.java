package owo.pigeon.settings;

import java.util.function.Predicate;

public class StringSetting extends AbstractSetting<String> {
    protected StringSetting(String name, String defaultValue, Predicate<Boolean> visible) {
        super(name, defaultValue, visible);
    }
}
