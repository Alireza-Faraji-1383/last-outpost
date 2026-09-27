package dev.exodus.supply.block;

import dev.exodus.supply.domain.RadioType;
import net.minecraft.world.level.block.Block;

public class SupplyRadioBlock extends Block {
    private final RadioType radioType;

    public SupplyRadioBlock(Properties properties, RadioType radioType) {
        super(properties);
        this.radioType = radioType;
    }

    public RadioType radioType() { return radioType; }
}
