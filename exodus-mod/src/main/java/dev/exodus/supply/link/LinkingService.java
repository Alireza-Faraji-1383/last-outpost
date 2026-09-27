package dev.exodus.supply.link;

import dev.exodus.ExodusConfig;
import dev.exodus.ExodusSavedData;
import dev.exodus.MatchManager;
import dev.exodus.MatchState;
import dev.exodus.supply.blockentity.DropBeaconBlockEntity;
import dev.exodus.supply.blockentity.SupplyRadioBlockEntity;
import dev.exodus.supply.domain.LinkCandidate;
import dev.exodus.supply.domain.LinkPolicy;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

public final class LinkingService {
    private static final String KEY="ExodusRadioSelection";
    private LinkingService(){}

    public static InteractionResult use(UseOnContext context){
        if(context.getLevel().isClientSide)return InteractionResult.SUCCESS;
        if(!(context.getPlayer() instanceof ServerPlayer player))return InteractionResult.FAIL;
        ItemStack tool=context.getItemInHand();BlockPos pos=context.getClickedPos();
        if(context.getLevel().getBlockEntity(pos) instanceof SupplyRadioBlockEntity){
            LinkingSelection selection=new LinkingSelection(player.serverLevel().dimension().location().toString(),pos.getX(),pos.getY(),pos.getZ());
            tool.getOrCreateTag().put(KEY,selection.encode());player.sendSystemMessage(Component.literal("Supply Radio selected."));return InteractionResult.CONSUME;
        }
        if(!(context.getLevel().getBlockEntity(pos) instanceof DropBeaconBlockEntity beacon))return InteractionResult.PASS;
        var selected=LinkingSelection.decode(tool.getOrCreateTag().getCompound(KEY));if(selected.isEmpty()){player.sendSystemMessage(Component.literal("Select a Supply Radio first."));return InteractionResult.FAIL;}
        LinkingSelection s=selected.get();ExodusSavedData data=ExodusSavedData.get(player.server);ServerLevel level=player.serverLevel();
        if(!s.dimension().equals(level.dimension().location().toString()))return deny(player,"Radio and beacon must be in the same dimension.");
        BlockPos radioPos=new BlockPos(s.x(),s.y(),s.z());if(!(level.getBlockEntity(radioPos) instanceof SupplyRadioBlockEntity radio))return deny(player,"The selected Supply Radio no longer exists.");
        boolean existing=radio.beaconPos()!=null||beacon.radioPos()!=null;boolean exact=radio.beaconPos()!=null&&radio.beaconPos().equals(pos)&&beacon.radioPos()!=null&&beacon.radioPos().equals(radioPos);
        var border=level.getWorldBorder();boolean inside=border.isWithinBounds(radioPos)&&border.isWithinBounds(pos);
        LinkPolicy.Result result=LinkPolicy.evaluate(new LinkCandidate(data.state==MatchState.RUNNING,MatchManager.isActiveMatchPlayer(player),true,inside,Math.sqrt(radioPos.distSqr(pos)),ExodusConfig.RADIO_LINK_RANGE.get(),exact,existing));
        if(result!=LinkPolicy.Result.ALLOW)return deny(player,message(result));
        radio.claim(data.matchId,player.getUUID(),player.getGameProfile().getName(),s.dimension(),pos);
        beacon.claim(data.matchId,player.getUUID(),player.getGameProfile().getName(),s.dimension(),radioPos);
        tool.getOrCreateTag().remove(KEY);tool.shrink(1);player.sendSystemMessage(Component.literal("Supply Radio linked to Drop Beacon."));return InteractionResult.CONSUME;
    }
    private static InteractionResult deny(ServerPlayer p,String m){p.sendSystemMessage(Component.literal(m));return InteractionResult.FAIL;}
    private static String message(LinkPolicy.Result r){return switch(r){case NO_MATCH->"No active Exodus match.";case NOT_ACTIVE_PLAYER->"Only active match players can link supplies.";case OUTSIDE_BORDER->"Both blocks must be inside the match border.";case TOO_FAR->"The Drop Beacon is too far from the Supply Radio.";case ENDPOINT_OCCUPIED->"That endpoint is linked to another block.";default->"The supply link is invalid.";};}
}
