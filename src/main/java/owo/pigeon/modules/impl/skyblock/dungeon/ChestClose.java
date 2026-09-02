package owo.pigeon.modules.impl.skyblock.dungeon;

import net.engio.mbassy.listener.Handler;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import owo.pigeon.event.events.ClickSlotEvent;
import owo.pigeon.event.events.ClientTickEvent;
import owo.pigeon.event.events.KeyInputEvent;
import owo.pigeon.event.events.PacketEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.ModeSetting;
import owo.pigeon.utils.WorldUtil;
import owo.pigeon.utils.hypixel.skyblock.DungeonUtil;

import static owo.pigeon.Pigeon.mc;

public class ChestClose extends Module {
    public ChestClose() {
        super("ChestClose", Category.DUNGEON);
    }

    public enum Mode {
        TICK, PACKET, INPUT
    }

    public ModeSetting<Mode> closeMode = setting("close-mode", Mode.TICK, v -> true);

    private boolean shouldClose;

    @Handler
    public void onTick(ClientTickEvent event) {
        if (WorldUtil.nullCheck()) return;
        if (closeMode.getValue() != Mode.TICK || !DungeonUtil.isInDungeon()) return;

        if (event instanceof ClientTickEvent.Pre) {
            if (mc.gui.screen() instanceof ContainerScreen screen) {
                String title = screen.getTitle().getString();

                if (title.equals("Chest") || title.equals("Large Chest")) {
                    shouldClose = true;
                }
            }
        }

        if (event instanceof ClientTickEvent.Post) {
            if (shouldClose) mc.player.closeContainer();
            shouldClose = false;
        }
    }

    @Handler
    public void onPacketReceive(PacketEvent.ReceivePacketEvent.Pre event) {
        if (event.getPacket() instanceof ClientboundOpenScreenPacket packet) {

            // GrimAC MultActionsD
            // 1.8.9 -> 疾跑时开箱
            // 1.21.10 -> 移动与疾跑时开箱

            if (closeMode.getValue() != Mode.PACKET || !DungeonUtil.isInDungeon()) return;
            String title = packet.getTitle().getString();

            if (title.equals("Chest") || title.equals("Large Chest")) {
                int syncId = packet.getContainerId();
                mc.getConnection().send(new ServerboundContainerClosePacket(syncId));
                event.setCancelled(true);
            }
        }
    }

    @Handler
    public void onKeyInput(KeyInputEvent event) {
        if (closeMode.getValue() != Mode.INPUT || !DungeonUtil.isInDungeon()) return;
        if (mc.gui.screen() instanceof ContainerScreen screen) {
            String title = screen.getTitle().getString();

            if (title.equals("Chest") || title.equals("Large Chest")) {
                mc.player.closeContainer();
            }
        }
    }

    @Handler
    public void onClickSlot(ClickSlotEvent event) {
        if (closeMode.getValue() != Mode.INPUT || !DungeonUtil.isInDungeon()) return;
        if (mc.gui.screen() instanceof ContainerScreen screen) {
            String title = screen.getTitle().getString();

            if (title.equals("Chest") || title.equals("Large Chest")) {
                mc.player.closeContainer();
            }
        }
    }
}
