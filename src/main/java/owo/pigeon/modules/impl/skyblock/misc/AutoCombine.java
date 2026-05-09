package owo.pigeon.modules.impl.skyblock.misc;

import net.engio.mbassy.listener.Handler;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import owo.pigeon.event.events.PacketEvent;
import owo.pigeon.event.events.RenderEvent;
import owo.pigeon.mixin.accessors.IAccessorHandledScreen;
import owo.pigeon.mixin.accessors.IAccessorScreen;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.IntSetting;
import owo.pigeon.settings.ListSetting;
import owo.pigeon.utils.hypixel.skyblock.SkyblockUtil;
import owo.pigeon.utils.player.PlayerUtil;

import java.util.*;
import java.util.stream.Collectors;

public class AutoCombine extends Module {
    public AutoCombine() {
        super("AutoCombine", Category.MISC);
    }

    private final List<String> AnvilCombineList = List.of(
            "infinite_quiver:6",
            "infinite_quiver:7",
            "infinite_quiver:8",
            "infinite_quiver:9"
    );

    public IntSetting delay = setting("delay", 450, 1, 1000, "ms", v -> true);
    public ListSetting anvilCombineList = setting("anvil-combine-list", AnvilCombineList, v -> true);

    private GenericContainerScreen lastScreen;
    private ButtonWidget button;
    private boolean combining;
    private long lastAction;

    @Handler
    public void onRenderContainer(RenderEvent.RenderContainerEvent event) {
        if (!(event.getScreen() instanceof GenericContainerScreen screen) || !screen.getTitle().getString().equals("Anvil")) {
            combining = false;
            button = null;
            lastScreen = null;
            return;
        }

        GenericContainerScreenHandler container = event.getContainer();
        if (container == null) return;

        if (lastScreen != screen) {
            button = null;
            lastScreen = screen;
        }

        IAccessorHandledScreen handled = (IAccessorHandledScreen) screen;
        IAccessorScreen accessor = (IAccessorScreen) screen;

        int x = handled.pigeon$getX() + handled.pigeon$getBackgroundWidth() + 5;
        int y = handled.pigeon$getY();

        if (button == null) {
            button = ButtonWidget.builder(Text.of("Combine"), button -> {
                        combining = !combining;
                        lastAction = System.currentTimeMillis();
                        button.setFocused(false);
                    })
                    .dimensions(x, y, 85, 20)
                    .build();

            if (!accessor.pigeon$getChildren().contains(button)) {
                accessor.pigeon$getChildren().add(button);
            }
        }

        button.setX(x);
        button.setY(y);

        if (button.isFocused()) button.setFocused(false);

        button.render(event.getContext(), event.getMouseX(), event.getMouseY(), event.getDelta());

        if (combining) process(container);
    }

    @Handler
    public void onPacketSend(PacketEvent.SendPacketEvent event) {
        if (combining && event.getPacket() instanceof CloseHandledScreenC2SPacket) {
            combining = false;
        }
    }

    private void process(GenericContainerScreenHandler container) {
        long now = System.currentTimeMillis();
        if (now - lastAction < delay.getValue()) return;

        ItemStack result = container.getSlot(13).getStack();
        ItemStack left = container.getSlot(29).getStack();
        ItemStack right = container.getSlot(33).getStack();

        lastAction = now;

        if (!result.isOf(Items.BARRIER) && left.isEmpty() && right.isEmpty()) {
            PlayerUtil.clickSlot(container.syncId, 13, 0, SlotActionType.QUICK_MOVE);
            return;
        }

        if (!left.isEmpty() && !right.isEmpty()) {
            if (!result.isOf(Items.BARRIER)) {
                PlayerUtil.clickSlot(container.syncId, 22, 0, SlotActionType.PICKUP);
            } else {
                combining = false;
            }
            return;
        }

        int slot = left.isEmpty() && right.isEmpty()
                ? findPair(container)
                : findMatch(container, left.isEmpty() ? right : left);

        if (slot == -1) {
            combining = false;
            return;
        }

        PlayerUtil.clickSlot(container.syncId, slot, 0, SlotActionType.QUICK_MOVE);
    }

    private int findPair(GenericContainerScreenHandler container) {
        Map<String, Integer> seen = new HashMap<>();
        for (int i = container.slots.size() - 36; i < container.slots.size(); i++) {
            ItemStack stack = container.getSlot(i).getStack();
            if (!isValidBook(stack)) continue;
            for (String key : getKeys(stack)) {
                if (!anvilCombineList.getValue().contains(key)) continue;
                if (seen.containsKey(key)) return seen.get(key);
                seen.put(key, i);
            }
        }

        return -1;
    }

    private int findMatch(GenericContainerScreenHandler container, ItemStack target) {
        Set<String> targetKeys = getKeys(target);
        if (targetKeys.isEmpty()) return -1;
        for (int i = container.slots.size() - 36; i < container.slots.size(); i++) {
            ItemStack stack = container.getSlot(i).getStack();
            if (!isValidBook(stack)) continue;
            if (getKeys(stack).stream().anyMatch(targetKeys::contains)) {
                return i;
            }
        }

        return -1;
    }

    public boolean isValidBook(ItemStack stack) {
        return !stack.isEmpty()
                && stack.hasGlint()
                && "ENCHANTED_BOOK".equals(SkyblockUtil.getItemCustomData(stack, "id", SkyblockUtil.STRING_EXTRACTOR));
    }

    public Set<String> getKeys(ItemStack stack) {
        NbtCompound enchants = SkyblockUtil.getItemCustomData(stack, "enchantments", SkyblockUtil.COMPOUND_EXTRACTOR);
        if (enchants == null) return Collections.emptySet();
        return enchants.getKeys().stream()
                .map(key -> enchants.getInt(key)
                        .map(level -> key + ":" + level)
                        .orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }
}
