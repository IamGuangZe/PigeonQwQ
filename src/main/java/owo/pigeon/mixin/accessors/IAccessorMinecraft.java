package owo.pigeon.mixin.accessors;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Minecraft.class)
public interface IAccessorMinecraft {

    @Invoker("startAttack")
    boolean pigeon$invokeStartAttack();

    @Invoker("startUseItem")
    void pigeon$invokeStartUseItem();
}
