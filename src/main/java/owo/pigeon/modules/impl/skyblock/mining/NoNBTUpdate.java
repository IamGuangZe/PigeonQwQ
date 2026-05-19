package owo.pigeon.modules.impl.skyblock.mining;

import net.engio.mbassy.listener.Handler;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import owo.pigeon.event.events.PacketEvent;
import owo.pigeon.modules.Category;
import owo.pigeon.modules.Module;
import owo.pigeon.utils.ColorUtil;

public class NoNBTUpdate extends Module {
    public NoNBTUpdate() {
        super("NoNBTUpdate", Category.MINING);
    }

    @Handler
    public void onReceivePacket(PacketEvent.ReceivePacketEvent.Pre event) {
        if (event.getPacket() instanceof ScreenHandlerSlotUpdateS2CPacket packet) {
            ItemStack stack = packet.getStack();
            String name = ColorUtil.removeColor(stack.getName().getString());

            if (name.contains(" Drill ") || stack.isOf(Items.PRISMARINE_SHARD) ||
                    name.contains("Pickonimbus 2000") || stack.isOf(Items.DIAMOND_PICKAXE) ||
                    name.contains("Gemstone Gauntlet"))
                event.setCancelled(true);
        }
    }
}
