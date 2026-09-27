package dev.exodus.supply.block;

import net.minecraft.world.level.block.Block;
import dev.exodus.supply.blockentity.SupplyCrateBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import javax.annotation.Nullable;

public class SupplyCrateBlock extends BaseEntityBlock {
    public SupplyCrateBlock(Properties properties) { super(properties); }
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new SupplyCrateBlockEntity(pos,state);}
    @Override public RenderShape getRenderShape(BlockState state){return RenderShape.MODEL;}
    @Override public InteractionResult use(BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit){if(!level.isClientSide&&level.getBlockEntity(pos) instanceof SupplyCrateBlockEntity crate)player.openMenu(crate);return InteractionResult.sidedSuccess(level.isClientSide);}
    @Nullable @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,BlockState state,BlockEntityType<T> type){return level instanceof ServerLevel&&type==dev.exodus.supply.ExodusSupplyRegistry.SUPPLY_CRATE_ENTITY.get()?(l,p,s,b)->SupplyCrateBlockEntity.tick((ServerLevel)l,p,s,(SupplyCrateBlockEntity)b):null;}
}
