package owo.pigeon.modules.impl.skyblock.misc;

import net.engio.mbassy.listener.Handler;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import owo.pigeon.event.events.PacketEvent;
import owo.pigeon.event.events.RenderEvent;
import owo.pigeon.mixin.accessors.IAccessorAbstractContainerScreen;
import owo.pigeon.mixin.accessors.IAccessorScreen;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.IntSetting;
import owo.pigeon.settings.ListSetting;
import owo.pigeon.utils.ItemUtil;
import owo.pigeon.utils.player.PlayerUtil;

import java.util.*;
import java.util.stream.Collectors;

public class AutoCombine extends Module {
    public AutoCombine() {
        super("AutoCombine", Category.MISC);
    }

    private final List<String> DefaultAnvilList = List.of(
            "infinite_quiver:6",
            "infinite_quiver:7",
            "infinite_quiver:8",
            "infinite_quiver:9"
    );

    private final List<String> DefaultRuneList = List.of(
            "GEM:1",
            "GEM:2"
    );

    public IntSetting delay = setting("delay", 450, 1, 1000, "ms", v -> true);
    public ListSetting anvilCombineList = setting("anvil-combine-list", DefaultAnvilList, v -> true);
    public ListSetting runeCombineList = setting("rune-combine-list", DefaultRuneList, v -> true);

    private ContainerScreen lastScreen;
    private Button button;
    private boolean combining;
    private long lastAction;
    private CombineMode mode = CombineMode.NONE;

    private enum CombineMode {
        NONE, ANVIL, PEDESTAL
    }

    @Handler
    public void onRenderContainer(RenderEvent.RenderContainerEvent event) {
        if (!(event.getScreen() instanceof ContainerScreen screen)) {
            button = null;
            lastScreen = null;
            combining = false;
            mode = CombineMode.NONE;
            return;
        }

        String title = screen.getTitle().getString();
        CombineMode currentMode = detectMode(title);

        if (currentMode == CombineMode.NONE) {
            button = null;
            lastScreen = null;
            combining = false;
            mode = CombineMode.NONE;
            return;
        }

        ChestMenu container = event.getContainer();
        if (container == null) return;

        if (lastScreen != screen) {
            button = null;
            lastScreen = screen;
        }
        mode = currentMode;

        IAccessorAbstractContainerScreen handled = (IAccessorAbstractContainerScreen) screen;
        IAccessorScreen accessor = (IAccessorScreen) screen;

        int x = handled.pigeon$getLeftPos() + handled.pigeon$getImageWidth() + 5;
        int y = handled.pigeon$getTopPos();

        if (button == null) {
            button = Button.builder(Component.nullToEmpty("Combine"), btn -> {
                        combining = !combining;
                        lastAction = System.currentTimeMillis();
                        btn.setFocused(false);
                    })
                    .bounds(x, y, 85, 20)
                    .build();

            if (!accessor.pigeon$getChildren().contains(button)) {
                accessor.pigeon$getChildren().add(button);
            }
        }

        button.setX(x);
        button.setY(y);

        if (button.isFocused()) button.setFocused(false);

        button.extractRenderState(event.getContext(), event.getMouseX(), event.getMouseY(), event.getDelta());

        if (combining) {
            if (mode == CombineMode.ANVIL) processAnvil(container);
            else if (mode == CombineMode.PEDESTAL) processPedestal(container);
        }
    }

    @Handler
    public void onPacketSend(PacketEvent.SendPacketEvent event) {
        if (event.getPacket() instanceof ServerboundContainerClosePacket) {
            combining = false;
            mode = CombineMode.NONE;
        }
    }

    private CombineMode detectMode(String title) {
        if (title.equals("Anvil")) return CombineMode.ANVIL;
        if (title.equals("Runic Pedestal")) return CombineMode.PEDESTAL;
        return CombineMode.NONE;
    }

    private void processAnvil(ChestMenu container) {
        long now = System.currentTimeMillis();
        if (now - lastAction < delay.getValue()) return;

        ItemStack result = container.getSlot(13).getItem();
        ItemStack left = container.getSlot(29).getItem();
        ItemStack right = container.getSlot(33).getItem();

        lastAction = now;

        if (!result.is(Items.BARRIER) && left.isEmpty() && right.isEmpty()) {
            PlayerUtil.clickSlot(container.containerId, 13, 0, ContainerInput.QUICK_MOVE);
            return;
        }

        if (!left.isEmpty() && !right.isEmpty()) {
            if (!result.is(Items.BARRIER)) {
                PlayerUtil.clickSlot(container.containerId, 22, 0, ContainerInput.PICKUP);
            } else {
                combining = false;
            }
            return;
        }

        int slot = left.isEmpty() && right.isEmpty()
                ? findAnvilPair(container)
                : findAnvilMatch(container, left.isEmpty() ? right : left);

        if (slot == -1) {
            combining = false;
            mode = CombineMode.NONE;
            return;
        }

        PlayerUtil.clickSlot(container.containerId, slot, 0, ContainerInput.QUICK_MOVE);
    }

