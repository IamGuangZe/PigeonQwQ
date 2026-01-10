package owo.pigeon.utils.Chat;

import net.engio.mbassy.listener.Handler;
import net.minecraft.text.Text;
import owo.pigeon.event.events.TickEvent;

import java.util.ArrayList;
import java.util.List;

import static owo.pigeon.Pigeonqwq.mc;

public class SafeMessage {
    public static List<String> messages = new ArrayList<>();

    @Handler
    public void onTick(TickEvent.ClientTickEvent event) {
        if (mc.inGameHud == null || mc.inGameHud.getChatHud() == null) return;
        for (String message : messages) mc.inGameHud.getChatHud().addMessage(Text.of(message));
        messages.clear();
    }
}
