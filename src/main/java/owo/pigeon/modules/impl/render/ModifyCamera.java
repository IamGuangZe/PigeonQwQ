package owo.pigeon.modules.impl.render;

import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.EnableSetting;
import owo.pigeon.settings.FloatSetting;

public class ModifyCamera extends Module {
    public ModifyCamera() {
        super("ModifyCamera", Category.RENDER);
    }

    // 不会命名导致的
    public EnableSetting noNausea = setting("no-nausea",true,v->true);
    public EnableSetting noDarkness = setting("no-darkness", true, v -> true);
    public EnableSetting noBlindness = setting("no-blindness", true, v -> true);
    public EnableSetting noFireOverlay = setting("no-fire-overlay", true, v -> true);
    public EnableSetting noPortalOverlay = setting("no-protal-overlay",true,v->true);
    public EnableSetting noInWallOverlay = setting("no-inwall-overlay", true, v -> true);
    public EnableSetting noPumpkinOverlay = setting("no-pumpkin-overlay",true,v->true);
    public EnableSetting noUnderwaterOverlay = setting("no-underwater-overlay", true, v -> true);
    public EnableSetting noHurtCam = setting("no-hurtcam", true, v -> true);
    public EnableSetting camNoClip = setting("cam-noclip", true, v -> true);
    public FloatSetting distance = setting("distance", 5.0f, 3.0f, 12.0f, v -> true);
}
