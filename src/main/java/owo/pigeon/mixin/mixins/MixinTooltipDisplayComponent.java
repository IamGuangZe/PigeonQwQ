package owo.pigeon.mixin.mixins;

import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.TooltipDisplayComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import owo.pigeon.modules.impl.skyblock.misc.ArmorColor;
import owo.pigeon.utils.ModuleUtil;

@Mixin(TooltipDisplayComponent.class)
public class MixinTooltipDisplayComponent {

    @Inject(method = "shouldDisplay", at = @At("RETURN"), cancellable = true)
    private void onShouldDisplay(ComponentType<?> type, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) return;
        if (ModuleUtil.isEnable(ArmorColor.class) && type == DataComponentTypes.DYED_COLOR) {
            cir.setReturnValue(true);
        }
    }
}
