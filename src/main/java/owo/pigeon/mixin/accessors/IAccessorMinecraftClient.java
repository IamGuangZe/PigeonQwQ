package owo.pigeon.mixin.accessors;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MinecraftClient.class)
public interface IAccessorMinecraftClient {

    @Invoker("doAttack")
    boolean pigeon$invokeDoAttack();

    @Invoker("doItemUse")
    void pigeon$invokeDoItemUse();
}
