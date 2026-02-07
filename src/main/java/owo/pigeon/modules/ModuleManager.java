package owo.pigeon.modules;

import net.engio.mbassy.listener.Handler;
import owo.pigeon.Pigeonqwq;
import owo.pigeon.event.events.KeyInputEvent;
import owo.pigeon.modules.impl.Client.ClickGui;
import owo.pigeon.modules.impl.Client.Debug.ClickSlotTest;
import owo.pigeon.modules.impl.Client.Debug.RenderTest;
import owo.pigeon.modules.impl.Client.Debug.SettingTest;
import owo.pigeon.modules.impl.Client.Debug.SlayerESP;
import owo.pigeon.modules.impl.Client.PigeonQwQ;
import owo.pigeon.modules.impl.Combat.AutoClicker;
import owo.pigeon.modules.impl.Combat.NoHitDelay;
import owo.pigeon.modules.impl.Movement.NoJumpDelay;
import owo.pigeon.modules.impl.Movement.Sprint;
import owo.pigeon.modules.impl.Player.AutoFish;
import owo.pigeon.modules.impl.Player.AutoHeal;
import owo.pigeon.modules.impl.Player.FastPlace;
import owo.pigeon.modules.impl.Player.NoBreakDelay;
import owo.pigeon.modules.impl.Render.FullBright;
import owo.pigeon.modules.impl.Render.ModifyCamera;
import owo.pigeon.modules.impl.Render.PlayerESP;
import owo.pigeon.modules.impl.Skyblock.AutoExperiments;
import owo.pigeon.modules.impl.Skyblock.Combat.FlaySwitch;
import owo.pigeon.modules.impl.Skyblock.Dungeon.StarMobESP;
import owo.pigeon.modules.impl.Skyblock.Rift.AgaricusMiner;
import owo.pigeon.modules.impl.Skyblock.Rift.SnakingHelper;
import owo.pigeon.modules.impl.Skyblock.Rift.TimiteMiner;
import owo.pigeon.modules.impl.Skyblock.Slayer.AutoMaddox;
import owo.pigeon.modules.impl.Skyblock.Slayer.VampireSlayer;

import java.util.ArrayList;

import static owo.pigeon.Pigeonqwq.mc;

public class ModuleManager {

    public static final ArrayList<Module> modules = new ArrayList<>();

    public void init() {
        Pigeonqwq.EVENT_BUS.subscribe(this);

        modules.add(new ClickSlotTest());
        modules.add(new SettingTest());
        modules.add(new RenderTest());
        modules.add(new SlayerESP());

        modules.add(new ClickGui());
        modules.add(new PigeonQwQ());

        modules.add(new AutoClicker());
        modules.add(new NoHitDelay());

        modules.add(new NoJumpDelay());
        modules.add(new Sprint());

        modules.add(new AutoFish());
        modules.add(new AutoHeal());
        modules.add(new FastPlace());
        modules.add(new NoBreakDelay());

        modules.add(new FullBright());
        modules.add(new ModifyCamera());
        modules.add(new PlayerESP());

        /* =======*SkyBlock Module*======= */
        modules.add(new FlaySwitch());
        modules.add(new StarMobESP());
        modules.add(new AgaricusMiner());
        modules.add(new SnakingHelper());
        modules.add(new TimiteMiner());
        modules.add(new AutoMaddox());
        modules.add(new VampireSlayer());
        modules.add(new AutoExperiments());
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
