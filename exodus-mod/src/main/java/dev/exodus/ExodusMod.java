package dev.exodus;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import dev.exodus.supply.ExodusSupplyRegistry;

@Mod(ExodusMod.MOD_ID)
public final class ExodusMod {
    public static final String MOD_ID = "exodus";

    public ExodusMod() {
        ExodusSupplyRegistry.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ExodusConfig.SPEC, "exodus-common.toml");
        MinecraftForge.EVENT_BUS.register(ExodusEvents.class);
    }
}
