package owo.pigeon.mixin.mixins;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.TooltipDisplay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import owo.pigeon.modules.impl.skyblock.misc.ArmorColor;
import owo.pigeon.utils.ModuleUtil;

@Mixin(TooltipDisplay.class)
public class MixinTooltipDisplay {

    @Inject(method = "shows", at = @At("RETURN"), cancellable = true)
    private void onShows(DataComponentType<?> type, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) return;
        if (ModuleUtil.isEnable(ArmorColor.class) && type == DataComponents.DYED_COLOR) {
            cir.setReturnValue(true);
        }
    }
}
