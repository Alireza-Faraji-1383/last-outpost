package dev.exodus.event;

import com.google.gson.*;
import com.mojang.logging.LogUtils;
import dev.exodus.event.domain.EventDefinition;
import dev.exodus.event.domain.EventDefinitionParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.*;
import net.minecraft.util.profiling.ProfilerFiller;
import java.util.*;

public final class EventCatalogReloadListener extends SimpleJsonResourceReloadListener {
    public EventCatalogReloadListener(){super(new Gson(),"exodus_events");}
    @Override protected void apply(Map<ResourceLocation,JsonElement> resources,ResourceManager manager,ProfilerFiller profiler){
        List<EventDefinition> values=new ArrayList<>();
        resources.forEach((id,json)->{try{if(!json.getAsJsonObject().has("enabled")||json.getAsJsonObject().get("enabled").getAsBoolean())values.add(EventDefinitionParser.parse(id.toString(),json.getAsJsonObject()));}
        catch(RuntimeException ex){LogUtils.getLogger().warn("[Exodus] Invalid event {}: {}",id,ex.getMessage());}});
        EventCatalog.replace(values);LogUtils.getLogger().info("[Exodus] Loaded {} event definitions.",values.size());
    }
}
