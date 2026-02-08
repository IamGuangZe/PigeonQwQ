package owo.pigeon.commands.impl;

import net.minecraft.block.Block;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import owo.pigeon.commands.Command;
import owo.pigeon.utils.CommandUtil;
import owo.pigeon.utils.WorldUtil;

import static owo.pigeon.Pigeon.mc;

public class SetBlockCommand extends Command {
    public SetBlockCommand() {
        super("setblock");
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 4) {
            CommandUtil.sendCommandError(CommandUtil.errorReason.UnknownOrIncompleteCommand,
                    this.getCommand(),
                    args,
                    args.length
            );
            return;
        }

        Identifier id = Identifier.tryParse(args[3]);
        if (id == null || !Registries.BLOCK.containsId(id)) {
            CommandUtil.sendCommandError(
                    CommandUtil.errorReason.UnknownBlock,
                    getCommand(),
                    args,
                    3
            );
            return;
        }
        Block block = Registries.BLOCK.get(id);

        Double x = WorldUtil.parseCoordinate(args[0], mc.player.getX());
        if (x == null) {
            CommandUtil.sendCommandError(
                    CommandUtil.errorReason.ExpectedInteger,
                    getCommand(),
                    args,
                    0
            );
            return;
        }

        Double y = WorldUtil.parseCoordinate(args[1], mc.player.getY());
        if (y == null) {
            CommandUtil.sendCommandError(
                    CommandUtil.errorReason.ExpectedInteger,
                    getCommand(),
                    args,
                    1
            );
            return;
        }

        Double z = WorldUtil.parseCoordinate(args[2], mc.player.getZ());
        if (z == null) {
            CommandUtil.sendCommandError(
                    CommandUtil.errorReason.ExpectedInteger,
                    getCommand(),
                    args,
                    2
            );
            return;
        }

        BlockPos pos = BlockPos.ofFloored(x, y, z);

        WorldUtil.setBlock(pos, block);
    }

    @Override
    public String getUsage() {
        return CommandUtil.getCommandPrefix() + "setblock <pos> <block>";
    }
}
