package owo.pigeon.mixin.mixins;

import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.DirectionalLayoutWidget;
import net.minecraft.text.Text;
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
    protected MixinDisconnectedScreen(Text title) {
        super(title);
    }

    @Shadow
    @Final
    private DirectionalLayoutWidget grid;
    @Unique
    private ButtonWidget reconnectButton;
    @Unique
    private ButtonWidget autoReconnectButton;

    @Unique
    private double reconnectTicks;

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/DirectionalLayoutWidget;refreshPositions()V", shift = At.Shift.BEFORE))
    private void onInit(CallbackInfo ci) {
        AutoReconnect autoReconnect = ModuleUtil.getModule(AutoReconnect.class);

        if (autoReconnect.address == null) return;
        reconnectTicks = autoReconnect.delay.getValue() / 50.0;

        reconnectButton = new ButtonWidget.Builder(Text.literal(reconnectLabel()), button -> reconnect()).width(200).build();
        grid.add(reconnectButton);

        autoReconnectButton = new ButtonWidget.Builder(Text.literal(autoReconnectLabel()), button -> {
            ModuleUtil.toggleModule(AutoReconnect.class);
            reconnectButton.setMessage(Text.literal(reconnectLabel()));
            autoReconnectButton.setMessage(Text.literal(autoReconnectLabel()));
            reconnectTicks = autoReconnect.delay.getValue() / 50.0;
        }).width(200).build();
        grid.add(autoReconnectButton);
    }

    @Override
    public void tick() {
        if (!ModuleUtil.isEnable(AutoReconnect.class) || ModuleUtil.getModule(AutoReconnect.class).address == null)
            return;

        if (reconnectTicks <= 0) {
            reconnect();
        } else {
            reconnectTicks--;
            if (reconnectButton != null) reconnectButton.setMessage(Text.literal(reconnectLabel()));
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
        ConnectScreen.connect(
                new MultiplayerScreen(new TitleScreen()),
                mc,
                autoReconnect.address,
                autoReconnect.info,
                false,
                autoReconnect.cookieStorage);
    }
}
