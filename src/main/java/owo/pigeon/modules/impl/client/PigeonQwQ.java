package owo.pigeon.modules.impl.client;

import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.CharSetting;
import owo.pigeon.settings.EnableSetting;
import owo.pigeon.settings.ModeSetting;
import owo.pigeon.utils.ColorUtil;

public class PigeonQwQ extends Module {
    public PigeonQwQ() {
        super("PigeonQwQ", Category.CLIENT);
    }

    public ModeSetting<ColorUtil.Theme> theme = setting("theme", ColorUtil.Theme.NORMAL, v -> true);
    public CharSetting commandPrefix = setting("command-prefix", '>', v -> true);
    public EnableSetting debug = setting("debug", false, v -> true);

    @Override
    public void enable() {

    }
}
