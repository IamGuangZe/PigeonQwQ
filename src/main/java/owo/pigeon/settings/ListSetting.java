package owo.pigeon.settings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class ListSetting extends AbstractSetting<List<String>> {

    private final List<String> initialDefaultValue;

    protected ListSetting(String name, List<String> defaultValue, Predicate<Boolean> visible) {
        super(name, new ArrayList<>(defaultValue), visible);
        this.initialDefaultValue = new ArrayList<>(defaultValue);
    }

    public void add(String value) {
        if (!this.value.contains(value)) {
            this.value.add(value);
        }
    }

    public void remove(String value) {
        this.value.remove(value);
    }

    public boolean contains(String value) {
        return this.value.contains(value);
    }

    public String get(int index) {
        return this.value.get(index);
    }

    public void clear() {
        this.value.clear();
    }

    @Override
    public List<String> getValue() {
        return Collections.unmodifiableList(value);
    }

    public int size() {
        return value.size();
    }

    @Override
    public void resetValue() {
        setValue(new ArrayList<>(initialDefaultValue));
    }
}
