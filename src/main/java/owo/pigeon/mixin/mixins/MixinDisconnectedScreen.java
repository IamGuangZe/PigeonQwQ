package owo.pigeon.mixin.mixins;

import net.minecraft.ChatFormatting;
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
    private Button minusButton;
    @Unique
    private Button plusButton;

    @Unique
    private double reconnectTicks;

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/LinearLayout;arrangeElements()V", shift = At.Shift.BEFORE))
    private void onInit(CallbackInfo ci) {
        AutoReconnect autoReconnect = ModuleUtil.getModule(AutoReconnect.class);

        if (autoReconnect.address == null) return;
        reconnectTicks = autoReconnect.delay.getValue() * 20.0;

        reconnectButton = new Button.Builder(Component.literal(reconnectLabel()), button -> reconnect()).width(200).build();
        layout.addChild(reconnectButton);

        minusButton = new Button.Builder(Component.literal("-").withStyle(ChatFormatting.RED), button -> {
            AutoReconnect ar = ModuleUtil.getModule(AutoReconnect.class);
            ar.delay.setValue(ar.delay.getValue() - 1);
            reconnectTicks = ar.delay.getValue() * 20.0;
            reconnectButton.setMessage(Component.literal(reconnectLabel()));
            autoReconnectButton.setMessage(Component.literal(autoReconnectLabel()));
        }).width(20).build();

        autoReconnectButton = new Button.Builder(Component.literal(autoReconnectLabel()), button -> {
            ModuleUtil.toggleModule(AutoReconnect.class);
            reconnectButton.setMessage(Component.literal(reconnectLabel()));
            autoReconnectButton.setMessage(Component.literal(autoReconnectLabel()));
            AutoReconnect ar = ModuleUtil.getModule(AutoReconnect.class);
            reconnectTicks = ar.delay.getValue() * 20.0;
        }).width(156).build();

        plusButton = new Button.Builder(Component.literal("+").withStyle(ChatFormatting.GREEN), button -> {
            AutoReconnect ar = ModuleUtil.getModule(AutoReconnect.class);
            ar.delay.setValue(ar.delay.getValue() + 1);
            reconnectTicks = ar.delay.getValue() * 20.0;
            reconnectButton.setMessage(Component.literal(reconnectLabel()));
            autoReconnectButton.setMessage(Component.literal(autoReconnectLabel()));
        }).width(20).build();

        LinearLayout autoRow = LinearLayout.horizontal().spacing(2);
        autoRow.addChild(minusButton);
        autoRow.addChild(autoReconnectButton);
        autoRow.addChild(plusButton);
        layout.addChild(autoRow);
    }

    @Override
    public void tick() {
        if (minusButton != null) minusButton.setFocused(false);
        if (plusButton != null) plusButton.setFocused(false);
        if (reconnectButton != null) reconnectButton.setFocused(false);
        if (autoReconnectButton != null) autoReconnectButton.setFocused(false);

        if (!ModuleUtil.isEnable(AutoReconnect.class) || ModuleUtil.getModule(AutoReconnect.class).address == null)
            return;

        if (reconnectTicks <= 0) {
            reconnect();
        } else {
            reconnectTicks--;
            if (reconnectButton != null) reconnectButton.setMessage(Component.literal(reconnectLabel()));
            if (autoReconnectButton != null) autoReconnectButton.setMessage(Component.literal(autoReconnectLabel()));
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
