package owo.pigeon.modules.impl.render;

import com.mojang.blaze3d.platform.InputConstants;
import net.engio.mbassy.listener.Handler;
import net.minecraft.client.CameraType;
import owo.pigeon.event.events.ClientTickEvent;
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
        TOGGLE, HOLD
    }

    public enum PerspectiveMode {
        BACK(CameraType.THIRD_PERSON_BACK),
        FRONT(CameraType.THIRD_PERSON_FRONT);

        private final CameraType perspective;

        PerspectiveMode(CameraType perspective) {
            this.perspective = perspective;
        }

        public CameraType getPerspective() {
            return perspective;
        }
    }

    public ModeSetting<Mode> mode = setting("mode", Mode.HOLD, v -> true);
    public ModeSetting<PerspectiveMode> perspective = setting("perspective", PerspectiveMode.BACK, v -> true);
    public KeySetting freeLookKey = setting("free-look-key", InputConstants.KEY_F, v -> true);

    public boolean freelooking = false;
    private CameraType oldPerspective = CameraType.FIRST_PERSON;
    private boolean toggleState = false;
    private boolean lastKeyState = false;

    @Override
    public void onEnable() {
        freelooking = false;
        toggleState = false;
        lastKeyState = false;
    }

    @Handler
    public void onTickPre(ClientTickEvent.Pre event) {
        if (WorldUtil.nullCheck()) return;

        boolean isKeyDown = KeybindUtil.isPressed(freeLookKey.getValue());
        boolean isInScreen = mc.screen != null;

        if (mode.getValue() == Mode.HOLD) {
            if (isKeyDown && !isInScreen) {
                if (!freelooking) startFreeLook();
            } else {
                if (freelooking) stopFreeLook();
            }
        } else {
            if (isKeyDown && !lastKeyState && !isInScreen) {
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

        freelooking = false;
        toggleState = false;
        lastKeyState = false;
    }

    private void startFreeLook() {
        oldPerspective = mc.options.getCameraType();
        freelooking = true;
        mc.options.setCameraType(perspective.getValue().getPerspective());
    }

    private void stopFreeLook() {
        freelooking = false;
        mc.options.setCameraType(oldPerspective);
    }
}
