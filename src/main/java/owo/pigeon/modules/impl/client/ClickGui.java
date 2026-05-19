package owo.pigeon.modules.impl.client;

import net.minecraft.client.util.InputUtil;
import owo.pigeon.Pigeon;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.FloatSetting;
import owo.pigeon.settings.ModeSetting;

import static owo.pigeon.Pigeon.mc;

public class ClickGui extends Module {
    public ClickGui() {
        super("ClickGui", Category.CLIENT, InputUtil.GLFW_KEY_RIGHT_SHIFT);
    }

    public enum Background {
        INGAME, PANORAMA, BLUR, DARKENING, NONE
    }

    public ModeSetting<Background> background = setting("background", Background.BLUR, v -> true);
    public FloatSetting animationSpeed = setting("animation-speed", 0.3f, 0.0f, 1.0f, "s", v -> true);

    @Override
    public void enable() {
        if (mc.currentScreen == null) mc.setScreen(Pigeon.clickGuiScreen);
    }
}
