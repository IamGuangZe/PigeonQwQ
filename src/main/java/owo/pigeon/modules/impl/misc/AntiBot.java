package owo.pigeon.modules.impl.misc;

import net.engio.mbassy.listener.Handler;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import owo.pigeon.event.events.ClientTickEvent;
import owo.pigeon.event.events.PacketEvent;
import owo.pigeon.event.events.WorldChangeEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.EnableSetting;
import owo.pigeon.settings.FloatSetting;
import owo.pigeon.settings.IntSetting;
import owo.pigeon.utils.ScoreBoardUtil;
import owo.pigeon.utils.WorldUtil;
import owo.pigeon.utils.player.PlayerUtil;

import java.util.HashSet;
import java.util.Set;

import static owo.pigeon.Pigeon.mc;

public class AntiBot extends Module {
    public AntiBot() {
        super("AntiBot", Category.MISC);
    }

    public EnableSetting literalNpc = setting("literal-npc", true, v -> true);
    public EnableSetting notInTabList = setting("not-in-tab-list", false, v -> true);
    public EnableSetting noGameMode = setting("no-game-mode", true, v -> true);
    public EnableSetting illegalPitch = setting("illegal-pitch", true, v -> true);
    public EnableSetting fakeEntityId = setting("fake-entity-id", true, v -> true);
    public EnableSetting notPremiumUuid = setting("not-premium-uuid", true, v -> true);
    public EnableSetting alwaysInRadius = setting("always-in-radius", false, v -> true);
    public FloatSetting alwaysInRadiusRange = setting("always-in-radius-range", 20f, 5f, 30f, v -> alwaysInRadius.getValue());
    public EnableSetting nameCheck = setting("name-check", true, v -> true);
    public IntSetting nameMinLength = setting("name-min-length", 3, 1, 32, v -> nameCheck.getValue());
    public IntSetting nameMaxLength = setting("name-max-length", 16, 1, 32, v -> nameCheck.getValue());
    public EnableSetting nameValidateChars = setting("name-validate-chars", true, v -> nameCheck.getValue());

    private final Set<Integer> notAlwaysInRadiusSet = new HashSet<>();

    @Override
    public void onEnable() {
        notAlwaysInRadiusSet.clear();
    }

    @Override
    public void onDisable() {
        notAlwaysInRadiusSet.clear();
    }

    @Handler
    public void onTickPost(ClientTickEvent.Post event) {
        if (!alwaysInRadius.getValue() || WorldUtil.nullCheck()) return;

        double rangeSquared = (double) alwaysInRadiusRange.getValue() * alwaysInRadiusRange.getValue();
        for (Entity entity : mc.level.players()) {
            if (entity == mc.player) continue;
            if (mc.player.distanceToSqr(entity) > rangeSquared) {
                notAlwaysInRadiusSet.add(entity.getId());
            }
        }
    }

    @Handler
    public void onWorldChange(WorldChangeEvent event) {
        notAlwaysInRadiusSet.clear();
    }

    @Handler
    public void onPacketReceive(PacketEvent.ReceivePacketEvent.Pre event) {
        if (event.getPacket() instanceof ClientboundRemoveEntitiesPacket packet) {
            for (int entityId : packet.getEntityIds()) {
                notAlwaysInRadiusSet.remove(entityId);
            }
        }
    }

    public boolean isLiteralNpc(Player player) {
        return !PlayerUtil.hasPlayerInfo(player);
    }

    public boolean isNotInTabList(Player player) {
        return !ScoreBoardUtil.isInTabList(player);
    }

    public boolean isNoGameMode(Player player) {
        PlayerInfo info = PlayerUtil.getPlayerInfo(player);
        return info == null || info.getGameMode() == null;
    }

    public boolean isIllegalPitch(Player player) {
        return Math.abs(player.getXRot()) > 90f;
    }

    public boolean isFakeEntityId(Player player) {
        return player.getId() < 0 || player.getId() > 1_000_000_000;
    }

    public boolean isNotPremiumUuid(Player player) {
        return player.getUUID().version() != 4;
    }

    public boolean isNotAlwaysInRadius(Player player) {
        return !notAlwaysInRadiusSet.contains(player.getId());
    }

    public boolean isInvalidName(Player player) {
        String name = player.getScoreboardName();
        if (name.length() < nameMinLength.getValue() || name.length() > nameMaxLength.getValue()) {
            return true;
        }
        return nameValidateChars.getValue() && !PlayerUtil.isValidName(name);
    }

    public boolean isBot(Player player) {
        if (literalNpc.getValue() && isLiteralNpc(player)) return true;
        if (notInTabList.getValue() && isNotInTabList(player)) return true;
        if (noGameMode.getValue() && isNoGameMode(player)) return true;
        if (illegalPitch.getValue() && isIllegalPitch(player)) return true;
        if (fakeEntityId.getValue() && isFakeEntityId(player)) return true;
        if (notPremiumUuid.getValue() && isNotPremiumUuid(player)) return true;
        if (alwaysInRadius.getValue() && isNotAlwaysInRadius(player)) return true;
        if (nameCheck.getValue() && isInvalidName(player)) return true;
        return false;
    }
}
