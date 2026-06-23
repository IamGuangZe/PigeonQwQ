package owo.pigeon.mixin.skyblocker;

import de.hysky.skyblocker.skyblock.garden.FarmingHudWidget;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.Element;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.PlainTextElement;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import owo.pigeon.modules.impl.skyblock.farming.RotationLock;
import owo.pigeon.utils.ModuleUtil;

import java.util.ArrayList;
import java.util.List;

@Mixin(FarmingHudWidget.class)
public class MixinFarmingHudWidget {

    @Unique
    private static boolean pigeon$isRotationLockActive() {
        RotationLock rotationLock = ModuleUtil.getModule(RotationLock.class);
        return rotationLock != null && rotationLock.isEnable() && rotationLock.shouldLock();
    }

    @Inject(method = "updateContent", at = @At("RETURN"), remap = false)
    private void pigeon$appendLockSource(CallbackInfo ci) {
        if (!pigeon$isRotationLockActive()) return;

        ArrayList<Element> elements = ((IAccessorElementBasedWidget) this).pigeon$getElements();
        if (elements.isEmpty()) return;

        Object lastElement = elements.get(elements.size() - 1);
        if (!(lastElement instanceof PlainTextElement)) return;

        List<Component> lines = ((IAccessorPlainTextElement) lastElement).pigeon$getLines();
        if (lines.isEmpty()) return;

        Component firstLine = lines.getFirst();
        if (firstLine instanceof MutableComponent mutableText) {
            lines.set(0, mutableText.append(Component.literal(" (From PigeonQwQ)").withStyle(ChatFormatting.GRAY)));

            Font renderer = Minecraft.getInstance().font;
            int maxWidth = lines.stream().mapToInt(renderer::width).max().orElse(0);
            ((IAccessorElementWidth) lastElement).pigeon$setWidth(2 + maxWidth);
        }
    }
}
