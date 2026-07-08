package owo.pigeon;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.engio.mbassy.bus.MBassador;
import net.fabricmc.api.ModInitializer;
import net.minecraft.client.Minecraft;
import owo.pigeon.commands.CommandManager;
import owo.pigeon.config.ConfigManager;
import owo.pigeon.event.Event;
import owo.pigeon.gui.clickgui.pigeon.ClickGuiScreen;
import owo.pigeon.modules.ModuleManager;
import owo.pigeon.modules.impl.client.PigeonQwQ;
import owo.pigeon.utils.ModuleUtil;
import owo.pigeon.utils.chat.SafeMessage;
import owo.pigeon.utils.export.ExportManager;
import owo.pigeon.utils.hypixel.BanTracker;
import owo.pigeon.utils.hypixel.HypixelStateCache;
import owo.pigeon.utils.player.InstantUse;

public class Pigeon implements ModInitializer {

    // public static final NyaEventBus EVENT_BUS = new NyaEventBus();
    public static final MBassador<Event> EVENT_BUS = new MBassador<>();
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    public static Minecraft mc = Minecraft.getInstance();
    public static ModuleManager moduleManager;
    public static CommandManager commandManager;
    public static ClickGuiScreen clickGuiScreen;
    public static ConfigManager configManager;

    public static SafeMessage safeMessage = new SafeMessage();
    public static ExportManager exportManager = new ExportManager();
    public static InstantUse instantUse = new InstantUse();
    public static HypixelStateCache hypixelStateCache = new HypixelStateCache();
    public static BanTracker banTracker = new BanTracker();

    public static final String MOD_ID = "pigeonqwq";
    public static final String MOD_NAME = "PigeonQwQ";
    public static final String MOD_VERSION = "0.0.1";
    public static final String WATERMARK = MOD_NAME + " v" + MOD_VERSION + " by GuangZe233";

    @Override
    public void onInitialize() {
        EVENT_BUS.subscribe(safeMessage);
        EVENT_BUS.subscribe(exportManager);
        EVENT_BUS.subscribe(instantUse);
        EVENT_BUS.subscribe(hypixelStateCache);

        moduleManager = new ModuleManager();
        moduleManager.init();

        commandManager = new CommandManager();
        commandManager.init();

        clickGuiScreen = new ClickGuiScreen();

        configManager = new ConfigManager();
        configManager.init();

        banTracker.start();
    }

    public static boolean isDebug() {
        return ModuleUtil.getModule(PigeonQwQ.class).debug.getValue();
    }
}
