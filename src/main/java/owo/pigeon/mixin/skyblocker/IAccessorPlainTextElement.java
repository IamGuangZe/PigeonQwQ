package owo.pigeon.mixin.skyblocker;

import de.hysky.skyblocker.skyblock.tabhud.widget.element.PlainTextElement;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(PlainTextElement.class)
public interface IAccessorPlainTextElement {

    @Accessor(value = "lines", remap = false)
    List<Component> pigeon$getLines();
}
