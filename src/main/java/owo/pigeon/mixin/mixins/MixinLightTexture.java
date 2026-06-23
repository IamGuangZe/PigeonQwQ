package owo.pigeon.mixin.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.ARGB;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import owo.pigeon.modules.impl.render.FullBright;
import owo.pigeon.utils.ModuleUtil;

@Mixin(LightTexture.class)
public class MixinLightTexture {
    @Shadow
    @Final
    private GpuTexture texture;

    @Inject(method = "updateLightTexture", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;push(Ljava/lang/String;)V", shift = At.Shift.AFTER), cancellable = true)
    private void onUpdate(float tickProgress, CallbackInfo ci, @Local ProfilerFiller profiler) {
        if (ModuleUtil.isEnable(FullBright.class) && ModuleUtil.getModule(FullBright.class).mode.getValue() == FullBright.Mode.LIGHTMAP) {
            RenderSystem.getDevice().createCommandEncoder().clearColorTexture(texture, ARGB.color(255, 255, 255, 255));
            profiler.pop();
            ci.cancel();
        }
    }
}
