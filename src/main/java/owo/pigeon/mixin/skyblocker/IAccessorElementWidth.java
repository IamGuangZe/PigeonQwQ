package owo.pigeon.mixin.skyblocker;

import de.hysky.skyblocker.skyblock.tabhud.widget.element.Element;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Element.class)
public interface IAccessorElementWidth {

    @Accessor(value = "width", remap = false)
    void pigeon$setWidth(int width);
}
