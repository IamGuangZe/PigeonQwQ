package owo.pigeon.modules.impl.debug;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.engio.mbassy.listener.Handler;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.text.Text;
import owo.pigeon.event.events.RenderEvent;
import owo.pigeon.mixin.accessors.IAccessorHandledScreen;
import owo.pigeon.mixin.accessors.IAccessorScreen;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.utils.ItemUtil;
import owo.pigeon.utils.chat.ChatUtil;
import owo.pigeon.utils.hypixel.skyblock.SkyblockUtil;

import java.util.List;

import static owo.pigeon.Pigeon.GSON;
import static owo.pigeon.Pigeon.mc;

public class ExportButton extends Module {
    private ButtonWidget exportButton;
    private GenericContainerScreen lastScreen;

    public ExportButton() {
        super("ExportButton", Category.DEBUG);
    }

    @Handler
    public void onRenderContainer(RenderEvent.RenderContainerEvent event) {
        if (!(event.getScreen() instanceof GenericContainerScreen screen)) {
            exportButton = null;
            lastScreen = null;
            return;
        }

        if (lastScreen != screen) {
            exportButton = null;
            lastScreen = screen;
        }

        GenericContainerScreenHandler container = event.getContainer();
        if (container == null) return;

        IAccessorHandledScreen guiAccessor = (IAccessorHandledScreen) screen;
        IAccessorScreen screenAccessor = (IAccessorScreen) screen;

        int buttonX = guiAccessor.pigeon$getX() + guiAccessor.pigeon$getBackgroundWidth() + 5;
        int buttonY = guiAccessor.pigeon$getY();

        if (exportButton == null) {
            exportButton = ButtonWidget.builder(Text.of("Export JSON"), button -> {
                        exportToJson(screen, container);
                        button.setFocused(false);
                    })
                    .dimensions(buttonX, buttonY, 85, 20)
                    .build();

            if (!screenAccessor.pigeon$getChildren().contains(exportButton)) {
                screenAccessor.pigeon$getChildren().add(exportButton);
            }
        }

        exportButton.setX(buttonX);
        exportButton.setY(buttonY);

        if (exportButton.isFocused()) {
            exportButton.setFocused(false);
        }

        exportButton.render(event.getContext(), event.getMouseX(), event.getMouseY(), event.getDelta());
    }

    private void exportToJson(GenericContainerScreen screen, GenericContainerScreenHandler container) {
        JsonObject result = new JsonObject();
        result.addProperty("title", screen.getTitle().getString());
        result.addProperty("slots", container.getInventory().size());

        JsonArray itemsArray = new JsonArray();

        for (int i = 0; i < container.slots.size(); i++) {
            if (container.getSlot(i).inventory == mc.player.getInventory()) continue;

            ItemStack stack = container.getSlot(i).getStack();
            if (stack.isEmpty()) continue;

            JsonObject itemJson = new JsonObject();
            itemJson.addProperty("slot", i);
            itemJson.addProperty("name", stack.getName().getString());
            itemJson.addProperty("id", Registries.ITEM.getId(stack.getItem()).toString());

            if (stack.isOf(Items.PLAYER_HEAD)) {
                String texture = ItemUtil.getSkullTexture(stack);
                if (texture != null) {
                    itemJson.addProperty("texture", texture);
                }
            }

            String skyblockId = SkyblockUtil.getItemCustomData(stack, "id", SkyblockUtil.STRING_EXTRACTOR);
            if (skyblockId != null) {
                itemJson.addProperty("skyblockId", skyblockId);
            }

            List<Text> loreLines = ItemUtil.getItemLore(stack);
            if (!loreLines.isEmpty()) {
                JsonArray loreArray = new JsonArray();
                for (Text line : loreLines) {
                    loreArray.add(line.getString());
                }
                itemJson.add("lore", loreArray);
            }

            itemsArray.add(itemJson);
        }

        if (itemsArray.isEmpty()) {
            ChatUtil.sendMessage("ExportButton", "&cContainer is empty!");
            return;
        }

        result.add("items", itemsArray);
        String finalJson = GSON.toJson(result);
        mc.keyboard.setClipboard(finalJson);
        ChatUtil.sendMessage("ExportButton", "&aJSON copied to clipboard! (" + itemsArray.size() + " items)");
    }
}