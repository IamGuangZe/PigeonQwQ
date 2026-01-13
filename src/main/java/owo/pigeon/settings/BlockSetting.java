package owo.pigeon.settings;

import net.minecraft.block.Block;

import java.util.function.Predicate;

public class BlockSetting extends AbstractSetting<Block> {
    protected BlockSetting(String name, Block defaultValue, Predicate<Boolean> visible) {
        super(name, defaultValue, visible);
    }
}
