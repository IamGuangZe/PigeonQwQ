package owo.pigeon.modules.impl.skyblock.misc;

import net.engio.mbassy.listener.Handler;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.Items;
import owo.pigeon.event.events.ClientTickEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.EnableSetting;
import owo.pigeon.settings.IntSetting;
import owo.pigeon.settings.ModeSetting;
import owo.pigeon.utils.ColorUtil;
import owo.pigeon.utils.ItemUtil;
import owo.pigeon.utils.WorldUtil;
import owo.pigeon.utils.chat.ChatUtil;
import owo.pigeon.utils.player.PlayerUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static owo.pigeon.Pigeon.mc;

public class AutoExperiments extends Module {
    public AutoExperiments() {
        super("AutoExperiments", Category.MISC);
    }

    public enum AutoCloseMode {
        MAX_XP, MAX_CLICK, CUSTOM
    }

    public IntSetting clickDelay = setting("click-delay", 4, 1, 20, "tick", v -> true);
    public EnableSetting autoClaim = setting("auto-claim", true, v -> true);
    public EnableSetting autoClose = setting("auto-close", true, v -> true);
    public ModeSetting<AutoCloseMode> autoCloseMode = setting("auto-close-mode", AutoCloseMode.MAX_XP, v -> autoClose.getValue());
    public IntSetting metaphysicalSerumUsed = setting("metaphysical-serum-used", 0, 0, 3, v -> autoCloseMode.getValue() == AutoCloseMode.MAX_CLICK);
    public IntSetting chronomatronCustomRound = setting("chronomatron-custom-round", 15, 0, 36, v -> autoCloseMode.getValue() == AutoCloseMode.CUSTOM);
    public IntSetting ultrasequencerCustomRound = setting("ultrasequencer-custom-round", 20, 0, 36, v -> autoCloseMode.getValue() == AutoCloseMode.CUSTOM);

    private int clickCooldown, clickIndex;
    private boolean addedThisRound;
    private final List<Integer> chronomatronOrder = new ArrayList<>();
    private final Map<Integer, Integer> ultrasequencerOrder = new HashMap<>();

