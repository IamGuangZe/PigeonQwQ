package owo.pigeon.gui.ClickGui.panels;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.util.InputUtil;
import owo.pigeon.gui.ClickGui.AbstractDisplableItem;
import owo.pigeon.settings.*;
import owo.pigeon.utils.ColorUtil;
import owo.pigeon.utils.Render.RenderUtil;

import java.awt.*;

import static owo.pigeon.utils.Render.TextRendererUtil.textRenderer;

public class SettingPanel extends AbstractDisplableItem {
    private AbstractSetting<?> setting;

    private boolean hovered;
    private boolean waitingForKey;

    public int color_old;

    public SettingPanel(AbstractSetting<?> setting, int x, int y, int width, int height) {
        this.setting = setting;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public void drawScreen(DrawContext context, int mouseX, int mouseY, float delta) {
        hovered = isHovered(mouseX, mouseY, x, y, width, height);
        String displayName = setting.getName().replaceAll("-and-", "-&&-").replaceAll("-", " ");

        switch (clickGui.style.getValue()) {
            case OLD:
                context.fill(x, y, x + width, y + height, color_old);
                break;
            case NEW:
            default:
                context.fill(x, y, x + width, y + height, new Color(0, 0, 0, 100).getRGB());
        }

        float scale = 0.5f;
        context.getMatrices().pushMatrix();
        context.getMatrices().scale(scale, scale);

        if (setting instanceof BlockSetting blockSetting) {
            String displayValue = blockSetting.getValue().getName().getString();

            context.drawTextWithShadow(
                    textRenderer,
                    ColorUtil.parseColor(displayName + " : " + displayValue),
                    (int) ((x + 4) / scale),
                    (int) ((y + (float) height / 2 - (float) textRenderer.fontHeight * scale / 2) / scale),
                    Color.LIGHT_GRAY.getRGB()
            );

        } else if (setting instanceof CharSetting charSetting) {
            String displayValue = "'&7" + String.valueOf(charSetting.getValue()).replace("&", "&&") + "&r'";

            context.drawTextWithShadow(
                    textRenderer,
                    ColorUtil.parseColor(displayName + " : " + displayValue),
                    (int) ((x + 4) / scale),
                    (int) ((y + (float) height / 2 - (float) textRenderer.fontHeight * scale / 2) / scale),
                    Color.LIGHT_GRAY.getRGB());

        } else if (setting instanceof EnableSetting enableSetting) {
            boolean value = enableSetting.getValue();

            switch (clickGui.style.getValue()) {
                case OLD:
                    context.drawTextWithShadow(
                            textRenderer,
                            displayName,
                            (int) ((x + 4) / scale),
                            (int) ((y + (float) height / 2 - (float) textRenderer.fontHeight * scale / 2) / scale),
                            value ? Color.WHITE.getRGB() : Color.GRAY.getRGB()
                    );
                    break;

                case NEW:
                default:
                    String displayValue = value ? "&atrue" : "&cfalse";
                    context.drawTextWithShadow(
                            textRenderer,
                            ColorUtil.parseColor(displayName + " : " + displayValue),
                            (int) ((x + 4) / scale),
                            (int) ((y + (float) height / 2 - (float) textRenderer.fontHeight * scale / 2) / scale),
                            Color.LIGHT_GRAY.getRGB());
                    break;
            }

        } else if (setting instanceof KeySetting keySetting) {
            String displayValue;

            if (waitingForKey) {
                displayValue = "Press a key...";
            } else {
                if (keySetting.getValue() > 0) {
                    displayValue = InputUtil.Type.KEYSYM
                            .createFromCode(keySetting.getValue())
                            .getTranslationKey()
                            .replace("key.keyboard.", "")
                            .replace(".", " ")
                            .toUpperCase();
                } else {
                    displayValue = "&cNone";
                }
            }

            context.drawTextWithShadow(
                    textRenderer,
                    ColorUtil.parseColor(displayName + " : " + displayValue),
                    (int) ((x + 4) / scale),
                    (int) ((y + (float) height / 2 - (float) textRenderer.fontHeight * scale / 2) / scale),
                    Color.LIGHT_GRAY.getRGB());

        } else if (setting instanceof ModeSetting<?> modeSetting) {
            String displayValue = "&b" + modeSetting.getValue().toString().toUpperCase();

            context.drawTextWithShadow(
                    textRenderer,
                    ColorUtil.parseColor(displayName + " : " + displayValue),
                    (int) ((x + 4) / scale),
                    (int) ((y + (float) height / 2 - (float) textRenderer.fontHeight * scale / 2) / scale),
                    Color.LIGHT_GRAY.getRGB());

        } else if (setting instanceof StringSetting stringSetting) {
            String displayValue = "\"&7" + stringSetting.getValue().replace("&", "&&") + "&r\"";

            context.drawTextWithShadow(
                    textRenderer,
                    ColorUtil.parseColor(displayName + " : " + displayValue),
                    (int) ((x + 4) / scale),
                    (int) ((y + (float) height / 2 - (float) textRenderer.fontHeight * scale / 2) / scale),
                    Color.LIGHT_GRAY.getRGB());

        } else {
            context.drawTextWithShadow(
                    textRenderer,
                    displayName,
                    (int) ((x + 4) / scale),
                    (int) (((y + (float) height / 2) - (float) textRenderer.fontHeight / 2 + 1) / scale),
                    Color.LIGHT_GRAY.getRGB());
        }

        context.getMatrices().popMatrix();
        if (owo.pigeon.Pigeon.isDebug()) RenderUtil.drawBorder(context, x, y, width, height, Color.GREEN.getRGB());
    }

    public boolean mouseClicked(Click click, boolean doubled) {
        if (!hovered) return false;

        if (click.button() == 0) {
            if (setting instanceof EnableSetting enableSetting) {
                enableSetting.setValue(!enableSetting.getValue());
            } else if (setting instanceof KeySetting keySetting) {
                waitingForKey = !waitingForKey;
            } else if (setting instanceof ModeSetting<?> modeSetting) {
                switchToNextMode(modeSetting);
            }
        } else if (click.button() == 1) {
            if (setting instanceof ModeSetting<?> modeSetting) {
                switchToPreviousMode(modeSetting);
            }
        }

        return true;
    }

    public void mouseReleased(Click click) {

    }

    public void mouseDragged(Click click, double offsetX, double offsetY) {

    }

    public void keyPressed(KeyInput input) {
        if (waitingForKey && setting instanceof KeySetting keySetting) {
            if (input.getKeycode() == InputUtil.GLFW_KEY_ESCAPE) {
                keySetting.setValue(-1);
            } else {
                keySetting.setValue(input.getKeycode());
            }
            waitingForKey = false;
        }
    }

    private <T extends Enum<T>> void switchToNextMode(ModeSetting<T> setting) {
        T current = setting.getValue();
        T[] values = current.getDeclaringClass().getEnumConstants();
        int index = current.ordinal();
        int nextIndex = (index + 1) % values.length;
        setting.setValue(values[nextIndex]);
    }

    private <T extends Enum<T>> void switchToPreviousMode(ModeSetting<T> setting) {
        T current = setting.getValue();
        T[] values = current.getDeclaringClass().getEnumConstants();
        int index = current.ordinal();
        int prevIndex = (index - 1 + values.length) % values.length;
        setting.setValue(values[prevIndex]);
    }

    public AbstractSetting<?> getSetting() {
        return setting;
    }

    public boolean isWaitingForKey() {
        return waitingForKey;
    }
}
