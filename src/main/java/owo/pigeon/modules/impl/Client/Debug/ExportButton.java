package owo.pigeon.modules.impl.Client.Debug;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.engio.mbassy.listener.Handler;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.text.Text;
import owo.pigeon.event.events.RenderEvent;
import owo.pigeon.mixin.accessors.IAccessorHandledScreen;
import owo.pigeon.mixin.accessors.IAccessorScreen;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.utils.Chat.ChatUtil;
import owo.pigeon.utils.ItemUtil;

import java.util.List;

import static owo.pigeon.Pigeon.mc;

public class ExportButton extends Module {
    private ButtonWidget exportButton;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public ExportButton() {
        super("ExportButton", Category.CLIENT);
    }

    @Handler
    public void onRenderContainer(RenderEvent.RenderContainerEvent event) {
        if (!(event.getScreen() instanceof GenericContainerScreen screen)) {
            exportButton = null;
            return;
        }

        GenericContainerScreenHandler container = event.getContainer();
        if (container == null) return;

        IAccessorHandledScreen guiAccessor = (IAccessorHandledScreen) screen;
        IAccessorScreen screenAccessor = (IAccessorScreen) screen;

        int buttonX = guiAccessor.pigeon$getX() + guiAccessor.pigeon$getBackgroundWidth() + 5;
        int buttonY = guiAccessor.pigeon$getY();

        if (exportButton == null) {
            exportButton = ButtonWidget.builder(Text.of("Export JSON"), button -> exportToJson(container))
                    .dimensions(buttonX, buttonY, 85, 20)
                    .build();

            if (!screenAccessor.pigeon$getChildren().contains(exportButton)) {
                screenAccessor.pigeon$getChildren().add(exportButton);
            }
        }

        exportButton.setX(buttonX);
        exportButton.setY(buttonY);

        exportButton.render(event.getContext(), event.getMouseX(), event.getMouseY(), event.getDelta());
    }

    private void exportToJson(GenericContainerScreenHandler container) {
        JsonArray itemsArray = new JsonArray();

        for (int i = 0; i < container.slots.size(); i++) {
            if (container.getSlot(i).inventory == mc.player.getInventory()) continue;

            ItemStack stack = container.getSlot(i).getStack();
            if (stack.isEmpty()) continue;

            JsonObject itemJson = new JsonObject();
            itemJson.addProperty("slot", i);
            itemJson.addProperty("name", stack.getName().getString());
            itemJson.addProperty("id", Registries.ITEM.getId(stack.getItem()).toString());

            JsonArray loreArray = new JsonArray();
            List<Text> loreLines = ItemUtil.getItemLore(stack);
            for (Text line : loreLines) {
                loreArray.add(line.getString());
            }
            itemJson.add("lore", loreArray);

            itemsArray.add(itemJson);
        }

        if (itemsArray.isEmpty()) {
            ChatUtil.sendCustomPrefixMessage("ExportButton", "§cContainer is empty!");
            return;
        }

        String finalJson = gson.toJson(itemsArray);
        mc.keyboard.setClipboard(finalJson);
        ChatUtil.sendCustomPrefixMessage("ExportButton", "§aJSON copied to clipboard! (" + itemsArray.size() + " items)");
    }
}