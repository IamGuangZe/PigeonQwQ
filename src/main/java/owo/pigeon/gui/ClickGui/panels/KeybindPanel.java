
package owo.pigeon.gui.ClickGui.panels;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.util.InputUtil;
import owo.pigeon.gui.ClickGui.AbstractDisplableItem;
import owo.pigeon.modules.Module;
import owo.pigeon.utils.ColorUtil;
import owo.pigeon.utils.Render.RenderUtil;

import java.awt.*;

import static owo.pigeon.utils.Render.TextRendererUtil.textRenderer;

public class KeybindPanel extends AbstractDisplableItem {
    private Module module;
    private boolean hovered;
    private boolean waitingForKey;

    public int color_old;

    public KeybindPanel(Module module, int x, int y, int width, int height) {
        this.module = module;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public void drawScreen(DrawContext context, int mouseX, int mouseY, float delta) {
        hovered = isHovered(mouseX, mouseY, x, y, width, height);

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

        String displayValue;
        String displayName = "bind";

        if (waitingForKey) {
            displayValue = "Press a key...";
        } else {
            if (module.getKey() > 0) {
                displayValue = InputUtil.Type.KEYSYM
                        .createFromCode(module.getKey())
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

        context.getMatrices().popMatrix();
        if (owo.pigeon.Pigeon.isDebug()) RenderUtil.drawBorder(context, x, y, width, height, Color.GREEN.getRGB());
    }

    public boolean mouseClicked(Click click, boolean doubled) {
        if (!hovered) return false;

        if (click.button() == 0) {
            waitingForKey = !waitingForKey;
        }

        return true;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return hovered;
    }

    public void keyPressed(KeyInput input) {
        if (waitingForKey) {
            if (input.getKeycode() == InputUtil.GLFW_KEY_ESCAPE) {
                module.setKey(-1);
            } else {
                module.setKey(input.getKeycode());
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
