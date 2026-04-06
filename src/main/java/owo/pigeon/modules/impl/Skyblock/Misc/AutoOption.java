package owo.pigeon.modules.impl.skyblock.misc;

import net.engio.mbassy.listener.Handler;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import owo.pigeon.event.events.MessageEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.utils.ColorUtil;
import owo.pigeon.utils.chat.ChatUtil;

import static owo.pigeon.Pigeon.mc;

public class AutoOption extends Module {
    public AutoOption() {
        super("AutoOption", Category.SKYBLOCK);
    }

    @Handler
    public void onChatReceive(MessageEvent.ReceiveMessageEvent event) {
        if (event.isOverlay()) return;

        Text messageText = event.getMessage();
        String message = ColorUtil.removeColor(messageText.getString());

        if (message.startsWith("Select an option:")) {
            for (Text sibling : messageText.getSiblings()) {
                // ChatUtil.sendDebugMessage(this.name, sibling.toString());
                if (sibling.getString().contains("§a[") || sibling.getString().contains("§a§l[")) {
                    if (sibling.getStyle() != null && sibling.getStyle().getClickEvent() != null) {
                        ClickEvent clickEvent = sibling.getStyle().getClickEvent();

                        if (clickEvent instanceof ClickEvent.RunCommand(String command)) {
                            mc.execute(() -> mc.player.networkHandler.sendChatMessage(command));
                            ChatUtil.sendDebugMessage(this.name, "Send Command : " + command);
                            break;
                        }
                    }
                }
            }
        }
    }
}
