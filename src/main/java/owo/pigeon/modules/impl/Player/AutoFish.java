package owo.pigeon.modules.impl.Player;

import net.engio.mbassy.listener.Handler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.FishingRodItem;
import owo.pigeon.event.events.TickEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.EnableSetting;
import owo.pigeon.settings.IntSetting;
import owo.pigeon.settings.ModeSetting;
import owo.pigeon.utils.Player.PlayerUtil;
import owo.pigeon.utils.WorldUtil;

import static owo.pigeon.Pigeon.mc;

public class AutoFish extends Module {
    public AutoFish() {
        super("AutoFish", Category.PLAYER);
    }

    public ModeSetting<PlayerUtil.RightClickMode> castMode = setting("cast-mode", PlayerUtil.RightClickMode.MOUSE, v -> true);
    public EnableSetting slugfishMode = setting("slugfish-mode", false, v -> true);
    public EnableSetting stopInGui = setting("stop-in-gui", true, v -> true);
    public EnableSetting rethrow = setting("rethrow", true, v -> true);
    public IntSetting rethrowDelay = setting("rethrow-delay", 10, 1, 20, "tick", v -> rethrow.getValue());
    public EnableSetting idleTimeoutCheck = setting("idle-timeout-check", true, v -> rethrow.getValue());
    public IntSetting idleTimeout = setting("idle-timeout", 5, 1, 30, "s", v -> rethrow.getValue() && idleTimeoutCheck.getValue());
    public EnableSetting hookTimeoutCheck = setting("hook-timeout-check", true, v -> rethrow.getValue());
    public IntSetting hookTimeout = setting("hook-timeout", 20, 10, 90, "s", v -> rethrow.getValue() && hookTimeoutCheck.getValue());

    private int rethrowTick, idleTick;
    private boolean fishIncoming;
    private boolean wasHoldingRod;

    @Override
    public void onEnable() {
        rethrowTick = rethrowDelay.getMaxValue() + 1;
        idleTick = 0;
        wasHoldingRod = false;
    }

    @Handler
    public void onTickPost(TickEvent.ClientTickEvent.Post event) {
        if (WorldUtil.nullCheck()) return;
        if (stopInGui.getValue() && mc.currentScreen != null) return;

        // fishHookAge
        int fishHookAge;
        if (mc.player.fishHook != null) fishHookAge = mc.player.fishHook.age;
        else fishHookAge = 0;

        // Rethrow
        if (fishHookAge == 0 && rethrowTick < rethrowDelay.getMaxValue() + 1) rethrowTick++;
        if (isHeldRod() && rethrowTick == rethrowDelay.getMaxValue()) PlayerUtil.RightClick(castMode.getValue());

        // Idle Timeout
        if (rethrow.getValue() && idleTimeoutCheck.getValue() && isHeldRod()) {
            // ChatUtil.sendDebugMessage(this.name,"idleTick: " + idleTick);
            if (!wasHoldingRod) {
                idleTick = Integer.MIN_VALUE;
                wasHoldingRod = true;
            }
            if (fishHookAge == 0 && rethrowTick > rethrowDelay.getValue()) {
                idleTick++;
                if (idleTick >= idleTimeout.getValue() * 20) {
                    PlayerUtil.RightClick(castMode.getValue());
                    idleTick = 0;
                }
            } else {
                idleTick = 0;
            }
        } else {
            wasHoldingRod = isHeldRod();
        }

        // Hook Timeout
        if (rethrow.getValue() && hookTimeoutCheck.getValue() && fishHookAge != 0) {
            // ChatUtil.sendDebugMessage(this.name,"fishHookAge: " + fishHookAge);
            if (fishHookAge >= hookTimeout.getValue() * 20 && rethrowTick > rethrowDelay.getValue()) {
                PlayerUtil.RightClick(castMode.getValue());
                fishIncoming = false;
                rethrowTick = 0;
                return;
            }
        }

        // Catch Fish
        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof ArmorStandEntity armorStand) {
                String name = armorStand.getDisplayName().getString();

                if (name.equals("?") || name.matches("\\d\\.\\d")) {
                    fishIncoming = true;
                }

                if (name.equals("!!!") && fishIncoming) {
                    fishIncoming = false;

                    if (!slugfishMode.getValue() || fishHookAge > 22 * 20) {
                        PlayerUtil.RightClick(castMode.getValue());
                        if (rethrow.getValue()) rethrowTick = 0;
                    }
                }
            }
        }

        /*
        if (!isHeldRod()) return;
        if (mc.player.fishHook == null) ChatUtil.sendDebugMessage(this.name, "Player's bobber not found.");
        else if (!fishIncoming) ChatUtil.sendDebugMessage(this.name, "Fish not incoming.");
        else ChatUtil.sendDebugMessage(this.name, "Waiting to catch.");
        */
    }

    private boolean isHeldRod() {
        return mc.player.getMainHandStack().getItem() instanceof FishingRodItem;
    }
}
