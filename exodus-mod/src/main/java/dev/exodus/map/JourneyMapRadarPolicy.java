package dev.exodus.map;
import com.mojang.logging.LogUtils;import net.minecraft.server.MinecraftServer;import net.minecraft.server.level.ServerPlayer;import net.minecraft.resources.ResourceKey;import net.minecraft.world.level.Level;import java.util.*;
/** Optional version-pinned adapter. Reflection isolates JourneyMap internals from dedicated loading. */
public final class JourneyMapRadarPolicy {
 private static final Map<MinecraftServer,Map<Object,Object>> ORIGINAL=new WeakHashMap<>();private static boolean warned;
 private JourneyMapRadarPolicy(){}
 public static void enforce(MinecraftServer server,ResourceKey<Level> dimension){
  if(!net.minecraftforge.fml.ModList.get().isLoaded("journeymap"))return;
  try{
   Class<?> managerClass=Class.forName("journeymap.common.properties.PropertiesManager");Object manager=managerClass.getMethod("getInstance").invoke(null);
   Object global=managerClass.getMethod("getGlobalProperties").invoke(manager),dim=managerClass.getMethod("getDimProperties",ResourceKey.class).invoke(manager,dimension);
   Map<Object,Object> original=ORIGINAL.computeIfAbsent(server,k->new IdentityHashMap<>());
   boolean changed=set(global,"playerRadarEnabled",false,original)|set(global,"playerRadarNamesEnabled",false,original)|set(global,"opsSeeHiddenPlayers",false,original);
   Object none=Class.forName("journeymap.common.properties.ServerOption").getField("NONE").get(null);changed|=set(global,"worldPlayerRadar",none,original);
   changed|=set(dim,"playerRadarEnabled",false,original)|set(dim,"playerRadarNamesEnabled",false,original);
   if(changed)for(ServerPlayer p:server.getPlayerList().getPlayers())send(p);
  }catch(ReflectiveOperationException ex){if(!warned){warned=true;LogUtils.getLogger().error("[Exodus] JourneyMap 6.0.6 radar enforcement failed",ex);}}
 }
 private static boolean set(Object owner,String name,Object value,Map<Object,Object> original)throws ReflectiveOperationException{Object field=owner.getClass().getField(name).get(owner);Object current=field.getClass().getMethod("get").invoke(field);original.putIfAbsent(field,current);if(Objects.equals(current,value))return false;field.getClass().getMethod("set",Object.class).invoke(field,value);return true;}
 public static void send(ServerPlayer p)throws ReflectiveOperationException{Class<?> type=Class.forName("journeymap.common.util.PermissionsManager");Object manager=type.getMethod("getInstance").invoke(null);type.getMethod("sendPermissions",ServerPlayer.class).invoke(manager,p);}
 public static void restore(MinecraftServer server){Map<Object,Object> original=ORIGINAL.remove(server);if(original==null)return;try{for(var e:original.entrySet())e.getKey().getClass().getMethod("set",Object.class).invoke(e.getKey(),e.getValue());for(ServerPlayer p:server.getPlayerList().getPlayers())send(p);}catch(ReflectiveOperationException ex){LogUtils.getLogger().error("[Exodus] Could not restore JourneyMap radar permissions",ex);}}
}
