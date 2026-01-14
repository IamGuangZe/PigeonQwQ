package owo.pigeon.modules.impl.Client;

import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.CharSetting;

public class PigeonQwQ extends Module {
    public PigeonQwQ() {
        super("PigeonQwQ", Category.CLIENT);
    }

    public CharSetting commandPrefix = setting("command-prefix",'>',v->true);

}
