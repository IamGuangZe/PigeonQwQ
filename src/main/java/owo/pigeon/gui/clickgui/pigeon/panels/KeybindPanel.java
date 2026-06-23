package owo.pigeon.gui.clickgui.pigeon.panels;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import owo.pigeon.gui.clickgui.pigeon.AbstractDisplableItem;
import owo.pigeon.modules.Module;
import owo.pigeon.utils.ColorUtil;
import owo.pigeon.utils.render.RenderUtil;

import java.awt.*;

import static owo.pigeon.utils.render.TextRendererUtil.textRenderer;

public class KeybindPanel extends AbstractDisplableItem {
    private final Module module;
    private boolean hovered;
    private boolean waitingForKey;

    public KeybindPanel(Module module, int x, int y, int width, int height) {
        this.module = module;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public void drawScreen(GuiGraphics context, int mouseX, int mouseY, float delta) {
        hovered = isHovered(mouseX, mouseY, x, y, width, height);

        context.fill(x, y, x + width, y + height, new Color(0, 0, 0, 100).getRGB());

        float scale = 0.5f;
        context.pose().pushMatrix();
        context.pose().scale(scale, scale);

        String displayValue;
        String displayName = "bind";

        if (waitingForKey) {
            displayValue = "Press a key...";
        } else {
            if (module.getKey() > 0) {
                displayValue = InputConstants.Type.KEYSYM
                        .getOrCreate(module.getKey())
                        .getName()
                        .replace("key.keyboard.", "")
                        .replace(".", " ")
                        .toUpperCase();
            } else {
                displayValue = "&cNone";
            }
        }

        context.drawString(
                textRenderer,
                ColorUtil.parseColor(displayName + " : " + displayValue),
                (int) ((x + 4) / scale),
                (int) ((y + (float) height / 2 - (float) textRenderer.lineHeight * scale / 2) / scale),
                Color.LIGHT_GRAY.getRGB());

        context.pose().popMatrix();
        if (owo.pigeon.Pigeon.isDebug())
            RenderUtil.drawBorder(context, x, y, width, height, hovered ? Color.YELLOW.getRGB() : Color.GREEN.getRGB());
    }

    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (!hovered) return false;

        if (click.button() == 0) {
            waitingForKey = !waitingForKey;
        }

        return true;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return hovered;
    }

    public void keyPressed(KeyEvent input) {
        if (waitingForKey) {
            if (input.input() == InputConstants.KEY_ESCAPE) {
                module.setKey(-1);
            } else {
                module.setKey(input.input());
            }
            waitingForKey = false;
        }
    }

    public Module getModule() {
        return module;
    }

    public boolean isWaitingForKey() {
        return waitingForKey;
    }
}
