package dev.exodus.party;

import com.mojang.logging.LogUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.ModList;
import java.lang.reflect.Method;

/** Optional TacZ 1.1.8 bridge: cancel Pre before ignition, knockback and both damage parts. */
public final class PartyGunIntegration {
 private record Accessors(Method victim,Method attacker){}
 private PartyGunIntegration(){}
 @SuppressWarnings("unchecked")
 public static void register(){
  if(!ModList.get().isLoaded("tacz"))return;
  try{
   Class<? extends Event> type=Class.forName("com.tacz.guns.api.event.common.EntityHurtByGunEvent$Pre").asSubclass(Event.class);
   Accessors accessors=accessors(type);
   MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST,false,(Class<Event>)(Class<?>)type,(Event event)->apply(event,accessors));
   LogUtils.getLogger().info("[Exodus] TacZ party pre-hit protection registered.");
  }catch(ReflectiveOperationException e){throw new IllegalStateException("Cannot register installed TacZ party protection",e);}
 }
 public static void handle(Event event){try{apply(event,accessors(event.getClass()));}catch(ReflectiveOperationException e){throw new IllegalStateException("Cannot inspect gun pre-hit event",e);}}
 private static Accessors accessors(Class<?> type)throws ReflectiveOperationException{return new Accessors(type.getMethod("getHurtEntity"),type.getMethod("getAttacker"));}
 private static void apply(Event event,Accessors accessors){
  try{
   if(event.isCancelable()&&accessors.victim().invoke(event) instanceof ServerPlayer target&&accessors.attacker().invoke(event) instanceof ServerPlayer attacker&&target.server==attacker.server&&PartyService.sameParty(target.server,target.getUUID(),attacker.getUUID()))event.setCanceled(true);
  }catch(ReflectiveOperationException e){throw new IllegalStateException("Cannot apply TacZ party protection",e);}
 }
}
