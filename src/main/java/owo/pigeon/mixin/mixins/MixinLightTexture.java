package owo.pigeon.mixin.mixins;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import net.minecraft.client.renderer.Lightmap;
import net.minecraft.client.renderer.state.LightmapRenderState;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import owo.pigeon.modules.impl.render.FullBright;
import owo.pigeon.utils.ModuleUtil;

@Mixin(Lightmap.class)
public class MixinLightTexture {
    @Shadow
    @Final
    private GpuTexture texture;

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRender(LightmapRenderState renderState, CallbackInfo ci) {
        if (ModuleUtil.isEnable(FullBright.class) && ModuleUtil.getModule(FullBright.class).mode.getValue() == FullBright.Mode.LIGHTMAP) {
            RenderSystem.getDevice().createCommandEncoder().clearColorTexture(texture, new Vector4f(1.0f, 1.0f, 1.0f, 1.0f));
            ci.cancel();
        }
    }
}
