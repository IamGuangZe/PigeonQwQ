package owo.pigeon.gui.clickgui.pigeon.edits;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import owo.pigeon.Pigeon;
import owo.pigeon.modules.impl.client.ClickGui;
import owo.pigeon.settings.AbstractSetting;
import owo.pigeon.settings.BlockSetting;
import owo.pigeon.settings.CharSetting;
import owo.pigeon.settings.StringSetting;
import owo.pigeon.utils.ModuleUtil;
import owo.pigeon.utils.render.TextRendererUtil;

import java.awt.*;

public class SettingEditScreen extends Screen {
    protected final AbstractSetting<?> setting;

    private TextFieldWidget textField;
    private String currentText = null;

    public SettingEditScreen(AbstractSetting<?> setting) {
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

        this.textField = new TextFieldWidget(TextRendererUtil.textRenderer, x, y, fieldWidth, 20, Text.of("Setting Input"));

        if (currentText != null) {
            textField.setText(currentText);
        } else if (setting instanceof BlockSetting blockSetting) {
            textField.setText(Registries.BLOCK.getId(blockSetting.getValue()).toString());
        } else if (setting instanceof StringSetting stringSetting) {
            textField.setText(stringSetting.getValue());
        } else if (setting instanceof CharSetting charSetting) {
            textField.setText(String.valueOf(charSetting.getValue()));
            textField.setMaxLength(1);
        }

        this.textField.setChangedListener(text -> this.currentText = text);

        this.addDrawableChild(this.textField);
        this.setInitialFocus(this.textField);

        int buttonWidth = (fieldWidth - 10) / 2;
        int buttonY = y + 25;

        this.addDrawableChild(ButtonWidget.builder(Text.of("Confirm"), (button) -> saveAndClose()).dimensions(x, buttonY, buttonWidth, 20).build());

        this.addDrawableChild(ButtonWidget.builder(Text.of("Cancel"), (button) -> this.close()).dimensions(x + buttonWidth + 10, buttonY, buttonWidth, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        String text = "You are editing: " + setting.getName();
        int textWidth = TextRendererUtil.textRenderer.getWidth(text);
        int textX = (this.width - textWidth) / 2;
        int textY = this.textField.getY() - TextRendererUtil.textRenderer.fontHeight - 2;
        context.drawTextWithShadow(TextRendererUtil.textRenderer, text, textX, textY, Color.WHITE.getRGB());

        if (setting instanceof BlockSetting blockSetting) {
            Identifier id = Identifier.tryParse(textField.getText().toLowerCase().trim());

            if (id != null && Registries.BLOCK.containsId(id)) {
                textField.setEditableColor(0xFFFFFFFF);

                int previewX = textField.getX() + textField.getWidth() + 4;
                int previewY = textField.getY() + (textField.getHeight() - 16) / 2;

                context.drawItem(Registries.BLOCK.get(id).asItem().getDefaultStack(), previewX, previewY);
            } else {
                textField.setEditableColor(0xFFFF5555);
            }

        }

        super.render(context, mouseX, mouseY, deltaTicks);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
        ClickGui clickGui = ModuleUtil.getModule(ClickGui.class);

        // if (clickGui.background.getValue()) super.renderBackground(context, mouseX, mouseY, deltaTicks);
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
            saveAndClose();
            return true;
        }

        return super.keyPressed(input);
    }

    @Override
    public void close() {
        this.client.setScreen(Pigeon.clickGuiScreen);
    }

    private void saveAndClose() {
        if (textField.getText().isEmpty()) {
            this.close();
            return;
        }

        if (setting instanceof BlockSetting blockSetting) {
            Identifier id = Identifier.tryParse(textField.getText().toLowerCase());
            if (id != null && Registries.BLOCK.containsId(id)) {
                blockSetting.setValue(Registries.BLOCK.get(id));
            } else {
                this.close();
                return;
            }
        } else if (setting instanceof CharSetting charSetting) {
            charSetting.setValue(textField.getText().charAt(0));
        } else if (setting instanceof StringSetting stringSetting) {
            stringSetting.setValue(textField.getText());
        }

        this.close();
    }
}
