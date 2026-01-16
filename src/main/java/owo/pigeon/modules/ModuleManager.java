package owo.pigeon.modules;

import net.engio.mbassy.listener.Handler;
import owo.pigeon.Pigeonqwq;
import owo.pigeon.event.events.KeyInputEvent;
import owo.pigeon.event.events.RenderEvent;
import owo.pigeon.event.events.TickEvent;
import owo.pigeon.modules.impl.Client.Debug.SettingTest;
import owo.pigeon.modules.impl.Client.Debug.TextRenderTest;
import owo.pigeon.modules.impl.Client.PigeonQwQ;
import owo.pigeon.modules.impl.Movement.Sprint;

import java.util.ArrayList;

import static owo.pigeon.Pigeonqwq.mc;

public class ModuleManager {

    public static final ArrayList<Module> modules = new ArrayList<>();

    public void init() {
        Pigeonqwq.EVENT_BUS.subscribe(this);

        modules.add(new SettingTest());
        modules.add(new TextRenderTest());

        modules.add(new PigeonQwQ());
        modules.add(new Sprint());
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
