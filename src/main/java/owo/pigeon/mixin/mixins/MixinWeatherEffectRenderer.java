package owo.pigeon.mixin.mixins;

import net.minecraft.client.renderer.WeatherEffectRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import owo.pigeon.modules.impl.world.Environment;
import owo.pigeon.utils.ModuleUtil;

@Mixin(WeatherEffectRenderer.class)
public class MixinWeatherEffectRenderer {
    @Redirect(method = {"extractRenderState", "tickRainParticles"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getRainLevel(F)F"))
    private float redirectRainLevel(Level instance, float tickProgress) {
        if (ModuleUtil.isEnable(Environment.class) && ModuleUtil.getModule(Environment.class).shouldModifyWeather()) {
            return ModuleUtil.getModule(Environment.class).isRaining() ? 1.0f : 0.0f;
        }
        return instance.getRainLevel(tickProgress);
    }

    @Redirect(method = "getPrecipitationAt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/biome/Biome;getPrecipitationAt(Lnet/minecraft/core/BlockPos;I)Lnet/minecraft/world/level/biome/Biome$Precipitation;"))
    private Biome.Precipitation redirectPrecipitation(Biome instance, BlockPos pos, int seaLevel) {
        if (ModuleUtil.isEnable(Environment.class) && ModuleUtil.getModule(Environment.class).forceSnow.getValue()) {
            return Biome.Precipitation.SNOW;
        }
        return instance.getPrecipitationAt(pos, seaLevel);
    }
}
