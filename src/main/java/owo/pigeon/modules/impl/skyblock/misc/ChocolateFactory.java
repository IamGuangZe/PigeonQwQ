package owo.pigeon.modules.impl.skyblock.misc;

import net.engio.mbassy.listener.Handler;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import owo.pigeon.Pigeon;
import owo.pigeon.event.events.ClickSlotEvent;
import owo.pigeon.event.events.ClientTickEvent;
import owo.pigeon.event.events.RenderEvent;
import owo.pigeon.mixin.accessors.IAccessorAbstractContainerScreen;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.EnableSetting;
import owo.pigeon.settings.IntSetting;
import owo.pigeon.utils.ItemUtil;
import owo.pigeon.utils.RegexUtil;
import owo.pigeon.utils.WorldUtil;
import owo.pigeon.utils.player.PlayerUtil;
import owo.pigeon.utils.render.FontUtil;

import java.util.ArrayList;
import java.util.List;

import static owo.pigeon.Pigeon.mc;

public class ChocolateFactory extends Module {
    public ChocolateFactory() {
        super("ChocolateFactory", Category.MISC);
    }

    public EnableSetting autoTower = setting("auto-tower", true, v -> true);
    public EnableSetting autoUpgrade = setting("auto-upgrade", false, v -> true);
    public IntSetting upgradeDelay = setting("upgrade-delay", 10, 1, 40, "tick", v -> autoUpgrade.getValue());
    public EnableSetting bestOnly = setting("best-only", true, v -> autoUpgrade.getValue());
    public EnableSetting upgradeTower = setting("upgrade-tower", true, v -> autoUpgrade.getValue());
    public EnableSetting upgradeCoach = setting("upgrade-coach", true, v -> autoUpgrade.getValue());
    public EnableSetting catchStrays = setting("catch-strays", true, v -> true);
    public IntSetting catchDelay = setting("catch-delay", 20, 1, 300, "tick", v -> catchStrays.getValue());

    private int catchTick = 0;
    private int upgradeTick = 0;

    @Handler
    public void onTickPost(ClientTickEvent.Post event) {
        if (WorldUtil.nullCheck()) return;

        if (mc.player.containerMenu instanceof ChestMenu container) {
            String title = mc.screen.getTitle().getString();
            if (!title.contains("Chocolate Factory")) return;

            // catch strays
            int straySlot = getStraySlot(container);
            boolean strayFound = straySlot != -1;

            if (catchStrays.getValue() && strayFound) {
                catchTick++;
                upgradeTick = 0;

                if (catchTick >= catchDelay.getValue()) {
                    PlayerUtil.clickSlot(container.containerId, straySlot, 0, ClickType.PICKUP);
                    return;
                }

            } else {
                catchTick = 0;
            }

            // auto tower
            if (autoTower.getValue()) {
                ItemStack towerStack = container.getSlot(39).getItem();
                if (!towerStack.isEmpty() && towerStack.is(Items.CLOCK)) {
                    String loreString = String.join("\n", ItemUtil.getItemLore(towerStack).stream().map(Component::getString).toList());
                    if (loreString.contains("Right-click to activate!")) {
                        PlayerUtil.clickSlot(container.containerId, 39, 1, ClickType.PICKUP);
                        return;
                    }
                }
            }

            // auto upgrade
            if (upgradeTick < upgradeDelay.getMaxValue() + 1) upgradeTick++;

            if (autoUpgrade.getValue() && upgradeTick >= upgradeDelay.getValue()) {
                long balance = getBalance(container);
                double baseProd = getBaseProduction(container);
                double multiplier = getMultiplier(container);

                int bestSlot = -1;
                double minCostPerUnit = Double.MAX_VALUE;
                long targetCost = 0;

                int[] slots = {28, 29, 30, 31, 32, 33, 34, 39, 42};
                for (int slot : slots) {
                    if (slot == 39 && !upgradeTower.getValue()) continue;
                    if (slot == 42 && !upgradeCoach.getValue()) continue;

                    ItemStack stack = container.getSlot(slot).getItem();
                    if (isInvalid(stack)) continue;

                    String lore = String.join("\n", ItemUtil.getItemLore(stack).stream().map(Component::getString).toList());
                    long cost = parseCost(lore);
                    double delta = calculateDelta(slot, stack, baseProd, multiplier);

                    if (cost <= 0 || delta <= 0) continue;
                    double efficiency = (double) cost / delta;

                    if (bestOnly.getValue()) {
                        if (efficiency < minCostPerUnit) {
                            minCostPerUnit = efficiency;
                            bestSlot = slot;
                            targetCost = cost;
                        }
                    } else if (balance >= cost && efficiency < minCostPerUnit) {
                        minCostPerUnit = efficiency;
                        bestSlot = slot;
                        targetCost = cost;
                    }
                }

                if (bestSlot != -1 && balance >= targetCost) {
                    PlayerUtil.clickSlot(container.containerId, bestSlot, 0, ClickType.PICKUP);
                }
            }
        } else {
            catchTick = 0;
            upgradeTick = 0;
        }
    }

