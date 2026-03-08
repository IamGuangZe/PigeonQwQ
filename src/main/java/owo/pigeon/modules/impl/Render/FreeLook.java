package owo.pigeon.modules.impl.Render;

import net.engio.mbassy.listener.Handler;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.util.InputUtil;
import owo.pigeon.event.events.TickEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.KeySetting;
import owo.pigeon.settings.ModeSetting;
import owo.pigeon.utils.KeybindUtil;
import owo.pigeon.utils.WorldUtil;

import static owo.pigeon.Pigeon.mc;

public class FreeLook extends Module {
    public FreeLook() {
        super("FreeLook", Category.RENDER);
    }

    public enum Mode {
        TOGGLE, HOLD;
    }

    public enum PerspectiveMode {
        BACK(Perspective.THIRD_PERSON_BACK),
        FRONT(Perspective.THIRD_PERSON_FRONT);

        private final Perspective perspective;

        PerspectiveMode(Perspective perspective) {
            this.perspective = perspective;
        }

        public Perspective getPerspective() {
            return perspective;
        }
    }

    public ModeSetting<Mode> mode = setting("mode", Mode.HOLD, v -> true);
    public ModeSetting<PerspectiveMode> perspective = setting("perspective", PerspectiveMode.BACK, v -> true);
    public KeySetting freeLookKey = setting("free-look-key", InputUtil.GLFW_KEY_F, v -> true);

    public boolean freelooking = false;
    private Perspective oldPerspective = Perspective.FIRST_PERSON;
    private boolean toggleState = false;
    private boolean lastKeyState = false;

    @Override
    public void onEnable() {
        freelooking = false;
        toggleState = false;
        lastKeyState = false;
    }

    @Handler
    public void onTickPre(TickEvent.ClientTickEvent.Pre event) {
        if (WorldUtil.nullCheck()) return;

        boolean isKeyDown = KeybindUtil.isPressed(freeLookKey.getValue());

        if (mode.getValue() == Mode.HOLD) {
            if (isKeyDown && mc.currentScreen == null) {
                if (!freelooking) startFreeLook();
            } else {
                if (freelooking) stopFreeLook();
            }
        } else {
            if (isKeyDown && !lastKeyState) {
                toggleState = !toggleState;
                if (toggleState) startFreeLook();
                else stopFreeLook();
            }
        }

        lastKeyState = isKeyDown;
    }

    @Override
    public void onDisable() {
        if (freelooking) {
            stopFreeLook();
        }
    }

    private void startFreeLook() {
        oldPerspective = mc.options.getPerspective();
        freelooking = true;
        mc.options.setPerspective(perspective.getValue().getPerspective());
    }

    private void stopFreeLook() {
        freelooking = false;
        mc.options.setPerspective(oldPerspective);
    }
}
