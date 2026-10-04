package dev.exodus.network;
import dev.exodus.*;import dev.exodus.map.*;import net.minecraft.network.FriendlyByteBuf;import net.minecraft.resources.ResourceLocation;import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.*;import net.minecraftforge.network.simple.SimpleChannel;import java.util.*;
public final class ExodusNetwork {
 private static final SimpleChannel CHANNEL=NetworkRegistry.newSimpleChannel(new ResourceLocation("exodus","match_map"),()->"3","3"::equals,"3"::equals);
 private ExodusNetwork(){}
 public static void register(){CHANNEL.registerMessage(0,MatchMapSnapshot.class,MatchMapPacket::encode,MatchMapPacket::decode,(packet,ctx)->{var context=ctx.get();context.enqueueWork(()->net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(net.minecraftforge.api.distmarker.Dist.CLIENT,()->()->dev.exodus.map.client.MatchMapClient.receive(packet)));context.setPacketHandled(true);},Optional.of(NetworkDirection.PLAY_TO_CLIENT));}
 public static void send(ServerPlayer p,MatchMapSnapshot snapshot){CHANNEL.send(PacketDistributor.PLAYER.with(()->p),snapshot);}
}
