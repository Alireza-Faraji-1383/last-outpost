package dev.exodus.wasteland.profile;

import dev.exodus.wasteland.lostcities.LostCitiesIntegration;
import mcjty.lostcities.api.LostCityEvent;
import mcjty.lostcities.worldgen.lost.cityassets.AssetRegistries;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** Apply zoning through the supported characteristics event, before floor selection. */
@Mod.EventBusSubscriber(modid = "exodus")
public final class FixedCityZoning {
    private static final FixedCityLayout LAYOUT = FixedCityLayout.load();
    private FixedCityZoning() {}

    public static String styleAt(long blockX, long blockZ) {
        for (int ordinal = 0; ordinal < LAYOUT.count(); ordinal++) {
            var center = LAYOUT.arena(ordinal);
            double distance = Math.hypot(blockX - center.x() - LAYOUT.offsetX(),
                    blockZ - center.z() - LAYOUT.offsetZ());
            if (distance <= LAYOUT.effectiveRadius()) {
                return distance > 384 ? "exodus:outskirts" : "exodus:residential";
            }
        }
        return null;
    }

    @SubscribeEvent
    public static void characteristics(LostCityEvent.CharacteristicsEvent event) {
        if (!event.getWorld().getLevel().dimension().equals(LostCitiesIntegration.WASTELAND_DIMENSION)
                || !event.getCharacteristics().isCity) return;
        String style = styleAt((long) event.getChunkX() * 16, (long) event.getChunkZ() * 16);
        if (style == null) return;
        var characteristics = event.getCharacteristics();
        characteristics.cityStyle = AssetRegistries.CITYSTYLES.getOrThrow(event.getWorld(), style);
    }
}
