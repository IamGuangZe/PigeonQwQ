package owo.pigeon.gui.clickgui.pigeon;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import owo.pigeon.Pigeon;
import owo.pigeon.gui.clickgui.pigeon.panels.CategoryPanel;
import owo.pigeon.gui.clickgui.pigeon.panels.ModulePanel;
import owo.pigeon.gui.clickgui.pigeon.panels.SettingPanel;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.impl.client.ClickGui;
import owo.pigeon.utils.ModuleUtil;
import owo.pigeon.utils.render.FontUtil;

import java.awt.*;
import java.util.ArrayList;

import static owo.pigeon.Pigeon.mc;

public class ClickGuiScreen extends Screen {

    public final ArrayList<CategoryPanel> categoryPanels = new ArrayList<>();
    private Screen parentScreen = null;

    public ClickGuiScreen() {
        super(Component.literal("ClickGui"));

        int x = 5;
        int y = 5;
        int width = 100;
        int height = 16;

        for (Category category : Category.values()) {
            categoryPanels.add(new CategoryPanel(category, x, y, width, height));
            // x += width + 2;
            y += height + 2;
        }
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
        for (CategoryPanel panel : categoryPanels) {
            panel.drawScreen(context, mouseX, mouseY, delta);
        }

        context.drawString(FontUtil.font,
                Pigeon.WATERMARK,
                this.width - FontUtil.font.width(Pigeon.WATERMARK) - 2,
                this.height - FontUtil.font.lineHeight - 2,
                Color.WHITE.getRGB());
    }

    @Override
    public void renderBackground(GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
        ClickGui clickGui = ModuleUtil.getModule(ClickGui.class);

        switch (clickGui.background.getValue()) {
            case INGAME -> this.renderTransparentBackground(context);
            case PANORAMA -> this.renderPanorama(context, deltaTicks);
            case BLUR -> this.renderBlurredBackground(context);
            case DARKENING -> this.renderMenuBackground(context);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        for (int i = categoryPanels.size() - 1; i >= 0; i--) {
            CategoryPanel panel = categoryPanels.get(i);
            if (panel.mouseClicked(click, doubled)) {
                categoryPanels.remove(panel);
                categoryPanels.add(panel);
                return true;
            }
        }

        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent click) {
        for (CategoryPanel panel : categoryPanels) {
            panel.mouseReleased(click);
        }

        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent click, double offsetX, double offsetY) {
        for (CategoryPanel panel : categoryPanels) {
            panel.mouseDragged(click, offsetX, offsetY);
        }

        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (verticalAmount == 0) return false;

        for (CategoryPanel categoryPanel : categoryPanels) {
            if (categoryPanel.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
                return true;
            }
        }

        int moveAmount = verticalAmount > 0 ? 10 : -10;

        if (mc.hasShiftDown()) {
            for (CategoryPanel categoryPanel : categoryPanels) {
                categoryPanel.x += moveAmount;
            }
        } else {
            for (CategoryPanel categoryPanel : categoryPanels) {
                categoryPanel.y += moveAmount;
            }
        }

        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        boolean waitingForKey = false;

        for (CategoryPanel panel : categoryPanels) {
            for (ModulePanel modulePanel : panel.modulePanels) {

                if (modulePanel.keybindPanel.isWaitingForKey()) {
                    waitingForKey = true;
                }

                for (SettingPanel settingPanel : modulePanel.visiblePanels) {
                    if (settingPanel.isWaitingForKey()) {
                        waitingForKey = true;
                        break;
                    }
                }
            }
            panel.keyPressed(input);
        }

        if (waitingForKey && input.isEscape()) {
            return true;
        }

        return super.keyPressed(input);
    }

    @Override
    public void onClose() {
        mc.setScreen(this.parentScreen);
    }

    public void setParentScreen(Screen parent) {
        this.parentScreen = parent;
    }
}
