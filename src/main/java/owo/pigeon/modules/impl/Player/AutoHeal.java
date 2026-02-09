package owo.pigeon.modules.impl.Player;

import net.engio.mbassy.listener.Handler;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.GameMode;
import owo.pigeon.event.events.TickEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.EnableSetting;
import owo.pigeon.settings.FloatSetting;
import owo.pigeon.settings.IntSetting;
import owo.pigeon.settings.ModeSetting;
import owo.pigeon.utils.ItemUtil;
import owo.pigeon.utils.Player.PlayerUtil;
import owo.pigeon.utils.WorldUtil;

import static owo.pigeon.Pigeon.mc;

public class AutoHeal extends Module {
    public AutoHeal() {
        super("AutoHeal", Category.PLAYER);
    }

    public enum SwitchMode {
        NORMAL, INSTANT
    }

    public FloatSetting health = setting("health", 10F, 1F, 20F, v -> true);
    public ModeSetting<PlayerUtil.RightClickMode> clickMode = setting("click-mode", PlayerUtil.RightClickMode.MOUSE, v -> true);
    public ModeSetting<SwitchMode> switchMode = setting("switch-mode", SwitchMode.NORMAL, v -> true);
    public IntSetting switchTick = setting("switch-tick", 2, 1, 20, "tick", v -> switchMode.getValue() == SwitchMode.NORMAL);
    public IntSetting checkDelay = setting("check-delay", 10, 1, 60, "tick", v -> true);
    public EnableSetting absorptionCheck = setting("absorption-check", true, v -> true);
    public EnableSetting regenerationCheck = setting("regeneration-check", true, v -> true);
    public EnableSetting goldenHead = setting("golden-head", true, v -> true);
    public EnableSetting ragePotato = setting("rage-potato", true, v -> true);
    public EnableSetting firstAidEgg = setting("first-aid-egg", true, v -> true);
    public EnableSetting fracturedSoul = setting("fractured-soul", true, v -> true);

    private int s_tick;     // switch-tick
    private int d_tick;
    private int rawSlot = -1;

    @Handler
    public void onTickPost(TickEvent.ClientTickEvent.Post event) {
        if (WorldUtil.nullCheck()) return;

        int healingItemSlot = -1;
        if (goldenHead.getValue()) {
            int slot = ItemUtil.getSlotFromItemName("Golden Head");
            if (slot != -1) healingItemSlot = slot;
        }
        if (ragePotato.getValue()) {
            int slot = ItemUtil.getSlotFromItemName("Rage Potato");
            if (slot != -1) healingItemSlot = slot;
        }
        if (firstAidEgg.getValue()) {
            int slot = ItemUtil.getSlotFromItemName("First-Aid Egg");
            if (slot != -1) {
                ItemStack itemStack = ItemUtil.getItemStackfromSlot(slot);
                if (itemStack.getItem() == Items.MOOSHROOM_SPAWN_EGG) healingItemSlot = slot;
            }
        }
        if (fracturedSoul.getValue()) {
            int slot = ItemUtil.getSlotFromItemName("Fractured Soul");
            if (slot != -1) healingItemSlot = slot;
        }

        if (s_tick <= 0) {
            if (rawSlot != -1) {
                PlayerUtil.switchItemSlot(rawSlot);
                rawSlot = -1;
            }
        } else {
            s_tick--;
        }

        if (d_tick <= 0) {
            if (mc.player.getHealth() < health.getValue() &&
                    mc.interactionManager.getCurrentGameMode() != GameMode.CREATIVE &&
                    !hasAbsorption() &&
                    !hasRegen() &&
                    healingItemSlot != -1
            ) {
                switch (switchMode.getValue()) {
                    case NORMAL -> {
                        s_tick = switchTick.getValue();
                        rawSlot = mc.player.getInventory().getSelectedSlot();
                        PlayerUtil.switchUseItem(healingItemSlot, clickMode.getValue());
                    }

                    case INSTANT -> {
                        PlayerUtil.InstantUseItem(healingItemSlot, clickMode.getValue());
                    }
                }

                d_tick = checkDelay.getValue();
            }
        } else {
            d_tick--;
        }
    }

    private boolean hasAbsorption() {
        return absorptionCheck.getValue() && mc.player.getAbsorptionAmount() > 0;
    }

    private boolean hasRegen() {
        return regenerationCheck.getValue() && mc.player.hasStatusEffect(StatusEffects.REGENERATION);
    }
}
