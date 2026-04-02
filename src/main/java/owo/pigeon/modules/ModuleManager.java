package owo.pigeon.modules;

import net.engio.mbassy.listener.Handler;
import owo.pigeon.Pigeon;
import owo.pigeon.event.events.KeyInputEvent;
import owo.pigeon.modules.impl.client.ClickGui;
import owo.pigeon.modules.impl.client.PigeonQwQ;
import owo.pigeon.modules.impl.client.debug.*;
import owo.pigeon.modules.impl.combat.AutoBow;
import owo.pigeon.modules.impl.combat.AutoClicker;
import owo.pigeon.modules.impl.combat.HitBox;
import owo.pigeon.modules.impl.combat.NoHitDelay;
import owo.pigeon.modules.impl.hypixel.*;
import owo.pigeon.modules.impl.movement.NoJumpDelay;
import owo.pigeon.modules.impl.movement.Sprint;
import owo.pigeon.modules.impl.player.*;
import owo.pigeon.modules.impl.render.*;
import owo.pigeon.modules.impl.skyblock.combat.FlaySwitch;
import owo.pigeon.modules.impl.skyblock.dungeon.AutoGFS;
import owo.pigeon.modules.impl.skyblock.dungeon.ChestClose;
import owo.pigeon.modules.impl.skyblock.dungeon.StarMobESP;
import owo.pigeon.modules.impl.skyblock.event.AutoBouncingBall;
import owo.pigeon.modules.impl.skyblock.farming.PestESP;
import owo.pigeon.modules.impl.skyblock.farming.TrevorHelper;
import owo.pigeon.modules.impl.skyblock.hunting.AutoReel;
import owo.pigeon.modules.impl.skyblock.misc.*;
import owo.pigeon.modules.impl.skyblock.rift.AgaricusMiner;
import owo.pigeon.modules.impl.skyblock.rift.PotReplace;
import owo.pigeon.modules.impl.skyblock.rift.SnakingHelper;
import owo.pigeon.modules.impl.skyblock.rift.TimiteMiner;
import owo.pigeon.modules.impl.skyblock.slayer.AutoMaddox;
import owo.pigeon.modules.impl.skyblock.slayer.VampireSlayer;
import owo.pigeon.modules.impl.world.Environment;

import java.util.ArrayList;

import static owo.pigeon.Pigeon.mc;

public class ModuleManager {

    public static final ArrayList<Module> modules = new ArrayList<>();

    public void init() {
        Pigeon.EVENT_BUS.subscribe(this);

        modules.add(new ClickSlotTest());
        modules.add(new ExportButton());
        modules.add(new SettingTest());
        modules.add(new RenderTest());
        modules.add(new SlayerESP());

        modules.add(new ClickGui());
        modules.add(new PigeonQwQ());

        modules.add(new AutoBow());
        modules.add(new AutoClicker());
        modules.add(new HitBox());
        modules.add(new NoHitDelay());

        modules.add(new AutoTipall());
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
        modules.add(new ParticlesHalo());
        modules.add(new PlayerESP());

        modules.add(new Environment());

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
        modules.add(new AutoOption());
        modules.add(new ChocolateFactory());
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
