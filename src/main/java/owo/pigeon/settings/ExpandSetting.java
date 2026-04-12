package owo.pigeon.settings;

import java.util.function.Predicate;

public class ExpandSetting extends AbstractSetting<Boolean> {
    protected ExpandSetting(String name, Predicate<Boolean> visible) {
        super(name, false, visible);
    }
}
