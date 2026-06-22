package owo.pigeon.mixin.skyblocker;

import de.hysky.skyblocker.skyblock.garden.FarmingHudWidget;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.Element;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.PlainTextElement;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
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

        List<Text> lines = ((IAccessorPlainTextElement) lastElement).pigeon$getLines();
        if (lines.isEmpty()) return;

        Text firstLine = lines.getFirst();
        if (firstLine instanceof MutableText mutableText) {
            lines.set(0, mutableText.append(Text.literal(" (From PigeonQwQ)").formatted(Formatting.GRAY)));

            TextRenderer renderer = MinecraftClient.getInstance().textRenderer;
            int maxWidth = lines.stream().mapToInt(renderer::getWidth).max().orElse(0);
            ((IAccessorElementWidth) lastElement).pigeon$setWidth(2 + maxWidth);
        }
    }
}
