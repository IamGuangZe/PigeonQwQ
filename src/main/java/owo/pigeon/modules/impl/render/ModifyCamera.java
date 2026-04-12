package owo.pigeon.modules.impl.render;

import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.EnableSetting;
import owo.pigeon.settings.ExpandSetting;
import owo.pigeon.settings.FloatSetting;

public class ModifyCamera extends Module {
    public ModifyCamera() {
        super("ModifyCamera", Category.RENDER);
    }

    // 不会命名导致的
    public ExpandSetting debuffs = setting("debuff", v -> true);
    public EnableSetting noNausea = setting("no-nausea", true, v -> debuffs.getValue());
    public EnableSetting noDarkness = setting("no-darkness", true, v -> debuffs.getValue());
    public EnableSetting noBlindness = setting("no-blindness", true, v -> debuffs.getValue());
    public ExpandSetting overlays = setting("overlays", v -> true);
    public EnableSetting noFireOverlay = setting("no-fire-overlay", true, v -> overlays.getValue());
    public EnableSetting noPortalOverlay = setting("no-protal-overlay", true, v -> overlays.getValue());
    public EnableSetting noInWallOverlay = setting("no-inwall-overlay", true, v -> overlays.getValue());
    public EnableSetting noPumpkinOverlay = setting("no-pumpkin-overlay", true, v -> overlays.getValue());
    public EnableSetting noUnderwaterOverlay = setting("no-underwater-overlay", true, v -> overlays.getValue());
    public EnableSetting noHurtCam = setting("no-hurtcam", true, v -> true);
    public EnableSetting camNoClip = setting("cam-noclip", true, v -> true);
    public FloatSetting distance = setting("distance", 5.0f, 3.0f, 12.0f, v -> true);
}
