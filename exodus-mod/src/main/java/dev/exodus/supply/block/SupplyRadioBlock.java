package dev.exodus.supply.block;

import dev.exodus.supply.domain.RadioType;
import net.minecraft.world.level.block.Block;
import dev.exodus.supply.blockentity.SupplyRadioBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.entity.player.Inventory;
import dev.exodus.MatchManager;
import dev.exodus.ExodusSavedData;
import dev.exodus.supply.menu.SupplyRadioMenu;
import net.minecraftforge.network.NetworkHooks;
import net.minecraft.world.level.BlockGetter;

public class SupplyRadioBlock extends BaseEntityBlock {
    private final RadioType radioType;

    public SupplyRadioBlock(Properties properties, RadioType radioType) {
        super(properties);
        this.radioType = radioType;
    }

    public RadioType radioType() { return radioType; }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state){return new SupplyRadioBlockEntity(pos,state);}
    @Override public RenderShape getRenderShape(BlockState state){return RenderShape.MODEL;}
    @Override public float getDestroyProgress(BlockState state,Player player,BlockGetter level,BlockPos pos){return player.isCreative()?1.0F:0.0F;}
    @Override public InteractionResult use(BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit){if(level.isClientSide)return InteractionResult.SUCCESS;if(!(player instanceof ServerPlayer sp)||!(level.getBlockEntity(pos) instanceof SupplyRadioBlockEntity radio))return InteractionResult.FAIL;if(!MatchManager.isActiveMatchPlayer(sp)||!sp.getUUID().equals(radio.ownerId())){sp.sendSystemMessage(Component.literal("Only the current owner can use this Supply Radio."));return InteractionResult.FAIL;}var data=ExodusSavedData.get(sp.server);var entries=SupplyRadioMenu.entries(radio,radioType,data.matchId,level.getGameTime());NetworkHooks.openScreen(sp,new MenuProvider(){public Component getDisplayName(){return Component.literal("Supply Radio");}public AbstractContainerMenu createMenu(int id,Inventory inv,Player p){return new SupplyRadioMenu(id,inv,pos,entries);}},buf->SupplyRadioMenu.write(buf,pos,entries));return InteractionResult.CONSUME;}
}
