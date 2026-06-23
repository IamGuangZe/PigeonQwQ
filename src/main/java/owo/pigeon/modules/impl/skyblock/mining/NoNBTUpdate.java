package owo.pigeon.modules.impl.skyblock.mining;

import net.engio.mbassy.listener.Handler;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
        if (event.getPacket() instanceof ClientboundContainerSetSlotPacket packet) {
            ItemStack stack = packet.getItem();
            String name = ColorUtil.removeColor(stack.getHoverName().getString());

            if (name.contains(" Drill ") || stack.is(Items.PRISMARINE_SHARD) ||
                    name.contains("Pickonimbus 2000") || stack.is(Items.DIAMOND_PICKAXE) ||
                    name.contains("Gemstone Gauntlet"))
                event.setCancelled(true);
        }
    }
}
