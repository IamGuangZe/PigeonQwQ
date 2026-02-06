package owo.pigeon.modules.impl.Render;

import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.EnableSetting;
import owo.pigeon.settings.FloatSetting;

public class ModifyCamera extends Module {
    public ModifyCamera() {
        super("ModifyCamera", Category.RENDER);
    }

    public EnableSetting noFire = setting("no-fire", true, v -> true);
    public EnableSetting noUnderwater = setting("no-underwater", true, v -> true);
    public EnableSetting noHurtCam = setting("no-hurtcam", true, v -> true);
    public EnableSetting camNoClip = setting("cam-noclip", true, v -> true);
    public FloatSetting distance = setting("distance", 5.0f, 3.0f, 12.0f, v -> true);
    // TODO: no-blindness
    // public EnableSetting noBlindness = setting("no-blindness", true, v -> true);
}
