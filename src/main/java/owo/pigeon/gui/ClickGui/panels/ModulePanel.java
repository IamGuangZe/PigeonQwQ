package owo.pigeon.gui.ClickGui.panels;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.input.KeyInput;
import owo.pigeon.gui.ClickGui.AbstractDisplableItem;
import owo.pigeon.modules.Module;
import owo.pigeon.modules.impl.Client.ClickGui;
import owo.pigeon.settings.AbstractNumSetting;
import owo.pigeon.settings.AbstractSetting;
import owo.pigeon.settings.ColorSetting;
import owo.pigeon.utils.Render.RenderUtil;

import java.awt.*;
import java.util.ArrayList;

import static owo.pigeon.utils.Render.TextRendererUtil.textRenderer;

public class ModulePanel extends AbstractDisplableItem {
    private Module module;

    private boolean hovered;
    private boolean displaySetting;
    public ArrayList<SettingPanel> settingPanels = new ArrayList<>();
    public ArrayList<SettingPanel> visiblePanels = new ArrayList<>();

    public ModulePanel(Module module, int x, int y, int width, int height) {
        this.module = module;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        for (AbstractSetting<?> setting : module.getSettings()) {
            if (setting instanceof AbstractNumSetting) {
                settingPanels.add(new SettingNumPanel(setting, x, 0, width, height));
            } else if (setting instanceof ColorSetting) {
                settingPanels.add(new SettingColorPanel(setting, x, 0, width, height));
            } else {
                settingPanels.add(new SettingPanel(setting, x, 0, width, height));
            }
        }
    }

    @Override
    public void drawScreen(DrawContext context, int mouseX, int mouseY, float delta) {
        hovered = isHovered(mouseX, mouseY, x, y, width, height);

        switch (clickGui.style.getValue()) {
            case OLD:
                int color_old = module.isEnable() ?
                        new Color(20, 20, 20, 186).getRGB() :
                        new Color(50, 50, 50, 186).getRGB();
                context.fill(x, y, x + width, y + height, color_old);
                context.drawTextWithShadow(textRenderer,
                        module.name,
                        x + width / 2 - textRenderer.getWidth(module.name) / 2,
                        y + height / 2 - textRenderer.fontHeight / 2 + 1,
                        Color.WHITE.getRGB());
                break;

            case NEW:
            default:
                context.fill(x, y, x + width, y + height, new Color(0, 0, 0, 100).getRGB());
                int color_new = module.isEnable() ? Color.WHITE.getRGB() : Color.GRAY.getRGB();
                context.drawTextWithShadow(textRenderer,
                        module.name,
                        x + width / 2 - textRenderer.getWidth(module.name) / 2,
                        y + height / 2 - textRenderer.fontHeight / 2 + 1,
                        color_new);
        }
        if (owo.pigeon.Pigeon.isDebug()) RenderUtil.drawBorder(context, x, y, width, height, Color.BLUE.getRGB());

        visiblePanels.clear();

        if (displaySetting) {

            for (SettingPanel panel : settingPanels) {
                if (panel.getSetting().isVisible()) {
                    visiblePanels.add(panel);
                }
            }

            int startY = y + height;
            for (SettingPanel panel : visiblePanels) {
                panel.x = this.x;
                panel.y = startY;
                panel.width = this.width;

                if (panel instanceof SettingNumPanel) {
                    panel.height = this.height / 2 + 8;
                } else if (panel instanceof SettingColorPanel) {
                    panel.height = this.height / 2 + 20;
                } else {
                    panel.height = this.height / 2;
                }
                panel.drawScreen(context, mouseX, mouseY, delta);
                startY += panel.height;

                if (clickGui.style.getValue() == ClickGui.Style.OLD) {
                    panel.color_old = module.isEnable() ?
                            new Color(20, 20, 20, 186).getRGB() :
                            new Color(50, 50, 50, 186).getRGB();
                }
            }
        }
    }

    public boolean mouseClicked(Click click, boolean doubled) {
        boolean handled = false;

        if (hovered) {
            handled = true;
            if (click.button() == 0) {
                module.toggle();
            } else if (click.button() == 1) {
                displaySetting = !displaySetting;
            }
        }

        for (int i = visiblePanels.size() - 1; i >= 0; i--) {
            SettingPanel panel = visiblePanels.get(i);
            if (panel.mouseClicked(click, doubled)) {
                handled = true;
                break;
            }
        }

        return handled;
    }

    public void mouseReleased(Click click) {
        for (SettingPanel panel : visiblePanels) {
            panel.mouseReleased(click);
        }
    }

    public void mouseDragged(Click click, double offsetX, double offsetY) {
        for (SettingPanel panel : visiblePanels) {
            panel.mouseDragged(click, offsetX, offsetY);
        }
    }

    public void keyPressed(KeyInput input) {
        for (SettingPanel panel : visiblePanels) {
            panel.keyPressed(input);
        }
    }

    public int getSettingHeight() {
        int settingHeight = 0;

        for (SettingPanel panel : visiblePanels) {
            settingHeight += panel.height;
        }

        return settingHeight;
    }

    public Module getModule() {
        return module;
    }

    public boolean isDisplaySetting() {
        return displaySetting;
    }

    public void setDisplaySetting(boolean displaySetting) {
        this.displaySetting = displaySetting;
    }
}
