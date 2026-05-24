package owo.pigeon.modules.impl.misc;

import net.minecraft.client.network.CookieStorage;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.IntSetting;

public class AutoReconnect extends Module {
    public AutoReconnect() {
        super("AutoReconnect", Category.MISC);
    }

    public IntSetting delay = setting("delay", 1000, 100, 5000, "ms", v -> true);

    public ServerAddress address;
    public ServerInfo info;
    public CookieStorage cookieStorage;
}
