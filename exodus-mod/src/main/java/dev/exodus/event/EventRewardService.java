package dev.exodus.event;

import dev.exodus.ExodusSavedData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import java.util.UUID;

public final class EventRewardService {
    private EventRewardService(){}
    public static boolean pay(ServerPlayer player,UUID run,int amount,String title){
        var d=ExodusSavedData.get(player.server);String transaction=run+":"+player.getUUID();
        if(!d.session.events.paidRewards.add(transaction))return false;
        d.setDirty();
        int remaining=amount;while(remaining>0){int count=Math.min(remaining,Items.EMERALD.getMaxStackSize());var stack=new ItemStack(Items.EMERALD,count);if(!player.getInventory().add(stack))player.drop(stack,false);remaining-=count;}
        player.getInventory().setChanged();player.containerMenu.broadcastChanges();
        player.sendSystemMessage(Component.literal(title+" reward received: "+amount+" emeralds."));return true;
    }
}
