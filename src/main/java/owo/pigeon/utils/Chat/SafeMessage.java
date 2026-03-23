package owo.pigeon.utils.Chat;

import net.engio.mbassy.listener.Handler;
import net.minecraft.text.Text;
import owo.pigeon.event.events.TickEvent;

import java.util.ArrayList;
import java.util.List;

import static owo.pigeon.Pigeon.mc;

public class SafeMessage {
    public static List<Text> messages = new ArrayList<>();

    @Handler
    public void onTickPost(TickEvent.ClientTickEvent.Post event) {
        if (mc.inGameHud == null || mc.inGameHud.getChatHud() == null || messages.isEmpty()) return;
        for (Text message : messages) mc.inGameHud.getChatHud().addMessage(message);
        messages.clear();
    }
}
