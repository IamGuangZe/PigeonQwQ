package owo.pigeon.modules.impl.Skyblock.Misc;

import net.engio.mbassy.listener.Handler;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import owo.pigeon.event.events.PacketEvent;
import owo.pigeon.event.events.WorldChangeEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.modules.impl.Player.AutoFish;
import owo.pigeon.modules.impl.Skyblock.Event.AutoBouncingBall;
import owo.pigeon.modules.impl.Skyblock.Slayer.VampireSlayer;
import owo.pigeon.settings.EnableSetting;
import owo.pigeon.settings.IntSetting;
import owo.pigeon.settings.StringSetting;
import owo.pigeon.utils.Chat.ChatUtil;
import owo.pigeon.utils.Hypixel.SkyblockUtil;
import owo.pigeon.utils.KeybindUtil;
import owo.pigeon.utils.ModuleUtil;

import java.util.List;
import java.util.Set;

import static owo.pigeon.Pigeon.mc;

public class FailSafe extends Module {
    public FailSafe() {
        super("FailSafe", Category.SKYBLOCK);
    }

    public EnableSetting soundAlert = setting("sound-alert", true, v -> true);
    public IntSetting soundVolume = setting("sound-volume", 100, 0, 100, v -> soundAlert.getValue());
    public StringSetting soundId = setting("sound-id", "block.note_block.pling", v -> soundAlert.getValue());
    public EnableSetting disableModules = setting("disable-modules", true, v -> true);
    public EnableSetting teleportDetection = setting("teleport-detection", true, v -> true);
    public EnableSetting onlyWhenStop = setting("only-when-stop",true,v-> teleportDetection.getValue());
    public EnableSetting onlyOnGround = setting("only-on-ground",true,v->teleportDetection.getValue());
    public EnableSetting slotChangeDetection = setting("slot-change-detection", true, v -> true);
    public EnableSetting worldChangeDetection = setting("world-change-detection", true, v -> true);

    private final List<Class<? extends Module>> protectedModules = List.of(
            AutoFish.class, AutoBouncingBall.class, VampireSlayer.class
    );

    @Override
    public void onEnable() {
        ChatUtil.sendMessage(this.name,"This module is still under development and may trigger false positives!");
    }

    @Handler
    public void onReceivePacketPre(PacketEvent.ReceivePacketEvent.Pre event) {
        if (teleportDetection.getValue() && event.getPacket() instanceof PlayerPositionLookS2CPacket packet) {
            // ChatUtil.sendDebugMessage(this.name, packet.toString());

            if (onlyWhenStop.getValue() && isPlayerMoving()) return;
            if (onlyOnGround.getValue() && !mc.player.isOnGround()) return;

            Set<String> abilities = SkyblockUtil.getItemAbilityNames(mc.player.getInventory().getSelectedStack());
            if (abilities.contains("Instant Transmission") ||
                    abilities.contains("Wither Impact") ||
                    abilities.contains("Shadow Warp")
            ) {
                return;
            }


            ChatUtil.sendMessage(this.name, "Teleportation detected!");
            handleFailSafe();
        }

        if (slotChangeDetection.getValue() && event.getPacket() instanceof UpdateSelectedSlotS2CPacket) {
            ChatUtil.sendMessage(this.name, "Selected slot has been modified!");
            handleFailSafe();
        }
    }

    @Handler
    public void onWorldChange(WorldChangeEvent event) {
        if (worldChangeDetection.getValue()) {
            ChatUtil.sendMessage(this.name, "World change detected!");
            handleFailSafe();
        }
    }

    private void handleFailSafe() {
        mc.execute(() -> {
            if (soundAlert.getValue()) {
                String inputId = soundId.getValue().trim().isEmpty() ? "block.note_block.pling" : soundId.getValue();
                Identifier id = Identifier.of(inputId);

                SoundEvent sound = Registries.SOUND_EVENT.get(id);

                if (sound == null) {
                    sound = Registries.SOUND_EVENT.get(Identifier.of("block.note_block.pling"));
                    ChatUtil.sendDebugMessage(this.name, "Invalid Sound ID! Falling back to: block.note_block.pling");
                }

                float volume = soundVolume.getValue() / 100f;
                mc.world.playSound(
                        mc.player,
                        mc.player.getX(), mc.player.getY(), mc.player.getZ(),
                        sound,
                        SoundCategory.VOICE,
                        volume,
                        1.0f,
                        mc.world.random.nextLong()
                );

            }

            if (disableModules.getValue()) {
                protectedModules.forEach(ModuleUtil::disableModule);
            }
        });
    }

    private boolean isPlayerMoving() {
        return KeybindUtil.isPressed(mc.options.forwardKey) ||
                KeybindUtil.isPressed(mc.options.backKey) ||
                KeybindUtil.isPressed(mc.options.leftKey) ||
                KeybindUtil.isPressed(mc.options.rightKey) ||
                KeybindUtil.isPressed(mc.options.jumpKey) ||
                KeybindUtil.isPressed(mc.options.sneakKey);
    }
}
