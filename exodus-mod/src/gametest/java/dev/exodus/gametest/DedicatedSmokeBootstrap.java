package dev.exodus.gametest;
import net.minecraftforge.fml.common.Mod;import net.minecraftforge.eventbus.api.SubscribeEvent;import net.minecraftforge.event.TickEvent;
/** Test source set only. Runs the lifecycle harness on the real dedicated server. */
@Mod.EventBusSubscriber(modid="exodus")
public final class DedicatedSmokeBootstrap {
 private static boolean started;
 @SubscribeEvent public static void tick(TickEvent.ServerTickEvent event){
  if(!started&&event.phase==TickEvent.Phase.END&&event.getServer().isDedicatedServer()&&event.getServer().getTickCount()>20){started=true;event.getServer().getCommands().performPrefixedCommand(event.getServer().createCommandSourceStack(),"test runall");}
 }
}
