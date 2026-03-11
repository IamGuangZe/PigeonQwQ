package owo.pigeon.utils.Export;

import net.engio.mbassy.listener.Handler;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import owo.pigeon.event.events.TickEvent;
import owo.pigeon.utils.Chat.ChatUtil;
import owo.pigeon.utils.ItemUtil;
import owo.pigeon.utils.Player.PlayerUtil;
import owo.pigeon.utils.RegexUtil;
import owo.pigeon.utils.WorldUtil;

import java.util.*;

import static owo.pigeon.Pigeon.mc;

public class ExportManager {
    public enum ExportTask {
        NONE, HUNTING_BOX;
    }

    private static int lastProcessedSyncId = -1;
    private static ExportTask currentTask = ExportTask.NONE;
    private static final Map<String, Integer> shardData = new HashMap<>();

    public static void startExport(ExportTask task) {
        switch (task) {
            case HUNTING_BOX -> {
                shardData.clear();
                lastProcessedSyncId = -1;
                mc.player.networkHandler.sendChatMessage("/huntingbox");
            }

            case null, default -> {
                currentTask = ExportTask.NONE;
                return;
            }
        }

        currentTask = task;
    }

    @Handler
    public void onTickPre(TickEvent.ClientTickEvent.Pre event) {
        if (WorldUtil.nullCheck() || currentTask == ExportTask.NONE) return;

        if (mc.player.currentScreenHandler instanceof GenericContainerScreenHandler container) {
            String title = mc.currentScreen.getTitle().getString();

            switch (currentTask) {
                case HUNTING_BOX -> {
                    if (!title.contains("Hunting Box")) {
                        lastProcessedSyncId = -1;
                        currentTask = ExportTask.NONE;
                        ChatUtil.sendDebugMessage("ExportManager","Acquisition terminated.");
                        return;
                    }


                    if (container.syncId == lastProcessedSyncId) return;
                    if (!container.getSlot(49).getStack().isOf(Items.BARRIER)) return;

                    for (int r = 1; r <= 3; r++) {
                        for (int c = 1; c <= 7; c++) {
                            int slotId = r * 9 + c;
                            ItemStack stack = container.getSlot(slotId).getStack();
                            if (stack.isEmpty()) continue;

                            String lore = String.join("\n", ItemUtil.getItemLore(stack).stream().map(Text::getString).toList());
                            String amountStr = RegexUtil.regexGetPart("Owned: (\\d+)", lore, 1);
                            String idStr = RegexUtil.regexGetPart("ID ([A-Z]\\d+)", lore, 1);
                            ChatUtil.sendDebugMessage("ExportManager","slot: " + slotId + ", ID: " + idStr + ", amount: " + amountStr);
                            if (amountStr != null && idStr != null)
                                shardData.put(idStr, shardData.getOrDefault(idStr, 0) + Integer.parseInt(amountStr));
                        }
                    }

                    lastProcessedSyncId = container.syncId;

                    if (container.getSlot(53).getStack().isOf(Items.ARROW)) {
                        ChatUtil.sendDebugMessage("ExportManager","Hunting box: next page");
                        PlayerUtil.clickSlot(container.syncId, 53,0, SlotActionType.PICKUP);
                    } else {
                        ChatUtil.sendDebugMessage("ExportManager","Hunting box: done");
                        if (shardData.isEmpty()) {
                            ChatUtil.sendCustomPrefixMessage("Export","Failed to get shards!");
                        } else {
                            StringBuilder sb = new StringBuilder("{\n  \"hunting_box\": {\n");
                            List<String> keys = new ArrayList<>(shardData.keySet());
                            Collections.sort(keys);

                            for (int i = 0; i < keys.size(); i++) {
                                String key = keys.get(i);
                                sb.append("    \"").append(key).append("\": ").append(shardData.get(key));
                                if (i < keys.size() - 1) sb.append(",");
                                sb.append("\n");
                            }
                            sb.append("  }\n}");

                            mc.keyboard.setClipboard(sb.toString());
                            ChatUtil.sendCustomPrefixMessage("Export","Hunting box data has been exported to the clipboard!");
                        }

                        lastProcessedSyncId = -1;
                        currentTask = ExportTask.NONE;
                        mc.player.closeHandledScreen();
                    }
                }
            }
        }
    }
}
