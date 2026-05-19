package owo.pigeon.utils.export;

import com.google.gson.JsonObject;
import net.engio.mbassy.listener.Handler;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import owo.pigeon.event.events.ClientTickEvent;
import owo.pigeon.utils.ItemUtil;
import owo.pigeon.utils.RegexUtil;
import owo.pigeon.utils.WorldUtil;
import owo.pigeon.utils.chat.ChatUtil;
import owo.pigeon.utils.player.PlayerUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static owo.pigeon.Pigeon.GSON;
import static owo.pigeon.Pigeon.mc;

public class ExportManager {
    public enum ExportTask {
        NONE, HUNTING_BOX
    }

    private static int lastProcessedSyncId = -1;
    private static ExportTask currentTask = ExportTask.NONE;
    private static final Map<String, Integer> shardData = new TreeMap<>((o1, o2) -> {
        String s1 = o1.replaceAll("\\d", "");
        String s2 = o2.replaceAll("\\d", "");

        List<String> ORDER = Arrays.asList("C", "U", "R", "E", "L");
        int p1 = ORDER.indexOf(s1);
        int p2 = ORDER.indexOf(s2);

        int res = Integer.compare(p1, p2);
        if (res != 0) return res;

        try {
            int n1 = Integer.parseInt(o1.replaceAll("\\D", ""));
            int n2 = Integer.parseInt(o2.replaceAll("\\D", ""));
            return Integer.compare(n1, n2);
        } catch (Exception e) {
            return o1.compareTo(o2);
        }
    });

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
    public void onTickPre(ClientTickEvent.Pre event) {
        if (WorldUtil.nullCheck() || currentTask == ExportTask.NONE) return;

        if (mc.player.currentScreenHandler instanceof GenericContainerScreenHandler container) {
            String title = mc.currentScreen.getTitle().getString();

            switch (currentTask) {
                case HUNTING_BOX -> {
                    if (!title.contains("Hunting Box")) {
                        lastProcessedSyncId = -1;
                        currentTask = ExportTask.NONE;
                        ChatUtil.sendDebugMessage("ExportManager", "Acquisition terminated.");
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
                            ChatUtil.sendDebugMessage("ExportManager", "slot: " + slotId + ", ID: " + idStr + ", amount: " + amountStr);
                            if (amountStr != null && idStr != null)
                                shardData.put(idStr, shardData.getOrDefault(idStr, 0) + Integer.parseInt(amountStr));
                        }
                    }

                    lastProcessedSyncId = container.syncId;

                    if (container.getSlot(53).getStack().isOf(Items.ARROW)) {
                        ChatUtil.sendDebugMessage("ExportManager", "Hunting box: next page");
                        PlayerUtil.clickSlot(container.syncId, 53, 0, SlotActionType.PICKUP);
                    } else {
                        ChatUtil.sendDebugMessage("ExportManager", "Hunting box: done");
                        if (shardData.isEmpty()) {
                            ChatUtil.sendMessage("Export", "Failed to get shards!");
                        } else {
                            JsonObject root = new JsonObject();
                            root.add("hunting_box", GSON.toJsonTree(shardData));
                            mc.keyboard.setClipboard(GSON.toJson(root));
                            ChatUtil.sendMessage("Export", "Hunting box data has been exported to the clipboard!");
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
