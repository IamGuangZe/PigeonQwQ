package owo.pigeon.modules.impl.Hypixel;

import net.engio.mbassy.listener.Handler;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import owo.pigeon.event.events.MessageEvent;
import owo.pigeon.event.events.RenderEvent;
import owo.pigeon.event.events.WorldChangeEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.ModeSetting;
import owo.pigeon.utils.Chat.ChatUtil;
import owo.pigeon.utils.ColorUtil;
import owo.pigeon.utils.Player.PlayerUtil;
import owo.pigeon.utils.RegexUtil;

import java.util.HashSet;
import java.util.Set;

import static owo.pigeon.Pigeon.mc;

public class PartyDetector extends Module {
    public PartyDetector() {
        super("PartyDetector", Category.HYPIXEL);
    }

    public enum DetectionMode {
        CHAT, ENTITYLIST;
    }

    public ModeSetting<DetectionMode> detectionMode = setting("detection-mode", DetectionMode.CHAT, v -> true);

    private final Set<String> previousPlayers = new HashSet<>();
    private final Set<String> newPlayersOnTick = new HashSet<>();

    @Override
    public void onEnable() {
        previousPlayers.clear();
        newPlayersOnTick.clear();
    }

    @Handler
    public void onRender3D(RenderEvent.Render3DEvent event) {
        if (detectionMode.getValue() == DetectionMode.CHAT) {
            if (newPlayersOnTick.size() >= 2) {
                ChatUtil.sendMessage(this.name, "Suspected Party Join (" + newPlayersOnTick.size() + "): " + String.join(", ", newPlayersOnTick));
            }

            newPlayersOnTick.clear();
        }

        if (detectionMode.getValue() == DetectionMode.ENTITYLIST) {
            // if (WorldUtil.nullCheck()) return;

            Set<String> currentPlayers = new HashSet<>();

            for (AbstractClientPlayerEntity player : mc.world.getPlayers()) {
                if (PlayerUtil.hasUUID(player))
                    currentPlayers.add(player.getName().getString());
            }

            if (previousPlayers.isEmpty()) {
                previousPlayers.addAll(currentPlayers);
                return;
            }

            Set<String> newPlayers = new HashSet<>(currentPlayers);
            newPlayers.removeAll(previousPlayers);

            if (newPlayers.size() >= 2) {
                ChatUtil.sendMessage(this.name, "Suspected Party Join (" + newPlayers.size() + "): " + String.join(", ", newPlayers));
            }

            previousPlayers.clear();
            previousPlayers.addAll(currentPlayers);
        }
    }

    @Handler
    public void onReceiveMessage(MessageEvent.ReceiveMessageEvent event) {
        if (!event.isOverlay() && detectionMode.getValue() == DetectionMode.CHAT) {
            String message = ColorUtil.removeColor(event.getMessage().getString());

            String playerName = RegexUtil.regexGetPart("(.*) has joined \\(.*\\)!", message, 1);
            if (playerName != null) {
                newPlayersOnTick.add(playerName);
            }
        }
    }

    @Handler
    public void onWorldChange(WorldChangeEvent event) {
        previousPlayers.clear();
        newPlayersOnTick.clear();
    }
}
