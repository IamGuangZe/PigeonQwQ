package owo.pigeon.modules.impl.render;

import net.engio.mbassy.listener.Handler;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import owo.pigeon.event.events.ClientTickEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.EnableSetting;
import owo.pigeon.settings.FloatSetting;
import owo.pigeon.settings.IntSetting;
import owo.pigeon.settings.StringSetting;
import owo.pigeon.utils.WorldUtil;
import owo.pigeon.utils.player.PlayerUtil;

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
    public void onTick(ClientTickEvent.Pre event) {
        if (WorldUtil.nullCheck()) return;
        if (mc.isPaused()) return;

        tickCounter++;
        if (tickCounter < updateDelay.getValue()) return;
        tickCounter = 0;

        // 根据设置选择渲染目标
        if (allPlayer.getValue()) {
            mc.level.players()
                    .stream().filter(player -> !PlayerUtil.isBot(player))
                    .forEach(this::renderParticles);
        } else {
            renderParticles(mc.player);
        }
    }

    private void renderParticles(Player player) {
        if (player == mc.player || allPlayer.getValue()) {
            if (onlyWhenStopped.getValue() && !isPlayerStopped(player)) return;
            UUID uuid = player.getUUID();
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
            ParticleType<?> type = BuiltInRegistries.PARTICLE_TYPE.getValue(id);

            if (type != null) {
                ParticleOptions effect = (ParticleOptions) type;
                mc.particleEngine.createParticle(effect, x, y, z, 0, 0, 0);
            }
        } catch (Exception ignored) {
        }
    }

    private boolean isPlayerStopped(Player player) {
        if (player == null) return false;
        Vec3 velocity = player.getDeltaMovement();
        // ChatUtil.sendDebugMessage(this.name, "Player ID: " + player.getUuid() + ", Velocity: x=" + velocity.x + ", y=" + velocity.y + ", z=" + velocity.z);
        return velocity.x == 0 && velocity.z == 0;
    }
}
