package owo.pigeon.utils.player;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import owo.pigeon.mixin.accessors.IAccessorMinecraft;
import owo.pigeon.modules.impl.misc.AntiBot;
import owo.pigeon.utils.KeybindUtil;
import owo.pigeon.utils.ModuleUtil;
import owo.pigeon.utils.chat.ChatUtil;

import static owo.pigeon.Pigeon.mc;

public class PlayerUtil {
    public enum LeftClickMode {
        MOUSE, START_ATTACK
    }

    public enum RightClickMode {
        MOUSE, USE_ITEM, START_USE_ITEM
    }

    public static void leftClick(LeftClickMode mode) {
        ChatUtil.sendDebugMessage("PlayerUtil", "LeftClick, mode: " + mode.name());
        switch (mode) {
            case MOUSE -> KeybindUtil.onPressed(mc.options.keyAttack);
            case START_ATTACK -> ((IAccessorMinecraft) mc).pigeon$invokeStartAttack();
        }
    }

    public static void rightClick(RightClickMode mode) {
        ChatUtil.sendDebugMessage("PlayerUtil", "RightClick, mode: " + mode.name());
        switch (mode) {
            case MOUSE -> KeybindUtil.onPressed(mc.options.keyUse);
            case USE_ITEM -> mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
            case START_USE_ITEM -> ((IAccessorMinecraft) mc).pigeon$invokeStartUseItem();
        }
    }

    public static void switchItemSlot(int index) {
        if (index < 0 || index > 8 || mc.player == null) return;
        mc.player.getInventory().setSelectedSlot(index);
    }

    public static void switchUseItem(int slot, RightClickMode mode) {
        switchItemSlot(slot);
        rightClick(mode);
    }

    public static void InstantUseItem(int slot, RightClickMode mode) {
        InstantUse.instantUseItem(slot, mode);
    }

    public static void clickSlot(int syncId, int slotId, int button, ClickType actionType) {
        mc.gameMode.handleInventoryMouseClick(syncId, slotId, button, actionType, mc.player);
    }

    public static boolean isBreakingBlock() {
        return mc.player.swinging &&
                mc.hitResult != null && mc.hitResult.getType() == HitResult.Type.BLOCK &&
                mc.gameMode.getPlayerMode() != GameType.ADVENTURE &&
                KeybindUtil.isPressed(mc.options.keyAttack);
    }

    public static boolean hasPremiumUuid(Entity entity) {
        if (entity instanceof Player player)
            return player.getUUID().version() == 4;

        return false;
    }

    public static PlayerInfo getPlayerInfo(Player player) {
        ClientPacketListener connection = mc.getConnection();
        if (connection == null) return null;
        return connection.getPlayerInfo(player.getUUID());
    }

    public static boolean hasPlayerInfo(Player player) {
        ClientPacketListener connection = mc.getConnection();
        if (connection == null) return false;
        return connection.getOnlinePlayers().stream()
                .anyMatch(info -> info.getProfile().id().equals(player.getUUID()));
    }

    public static boolean isValidName(String name) {
        for (char c : name.toCharArray()) {
            boolean valid = (c >= '0' && c <= '9') || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
            if (!valid) return false;
        }
        return true;
    }

    public static boolean isBot(Entity entity) {
        if (entity == mc.player || !(entity instanceof Player player)) return false;
        if (mc.getConnection() == null) return false;
        AntiBot antiBot = ModuleUtil.getModule(AntiBot.class);
        if (!antiBot.isEnable()) return false;
        return antiBot.isBot(player);
    }


    public static boolean canMove(double x, double z) {
        return canMove(x, z, -1.0);
    }

    public static boolean canMove(double x, double z, double y) {
        AABB boundingBox = mc.player.getBoundingBox().move(x, y, z);
        return mc.level.noCollision(mc.player, boundingBox);
    }
}
