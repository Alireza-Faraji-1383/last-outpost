package dev.exodus.map.client;
import dev.exodus.map.*;import net.minecraftforge.api.distmarker.Dist;import net.minecraftforge.fml.common.Mod;import net.minecraftforge.eventbus.api.SubscribeEvent;import net.minecraftforge.client.event.ClientPlayerNetworkEvent;import net.minecraftforge.event.TickEvent;import java.util.function.Consumer;
@Mod.EventBusSubscriber(modid="exodus",value=Dist.CLIENT)
public final class MatchMapClient {
 private static final MapSessionGate GATE=new MapSessionGate();private static Consumer<MatchMapSnapshot> sink;private static Runnable clear=()->{},retry=()->{};private static MatchMapSnapshot latest;
 private MatchMapClient(){}
 public static void attach(Consumer<MatchMapSnapshot> renderer,Runnable cleaner,Runnable retryRenderer){sink=renderer;clear=cleaner;retry=retryRenderer;if(latest!=null)sink.accept(latest);}
 public static void receive(MatchMapSnapshot snapshot){if(!GATE.accept(snapshot.epoch(),snapshot.revision()))return;latest=snapshot;if(sink!=null)sink.accept(snapshot);}
 @SubscribeEvent public static void logout(ClientPlayerNetworkEvent.LoggingOut event){GATE.reset();latest=null;clear.run();}
 @SubscribeEvent public static void tick(TickEvent.ClientTickEvent e){if(e.phase==TickEvent.Phase.END)retry.run();}
}
