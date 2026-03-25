package owo.pigeon.mixin.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.render.fog.StatusEffectFogModifier;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import owo.pigeon.modules.impl.render.ModifyCamera;
import owo.pigeon.utils.ModuleUtil;

@Mixin(StatusEffectFogModifier.class)
public abstract class MixinStatusEffectFogModifier {
    @Shadow
    public abstract RegistryEntry<StatusEffect> getStatusEffect();

    @ModifyReturnValue(method = "shouldApply", at = @At("RETURN"))
    private boolean onShouldApply(boolean original) {
        if (getStatusEffect() == StatusEffects.BLINDNESS) return original && !(ModuleUtil.isEnable(ModifyCamera.class) && ModuleUtil.getModule(ModifyCamera.class).noBlindness.getValue());
        if (getStatusEffect() == StatusEffects.DARKNESS) return original && !(ModuleUtil.isEnable(ModifyCamera.class) && ModuleUtil.getModule(ModifyCamera.class).noDarkness.getValue());
        return original;
    }
}
