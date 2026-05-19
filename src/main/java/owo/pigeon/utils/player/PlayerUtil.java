package owo.pigeon.utils.player;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.world.GameMode;
import owo.pigeon.mixin.accessors.IAccessorMinecraftClient;
import owo.pigeon.utils.KeybindUtil;
import owo.pigeon.utils.chat.ChatUtil;

import static owo.pigeon.Pigeon.mc;

public class PlayerUtil {
    public enum LeftClickMode {
        MOUSE, DOATTACK
    }

    public enum RightClickMode {
        MOUSE, DOITEMUSE, INTERACTITEM
    }

    public static void LeftClick(LeftClickMode mode) {
        ChatUtil.sendDebugMessage("PlayerUtil", "LeftClick, mode: " + mode.name());
        switch (mode) {
            case MOUSE -> KeybindUtil.onPressed(mc.options.attackKey);
            case DOATTACK -> ((IAccessorMinecraftClient) mc).pigeon$invokeDoAttack();
        }
    }

    public static void RightClick(RightClickMode mode) {
        ChatUtil.sendDebugMessage("PlayerUtil", "RightClick, mode: " + mode.name());
        switch (mode) {
            case MOUSE -> KeybindUtil.onPressed(mc.options.useKey);
            case DOITEMUSE -> ((IAccessorMinecraftClient) mc).pigeon$invokeDoItemUse();
            case INTERACTITEM -> mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        }
    }

    public static void switchItemSlot(int index) {
        if (index < 0 || index > 8 || mc.player == null) return;
        mc.player.getInventory().setSelectedSlot(index);
    }

    public static void switchUseItem(int slot, RightClickMode mode) {
        switchItemSlot(slot);
        RightClick(mode);
    }

    public static void InstantUseItem(int slot, RightClickMode mode) {
        InstantUse.instantUseItem(slot, mode);
    }

    public static void clickSlot(int syncId, int slotId, int button, SlotActionType actionType) {
        mc.interactionManager.clickSlot(syncId, slotId, button, actionType, mc.player);
    }

    public static boolean isBreakingBlock() {
        return mc.player.handSwinging &&
                mc.crosshairTarget != null && mc.crosshairTarget.getType() == HitResult.Type.BLOCK &&
                mc.interactionManager.getCurrentGameMode() != GameMode.ADVENTURE &&
                KeybindUtil.isPressed(mc.options.attackKey);
    }

    public static boolean hasUUID(Entity entity) {
        if (entity instanceof PlayerEntity player)
            return player.getUuid().version() == 4;

        return false;
    }


    public static boolean canMove(double x, double z) {
        return canMove(x, z, -1.0);
    }

    public static boolean canMove(double x, double z, double y) {
        Box boundingBox = mc.player.getBoundingBox().offset(x, y, z);
        return mc.world.isSpaceEmpty(mc.player, boundingBox);
    }
}
