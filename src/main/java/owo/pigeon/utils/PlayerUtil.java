package owo.pigeon.utils;

import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.GameMode;
import owo.pigeon.mixin.accessors.IAccessorMinecraftClient;
import owo.pigeon.utils.Chat.ChatUtil;

import static owo.pigeon.Pigeonqwq.mc;

public class PlayerUtil {
    public enum LeftClickMode {
        MOUSE, DOATTACK
    }

    public enum RightClickMode {
        MOUSE, DOITEMUSE, INTERACTITEM
    }

    public static void LeftClick(LeftClickMode mode) {
        ChatUtil.sendDebugMessage("PlayerUtil","LeftClick, mode: " + mode.name());
        switch (mode) {
            case MOUSE -> KeybindUtil.onPressed(mc.options.attackKey);
            case DOATTACK -> ((IAccessorMinecraftClient) mc).invokeDoAttack();
        }
    }

    public static void RightClick(RightClickMode mode) {
        ChatUtil.sendDebugMessage("PlayerUtil","RightClick, mode: " + mode.name());
        switch (mode) {
            case MOUSE -> KeybindUtil.onPressed(mc.options.useKey);
            case DOITEMUSE -> ((IAccessorMinecraftClient) mc).invokeDoItemUse();
            case INTERACTITEM -> mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        }
    }

    public static boolean isBreakingBlock() {
        return mc.player.handSwinging &&
                mc.crosshairTarget != null && mc.crosshairTarget.getType() == HitResult.Type.BLOCK &&
                mc.interactionManager.getCurrentGameMode() != GameMode.ADVENTURE &&
                KeybindUtil.isPressed(mc.options.attackKey);
    }
}
