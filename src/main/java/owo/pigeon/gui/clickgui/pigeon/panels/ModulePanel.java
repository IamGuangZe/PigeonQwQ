package owo.pigeon.gui.clickgui.pigeon.panels;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.input.KeyInput;
import owo.pigeon.gui.clickgui.pigeon.AbstractDisplableItem;
import owo.pigeon.modules.Module;
import owo.pigeon.modules.impl.client.PigeonQwQ;
import owo.pigeon.settings.AbstractNumSetting;
import owo.pigeon.settings.AbstractSetting;
import owo.pigeon.settings.ColorSetting;
import owo.pigeon.utils.ColorUtil;
import owo.pigeon.utils.ModuleUtil;
import owo.pigeon.utils.animation.AnimationValue;
import owo.pigeon.utils.render.RenderUtil;

import java.awt.*;
import java.util.ArrayList;

import static owo.pigeon.utils.render.TextRendererUtil.textRenderer;

public class ModulePanel extends AbstractDisplableItem {
    private Module module;

    private boolean hovered;
    private final AnimationValue expandProgress = new AnimationValue(0.0f, 0.3f);
    public ArrayList<SettingPanel> settingPanels = new ArrayList<>();
    public ArrayList<SettingPanel> visiblePanels = new ArrayList<>();
    public final HidePanel hidePanel;
    public final KeybindPanel keybindPanel;

    public ModulePanel(Module module, int x, int y, int width, int height) {
        this.module = module;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        for (AbstractSetting<?> setting : module.getSettings()) {
            if (setting instanceof AbstractNumSetting) {
                settingPanels.add(new NumSettingPanel(setting, x, 0, width, height));
            } else if (setting instanceof ColorSetting) {
                settingPanels.add(new ColorSettingPanel(setting, x, 0, width, height));
            } else {
                settingPanels.add(new SettingPanel(setting, x, 0, width, height));
            }
        }

        hidePanel = new HidePanel(module, x, 0, width, height);
        keybindPanel = new KeybindPanel(module, x, 0, width, height);
    }

    @Override
    public void drawScreen(DrawContext context, int mouseX, int mouseY, float delta) {
        expandProgress.setDuration(clickGui.animationSpeed.getValue());
        expandProgress.update(delta);

        hovered = isHovered(mouseX, mouseY, x, y, width, height);

        PigeonQwQ pigeonQwQ = ModuleUtil.getModule(PigeonQwQ.class);
        ColorUtil.Theme theme = pigeonQwQ != null ? pigeonQwQ.theme.getValue() : ColorUtil.Theme.NORMAL;

        context.fill(x, y, x + width, y + height, new Color(0, 0, 0, 100).getRGB());
        int textColor = module.isEnable()
                ? (theme.isGradient() ? theme.getMidColor() : Color.WHITE.getRGB())
                : Color.GRAY.getRGB();
        context.drawTextWithShadow(textRenderer,
                module.name,
                x + width / 2 - textRenderer.getWidth(module.name) / 2,
                y + height / 2 - textRenderer.fontHeight / 2 + 1,
                textColor);
        if (owo.pigeon.Pigeon.isDebug()) RenderUtil.drawBorder(context, x, y, width, height, hovered ? Color.YELLOW.getRGB() : Color.BLUE.getRGB());

        visiblePanels.clear();

        for (SettingPanel panel : settingPanels) {
            if (panel.getSetting().isVisible()) {
                visiblePanels.add(panel);
            }
        }

        float progress = expandProgress.getValue();

        if (progress > 0.0f) {
            int fullSettingsHeight = computeFullSettingsHeight();
            int animatedSettingsHeight = (int) (fullSettingsHeight * progress);

            context.enableScissor(x, y + height, x + width, y + height + animatedSettingsHeight);

            int currentY = y + height;
            for (SettingPanel panel : visiblePanels) {
                panel.x = this.x;
                panel.y = currentY;

                if (panel instanceof NumSettingPanel) {
                    panel.height = this.height / 2 + 8;
                } else if (panel instanceof ColorSettingPanel) {
                    panel.height = this.height / 2 + 20;
                } else {
                    panel.height = this.height / 2;
                }
                panel.width = this.width;
                panel.drawScreen(context, mouseX, mouseY, delta);
                currentY += panel.height;
            }

            hidePanel.x = this.x;
            hidePanel.y = currentY;
            hidePanel.width = this.width;
            hidePanel.height = this.height / 2;
            hidePanel.drawScreen(context, mouseX, mouseY, delta);
            currentY += hidePanel.height;

            keybindPanel.x = this.x;
            keybindPanel.y = currentY;
            keybindPanel.width = this.width;
            keybindPanel.height = this.height / 2;
            keybindPanel.drawScreen(context, mouseX, mouseY, delta);

            context.disableScissor();
        }
    }