    private void processPedestal(ChestMenu container) {
        long now = System.currentTimeMillis();
        if (now - lastAction < delay.getValue()) return;

        ItemStack output = container.getSlot(31).getItem();
        ItemStack left = container.getSlot(19).getItem();
        ItemStack right = container.getSlot(25).getItem();

        lastAction = now;

        if (!output.is(Items.BARRIER) && !output.isEmpty() && left.isEmpty() && right.isEmpty()) {
            PlayerUtil.clickSlot(container.containerId, 31, 0, ContainerInput.QUICK_MOVE);
            return;
        }

        if (!left.isEmpty() && !right.isEmpty()) {
            if (!output.is(Items.BARRIER) && !output.isEmpty()) {
                PlayerUtil.clickSlot(container.containerId, 13, 0, ContainerInput.PICKUP);
            }
            return;
        }

        int slot = left.isEmpty() && right.isEmpty()
                ? findRunePair(container)
                : findRuneMatch(container, left.isEmpty() ? right : left);

        if (slot == -1) {
            if (!left.isEmpty() || !right.isEmpty()) return;
            combining = false;
            mode = CombineMode.NONE;
            return;
        }

        PlayerUtil.clickSlot(container.containerId, slot, 0, ContainerInput.QUICK_MOVE);
    }

    private int findAnvilPair(ChestMenu container) {
        Map<String, Integer> seen = new HashMap<>();
        for (int i = container.slots.size() - 36; i < container.slots.size(); i++) {
            ItemStack stack = container.getSlot(i).getItem();
            if (!isValidBook(stack)) continue;
            for (String key : getEnchantKeys(stack)) {
                if (!anvilCombineList.getValue().contains(key)) continue;
                if (seen.containsKey(key)) return seen.get(key);
                seen.put(key, i);
            }
        }
        return -1;
    }

    private int findAnvilMatch(ChestMenu container, ItemStack target) {
        Set<String> targetKeys = getEnchantKeys(target);
        if (targetKeys.isEmpty()) return -1;
        for (int i = container.slots.size() - 36; i < container.slots.size(); i++) {
            ItemStack stack = container.getSlot(i).getItem();
            if (!isValidBook(stack)) continue;
            if (getEnchantKeys(stack).stream().anyMatch(targetKeys::contains)) return i;
        }
        return -1;
    }

    private int findRunePair(ChestMenu container) {
        Map<String, Integer> seen = new HashMap<>();
        for (int i = container.slots.size() - 36; i < container.slots.size(); i++) {
            ItemStack stack = container.getSlot(i).getItem();
            if (!isValidRune(stack)) continue;
            for (String key : getRuneKeys(stack)) {
                if (!runeCombineList.getValue().contains(key)) continue;
                if (stack.getCount() >= 2) return i;
                if (seen.containsKey(key)) return seen.get(key);
                seen.put(key, i);
            }
        }
        return -1;
    }

    private int findRuneMatch(ChestMenu container, ItemStack target) {
        Set<String> targetKeys = getRuneKeys(target);
        if (targetKeys.isEmpty()) return -1;
        for (int i = container.slots.size() - 36; i < container.slots.size(); i++) {
            ItemStack stack = container.getSlot(i).getItem();
            if (!isValidRune(stack)) continue;
            if (getRuneKeys(stack).stream().anyMatch(targetKeys::contains)) return i;
        }
        return -1;
    }

    public boolean isValidBook(ItemStack stack) {
        return !stack.isEmpty()
                && stack.hasFoil()
                && "ENCHANTED_BOOK".equals(ItemUtil.getCustomDataValue(stack, "id", ItemUtil.STRING_EXTRACTOR));
    }

    public boolean isValidRune(ItemStack stack) {
        return !stack.isEmpty()
                && "RUNE".equals(ItemUtil.getCustomDataValue(stack, "id", ItemUtil.STRING_EXTRACTOR));
    }

    public Set<String> getEnchantKeys(ItemStack stack) {
        CompoundTag enchants = ItemUtil.getCustomDataValue(stack, "enchantments", ItemUtil.COMPOUND_EXTRACTOR);
        if (enchants == null) return Collections.emptySet();
        return enchants.keySet().stream()
                .map(key -> enchants.getInt(key)
                        .map(level -> key + ":" + level)
                        .orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public Set<String> getRuneKeys(ItemStack stack) {
        CompoundTag runes = ItemUtil.getCustomDataValue(stack, "runes", ItemUtil.COMPOUND_EXTRACTOR);
        if (runes == null) return Collections.emptySet();
        return runes.keySet().stream()
                .map(key -> runes.getInt(key)
                        .map(level -> key + ":" + level)
                        .orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }
}
