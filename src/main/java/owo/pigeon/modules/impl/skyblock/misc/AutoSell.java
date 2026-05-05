package owo.pigeon.modules.impl.skyblock.misc;

import net.engio.mbassy.listener.Handler;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import owo.pigeon.Pigeon;
import owo.pigeon.event.events.ClickSlotEvent;
import owo.pigeon.event.events.RenderEvent;
import owo.pigeon.event.events.TickEvent;
import owo.pigeon.mixin.accessors.IAccessorHandledScreen;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.IntSetting;
import owo.pigeon.settings.ListSetting;
import owo.pigeon.utils.ColorUtil;
import owo.pigeon.utils.WorldUtil;
import owo.pigeon.utils.hypixel.skyblock.SkyblockUtil;
import owo.pigeon.utils.player.PlayerUtil;
import owo.pigeon.utils.render.TextRendererUtil;

import java.util.ArrayList;
import java.util.List;

import static owo.pigeon.Pigeon.mc;

public class AutoSell extends Module {
    public AutoSell() {
        super("AutoSell", Category.MISC);
    }

    public IntSetting delay = setting("delay", 5, 1, 20, "tick", v -> true);
    public ListSetting itemName = setting("item-name", List.of(), v -> true);
    public ListSetting itemId = setting("item-id", List.of(), v -> true);
    private int tick;

    @Handler
    public void onTickPre(TickEvent.ClientTickEvent.Pre event) {
        if (WorldUtil.nullCheck()) return;

        if (mc.player.currentScreenHandler instanceof GenericContainerScreenHandler container) {
            if (!SkyblockUtil.isSellableMenu(container)) return;

            if (tick <= delay.getMaxValue()) tick++;

            if (tick == delay.getValue()) {
                int containerSize = container.slots.size() - 36;
                List<String> names = itemName.getValue();
                List<String> ids = itemId.getValue();

                for (int i = containerSize; i < container.slots.size(); i++) {
                    ItemStack stack = container.getSlot(i).getStack();
                    if (stack.isEmpty()) continue;

                    if (matchesItem(stack, names, ids)) {
                        PlayerUtil.clickSlot(container.syncId, i, 0, SlotActionType.QUICK_MOVE);
                        break;
                    }
                }
            }

        } else {
            tick = 0;
        }
    }

    @Handler
    public void onRenderContainer(RenderEvent.RenderContainerEvent event) {
        if (!Pigeon.isDebug()) return;
        if (!(event.getScreen() instanceof GenericContainerScreen screen)) return;
        GenericContainerScreenHandler container = event.getContainer();
        if (container == null) return;

        IAccessorHandledScreen guiAccessor = (IAccessorHandledScreen) screen;
        int startX = guiAccessor.pigeon$getX() + guiAccessor.pigeon$getBackgroundWidth() + 5;
        int startY = guiAccessor.pigeon$getY();

        boolean isSellable = SkyblockUtil.isSellableMenu(container);
        List<String> lines = new ArrayList<>();
        lines.add("&6&lAutoSell Debug");
        lines.add("&7Sellable: " + (isSellable ? "&aYes" : "&cNo"));
        if (isSellable) {
            lines.add("&7Tick: &e" + tick + "&7/&e" + delay.getValue());
            lines.add("&7ID list: &f" + itemId.getValue().size() + " items");
            lines.add("&7Name list: &f" + itemName.getValue().size() + " items");
        }
        TextRendererUtil.drawStringList(event.getContext(), lines, startX, startY);
    }

    @Handler
    public void onClickSlot(ClickSlotEvent event) {
        tick = 0;
    }

    private boolean matchesItem(ItemStack stack, List<String> names, List<String> ids) {
        String id = SkyblockUtil.getItemCustomData(stack, "id");
        if (id != null) {
            for (String targetId : ids) {
                if (id.equals(targetId)) return true;
            }
        }

        String displayName = ColorUtil.removeColor(stack.getName().getString());
        for (String targetName : names) {
            if (displayName.contains(targetName)) return true;
        }

        return false;
    }
}