    private int computeFullSettingsHeight() {
        int fullHeight = 0;
        for (SettingPanel panel : visiblePanels) {
            if (panel instanceof NumSettingPanel) {
                fullHeight += this.height / 2 + 8;
            } else if (panel instanceof ColorSettingPanel) {
                fullHeight += this.height / 2 + 20;
            } else {
                fullHeight += this.height / 2;
            }
        }
        fullHeight += this.height / 2; // hidePanel
        fullHeight += this.height / 2; // keybindPanel
        return fullHeight;
    }

    public boolean mouseClicked(Click click, boolean doubled) {
        if (hovered) {
            if (click.button() == 0) {
                module.toggle();
            } else if (click.button() == 1) {
                expandProgress.setTarget(expandProgress.isExpanded() ? 0.0f : 1.0f);
            }
            return true;
        }

        if (expandProgress.isExpanded() && keybindPanel.mouseClicked(click, doubled)) {
            return true;
        }

        if (expandProgress.isExpanded() && hidePanel.mouseClicked(click, doubled)) {
            return true;
        }

        if (expandProgress.isExpanded()) {
            for (int i = visiblePanels.size() - 1; i >= 0; i--) {
                SettingPanel panel = visiblePanels.get(i);
                if (panel.mouseClicked(click, doubled)) {
                    return true;
                }
            }
        }

        return false;
    }

    public void mouseReleased(Click click) {
        if (expandProgress.isExpanded()) {
            for (SettingPanel panel : visiblePanels) {
                panel.mouseReleased(click);
            }
        }
    }

    public void mouseDragged(Click click, double offsetX, double offsetY) {
        if (expandProgress.isExpanded()) {
            for (SettingPanel panel : visiblePanels) {
                panel.mouseDragged(click, offsetX, offsetY);
            }
        }
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (expandProgress.isExpanded()) {
            for (int i = visiblePanels.size() - 1; i >= 0; i--) {
                SettingPanel panel = visiblePanels.get(i);
                if (panel.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
                    return true;
                }
            }

            if (keybindPanel.mouseScrolled(mouseX,mouseY,horizontalAmount,verticalAmount)) {
                return true;
            }

            if (hidePanel.mouseScrolled(mouseX,mouseY,horizontalAmount,verticalAmount)) {
                return true;
            }
        }

        return hovered;
    }

    public void keyPressed(KeyInput input) {
        if (expandProgress.isExpanded()) {
            keybindPanel.keyPressed(input);

            for (SettingPanel panel : visiblePanels) {
                panel.keyPressed(input);
            }
        }
    }

    public int getSettingHeight() {
        int fullHeight = 0;
        for (SettingPanel panel : settingPanels) {
            if (panel.getSetting().isVisible()) {
                if (panel instanceof NumSettingPanel) {
                    fullHeight += this.height / 2 + 8;
                } else if (panel instanceof ColorSettingPanel) {
                    fullHeight += this.height / 2 + 20;
                } else {
                    fullHeight += this.height / 2;
                }
            }
        }
        fullHeight += this.height / 2; // hidePanel
        fullHeight += this.height / 2; // keybindPanel
        return (int) (fullHeight * expandProgress.getValue());
    }

    public Module getModule() {
        return module;
    }

    public boolean isDisplaySetting() {
        return expandProgress.isExpanded();
    }

    public void setDisplaySetting(boolean displaySetting) {
        expandProgress.force(displaySetting ? 1.0f : 0.0f);
    }
}
