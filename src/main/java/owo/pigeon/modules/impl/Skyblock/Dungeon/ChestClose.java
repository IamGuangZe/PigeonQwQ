package owo.pigeon.modules.impl.Skyblock.Dungeon;

import net.engio.mbassy.listener.Handler;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import owo.pigeon.event.events.ClickSlotEvent;
import owo.pigeon.event.events.KeyInputEvent;
import owo.pigeon.event.events.PacketEvent;
import owo.pigeon.event.events.TickEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.settings.ModeSetting;
import owo.pigeon.utils.Hypixel.SkyblockUtil;
import owo.pigeon.utils.WorldUtil;

import static owo.pigeon.Pigeon.mc;

public class ChestClose extends Module {
    public ChestClose() {
        super("ChestClose", Category.SKYBLOCK);
    }

    public enum Mode {
        TICK, PACKET, INPUT;
    }

    public ModeSetting<Mode> closeMode = setting("close-mode", Mode.TICK, v -> true);

    private boolean shouldClose;

    @Handler
    public void onTick(TickEvent.ClientTickEvent event) {
        if (WorldUtil.nullCheck()) return;
        if (closeMode.getValue() != Mode.TICK || !SkyblockUtil.isInIsland(SkyblockUtil.Island.Dungeon)) return;

        if (event instanceof TickEvent.ClientTickEvent.Pre) {
            if (mc.currentScreen instanceof GenericContainerScreen screen) {
                String title = screen.getTitle().getString();

                if (title.equals("Chest") || title.equals("Large Chest")) {
                    shouldClose = true;
                }
            }
        }

        if (event instanceof TickEvent.ClientTickEvent.Post) {
            if (shouldClose) mc.player.closeHandledScreen();
            shouldClose = false;
        }
    }

    @Handler
    public void onPacketReceive(PacketEvent.ReceivePacketEvent.Pre event) {
        if (event.getPacket() instanceof OpenScreenS2CPacket packet) {

            // GrimAC MultActionsD
            // 1.8.9 -> 疾跑时开箱
            // 1.21.10 -> 移动与疾跑时开箱

            if (closeMode.getValue() != Mode.PACKET || !SkyblockUtil.isInIsland(SkyblockUtil.Island.Dungeon)) return;
            String title = packet.getName().getString();

            if (title.equals("Chest") || title.equals("Large Chest")) {
                int syncId = packet.getSyncId();
                mc.getNetworkHandler().sendPacket(new CloseHandledScreenC2SPacket(syncId));
                event.setCancelled(true);
            }
        }
    }

    @Handler
    public void onKeyInput(KeyInputEvent event) {
        if (closeMode.getValue() != Mode.INPUT || !SkyblockUtil.isInIsland(SkyblockUtil.Island.Dungeon)) return;
        if (mc.currentScreen instanceof GenericContainerScreen screen) {
            String title = screen.getTitle().getString();

            if (title.equals("Chest") || title.equals("Large Chest")) {
                mc.player.closeHandledScreen();
            }
        }
    }

    @Handler
    public void onClickSlot(ClickSlotEvent event) {
        if (closeMode.getValue() != Mode.INPUT || !SkyblockUtil.isInIsland(SkyblockUtil.Island.Dungeon)) return;
        if (mc.currentScreen instanceof GenericContainerScreen screen) {
            String title = screen.getTitle().getString();

            if (title.equals("Chest") || title.equals("Large Chest")) {
                mc.player.closeHandledScreen();
            }
        }
    }
}
