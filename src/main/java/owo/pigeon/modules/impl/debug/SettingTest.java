package owo.pigeon.modules.impl.debug;

import net.minecraft.world.level.block.Blocks;
import owo.pigeon.modules.Category;
import owo.pigeon.settings.*;

import java.awt.*;
import java.util.List;

public class SettingTest extends owo.pigeon.modules.Module {
    public SettingTest() {
        super("SettingTest", Category.DEBUG);
    }

    public enum mode {
        MODEA, MODEB, MODEC
    }

    public BlockSetting blockSetting = setting("block", Blocks.AIR, v -> true);
    public CharSetting charSetting = setting("char", '&', v -> true);
    public ColorSetting colorSetting = setting("color", new Color(0x39C5BB, true), v -> true);
    public EnableSetting enableSetting = setting("boolean", true, v -> true);
    public FloatSetting floatSetting = setting("float", 0.0f, 0.0f, 20.0f, v -> true);
    public FloatSetting floatWithUnitSetting = setting("float-with-unit", 0.0f, 0.0f, 20.0f, "block", v -> true);
    public IntSetting intSetting = setting("int", 0, 0, 20, v -> true);
    public IntSetting intWithUnitSetting = setting("int-with-unit", 0, 0, 20, "second", v -> true);
    public KeySetting keySetting = setting("key", -1, v -> true);
    public ModeSetting<mode> modeSetting = setting("enum", mode.MODEA, v -> true);
    public StringSetting stringSetting = setting("string", "&string&", v -> true);
    public ListSetting listSetting = setting("string-list", List.of("default1", "default2"), v -> true);
    public ExpandSetting expand1Setting = setting("expand1", v -> true);
    public ExpandSetting expand2Setting = setting("expand2", v -> expand1Setting.getValue());
    public ExpandSetting expand3Setting = setting("expand3", v -> expand2Setting.isVisible() && expand2Setting.getValue());
    public ExpandSetting expand4Setting = setting("expand4", v -> expand3Setting.isVisible() && expand3Setting.getValue());
}
