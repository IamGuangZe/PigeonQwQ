package owo.pigeon;

import net.engio.mbassy.bus.MBassador;
import net.fabricmc.api.ModInitializer;
import net.minecraft.client.MinecraftClient;
import owo.pigeon.event.Event;
import owo.pigeon.modules.ModuleManager;

public class Pigeonqwq implements ModInitializer {

    // public static final NyaEventBus EVENT_BUS = new NyaEventBus();
    public static final MBassador<Event> EVENT_BUS = new MBassador<>();
    public static MinecraftClient mc = MinecraftClient.getInstance();
    public static ModuleManager moduleManager = new ModuleManager();

    public static final String MOD_NAME = "PigeonQwQ";
    public static final String MOD_VERSION = "0.0.1";
    public static boolean debug;

    @Override
    public void onInitialize() {
        moduleManager.init();
    }
}
