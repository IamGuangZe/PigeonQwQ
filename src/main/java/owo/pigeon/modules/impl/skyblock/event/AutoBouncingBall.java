package owo.pigeon.modules.impl.skyblock.event;

import net.engio.mbassy.listener.Handler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.MathHelper;
import owo.pigeon.event.events.ClientTickEvent;
import owo.pigeon.event.events.MessageEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.EnableSetting;
import owo.pigeon.settings.FloatSetting;
import owo.pigeon.settings.IntSetting;
import owo.pigeon.utils.*;
import owo.pigeon.utils.chat.ChatUtil;
import owo.pigeon.utils.hypixel.skyblock.SkyblockUtil;

import static owo.pigeon.Pigeon.mc;

public class AutoBouncingBall extends Module {
    public AutoBouncingBall() {
        super("AutoBouncingBall", Category.EVENT);
    }

    public IntSetting bounces = setting("bounces", 40, 1, 80, v -> true);
    public FloatSetting precision = setting("precision", 0.2F, 0.0F, 1.0F, v -> true);
    public EnableSetting preciseSearch = setting("preciseSearch", true, v -> true);
    public EnableSetting autoSneak = setting("autoSneak", false, v -> true);
    public FloatSetting sneakRange = setting("sneakRange", 0.3F, 0.0F, 1.0F, v -> true);

    private boolean searching;
    private ArmorStandEntity ballEntity;

    @Handler
    public void onTickPre(ClientTickEvent.Pre event) {
        if (WorldUtil.nullCheck()) return;

        if (searching && ballEntity == null) {
            ballEntity = findBall();
            if (ballEntity != null) {
                searching = false;
                ChatUtil.sendDebugMessage(this.name, "Target ball locked! Distance: " + String.format("%.2f", ballEntity.distanceTo(mc.player)) + " blocks");
            }
        }

        if (ballEntity == null) return;

        KeybindUtil.resetPressed(mc.options.forwardKey);
        KeybindUtil.resetPressed(mc.options.backKey);
        KeybindUtil.resetPressed(mc.options.leftKey);
        KeybindUtil.resetPressed(mc.options.rightKey);
        KeybindUtil.resetPressed(mc.options.sneakKey);

        if (ballEntity.isRemoved()) {
            ballEntity = null;
            return;
        }

        double playerYaw = mc.player.getYaw();
        double playerX = mc.player.getX();
        double playerZ = mc.player.getZ();
        double ballX = ballEntity.getX();
        double ballZ = ballEntity.getZ();

        double deltaX = ballX - playerX;
        double deltaZ = ballZ - playerZ;
        double distanceXZ = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        if (autoSneak.getValue()) {
            if (distanceXZ < sneakRange.getValue()) {
                KeybindUtil.setPressed(mc.options.sneakKey, true);
            } else {
                KeybindUtil.resetPressed(mc.options.sneakKey);
            }
        }

        if (distanceXZ < precision.getValue()) {
            return;
        }

        double targetAngle = Math.toDegrees(Math.atan2(-deltaX, deltaZ));
        double relativeAngle = MathHelper.wrapDegrees(targetAngle - playerYaw);

        KeybindUtil.setPressed(mc.options.forwardKey, relativeAngle > -67.5 && relativeAngle <= 67.5);
        KeybindUtil.setPressed(mc.options.backKey, relativeAngle > 112.5 || relativeAngle <= -112.5);
        KeybindUtil.setPressed(mc.options.leftKey, relativeAngle > -157.5 && relativeAngle <= -22.5);
        KeybindUtil.setPressed(mc.options.rightKey, relativeAngle > 22.5 && relativeAngle <= 157.5);
    }

    @Handler
    public void onReceiveMessage(MessageEvent.ReceiveMessageEvent event) {
        if (event.isOverlay()) {
            String cleanMsg = ColorUtil.removeColor(event.getMessage().getString());

            String bounceCountStr = RegexUtil.regexGetPart("Bounces: (\\d+)", cleanMsg, 1);

            if (bounceCountStr != null) {
                try {
                    int currentBounces = Integer.parseInt(bounceCountStr);
                    ChatUtil.sendDebugMessage(this.name,"current bounces: " + currentBounces);
                    if (currentBounces >= bounces.getValue()) {
                        ballEntity = null;
                        KeybindUtil.resetPressed(mc.options.forwardKey);
                        KeybindUtil.resetPressed(mc.options.backKey);
                        KeybindUtil.resetPressed(mc.options.leftKey);
                        KeybindUtil.resetPressed(mc.options.rightKey);
                        KeybindUtil.resetPressed(mc.options.sneakKey);
                    }
                } catch (NumberFormatException ignored) {

                }
            }
        }

        if (!event.isOverlay()) {
            if (ColorUtil.removeColor(event.getMessage().getString()).startsWith("BOUNCE BONANZA!")) {
                ChatUtil.sendDebugMessage(this.name, "BOUNCE BONANZA!");
                ballEntity = null;
                searching = true;
                // 防止 Texture 第一时间未加载, 放在每 Tick 中寻找
            }
        }
    }

    @Override
    public void onDisable() {
        searching = false;
        KeybindUtil.resetPressed(mc.options.forwardKey);
        KeybindUtil.resetPressed(mc.options.backKey);
        KeybindUtil.resetPressed(mc.options.leftKey);
        KeybindUtil.resetPressed(mc.options.rightKey);
        KeybindUtil.resetPressed(mc.options.sneakKey);
    }

    private ArmorStandEntity findBall() {
        ArmorStandEntity closest = null;
        double closestDistance = Double.MAX_VALUE;

        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof ArmorStandEntity stand) {
                if (stand.distanceTo(mc.player) < 20.0f) {

                    ItemStack helmet = stand.getEquippedStack(EquipmentSlot.HEAD);
                    if (helmet.isEmpty() || !helmet.isOf(Items.PLAYER_HEAD)) continue;

                    String texture = ItemUtil.getSkullTexture(helmet);

                    if (preciseSearch.getValue()) {
                        if (texture == null || texture.isEmpty()) continue;
                        if (!SkyblockUtil.BOUNCY_BEACH_BALL.equals(texture)) continue;
                    }

                    double dist = stand.distanceTo(mc.player);
                    if (dist < closestDistance) {
                        closestDistance = dist;
                        closest = stand;
                    }
                }
            }
        }
        return closest;
    }
}
