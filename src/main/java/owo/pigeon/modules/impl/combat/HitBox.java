package owo.pigeon.modules.impl.combat;

import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.FloatSetting;

public class HitBox extends Module {
    public HitBox() {
        super("HitBox", Category.COMBAT);
    }

    public FloatSetting expand = setting("expand", 0.2F, 0.0F, 3.0F, v -> true);
}
