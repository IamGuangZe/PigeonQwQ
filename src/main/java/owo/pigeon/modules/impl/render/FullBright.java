package owo.pigeon.modules.impl.render;

import net.engio.mbassy.listener.Handler;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import owo.pigeon.event.events.ClientTickEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.ModeSetting;

import static owo.pigeon.Pigeon.mc;

public class FullBright extends Module {
    public FullBright() {
        super("FullBright", Category.RENDER);
    }

    public enum Mode {
        GAMMA, NIGHT_VISION, LIGHTMAP
    }

    public ModeSetting<Mode> mode = setting("mode", Mode.GAMMA, v -> true);

    private Double rawGamma;
    private Mode lastMode;

    @Handler
    public void onTickPost(ClientTickEvent.Post event) {
        if (lastMode != null && lastMode != mode.getValue()) reset(lastMode);
        lastMode = mode.getValue();

        switch (mode.getValue()) {
            case GAMMA -> {
                if (rawGamma == null) rawGamma = mc.options.gamma().get();
                mc.options.gamma().set(15.0D);
            }

            case NIGHT_VISION -> {
                if (mc.player != null)
                    mc.player.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(MobEffects.NIGHT_VISION.value()), -1, 0, false, false, false));
            }
        }
    }

    @Override
    public void onDisable() {
        reset(mode.getValue());
        lastMode = null;
        rawGamma = null;
    }

    private void reset(Mode mode) {
        switch (mode) {
            case GAMMA -> resetGamma();
            case NIGHT_VISION -> resetNightVision();
        }
    }

    private void resetGamma() {
        if (rawGamma == null) rawGamma = 1.0;
        mc.options.gamma().set(rawGamma);
        rawGamma = null;
    }

    private void resetNightVision() {
        if (mc.player == null) return;
        if (mc.player.hasEffect(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(MobEffects.NIGHT_VISION.value()))) {
            mc.player.removeEffect(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(MobEffects.NIGHT_VISION.value()));
        }
    }
}
