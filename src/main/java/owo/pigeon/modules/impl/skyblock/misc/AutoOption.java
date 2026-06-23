package owo.pigeon.modules.impl.skyblock.misc;

import net.engio.mbassy.listener.Handler;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import owo.pigeon.event.events.MessageEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.EnableSetting;
import owo.pigeon.utils.ColorUtil;
import owo.pigeon.utils.chat.ChatUtil;

import static owo.pigeon.Pigeon.mc;

public class AutoOption extends Module {
    public AutoOption() {
        super("AutoOption", Category.MISC);
    }

    public EnableSetting greenOption = setting("green-option", true, v -> true);
    public EnableSetting boldGreenOption = setting("bold-green-option", false, v -> true);
    public EnableSetting pickUpAbiphone = setting("pick-up-abiphone", false, v -> true);

    @Handler
    public void onChatReceive(MessageEvent.ReceiveMessageEvent event) {
        if (event.isOverlay()) return;

        Component messageText = event.getMessage();
        String message = ColorUtil.removeColor(messageText.getString());

        if (message.startsWith("Select an option:")) {
            for (Component sibling : messageText.getSiblings()) {
                String siblingStr = sibling.getString();
                boolean isGreen = siblingStr.contains("§a[") && greenOption.getValue();
                boolean isBoldGreen = siblingStr.contains("§a§l[") && boldGreenOption.getValue();

                if (!isGreen && !isBoldGreen) continue;
                if (executeClickCommand(sibling, "Auto Option")) return;
            }
        } else if (pickUpAbiphone.getValue() && message.contains("RING...") && message.contains("[PICK UP]")) {
            for (Component sibling : messageText.getSiblings()) {
                if (!sibling.getString().contains("§2§l[PICK UP]")) continue;
                if (executeClickCommand(sibling, "Auto Abiphone")) return;
            }
        }
    }

    private boolean executeClickCommand(Component sibling, String debugName) {
        if (sibling.getStyle() == null || sibling.getStyle().getClickEvent() == null) return false;

        ClickEvent clickEvent = sibling.getStyle().getClickEvent();
        if (!(clickEvent instanceof ClickEvent.RunCommand(String command))) return false;

        mc.execute(() -> {
            mc.player.connection.sendChat(command);
            ChatUtil.sendDebugMessage(this.name, debugName + ": " + command);
        });

        return true;
    }
}