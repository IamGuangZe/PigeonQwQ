package owo.pigeon.modules.impl.client;

import com.mojang.blaze3d.platform.InputConstants;
import owo.pigeon.Pigeon;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.FloatSetting;
import owo.pigeon.settings.ModeSetting;

import static owo.pigeon.Pigeon.mc;

public class ClickGui extends Module {
    public ClickGui() {
        super("ClickGui", Category.CLIENT, InputConstants.KEY_RSHIFT);
    }

    public enum Background {
        INGAME, PANORAMA, BLUR, DARKENING, NONE
    }

    public ModeSetting<Background> background = setting("background", Background.BLUR, v -> true);
    public FloatSetting animationSpeed = setting("animation-speed", 0.3f, 0.0f, 1.0f, "s", v -> true);

    @Override
    public void enable() {
        if (mc.screen == null) {
            Pigeon.clickGuiScreen.setParentScreen(null);
            mc.setScreen(Pigeon.clickGuiScreen);
        }
    }
}
