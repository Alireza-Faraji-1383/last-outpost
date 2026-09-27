package dev.exodus.supply.block;

import net.minecraft.world.level.block.Block;
import dev.exodus.supply.blockentity.DropBeaconBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class DropBeaconBlock extends BaseEntityBlock {
    public DropBeaconBlock(Properties properties) { super(properties); }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state){return new DropBeaconBlockEntity(pos,state);}
    @Override public RenderShape getRenderShape(BlockState state){return RenderShape.MODEL;}
}
