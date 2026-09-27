package dev.exodus.supply.catalog;

import com.google.gson.*;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;
import java.util.*;

public final class SupplyCatalogReloadListener extends SimpleJsonResourceReloadListener {
    private static final Logger LOG=LogUtils.getLogger();
    public SupplyCatalogReloadListener(){super(new Gson(),"exodus_supply_drops");}
    @Override protected void apply(Map<ResourceLocation,JsonElement> resources, ResourceManager manager, ProfilerFiller profiler){List<dev.exodus.supply.domain.SupplyDefinition> valid=new ArrayList<>();resources.forEach((id,element)->{try{var result=SupplyJsonParser.parse(id.toString(),element.getAsJsonObject());if(result.errors().isEmpty())valid.add(result.definition());else LOG.warn("[Exodus] Invalid supply {}: {}",id,String.join("; ",result.errors()));}catch(Exception e){LOG.warn("[Exodus] Could not parse supply {}: {}",id,e.getMessage());}});SupplyCatalog.replace(valid);LOG.info("[Exodus] Loaded {} supply definitions.",valid.size());}
}
