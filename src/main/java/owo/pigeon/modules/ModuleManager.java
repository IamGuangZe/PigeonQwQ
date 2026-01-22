package owo.pigeon.modules;

import net.engio.mbassy.listener.Handler;
import owo.pigeon.Pigeonqwq;
import owo.pigeon.event.events.KeyInputEvent;
import owo.pigeon.event.events.RenderEvent;
import owo.pigeon.event.events.TickEvent;
import owo.pigeon.modules.impl.Client.ClickGui;
import owo.pigeon.modules.impl.Client.Debug.ClickSlotTest;
import owo.pigeon.modules.impl.Client.Debug.SettingTest;
import owo.pigeon.modules.impl.Client.Debug.TextRenderTest;
import owo.pigeon.modules.impl.Client.PigeonQwQ;
import owo.pigeon.modules.impl.Combat.AutoClicker;
import owo.pigeon.modules.impl.Combat.NoHitDelay;
import owo.pigeon.modules.impl.Movement.Sprint;
import owo.pigeon.modules.impl.Player.AutoFish;
import owo.pigeon.modules.impl.Skyblock.AutoExperiments;

import java.util.ArrayList;

import static owo.pigeon.Pigeonqwq.mc;

public class ModuleManager {

    public static final ArrayList<Module> modules = new ArrayList<>();

    public void init() {
        Pigeonqwq.EVENT_BUS.subscribe(this);

        modules.add(new ClickSlotTest());
        modules.add(new SettingTest());
        modules.add(new TextRenderTest());

        modules.add(new ClickGui());
        modules.add(new PigeonQwQ());

        modules.add(new AutoClicker());
        modules.add(new NoHitDelay());

        modules.add(new Sprint());

        modules.add(new AutoFish());

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

    @Handler
    public void onTickPost(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.POST) {
            modules.stream()
                    .filter(Module::isEnable)
                    .forEach(Module::onTickPost);
        }
    }

    @Handler
    public void onRender2D(RenderEvent.Render2DEvent event) {
        modules.stream()
                .filter(Module::isEnable)
                .forEach(module -> module.onRender2D(event.getContext()));

    }
}
