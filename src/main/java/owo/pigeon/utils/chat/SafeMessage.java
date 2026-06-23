package owo.pigeon.utils.chat;

import net.engio.mbassy.listener.Handler;
import net.minecraft.network.chat.Component;
import owo.pigeon.event.events.ClientTickEvent;

import java.util.ArrayList;
import java.util.List;

import static owo.pigeon.Pigeon.mc;

public class SafeMessage {
    public static List<Component> messages = new ArrayList<>();

    @Handler
    public void onTickPost(ClientTickEvent.Post event) {
        if (mc.gui == null || mc.gui.getChat() == null || messages.isEmpty()) return;
        for (Component message : messages) mc.gui.getChat().addMessage(message);
        messages.clear();
    }
}
