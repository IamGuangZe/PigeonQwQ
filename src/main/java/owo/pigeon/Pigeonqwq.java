package owo.pigeon;

import net.engio.mbassy.bus.MBassador;
import net.fabricmc.api.ModInitializer;
import net.minecraft.client.MinecraftClient;
import owo.pigeon.commands.CommandManager;
import owo.pigeon.event.Event;
import owo.pigeon.modules.ModuleManager;
import owo.pigeon.utils.Chat.SafeMessage;

public class Pigeonqwq implements ModInitializer {

    // public static final NyaEventBus EVENT_BUS = new NyaEventBus();
    public static final MBassador<Event> EVENT_BUS = new MBassador<>();
    public static MinecraftClient mc = MinecraftClient.getInstance();
    public static ModuleManager moduleManager = new ModuleManager();
    public static CommandManager commandManager = new CommandManager();

    public static SafeMessage safeMessage = new SafeMessage();

    public static final String MOD_NAME = "PigeonQwQ";
    public static final String MOD_VERSION = "0.0.1";
    public static boolean debug;

    @Override
    public void onInitialize() {
        EVENT_BUS.subscribe(safeMessage);

        moduleManager.init();
        commandManager.init();

    }
}
