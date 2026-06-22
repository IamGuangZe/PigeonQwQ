package owo.pigeon.mixin.skyblocker;

import de.hysky.skyblocker.skyblock.tabhud.widget.ElementBasedWidget;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.Element;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.ArrayList;

@Mixin(ElementBasedWidget.class)
public interface IAccessorElementBasedWidget {
    @Accessor("elements")
    ArrayList<Element> pigeon$getElements();
}
