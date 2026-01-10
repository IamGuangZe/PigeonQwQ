package owo.pigeon.modules;

import net.engio.mbassy.listener.Handler;
import owo.pigeon.Pigeonqwq;
import owo.pigeon.event.events.KeyInputEvent;
import owo.pigeon.event.events.TickEvent;
import owo.pigeon.modules.impl.Movement.Sprint;

import java.util.ArrayList;

public class ModuleManager {

    public static final ArrayList<Module> modules = new ArrayList<>();

    public void init() {
        Pigeonqwq.EVENT_BUS.subscribe(this);

        modules.add(new Sprint());
    }

    @Handler
    public void onKeyInput(KeyInputEvent event) {
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
            modules.stream().filter(Module::isEnable).forEach(Module::onTickPost);
        }
    }
}
