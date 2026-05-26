package owo.pigeon.mixin.mixins;

import net.minecraft.component.ComponentsAccess;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.item.Item;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import owo.pigeon.modules.impl.skyblock.misc.ArmorColor;
import owo.pigeon.utils.ModuleUtil;

import java.util.Locale;
import java.util.function.Consumer;

@Mixin(DyedColorComponent.class)
public class MixinDyedColorComponent {

    @Inject(method = "appendTooltip", at = @At("HEAD"), cancellable = true)
    private void onAppendTooltip(Item.TooltipContext context, Consumer<Text> textConsumer, TooltipType tooltipType, ComponentsAccess componentsAccess, CallbackInfo ci) {
        if (tooltipType.isAdvanced()) return;
        if (!ModuleUtil.isEnable(ArmorColor.class)) return;

        int rgb = ((DyedColorComponent) (Object) this).rgb();
        textConsumer.accept(
                Text.translatable("item.color", String.format(Locale.ROOT, "#%06X", rgb))
                        .formatted(Formatting.GRAY)
        );
        ci.cancel();
    }
}
