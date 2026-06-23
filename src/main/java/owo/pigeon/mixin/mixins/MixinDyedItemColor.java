package owo.pigeon.mixin.mixins;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.DyedItemColor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import owo.pigeon.modules.impl.skyblock.misc.ArmorColor;
import owo.pigeon.utils.ModuleUtil;

import java.util.Locale;
import java.util.function.Consumer;

@Mixin(DyedItemColor.class)
public class MixinDyedItemColor {

    @Inject(method = "addToTooltip", at = @At("HEAD"), cancellable = true)
    private void onAppendTooltip(Item.TooltipContext context, Consumer<Component> textConsumer, TooltipFlag tooltipType, DataComponentGetter componentsAccess, CallbackInfo ci) {
        if (tooltipType.isAdvanced()) return;
        if (!ModuleUtil.isEnable(ArmorColor.class)) return;

        int rgb = ((DyedItemColor) (Object) this).rgb();
        textConsumer.accept(
                Component.translatable("item.color", String.format(Locale.ROOT, "#%06X", rgb))
                        .withStyle(ChatFormatting.GRAY)
        );
        ci.cancel();
    }
}
