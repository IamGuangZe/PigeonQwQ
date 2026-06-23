package owo.pigeon.mixin.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.TransferState;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import owo.pigeon.modules.impl.misc.AutoReconnect;
import owo.pigeon.utils.ModuleUtil;

@Mixin(ConnectScreen.class)
public class MixinConnectScreen {
    @Inject(method = "connect(Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/multiplayer/resolver/ServerAddress;Lnet/minecraft/client/multiplayer/ServerData;Lnet/minecraft/client/multiplayer/TransferState;)V", at = @At("HEAD"))
    private void onConnectPre(Minecraft client, ServerAddress address, ServerData info, TransferState cookieStorage, CallbackInfo ci) {
        AutoReconnect autoReconnect = ModuleUtil.getModule(AutoReconnect.class);

        autoReconnect.address = address;
        autoReconnect.info = info;
        autoReconnect.cookieStorage = cookieStorage;
    }
}
