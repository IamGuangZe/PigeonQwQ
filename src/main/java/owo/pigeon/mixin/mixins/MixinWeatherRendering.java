package owo.pigeon.mixin.mixins;

import net.minecraft.client.render.WeatherRendering;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import owo.pigeon.modules.impl.world.Environment;
import owo.pigeon.utils.ModuleUtil;

@Mixin(WeatherRendering.class)
public class MixinWeatherRendering {
    @Redirect(method = {"buildPrecipitationPieces", "addParticlesAndSound"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;getRainGradient(F)F"))
    private float redirectRainGradient(World instance, float tickProgress) {
        if (ModuleUtil.isEnable(Environment.class) && ModuleUtil.getModule(Environment.class).shouldModifyWeather()) {
            return ModuleUtil.getModule(Environment.class).isRaining() ? 1.0f : 0.0f;
        }
        return instance.getRainGradient(tickProgress);
    }

    @Redirect(method = "getPrecipitationAt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/biome/Biome;getPrecipitation(Lnet/minecraft/util/math/BlockPos;I)Lnet/minecraft/world/biome/Biome$Precipitation;"))
    private Biome.Precipitation redirectPrecipitation(Biome instance, BlockPos pos, int seaLevel) {
        if (ModuleUtil.isEnable(Environment.class) && ModuleUtil.getModule(Environment.class).forceSnow.getValue()) {
            return Biome.Precipitation.SNOW;
        }
        return instance.getPrecipitation(pos,seaLevel);
    }
}
