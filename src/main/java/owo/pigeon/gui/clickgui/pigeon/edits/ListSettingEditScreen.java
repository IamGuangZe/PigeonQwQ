package owo.pigeon.gui.clickgui.pigeon.edits;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import owo.pigeon.Pigeon;
import owo.pigeon.modules.impl.client.ClickGui;
import owo.pigeon.settings.ListSetting;
import owo.pigeon.utils.ModuleUtil;
import owo.pigeon.utils.render.TextRendererUtil;

import java.awt.*;

public class ListSettingEditScreen extends Screen {
    private final ListSetting setting;

    private TextFieldWidget textField;
    private String currentText = null;
    private int scrollOffset = 0;

    public ListSettingEditScreen(ListSetting setting) {
        super(Text.of("Editing " + setting.getName()));
        this.setting = setting;
    }

    @Override
    protected void init() {
        if (this.textField != null) {
            this.currentText = this.textField.getText();
        }

        int fieldWidth = (int) (this.width * 0.618f);
        int x = (this.width - fieldWidth) / 2;
        int y = this.height / 2 - 10;

        this.textField = new TextFieldWidget(TextRendererUtil.textRenderer, x, y, fieldWidth - 60, 20, Text.of("Add Item"));

        if (currentText != null) {
            textField.setText(currentText);
        }

        this.textField.setChangedListener(text -> this.currentText = text);

        this.addDrawableChild(this.textField);
        this.setInitialFocus(this.textField);

        this.addDrawableChild(ButtonWidget.builder(Text.of("Add"), (button) -> {
            if (!textField.getText().isEmpty()) {
                setting.add(textField.getText());
                textField.setText("");
            }
        }).dimensions(x + fieldWidth - 55, y, 50, 20).build());

        int buttonY = y + 25;
        int buttonWidth = (fieldWidth - 10) / 2;
        this.addDrawableChild(ButtonWidget.builder(Text.of("Clear All"), (button) -> {
            setting.clear();
            scrollOffset = 0;
        }).dimensions(x, buttonY, buttonWidth, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.of("Back"), (button) -> this.close()).dimensions(x + buttonWidth + 10, buttonY, buttonWidth, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        String title = "Editing list: " + setting.getName() + " (" + setting.size() + " items)";
        int titleWidth = TextRendererUtil.textRenderer.getWidth(title);
        context.drawTextWithShadow(TextRendererUtil.textRenderer, title, (this.width - titleWidth) / 2, this.textField.getY() - TextRendererUtil.textRenderer.fontHeight - 6, Color.WHITE.getRGB());

        int listY = this.textField.getY() + 50;
        int listHeight = this.height - listY - 10;
        int itemHeight = TextRendererUtil.textRenderer.fontHeight + 4;
        int maxVisible = listHeight / itemHeight;

        int start = scrollOffset;
        int end = Math.min(start + maxVisible, setting.size());

        int listLeft = this.textField.getX();
        int listRight = listLeft + this.textField.getWidth() + 60;

        context.fill(listLeft, listY - 2, listRight, listY + listHeight, new Color(0, 0, 0, 120).getRGB());

        for (int i = start; i < end; i++) {
            String item = setting.get(i);
            int itemY = listY + (i - start) * itemHeight;

            boolean hovered = mouseX >= listLeft && mouseX <= listRight && mouseY >= itemY && mouseY <= itemY + itemHeight;
            if (hovered) {
                context.fill(listLeft, itemY, listRight, itemY + itemHeight, new Color(255, 255, 255, 30).getRGB());
            }

            context.drawTextWithShadow(TextRendererUtil.textRenderer, item, listLeft + 4, itemY + 2, Color.LIGHT_GRAY.getRGB());

            String removeLabel = "x";
            int removeX = listRight - 15;
            int removeColor = Color.RED.getRGB();

            if (hovered && mouseX >= removeX) {
                removeColor = Color.WHITE.getRGB();
            }
            context.drawTextWithShadow(TextRendererUtil.textRenderer, removeLabel, removeX, itemY + 2, removeColor);
        }

        super.render(context, mouseX, mouseY, deltaTicks);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (click.button() == 0) {
            int listY = this.textField.getY() + 50;
            int itemHeight = TextRendererUtil.textRenderer.fontHeight + 4;
            int listLeft = this.textField.getX();
            int listRight = listLeft + this.textField.getWidth() + 60;
            int removeX = listRight - 15;

            for (int i = scrollOffset; i < setting.size(); i++) {
                int itemY = listY + (i - scrollOffset) * itemHeight;
                if (click.x() >= removeX && click.x() <= listRight
                        && click.y() >= itemY && click.y() <= itemY + itemHeight) {
                    setting.remove(setting.get(i));
                    return true;
                }
            }
        }

        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int maxScroll = Math.max(0, setting.size() - 5);
        if (verticalAmount > 0) {
            scrollOffset = Math.max(0, scrollOffset - 1);
        } else if (verticalAmount < 0) {
            scrollOffset = Math.min(maxScroll, scrollOffset + 1);
        }
        return true;
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        ClickGui clickGui = ModuleUtil.getModule(ClickGui.class);
        switch (clickGui.background.getValue()) {
            case INGAME -> this.renderInGameBackground(context);
            case PANORAMA -> this.renderPanoramaBackground(context, deltaTicks);
            case BLUR -> this.applyBlur(context);
            case DARKENING -> this.renderDarkening(context);
        }
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (input.isEnter()) {
            if (!textField.getText().isEmpty()) {
                setting.add(textField.getText());
                textField.setText("");
            }
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public void close() {
        this.client.setScreen(Pigeon.clickGuiScreen);
    }
}
