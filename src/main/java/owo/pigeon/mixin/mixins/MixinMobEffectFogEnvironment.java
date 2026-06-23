package owo.pigeon.mixin.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.renderer.fog.environment.MobEffectFogEnvironment;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import owo.pigeon.modules.impl.render.ModifyCamera;
import owo.pigeon.utils.ModuleUtil;

@Mixin(MobEffectFogEnvironment.class)
public abstract class MixinMobEffectFogEnvironment {
    @Shadow
    public abstract Holder<MobEffect> getMobEffect();

    @ModifyReturnValue(method = "isApplicable", at = @At("RETURN"))
    private boolean onShouldApply(boolean original) {
        if (getMobEffect() == MobEffects.BLINDNESS)
            return original && !(ModuleUtil.isEnable(ModifyCamera.class) && ModuleUtil.getModule(ModifyCamera.class).noBlindness.getValue());
        if (getMobEffect() == MobEffects.DARKNESS)
            return original && !(ModuleUtil.isEnable(ModifyCamera.class) && ModuleUtil.getModule(ModifyCamera.class).noDarkness.getValue());
        return original;
    }
}
