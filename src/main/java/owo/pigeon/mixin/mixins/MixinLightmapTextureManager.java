package owo.pigeon.mixin.mixins;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import owo.pigeon.modules.impl.Render.FullBright;
import owo.pigeon.utils.ModuleUtil;

@Mixin(LightmapTextureManager.class)
public class MixinLightmapTextureManager {
    @Shadow
    @Final
    private GpuTexture glTexture;

    @Inject(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;push(Ljava/lang/String;)V", shift = At.Shift.AFTER), cancellable = true)
    private void onUpdate(float tickProgress, CallbackInfo ci, @Local Profiler profiler) {
        if (ModuleUtil.isEnable(FullBright.class) && ModuleUtil.getModule(FullBright.class).mode.getValue() == FullBright.Mode.LIGHTMAP) {
            RenderSystem.getDevice().createCommandEncoder().clearColorTexture(glTexture, ColorHelper.getArgb(255, 255, 255, 255));
            profiler.pop();
            ci.cancel();
        }
    }
}