    @Handler
    public void onRenderContainer(RenderEvent.RenderContainerEvent event) {
        if (!Pigeon.isDebug()) return;
        if (!(event.getScreen() instanceof ContainerScreen screen)) return;

        ChestMenu container = event.getContainer();
        if (container == null || !screen.getTitle().getString().contains("Chocolate Factory")) return;

        IAccessorAbstractContainerScreen guiAccessor = (IAccessorAbstractContainerScreen) screen;
        int startX = guiAccessor.pigeon$getLeftPos() + guiAccessor.pigeon$getImageWidth() + 5;
        int startY = guiAccessor.pigeon$getTopPos();

        long balance = getBalance(container);
        double baseProd = getBaseProduction(container);
        double multiplier = getMultiplier(container);

        List<String> debugLines = new ArrayList<>();
        debugLines.add("&6&lChocolate Factory");
        debugLines.add("&7Balance: &f" + String.format("%,d", balance));
        debugLines.add("&7Base Prod: &f" + String.format("%.1f", baseProd));
        debugLines.add("&7Multiplier: &b" + String.format("%.3fx", multiplier));
        debugLines.add("&7Catch Progress: &e" + catchTick + "/" + catchDelay.getValue());
        debugLines.add("&7Upgrade Progress: &e" + upgradeTick + "/" + upgradeDelay.getValue());
        debugLines.add("&8--------------------");

        int[] slots = {28, 29, 30, 31, 32, 33, 34, 39, 42};
        int bestSlot = -1;
        double minCostPerUnit = Double.MAX_VALUE;
        List<String> rabbitLines = new ArrayList<>();

        for (int slot : slots) {
            ItemStack stack = container.getSlot(slot).getItem();
            if (isInvalid(stack)) continue;

            String lore = String.join("\n", ItemUtil.getItemLore(stack).stream().map(Component::getString).toList());
            long cost = parseCost(lore);
            double delta = calculateDelta(slot, stack, baseProd, multiplier);

            if (cost <= 0 || delta <= 0) continue;
            double efficiency = (double) cost / delta;

            if (efficiency < minCostPerUnit) {
                minCostPerUnit = efficiency;
                bestSlot = slot;
            }

            String name = stack.getHoverName().getString().split("-")[0].trim();
            String color = (balance >= cost) ? "&a" : "&c";
            rabbitLines.add(String.format("%s%s|%d|: &fV: %.2f (Δ%.1f)", color, name, slot, efficiency, delta));
        }

        for (String line : rabbitLines) {
            if (bestSlot != -1 && line.contains("|" + bestSlot + "|")) {
                debugLines.add(line.replaceAll("\\|\\d+\\|", "") + " &b&l[BEST]");
            } else {
                debugLines.add(line.replaceAll("\\|\\d+\\|", ""));
            }
        }
        FontUtil.drawStringList(event.getContext(), debugLines, startX, startY);
    }

    @Handler
    public void onClickSlot(ClickSlotEvent event) {
        catchTick = 0;
        upgradeTick = 0;
    }

    private boolean isInvalid(ItemStack stack) {
        return stack.isEmpty() || stack.is(Items.GRAY_DYE) || stack.is(Items.BARRIER) || stack.is(Items.BLACK_STAINED_GLASS_PANE);
    }

    private int getStraySlot(ChestMenu container) {
        for (int i = 0; i < container.slots.size(); i++) {
            ItemStack stack = container.getSlot(i).getItem();
            if (stack.isEmpty()) continue;
            String name = stack.getHoverName().getString();
            if (name.contains("CLICK ME!") || name.contains("Golden Rabbit")) {
                return i;
            }
        }
        return -1;
    }

    private long getBalance(ChestMenu container) {
        String name = container.getSlot(13).getItem().getHoverName().getString().replaceAll("[^0-9]", "");
        return name.isEmpty() ? 0 : Long.parseLong(name);
    }

    private double getBaseProduction(ChestMenu container) {
        ItemStack stack = container.getSlot(45).getItem();
        if (isInvalid(stack)) return 0;
        String lore = String.join("\n", ItemUtil.getItemLore(stack).stream().map(Component::getString).toList());
        String res = RegexUtil.regexGetPart("Base Chocolate: ([\\d,.]+)", lore, 1);
        return res != null ? Double.parseDouble(res.replace(",", "")) : 0;
    }

    private double getMultiplier(ChestMenu container) {
        ItemStack stack = container.getSlot(45).getItem();
        if (isInvalid(stack)) return 1.0;
        String lore = String.join("\n", ItemUtil.getItemLore(stack).stream().map(Component::getString).toList());
        String res = RegexUtil.regexGetPart("Total Multiplier: ([\\d,.]+)x", lore, 1);
        return res != null ? Double.parseDouble(res.replace(",", "")) : 1.0;
    }

    private long parseCost(String lore) {
        if (!lore.contains("Cost")) return -1;
        String[] split = lore.split("Cost");
        String res = RegexUtil.regexGetPart("([\\d,]+) Chocolate", split[1], 1);
        return res != null ? Long.parseLong(res.replace(",", "")) : -1;
    }

    private double calculateDelta(int slot, ItemStack stack, double baseProd, double multiplier) {
        if (slot >= 28 && slot <= 34) return (slot - 27) * multiplier;
        if (stack.is(Items.CLOCK)) return baseProd * 0.1;
        if (slot == 42) return baseProd * 0.01;
        return 0;
    }
}