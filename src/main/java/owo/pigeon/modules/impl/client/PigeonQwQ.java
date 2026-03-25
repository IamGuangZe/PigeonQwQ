package owo.pigeon.modules.impl.client;

import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.CharSetting;
import owo.pigeon.settings.EnableSetting;

public class PigeonQwQ extends Module {
    public PigeonQwQ() {
        super("PigeonQwQ", Category.CLIENT);
    }

    public EnableSetting debug = setting("debug",false,v->true);
    public CharSetting commandPrefix = setting("command-prefix",'>',v->true);

    @Override
    public void enable() {

    }
}
