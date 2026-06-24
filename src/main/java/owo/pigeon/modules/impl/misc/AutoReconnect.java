package owo.pigeon.modules.impl.misc;

import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.TransferState;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.IntSetting;

public class AutoReconnect extends Module {
    public AutoReconnect() {
        super("AutoReconnect", Category.MISC);
    }

    public IntSetting delay = setting("delay", 5, 0, 60, "s", v -> true);

    public ServerAddress address;
    public ServerData info;
    public TransferState cookieStorage;
}
