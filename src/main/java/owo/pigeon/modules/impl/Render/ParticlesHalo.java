package owo.pigeon.modules.impl.Render;

import net.engio.mbassy.listener.Handler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import owo.pigeon.event.events.TickEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.EnableSetting;
import owo.pigeon.settings.FloatSetting;
import owo.pigeon.settings.IntSetting;
import owo.pigeon.settings.StringSetting;
import owo.pigeon.utils.Player.PlayerUtil;
import owo.pigeon.utils.WorldUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static owo.pigeon.Pigeon.mc;

public class ParticlesHalo extends Module {
    public ParticlesHalo() {
        super("ParticlesHalo", Category.RENDER);
    }

    public StringSetting particleId = setting("particle-id", "cherry_leaves", v -> true);
    public EnableSetting allPlayer = setting("all-player", false, v -> true);
    public EnableSetting onlyWhenStopped = setting("only-when-stopped", false, v -> true);
    public FloatSetting radius = setting("radius", 0.5f, 0.1f, 5.0f, "block", v -> true);
    public FloatSetting yOffset = setting("y-offset", 2.0f, 0.1f, 5.0f, v -> true);
    public FloatSetting rotationSpeed = setting("rotation-speed", 1.0f, 0.0f, 2.0f, v -> true);
    public IntSetting particleCount = setting("particle-count", 3, 1, 50, v -> true);
    public IntSetting updateDelay = setting("update-delay", 3, 1, 20, "tick", v -> true);

    private final Map<UUID, Float> rotationMap = new HashMap<>();
    private int tickCounter = 0;

    @Override
    public void onEnable() {
        rotationMap.clear();
        tickCounter = 0;
    }

    @Handler
    public void onTick(TickEvent.ClientTickEvent.Pre event) {
        if (WorldUtil.nullCheck()) return;
        if (mc.isPaused()) return;

        tickCounter++;
        if (tickCounter < updateDelay.getValue()) return;
        tickCounter = 0;

        // 根据设置选择渲染目标
        if (allPlayer.getValue()) {
            mc.world.getPlayers()
                    .stream().filter(PlayerUtil::hasUUID)
                    .forEach(this::renderParticles);
        } else {
            renderParticles(mc.player);
        }
    }

    private void renderParticles(PlayerEntity player) {
        if (player == mc.player || allPlayer.getValue()) {
            if (onlyWhenStopped.getValue() && !isPlayerStopped(player)) return;
            UUID uuid = player.getUuid();
            float rotation = rotationMap.getOrDefault(uuid, 0.0f);

            rotation += rotationSpeed.getValue() * 5;
            if (rotation >= 360.0f) {
                rotation -= 360.0f;
            }
            rotationMap.put(uuid, rotation);

            double x = player.getX();
            double y = player.getY() + yOffset.getValue();
            double z = player.getZ();

            double angleStep = 360.0 / particleCount.getValue();

            for (int i = 0; i < particleCount.getValue(); i++) {
                double angle = Math.toRadians(rotation + i * angleStep);
                double particleX = x + Math.cos(angle) * radius.getValue();
                double particleZ = z + Math.sin(angle) * radius.getValue();

                addParticle(particleId.getValue(), particleX, y, particleZ);
            }
        }
    }

    private void addParticle(String particleId, double x, double y, double z) {
        try {
            Identifier id = Identifier.tryParse(particleId);
            ParticleType<?> type = Registries.PARTICLE_TYPE.get(id);

            if (type != null) {
                ParticleEffect effect = (ParticleEffect) type;
                mc.particleManager.addParticle(effect, x, y, z, 0, 0, 0);
            }
        } catch (Exception ignored) {
        }
    }

    private boolean isPlayerStopped(PlayerEntity player) {
        if (player == null) return false;
        Vec3d velocity = player.getVelocity();
        // ChatUtil.sendDebugMessage(this.name, "Player ID: " + player.getUuid() + ", Velocity: x=" + velocity.x + ", y=" + velocity.y + ", z=" + velocity.z);
        return velocity.x == 0 && velocity.z == 0;
    }
}
