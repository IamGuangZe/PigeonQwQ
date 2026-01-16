package owo.pigeon.modules.impl.Client;

import net.minecraft.client.util.InputUtil;
import owo.pigeon.Pigeonqwq;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.EnableSetting;
import owo.pigeon.settings.ModeSetting;

import static owo.pigeon.Pigeonqwq.mc;

public class ClickGui extends Module {
    public ClickGui() {
        super("ClickGui", Category.CLIENT, InputUtil.GLFW_KEY_RIGHT_SHIFT);
    }

    public enum styleEnum {
        OLD, NEW
    }

    public ModeSetting<styleEnum> style = setting("style", styleEnum.NEW, v -> true);
    public EnableSetting background = setting("background", true, v -> true);

    @Override
    public void onEnable() {
        if (mc.currentScreen == null) {
            mc.setScreen(Pigeonqwq.clickGuiScreen);
        } else {
            this.disable();
        }
    }

    @Override
    public void onDisable() {
        if (mc.currentScreen == Pigeonqwq.clickGuiScreen) {
            mc.setScreen(null);
        }
    }
}
