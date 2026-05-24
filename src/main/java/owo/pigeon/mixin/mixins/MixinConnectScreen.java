package owo.pigeon.mixin.mixins;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.network.CookieStorage;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import owo.pigeon.modules.impl.misc.AutoReconnect;
import owo.pigeon.utils.ModuleUtil;

@Mixin(ConnectScreen.class)
public class MixinConnectScreen {
    @Inject(method = "connect(Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/client/network/ServerAddress;Lnet/minecraft/client/network/ServerInfo;Lnet/minecraft/client/network/CookieStorage;)V", at = @At("HEAD"))
    private void onConnectPre(MinecraftClient client, ServerAddress address, ServerInfo info, CookieStorage cookieStorage, CallbackInfo ci) {
        AutoReconnect autoReconnect = ModuleUtil.getModule(AutoReconnect.class);

        autoReconnect.address = address;
        autoReconnect.info = info;
        autoReconnect.cookieStorage = cookieStorage;
    }
}
