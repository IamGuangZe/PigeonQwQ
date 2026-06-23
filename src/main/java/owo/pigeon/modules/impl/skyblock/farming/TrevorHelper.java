package owo.pigeon.modules.impl.skyblock.farming;

import net.engio.mbassy.listener.Handler;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.AABB;
import owo.pigeon.Pigeon;
import owo.pigeon.event.events.MessageEvent;
import owo.pigeon.event.events.RenderEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.ColorSetting;
import owo.pigeon.settings.EnableSetting;
import owo.pigeon.settings.ModeSetting;
import owo.pigeon.utils.ColorUtil;
import owo.pigeon.utils.chat.ChatUtil;
import owo.pigeon.utils.hypixel.skyblock.SkyblockUtil;
import owo.pigeon.utils.render.RenderUtil;

import java.awt.*;

import static owo.pigeon.Pigeon.mc;

public class TrevorHelper extends Module {
    public TrevorHelper() {
        super("TrevorHelper", Category.FARMING);
    }

    public EnableSetting autoWarp = setting("auto-warp", true, v -> true);
    public EnableSetting autoAccept = setting("auto-accept", true, v -> true);
    public EnableSetting huntedAnimalEsp = setting("hunted-animal-esp", true, v -> true);
    public ModeSetting<RenderUtil.ESPMode> espMode = setting("esp-mode", RenderUtil.ESPMode.OUTLINE, v -> huntedAnimalEsp.getValue());
    public EnableSetting tracer = setting("tracer", true, v -> huntedAnimalEsp.getValue());
    public ColorSetting color = setting("color", new Color(0xAA00FF00, true), v -> huntedAnimalEsp.getValue());

    @Handler
    public void onRender3D(RenderEvent.Render3DEvent event) {
        if (!SkyblockUtil.isInIsland(SkyblockUtil.Island.FARMING_ISLAND)) return;

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (!(entity instanceof ArmorStand stand)) continue;

            String name = stand.getName().getString();

            if (!name.contains("Trackable") &&
                    !name.contains("Untrackable") &&
                    !name.contains("Undetected") &&
                    !name.contains("Endangered") &&
                    !name.contains("Elusive")) continue;

            AABB box = stand.getBoundingBox().move(0.0D, -0.75D, 0.0D);

            if (Pigeon.isDebug()) {
                RenderUtil.drawESP(event.getMatrix(), stand, stand.getBoundingBox().inflate(0.2), Color.GREEN, RenderUtil.ESPMode.BOTH, false);
                RenderUtil.drawESP(event.getMatrix(), stand, box.inflate(0.2), Color.BLUE, RenderUtil.ESPMode.BOTH, false);
            }

            Entity closestAnimal = null;
            double closestDistance = Double.MAX_VALUE;

            for (Entity e : mc.level.getEntities(stand, box)) {
                if (!(e instanceof Animal animal)) continue;

                double dist = stand.distanceTo(e);
                if (dist < closestDistance) {
                    closestDistance = dist;
                    closestAnimal = animal;
                }
            }

            if (closestAnimal != null && huntedAnimalEsp.getValue()) {
                RenderUtil.drawESP(event.getMatrix(), closestAnimal, color.getValue(), espMode.getValue(), tracer.getValue());
            }
        }
    }

    @Handler
    public void onChatReceive(MessageEvent.ReceiveMessageEvent event) {
        if (event.isOverlay()) return;
        if (!SkyblockUtil.isInIsland(SkyblockUtil.Island.FARMING_ISLAND)) return;

        Component messageText = event.getMessage();
        String message = ColorUtil.removeColor(messageText.getString());

        if (autoWarp.getValue() && (message.contains("Return to the Trapper soon") || message.contains("The animal saw you and disappeared!"))) {
            mc.execute(() -> mc.player.connection.sendChat("/warp trapper"));
        }

        if (autoAccept.getValue() && message.contains("[YES]")) {
            for (Component sibling : messageText.getSiblings()) {
                if (sibling.getString().contains("[YES]")) {
                    if (sibling.getStyle() != null && sibling.getStyle().getClickEvent() != null) {
                        ClickEvent clickEvent = sibling.getStyle().getClickEvent();

                        if (clickEvent instanceof ClickEvent.RunCommand(String command)) {
                            mc.execute(() -> mc.player.connection.sendChat(command));
                            ChatUtil.sendDebugMessage(this.name, "Send Command : " + command);
                            break;
                        }
                    }
                }
            }
        }
    }
}
