package dev.exodus.map.client;
import dev.exodus.map.*;import journeymap.api.v2.client.*;import journeymap.api.v2.common.*;import journeymap.api.v2.client.display.*;import journeymap.api.v2.client.model.*;
import net.minecraft.core.BlockPos;import net.minecraft.core.registries.Registries;import net.minecraft.resources.*;import java.util.*;
@JourneyMapPlugin(apiVersion="2.0.0")
public final class JourneyMapAdapter implements IClientPlugin {
 private IClientAPI api;private final MapRenderState renderState=new MapRenderState();private MatchMapSnapshot drawn;private PolygonOverlay border,outside;private final Map<String,MarkerOverlay> markers=new HashMap<>();
 @Override public String getModId(){return "exodus";}
 @Override public void initialize(IClientAPI api){this.api=api;MatchMapClient.attach(this::apply,this::disconnect,this::renderPending);}
 public void apply(MatchMapSnapshot snapshot){renderState.submit(snapshot);renderPending();}
 private void renderPending(){
  MatchMapSnapshot pending=renderState.pendingToRender();if(pending==null)return;
  if(pending.clear()){clear();drawn=pending;renderState.rendered(pending);return;}
  if(net.minecraft.client.Minecraft.getInstance().level==null)return;
  try{
   var dimension=ResourceKey.create(Registries.DIMENSION,new ResourceLocation(pending.dimension()));
   if(drawn==null||!Objects.equals(drawn.matchId(),pending.matchId())){clear();int h=pending.borderSize()/2;MapPolygon inner=square(pending.centerX(),pending.centerZ(),h);
    border=new PolygonOverlay("exodus",dimension,new ShapeProperties().setStrokeColor(0xFF3333).setStrokeOpacity(1).setStrokeWidth(2).setFillOpacity(0),inner);border.setActiveUIs(Context.UI.Minimap,Context.UI.Fullscreen);api.show(border);
    outside=new PolygonOverlay("exodus",dimension,new ShapeProperties().setStrokeOpacity(0).setFillColor(0xFF2222).setFillOpacity(.15f),new MapPolygonWithHoles(square(pending.centerX(),pending.centerZ(),h+pending.borderSize()),List.of(inner)));outside.setActiveUIs(Context.UI.Minimap,Context.UI.Fullscreen);api.show(outside);
   }
   Set<String> wanted=new HashSet<>();for(MapLocation l:pending.locations()){
    wanted.add(l.id());MarkerOverlay marker=markers.get(l.id());if(marker==null){MapImage image=new MapImage(new ResourceLocation("exodus","textures/map/"+icon(l.kind())+".png"),16,16).centerAnchors().setColor(color(l.kind()));marker=new MarkerOverlay("exodus",new BlockPos(l.x(),l.y(),l.z()),image);marker.setDimension(dimension).setActiveUIs(Context.UI.Minimap,Context.UI.Fullscreen);markers.put(l.id(),marker);}
    marker.setPoint(new BlockPos(l.x(),l.y(),l.z()));marker.setLabel(l.label()).setTitle(l.label());api.show(marker);
   }
   for(String id:new HashSet<>(markers.keySet()))if(!wanted.contains(id))api.remove(markers.remove(id));
   drawn=pending;renderState.rendered(pending);
  }catch(Exception ex){com.mojang.logging.LogUtils.getLogger().debug("[Exodus] Waiting for JourneyMap map context",ex);}
 }
 private static MapPolygon square(int x,int z,int h){return new MapPolygon(new BlockPos(x-h,64,z-h),new BlockPos(x+h,64,z-h),new BlockPos(x+h,64,z+h),new BlockPos(x-h,64,z+h));}
 private static String icon(MapLocation.Kind kind){return switch(kind){case ACTIVE_DEVICE,RARE_ITEM,AIRDROP->"star";case CITY,RUSSIAN_BASE,AMERICAN_BASE->"base";case DEVICE->"device";default->"camp";};}
 private static int color(MapLocation.Kind kind){return switch(kind){case RUSSIAN_BASE->0xF25A5A;case AMERICAN_BASE->0x60A5FA;case CITY->0xF5F5F5;case PLAYER_BASE->0x6EE7A0;case DEVICE->0xC4B5FD;case ACTIVE_DEVICE->0xE879F9;case RARE_ITEM->0xFACC15;case AIRDROP->0x55AAFF;default->0xFDBA74;};}
 private void disconnect(){renderState.disconnect();clear();}
 private void clear(){if(api!=null)api.removeAll("exodus");markers.clear();border=null;outside=null;drawn=null;}
}
