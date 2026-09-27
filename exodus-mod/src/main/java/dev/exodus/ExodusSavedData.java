package dev.exodus;

import dev.exodus.domain.Association;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import java.util.*;

public final class ExodusSavedData extends SavedData {
    public MatchState state = MatchState.IDLE;
    public UUID matchId;
    public String dimension = "";
    public int centerX, centerZ;
    public long startMillis, allocationSeed;
    public double oldBorderX, oldBorderZ, oldBorderSize, oldDamagePerBlock, oldSafeZone;
    public int oldWarningBlocks, oldWarningTime;
    public final Map<UUID, Association> associations = new HashMap<>();
    public final Map<UUID, Long> pendingPlayers = new HashMap<>();
    public final Map<UUID, String> names = new HashMap<>();
    public final Map<UUID, PlayerBaseData> bases = new HashMap<>();
    public final Map<UUID, Boolean> pendingReturns = new HashMap<>();
    public final List<String> placedCenters = new ArrayList<>();

    public static ExodusSavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(ExodusSavedData::load, ExodusSavedData::new, "exodus");
    }

    public static ExodusSavedData load(CompoundTag tag) {
        var d = new ExodusSavedData();
        try { d.state = MatchState.valueOf(tag.getString("state")); } catch (Exception ignored) {}
        if (tag.hasUUID("matchId")) d.matchId = tag.getUUID("matchId");
        d.dimension = tag.getString("dimension"); d.centerX = tag.getInt("centerX"); d.centerZ = tag.getInt("centerZ");
        d.startMillis = tag.getLong("startMillis"); d.allocationSeed = tag.getLong("allocationSeed");
        d.oldBorderX = tag.getDouble("oldBorderX"); d.oldBorderZ = tag.getDouble("oldBorderZ"); d.oldBorderSize = tag.getDouble("oldBorderSize");
        d.oldDamagePerBlock = tag.getDouble("oldDamagePerBlock"); d.oldSafeZone = tag.getDouble("oldSafeZone");
        d.oldWarningBlocks = tag.getInt("oldWarningBlocks"); d.oldWarningTime = tag.getInt("oldWarningTime");
        for (Tag raw : tag.getList("players", Tag.TAG_COMPOUND)) {
            var p=(CompoundTag)raw; UUID id=p.getUUID("id"); d.associations.put(id, Association.valueOf(p.getString("association")));
            d.names.put(id,p.getString("name")); if(p.contains("deadline")) d.pendingPlayers.put(id,p.getLong("deadline"));
        }
        for (Tag raw : tag.getList("bases", Tag.TAG_COMPOUND)) {
            var b=(CompoundTag)raw; UUID id=b.getUUID("id");
            var center=new net.minecraft.core.BlockPos(b.getInt("cx"),b.getInt("cy"),b.getInt("cz"));
            var origin=new net.minecraft.core.BlockPos(b.getInt("ox"),b.getInt("oy"),b.getInt("oz"));
            var spawn=new net.minecraft.core.BlockPos(b.getInt("sx"),b.getInt("sy"),b.getInt("sz"));
            d.bases.put(id,new PlayerBaseData(id,b.getString("name"),center,origin,spawn,new ResourceLocation(b.getString("structure"))));
        }
        for(Tag raw:tag.getList("pendingReturns",Tag.TAG_COMPOUND)){var r=(CompoundTag)raw;d.pendingReturns.put(r.getUUID("id"),r.getBoolean("survival"));}
        for(Tag raw:tag.getList("placedCenters",Tag.TAG_STRING)) d.placedCenters.add(raw.getAsString());
        return d;
    }

    @Override public CompoundTag save(CompoundTag tag) {
        tag.putString("state",state.name()); tag.putString("dimension",dimension); tag.putInt("centerX",centerX); tag.putInt("centerZ",centerZ);
        if (matchId != null) tag.putUUID("matchId", matchId);
        tag.putLong("startMillis",startMillis); tag.putLong("allocationSeed",allocationSeed);
        tag.putDouble("oldBorderX",oldBorderX); tag.putDouble("oldBorderZ",oldBorderZ); tag.putDouble("oldBorderSize",oldBorderSize);
        tag.putDouble("oldDamagePerBlock",oldDamagePerBlock); tag.putDouble("oldSafeZone",oldSafeZone); tag.putInt("oldWarningBlocks",oldWarningBlocks); tag.putInt("oldWarningTime",oldWarningTime);
        var players=new ListTag(); associations.forEach((id,a)->{var p=new CompoundTag();p.putUUID("id",id);p.putString("association",a.name());p.putString("name",names.getOrDefault(id,"Unknown"));if(pendingPlayers.containsKey(id))p.putLong("deadline",pendingPlayers.get(id));players.add(p);}); tag.put("players",players);
        var baseList=new ListTag(); bases.values().forEach(b->{var t=new CompoundTag();t.putUUID("id",b.uuid());t.putString("name",b.name());putPos(t,"c",b.center());putPos(t,"o",b.origin());putPos(t,"s",b.spawn());t.putString("structure",b.structureId().toString());baseList.add(t);});tag.put("bases",baseList);
        var returns=new ListTag(); pendingReturns.forEach((id,survival)->{var r=new CompoundTag();r.putUUID("id",id);r.putBoolean("survival",survival);returns.add(r);});tag.put("pendingReturns",returns);
        var old=new ListTag(); placedCenters.forEach(s->old.add(StringTag.valueOf(s)));tag.put("placedCenters",old);
        return tag;
    }
    private static void putPos(CompoundTag t,String p,net.minecraft.core.BlockPos v){t.putInt(p+"x",v.getX());t.putInt(p+"y",v.getY());t.putInt(p+"z",v.getZ());}
}
