package dev.exodus.party;

import dev.exodus.*;
import dev.exodus.device.*;
import dev.exodus.teleporter.item.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import java.util.*;

public final class PartyProgressService {
 private PartyProgressService(){}
 public static int count(ServerPlayer player){
  if(!MatchManager.isActiveMatchPlayer(player))return 0;
  var d=ExodusSavedData.get(player.server);List<ServerPlayer> members=new ArrayList<>();members.add(player);
  PartyService.teammate(player.server,player.getUUID()).ifPresent(id->{var mate=player.server.getPlayerList().getPlayer(id);if(mate!=null&&MatchManager.isActiveMatchPlayer(mate))members.add(mate);});
  List<Integer> masks=new ArrayList<>(),devices=new ArrayList<>();
  for(var member:members){int mask=0;for(int i=0;i<member.getInventory().getContainerSize();i++)mask|=mask(member.getInventory().getItem(i),d.matchId);mask|=mask(member.containerMenu.getCarried(),d.matchId);masks.add(mask);devices.addAll(DeviceIndex.ownedMasks(d.session,member.getUUID()));}
  return ComponentProgressPolicy.partyCount(masks,devices);
 }
 private static int mask(ItemStack stack,UUID match){return ComponentStacks.isCurrent(stack,match)&&!stack.isEmpty()&&stack.getItem() instanceof TeleporterComponentItem item?1<<item.component().ordinal():0;}
}
