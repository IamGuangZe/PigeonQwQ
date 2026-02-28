package owo.pigeon.modules.impl.Client;

import net.minecraft.client.util.InputUtil;
import owo.pigeon.Pigeon;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.EnableSetting;
import owo.pigeon.settings.ModeSetting;

import static owo.pigeon.Pigeon.mc;

public class ClickGui extends Module {
    public ClickGui() {
        super("ClickGui", Category.CLIENT, InputUtil.GLFW_KEY_RIGHT_SHIFT);
    }

    public enum Style {
        OLD, NEW
    }

    public ModeSetting<Style> style = setting("style", Style.NEW, v -> true);
    public EnableSetting background = setting("background", true, v -> true);

    @Override
    public void enable() {
        if (mc.currentScreen == null) mc.setScreen(Pigeon.clickGuiScreen);
    }
}
