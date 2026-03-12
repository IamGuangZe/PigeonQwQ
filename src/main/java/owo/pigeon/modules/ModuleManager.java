package owo.pigeon.modules;

import net.engio.mbassy.listener.Handler;
import owo.pigeon.Pigeon;
import owo.pigeon.event.events.KeyInputEvent;
import owo.pigeon.modules.impl.Client.ClickGui;
import owo.pigeon.modules.impl.Client.Debug.ClickSlotTest;
import owo.pigeon.modules.impl.Client.Debug.RenderTest;
import owo.pigeon.modules.impl.Client.Debug.SettingTest;
import owo.pigeon.modules.impl.Client.Debug.SlayerESP;
import owo.pigeon.modules.impl.Client.PigeonQwQ;
import owo.pigeon.modules.impl.Combat.AutoBow;
import owo.pigeon.modules.impl.Combat.AutoClicker;
import owo.pigeon.modules.impl.Combat.HitBox;
import owo.pigeon.modules.impl.Combat.NoHitDelay;
import owo.pigeon.modules.impl.Hypixel.*;
import owo.pigeon.modules.impl.Movement.NoJumpDelay;
import owo.pigeon.modules.impl.Movement.Sprint;
import owo.pigeon.modules.impl.Player.*;
import owo.pigeon.modules.impl.Render.*;
import owo.pigeon.modules.impl.Skyblock.Combat.FlaySwitch;
import owo.pigeon.modules.impl.Skyblock.Dungeon.AutoGFS;
import owo.pigeon.modules.impl.Skyblock.Dungeon.ChestClose;
import owo.pigeon.modules.impl.Skyblock.Dungeon.StarMobESP;
import owo.pigeon.modules.impl.Skyblock.Event.AutoBouncingBall;
import owo.pigeon.modules.impl.Skyblock.Farming.PestESP;
import owo.pigeon.modules.impl.Skyblock.Farming.TrevorHelper;
import owo.pigeon.modules.impl.Skyblock.Hunting.AutoReel;
import owo.pigeon.modules.impl.Skyblock.Misc.AutoEquipment;
import owo.pigeon.modules.impl.Skyblock.Misc.AutoExperiments;
import owo.pigeon.modules.impl.Skyblock.Misc.FailSafe;
import owo.pigeon.modules.impl.Skyblock.Rift.AgaricusMiner;
import owo.pigeon.modules.impl.Skyblock.Rift.PotReplace;
import owo.pigeon.modules.impl.Skyblock.Rift.SnakingHelper;
import owo.pigeon.modules.impl.Skyblock.Rift.TimiteMiner;
import owo.pigeon.modules.impl.Skyblock.Slayer.AutoMaddox;
import owo.pigeon.modules.impl.Skyblock.Slayer.VampireSlayer;

import java.util.ArrayList;

import static owo.pigeon.Pigeon.mc;

public class ModuleManager {

    public static final ArrayList<Module> modules = new ArrayList<>();

    public void init() {
        Pigeon.EVENT_BUS.subscribe(this);

        modules.add(new ClickSlotTest());
        modules.add(new SettingTest());
        modules.add(new RenderTest());
        modules.add(new SlayerESP());

        modules.add(new ClickGui());
        modules.add(new PigeonQwQ());

        modules.add(new AutoBow());
        modules.add(new AutoClicker());
        modules.add(new HitBox());
        modules.add(new NoHitDelay());

        modules.add(new BannedStats());
        modules.add(new GTBSolver());
        modules.add(new MurderHelper());
        modules.add(new PartyDetector());
        modules.add(new PixelHelper());

        modules.add(new NoJumpDelay());
        modules.add(new Sprint());

        modules.add(new AutoFish());
        modules.add(new AutoHeal());
        modules.add(new FastPlace());
        modules.add(new GhostHand());
        modules.add(new NoBreakDelay());

        modules.add(new BedESP());
        modules.add(new BlockESP());
        modules.add(new FreeLook());
        modules.add(new FullBright());
        modules.add(new ModifyCamera());
        modules.add(new PlayerESP());

        /* =======*SkyBlock Module*======= */
        modules.add(new FlaySwitch());
        modules.add(new AutoGFS());
        modules.add(new ChestClose());
        modules.add(new StarMobESP());
        modules.add(new AutoBouncingBall());
        modules.add(new PestESP());
        modules.add(new TrevorHelper());
        modules.add(new AutoReel());
        modules.add(new AutoEquipment());
        modules.add(new AutoExperiments());
        modules.add(new FailSafe());
        modules.add(new AgaricusMiner());
        modules.add(new PotReplace());
        modules.add(new SnakingHelper());
        modules.add(new TimiteMiner());
        modules.add(new AutoMaddox());
        modules.add(new VampireSlayer());
    }

    @Handler
    public void onKeyInput(KeyInputEvent event) {
        if (mc.currentScreen != null) return;
        int keyCode = event.getKeyCode();
        if (event.isPressed()) {
            modules.stream()
                    .filter(it -> it.getKey() == keyCode)
                    .forEach(Module::toggle);
        }
    }
}
