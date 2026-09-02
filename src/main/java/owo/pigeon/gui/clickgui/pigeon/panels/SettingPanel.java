package owo.pigeon.gui.clickgui.pigeon.panels;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import owo.pigeon.gui.clickgui.pigeon.AbstractDisplableItem;
import owo.pigeon.gui.clickgui.pigeon.edits.ListSettingEditScreen;
import owo.pigeon.gui.clickgui.pigeon.edits.SettingEditScreen;
import owo.pigeon.settings.*;
import owo.pigeon.utils.ColorUtil;
import owo.pigeon.utils.render.RenderUtil;

import java.awt.*;

import static owo.pigeon.Pigeon.mc;
import static owo.pigeon.utils.render.FontUtil.font;

public class SettingPanel extends AbstractDisplableItem {
    private final AbstractSetting<?> setting;

    private boolean hovered;
    private boolean waitingForKey;

    public SettingPanel(AbstractSetting<?> setting, int x, int y, int width, int height) {
        this.setting = setting;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public void drawScreen(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        hovered = isHovered(mouseX, mouseY, x, y, width, height);
        String displayName = setting.getName().replaceAll("-and-", "-&&-").replaceAll("-", " ");

        context.fill(x, y, x + width, y + height, new Color(0, 0, 0, 100).getRGB());

        float scale = 0.5f;
        context.pose().pushMatrix();
        context.pose().scale(scale, scale);

        if (setting instanceof BlockSetting blockSetting) {
            String displayValue = blockSetting.getValue().getName().getString();

            context.text(
                    font,
                    ColorUtil.parseColor(displayName + " : " + displayValue),
                    (int) ((x + 4) / scale),
                    (int) ((y + (float) height / 2 - (float) font.lineHeight * scale / 2) / scale),
                    Color.LIGHT_GRAY.getRGB()
            );

        } else if (setting instanceof CharSetting charSetting) {
            String displayValue = "'&7" + String.valueOf(charSetting.getValue()).replace("&", "&&") + "&r'";

            context.text(
                    font,
                    ColorUtil.parseColor(displayName + " : " + displayValue),
                    (int) ((x + 4) / scale),
                    (int) ((y + (float) height / 2 - (float) font.lineHeight * scale / 2) / scale),
                    Color.LIGHT_GRAY.getRGB());

        } else if (setting instanceof EnableSetting enableSetting) {
            boolean value = enableSetting.getValue();

            String displayValue = value ? "&atrue" : "&cfalse";
            context.text(
                    font,
                    ColorUtil.parseColor(displayName + " : " + displayValue),
                    (int) ((x + 4) / scale),
                    (int) ((y + (float) height / 2 - (float) font.lineHeight * scale / 2) / scale),
                    Color.LIGHT_GRAY.getRGB());

        } else if (setting instanceof KeySetting keySetting) {
            String displayValue;

            if (waitingForKey) {
                displayValue = "Press a key...";
            } else {
                if (keySetting.getValue() > 0) {
                    displayValue = InputConstants.Type.KEYSYM
                            .getOrCreate(keySetting.getValue())
                            .getName()
                            .replace("key.keyboard.", "")
                            .replace(".", " ")
                            .toUpperCase();
                } else {
                    displayValue = "&cNone";
                }
            }

            context.text(
                    font,
                    ColorUtil.parseColor(displayName + " : " + displayValue),
                    (int) ((x + 4) / scale),
                    (int) ((y + (float) height / 2 - (float) font.lineHeight * scale / 2) / scale),
                    Color.LIGHT_GRAY.getRGB());

        } else if (setting instanceof ModeSetting<?> modeSetting) {
            String displayValue = "&b" + modeSetting.getValue().toString().toUpperCase();

            context.text(
                    font,
                    ColorUtil.parseColor(displayName + " : " + displayValue),
                    (int) ((x + 4) / scale),
                    (int) ((y + (float) height / 2 - (float) font.lineHeight * scale / 2) / scale),
                    Color.LIGHT_GRAY.getRGB());

        } else if (setting instanceof StringSetting stringSetting) {
            String displayValue = "\"&7" + stringSetting.getValue().replace("&", "&&") + "&r\"";

            context.text(
                    font,
                    ColorUtil.parseColor(displayName + " : " + displayValue),
                    (int) ((x + 4) / scale),
                    (int) ((y + (float) height / 2 - (float) font.lineHeight * scale / 2) / scale),
                    Color.LIGHT_GRAY.getRGB());

        } else if (setting instanceof ListSetting listSetting) {
            String displayValue = "&b[" + listSetting.size() + " items]";

            context.text(
                    font,
                    ColorUtil.parseColor(displayName + " : " + displayValue),
                    (int) ((x + 4) / scale),
                    (int) ((y + (float) height / 2 - (float) font.lineHeight * scale / 2) / scale),
                    Color.LIGHT_GRAY.getRGB());

        } else if (setting instanceof ExpandSetting expandSetting) {
            boolean value = expandSetting.getValue();

            context.text(
                    font,
                    ColorUtil.parseColor(displayName + " :"),
                    (int) ((x + 4) / scale),
                    (int) ((y + (float) height / 2 - (float) font.lineHeight * scale / 2) / scale),
                    Color.LIGHT_GRAY.getRGB()
            );

            String symbol = value ? "-" : "+";
            int color = value ? Color.RED.getRGB() : Color.GREEN.getRGB();
            context.text(
                    font,
                    symbol,
                    (int) ((x + width - font.width(symbol) * scale - 4) / scale),
                    (int) ((y + (float) height / 2 - (float) font.lineHeight * scale / 2) / scale),
                    color
            );

        } else {
            context.text(
                    font,
                    displayName,
                    (int) ((x + 4) / scale),
                    (int) (((y + (float) height / 2) - (float) font.lineHeight / 2 + 1) / scale),
                    Color.LIGHT_GRAY.getRGB());
        }

        context.pose().popMatrix();
        if (owo.pigeon.Pigeon.isDebug())
            RenderUtil.drawBorder(context, x, y, width, height, hovered ? Color.YELLOW.getRGB() : Color.GREEN.getRGB());
    }

    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (!hovered) return false;

        if (click.button() == 0) {
            if (setting instanceof EnableSetting enableSetting) {
                enableSetting.setValue(!enableSetting.getValue());
            } else if (setting instanceof ExpandSetting expandSetting) {
                expandSetting.setValue(!expandSetting.getValue());
            } else if (setting instanceof KeySetting keySetting) {
                waitingForKey = !waitingForKey;
            } else if (setting instanceof ModeSetting<?> modeSetting) {
                switchToNextMode(modeSetting);
            } else if (setting instanceof ListSetting listSetting) {
                mc.gui.setScreen(new ListSettingEditScreen(listSetting));
            } else if (setting instanceof BlockSetting || setting instanceof CharSetting || setting instanceof StringSetting) {
                mc.gui.setScreen(new SettingEditScreen(setting));
            }
        } else if (click.button() == 1) {
            if (setting instanceof ModeSetting<?> modeSetting) {
                switchToPreviousMode(modeSetting);
            }
        }

        return true;
    }

    public void mouseReleased(MouseButtonEvent click) {

    }

    public void mouseDragged(MouseButtonEvent click, double offsetX, double offsetY) {

    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return hovered;
    }

    public void keyPressed(KeyEvent input) {
        if (waitingForKey && setting instanceof KeySetting keySetting) {
            if (input.input() == InputConstants.KEY_ESCAPE) {
                keySetting.setValue(-1);
            } else {
                keySetting.setValue(input.input());
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
