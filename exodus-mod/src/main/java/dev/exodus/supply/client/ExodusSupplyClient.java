package dev.exodus.supply.client;

import dev.exodus.ExodusMod;
import dev.exodus.supply.ExodusSupplyRegistry;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=ExodusMod.MOD_ID,bus=Mod.EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class ExodusSupplyClient {
    @SubscribeEvent public static void setup(FMLClientSetupEvent event){event.enqueueWork(()->MenuScreens.register(ExodusSupplyRegistry.SUPPLY_RADIO_MENU.get(),SupplyRadioScreen::new));}
}
