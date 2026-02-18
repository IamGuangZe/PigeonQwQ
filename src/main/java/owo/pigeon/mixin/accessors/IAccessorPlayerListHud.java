package owo.pigeon.mixin.accessors;

import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerListHud.class)
public interface IAccessorPlayerListHud {
    @Accessor("header")
    Text pigeon$getHeader();

    @Accessor("footer")
    Text pigeon$getFooter();
}
