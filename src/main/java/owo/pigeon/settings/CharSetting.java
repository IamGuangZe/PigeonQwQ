package owo.pigeon.settings;

import java.util.function.Predicate;

public class CharSetting extends AbstractSetting<Character> {
    protected CharSetting(String name, Character defaultValue, Predicate<Boolean> visible) {
        super(name, defaultValue, visible);
    }
}
