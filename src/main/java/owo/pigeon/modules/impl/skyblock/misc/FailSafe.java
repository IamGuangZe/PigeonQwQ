package owo.pigeon.modules.impl.skyblock.misc;

import net.engio.mbassy.listener.Handler;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetHeldSlotPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import owo.pigeon.event.events.PacketEvent;
import owo.pigeon.event.events.WorldChangeEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.modules.impl.player.AutoFish;
import owo.pigeon.modules.impl.skyblock.event.AutoBouncingBall;
import owo.pigeon.modules.impl.skyblock.slayer.VampireSlayer;
import owo.pigeon.settings.EnableSetting;
import owo.pigeon.settings.IntSetting;
import owo.pigeon.settings.StringSetting;
import owo.pigeon.utils.KeybindUtil;
import owo.pigeon.utils.ModuleUtil;
import owo.pigeon.utils.chat.ChatUtil;
import owo.pigeon.utils.hypixel.skyblock.SkyblockUtil;

import java.util.List;
import java.util.Set;

import static owo.pigeon.Pigeon.mc;

public class FailSafe extends Module {
    public FailSafe() {
        super("FailSafe", Category.MISC);
    }

    public EnableSetting soundAlert = setting("sound-alert", true, v -> true);
    public IntSetting soundVolume = setting("sound-volume", 100, 0, 100, v -> soundAlert.getValue());
    public StringSetting soundId = setting("sound-id", "block.note_block.pling", v -> soundAlert.getValue());
    public EnableSetting disableModules = setting("disable-modules", true, v -> true);
    public EnableSetting teleportDetection = setting("teleport-detection", true, v -> true);
    public EnableSetting onlyWhenStop = setting("only-when-stop", true, v -> teleportDetection.getValue());
    public EnableSetting onlyOnGround = setting("only-on-ground", true, v -> teleportDetection.getValue());
    public EnableSetting slotChangeDetection = setting("slot-change-detection", true, v -> true);
    public EnableSetting worldChangeDetection = setting("world-change-detection", true, v -> true);

    private final List<Class<? extends Module>> protectedModules = List.of(
            AutoFish.class, AutoBouncingBall.class, VampireSlayer.class
    );

    @Override
    public void onEnable() {
        ChatUtil.sendMessage(this.name, "This module is still under development and may trigger false positives!");
    }

    @Handler
    public void onReceivePacketPre(PacketEvent.ReceivePacketEvent.Pre event) {
        if (teleportDetection.getValue() && event.getPacket() instanceof ClientboundPlayerPositionPacket packet) {
            // ChatUtil.sendDebugMessage(this.name, packet.toString());

            if (onlyWhenStop.getValue() && isPlayerMoving()) return;
            if (onlyOnGround.getValue() && !mc.player.onGround()) return;

            Set<String> abilities = SkyblockUtil.getItemAbilityNames(mc.player.getMainHandItem());
            if (abilities.contains("Instant Transmission") ||
                    abilities.contains("Wither Impact") ||
                    abilities.contains("Shadow Warp")
            ) {
                return;
            }


            ChatUtil.sendMessage(this.name, "Teleportation detected!");
            handleFailSafe();
        }

        if (slotChangeDetection.getValue() && event.getPacket() instanceof ClientboundSetHeldSlotPacket) {
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
                Identifier id = Identifier.parse(inputId);

                SoundEvent sound = BuiltInRegistries.SOUND_EVENT.getValue(id);

                if (sound == null) {
                    sound = BuiltInRegistries.SOUND_EVENT.getValue(Identifier.parse("block.note_block.pling"));
                    ChatUtil.sendDebugMessage(this.name, "Invalid Sound ID! Falling back to: block.note_block.pling");
                }

                float volume = soundVolume.getValue() / 100f;
                mc.level.playSeededSound(
                        mc.player,
                        mc.player.getX(), mc.player.getY(), mc.player.getZ(),
                        sound,
                        SoundSource.VOICE,
                        volume,
                        1.0f,
                        mc.level.random.nextLong()
                );

            }

            if (disableModules.getValue()) {
                protectedModules.forEach(ModuleUtil::disableModule);
            }
        });
    }

    private boolean isPlayerMoving() {
        return KeybindUtil.isPressed(mc.options.keyUp) ||
                KeybindUtil.isPressed(mc.options.keyDown) ||
                KeybindUtil.isPressed(mc.options.keyLeft) ||
                KeybindUtil.isPressed(mc.options.keyRight) ||
                KeybindUtil.isPressed(mc.options.keyJump) ||
                KeybindUtil.isPressed(mc.options.keyShift);
    }
}
