package owo.pigeon.mixin.mixins;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import owo.pigeon.modules.impl.misc.AutoReconnect;
import owo.pigeon.utils.ModuleUtil;

import static owo.pigeon.Pigeon.mc;

@Mixin(DisconnectedScreen.class)
public abstract class MixinDisconnectedScreen extends Screen {
    protected MixinDisconnectedScreen(Component title) {
        super(title);
    }

    @Shadow
    @Final
    private LinearLayout layout;
    @Unique
    private Button reconnectButton;
    @Unique
    private Button autoReconnectButton;

    @Unique
    private double reconnectTicks;

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/LinearLayout;arrangeElements()V", shift = At.Shift.BEFORE))
    private void onInit(CallbackInfo ci) {
        AutoReconnect autoReconnect = ModuleUtil.getModule(AutoReconnect.class);

        if (autoReconnect.address == null) return;
        reconnectTicks = autoReconnect.delay.getValue() / 50.0;

        reconnectButton = new Button.Builder(Component.literal(reconnectLabel()), button -> reconnect()).width(200).build();
        layout.addChild(reconnectButton);

        autoReconnectButton = new Button.Builder(Component.literal(autoReconnectLabel()), button -> {
            ModuleUtil.toggleModule(AutoReconnect.class);
            reconnectButton.setMessage(Component.literal(reconnectLabel()));
            autoReconnectButton.setMessage(Component.literal(autoReconnectLabel()));
            reconnectTicks = autoReconnect.delay.getValue() / 50.0;
        }).width(200).build();
        layout.addChild(autoReconnectButton);
    }

    @Override
    public void tick() {
        if (!ModuleUtil.isEnable(AutoReconnect.class) || ModuleUtil.getModule(AutoReconnect.class).address == null)
            return;

        if (reconnectTicks <= 0) {
            reconnect();
        } else {
            reconnectTicks--;
            if (reconnectButton != null) reconnectButton.setMessage(Component.literal(reconnectLabel()));
        }
    }

    @Unique
    private String reconnectLabel() {
        String label = "Reconnect";
        if (ModuleUtil.isEnable(AutoReconnect.class)) label += " " + String.format("(%.1fs)", reconnectTicks * 0.05);
        return label;
    }

    @Unique
    private String autoReconnectLabel() {
        return "Auto Reconnect: " + (ModuleUtil.isEnable(AutoReconnect.class) ? "§aEnabled" : "§cDisabled");
    }

    @Unique
    private void reconnect() {
        AutoReconnect autoReconnect = ModuleUtil.getModule(AutoReconnect.class);
        ConnectScreen.startConnecting(
                new JoinMultiplayerScreen(new TitleScreen()),
                mc,
                autoReconnect.address,
                autoReconnect.info,
                false,
                autoReconnect.cookieStorage);
    }
}
