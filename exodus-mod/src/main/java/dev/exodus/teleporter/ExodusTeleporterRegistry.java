package dev.exodus.teleporter;

import dev.exodus.ExodusMod;
import dev.exodus.supply.ExodusSupplyRegistry;
import dev.exodus.teleporter.domain.TeleporterComponent;
import dev.exodus.teleporter.item.TeleporterComponentItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.*;
import java.util.*;

public final class ExodusTeleporterRegistry {
    public static final DeferredRegister<Item> ITEMS=DeferredRegister.create(ForgeRegistries.ITEMS,ExodusMod.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> TABS=DeferredRegister.create(Registries.CREATIVE_MODE_TAB,ExodusMod.MOD_ID);
    private static final EnumMap<TeleporterComponent,RegistryObject<Item>> COMPONENTS=new EnumMap<>(TeleporterComponent.class);
    static {for(TeleporterComponent component:TeleporterComponent.values())COMPONENTS.put(component,ITEMS.register(id(component),()->new TeleporterComponentItem(component)));}
    public static final RegistryObject<CreativeModeTab> PROJECT_EXODUS=TABS.register("project_exodus",()->CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.exodus.project_exodus"))
            .icon(()->new ItemStack(COMPONENTS.get(TeleporterComponent.DIMENSIONAL_CORE).get()))
            .displayItems((parameters,output)->{
                output.accept(ExodusSupplyRegistry.BASIC_SUPPLY_RADIO_ITEM.get());output.accept(ExodusSupplyRegistry.SPECIAL_SUPPLY_RADIO_ITEM.get());
                output.accept(ExodusSupplyRegistry.DROP_BEACON_ITEM.get());output.accept(ExodusSupplyRegistry.LINKING_TOOL.get());output.accept(ExodusSupplyRegistry.SUPPLY_CRATE_ITEM.get());
                for(TeleporterComponent component:TeleporterComponent.values())output.accept(COMPONENTS.get(component).get());
            }).build());
    private ExodusTeleporterRegistry() {}
    public static RegistryObject<Item> item(TeleporterComponent component){return COMPONENTS.get(component);}
    public static void register(IEventBus bus){ITEMS.register(bus);TABS.register(bus);}
    private static String id(TeleporterComponent component){return component.name().toLowerCase(Locale.ROOT);}
}
