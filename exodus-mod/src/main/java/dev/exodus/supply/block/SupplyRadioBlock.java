package dev.exodus.supply.block;

import dev.exodus.supply.domain.RadioType;
import net.minecraft.world.level.block.Block;
import dev.exodus.supply.blockentity.SupplyRadioBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class SupplyRadioBlock extends BaseEntityBlock {
    private final RadioType radioType;

    public SupplyRadioBlock(Properties properties, RadioType radioType) {
        super(properties);
        this.radioType = radioType;
    }

    public RadioType radioType() { return radioType; }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state){return new SupplyRadioBlockEntity(pos,state);}
    @Override public RenderShape getRenderShape(BlockState state){return RenderShape.MODEL;}
}
