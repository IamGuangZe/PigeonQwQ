package owo.pigeon.modules.impl.Client.Debug;

import net.minecraft.block.Blocks;
import owo.pigeon.modules.Category;
import owo.pigeon.settings.*;

import java.awt.*;

public class SettingTest extends owo.pigeon.modules.Module {
    public SettingTest() {
        super("SettingTest", Category.CLIENT);
    }

    public enum mode {
        MODEA, MODEB, MODEC
    }

    public BlockSetting blockSetting = setting("block", Blocks.AIR, v -> true);
    public CharSetting charSetting = setting("char", '&', v -> true);
    public ColorSetting colorSetting = setting("color", new Color(0x39C5BB, true), v -> true);
    public EnableSetting enableSetting = setting("boolean", true, v -> true);
    public FloatSetting floatSetting = setting("float", 0.0f, 0.0f, 20.0f, v -> true);
    public IntSetting intSetting = setting("int", 0, 0, 20, v -> true);
    public KeySetting keySetting = setting("key", -1, v -> true);
    public ModeSetting<mode> modeSetting = setting("enum", mode.MODEA, v -> true);
    public StringSetting stringSetting = setting("string", "&string&", v -> true);
}