    @Handler
    public void onTickPost(ClientTickEvent.Post event) {
        if (WorldUtil.nullCheck()) return;
        if (mc.player.containerMenu instanceof ChestMenu containerScreen) {
            String title = mc.gui.screen().getTitle().getString();

            int maxRound = 999;

            if (title.startsWith("Chronomatron (")) {
                ChatUtil.sendDebugMessage(this.name, "Chronomatron");
                if (autoClose.getValue()) {
                    if (autoCloseMode.getValue() == AutoCloseMode.MAX_XP) {
                        maxRound = 15;
                    }

                    if (autoCloseMode.getValue() == AutoCloseMode.MAX_CLICK) {
                        maxRound = 12 - metaphysicalSerumUsed.getValue();
                    }

                    if (autoCloseMode.getValue() == AutoCloseMode.CUSTOM) {
                        maxRound = chronomatronCustomRound.getValue();
                    }
                }


                if (containerScreen.getSlot(49).getItem().is(Items.GLOWSTONE)) {
                    ChatUtil.sendDebugMessage(this.name, "Chronomatron: GLOWSTONE");

                    addedThisRound = false;
                    clickCooldown = clickDelay.getValue();
                    clickIndex = 0;

                    ChatUtil.sendDebugMessage(this.name, "Chronomatron: Order size-" + chronomatronOrder.size());
                    ChatUtil.sendDebugMessage(this.name, "Chronomatron: Max round-" + maxRound);
                    ChatUtil.sendDebugMessage(this.name, "Chronomatron: Should close-" + (chronomatronOrder.size() >= maxRound));

                    if (chronomatronOrder.size() >= maxRound) {
                        // containerScreen.onClosed(mc.player);
                        mc.player.closeContainer();
                        // mc.player.closeScreen();
                        return;
                    }
                }

                if (containerScreen.getSlot(49).getItem().is(Items.CLOCK)) {
                    ChatUtil.sendDebugMessage(this.name, "Chronomatron: CLOCK");

                    if (clickCooldown > 0) clickCooldown--;

                    if (!addedThisRound) {
                        for (int i = 0; i < containerScreen.getContainer().getContainerSize(); i++) {

                            ChatUtil.sendDebugMessage("Chronomatron: Glint-" + containerScreen.getSlot(i).getItem().hasFoil());
                            ChatUtil.sendDebugMessage("Chronomatron: Enchantments-" + containerScreen.getSlot(i).getItem().isEnchanted());

                            if (containerScreen.getSlot(i).getItem().hasFoil()) {
                                ChatUtil.sendDebugMessage(this.name, "Chronomatron: add " + i);
                                chronomatronOrder.add(i);
                                addedThisRound = true;
                                break;
                            }
                        }
                    }

                    if (clickCooldown == 0 && clickIndex < chronomatronOrder.size()) {
                        PlayerUtil.clickSlot(
                                containerScreen.containerId,
                                chronomatronOrder.get(clickIndex),
                                0,
                                ContainerInput.PICKUP
                        );
                        clickCooldown = clickDelay.getValue();
                        clickIndex++;
                    }
                }
            }

            if (title.startsWith("Ultrasequencer (")) {
                ChatUtil.sendDebugMessage(this.name, "Ultrasequencer");
                if (autoClose.getValue()) {
                    if (autoCloseMode.getValue() == AutoCloseMode.MAX_XP) {
                        maxRound = 20;
                    }

                    if (autoCloseMode.getValue() == AutoCloseMode.MAX_CLICK) {
                        maxRound = 9 - metaphysicalSerumUsed.getValue();
                    }

                    if (autoCloseMode.getValue() == AutoCloseMode.CUSTOM) {
                        maxRound = ultrasequencerCustomRound.getValue();
                    }
                }

                if (containerScreen.getSlot(49).getItem().is(Items.GLOWSTONE)) {
                    ChatUtil.sendDebugMessage(this.name, "Ultrasequencer: GLOWSTONE");

                    clickCooldown = clickDelay.getValue();
                    clickIndex = 0;

                    ChatUtil.sendDebugMessage(this.name, "Ultrasequencer: Order size-" + ultrasequencerOrder.size());
                    ChatUtil.sendDebugMessage(this.name, "Ultrasequencer: Max round-" + maxRound);
                    ChatUtil.sendDebugMessage(this.name, "Ultrasequencer: Should close-" + (ultrasequencerOrder.size() > maxRound));

                    if (ultrasequencerOrder.size() > maxRound) {
                        mc.player.closeContainer();
                        return;
                    }

                    if (!addedThisRound) {
                        ultrasequencerOrder.clear();

                        for (int i = 0; i < containerScreen.getContainer().getContainerSize(); i++) {
                            String itemCustomName = ColorUtil.removeColor(containerScreen.getSlot(i).getItem().getCustomName().getString());

                            ChatUtil.sendDebugMessage(this.name, "Ultrasequencer: Item custom name-" + itemCustomName);

                            if (itemCustomName.matches("\\d{1,2}")) {
                                ultrasequencerOrder.put(Integer.parseInt(itemCustomName) - 1, i);
                                ChatUtil.sendDebugMessage(this.name, "Ultrasequencer: add " + itemCustomName + " -> " + i);
                            }
                        }

                        addedThisRound = true;
                    }
                }

                if (containerScreen.getSlot(49).getItem().is(Items.CLOCK)) {
                    ChatUtil.sendDebugMessage(this.name, "Ultrasequencer: CLOCK");

                    if (clickCooldown > 0) clickCooldown--;
                    addedThisRound = false;

                    if (clickCooldown == 0 && clickIndex < ultrasequencerOrder.size()) {
                        PlayerUtil.clickSlot(
                                containerScreen.containerId,
                                ultrasequencerOrder.get(clickIndex),
                                0,
                                ContainerInput.PICKUP
                        );
                        clickCooldown = clickDelay.getValue();
                        clickIndex++;
                    }
                }
            }

            if (title.startsWith("Experiment")) {
                addedThisRound = false;
                clickCooldown = clickDelay.getValue();
                clickIndex = 0;
                chronomatronOrder.clear();
                ultrasequencerOrder.clear();

                if (autoClaim.getValue() && title.startsWith("Experiment Over")) {
                    List<Component> lore = ItemUtil.getItemLore(containerScreen.getSlot(11).getItem());
                    List<String> target = List.of("You closed the game!", "Game closed");
                    boolean isMatched = lore.stream()
                            .map(Component::getString)
                            .anyMatch(target::contains);

                    if (isMatched) mc.player.closeContainer();
                }
            }
        }
    }
}
