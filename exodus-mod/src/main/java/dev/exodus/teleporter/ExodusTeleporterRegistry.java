package dev.exodus.teleporter;

import dev.exodus.ExodusMod;
import dev.exodus.supply.ExodusSupplyRegistry;
import dev.exodus.teleporter.domain.TeleporterComponent;
import dev.exodus.teleporter.item.TeleporterComponentItem;
import dev.exodus.teleporter.block.ExodusTeleporterBlock;
import dev.exodus.teleporter.blockentity.ExodusTeleporterBlockEntity;
import dev.exodus.teleporter.menu.ExodusTeleporterMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.*;
import java.util.*;

public final class ExodusTeleporterRegistry {
    public static final DeferredRegister<Item> ITEMS=DeferredRegister.create(ForgeRegistries.ITEMS,ExodusMod.MOD_ID);
    public static final DeferredRegister<Block> BLOCKS=DeferredRegister.create(ForgeRegistries.BLOCKS,ExodusMod.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES=DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES,ExodusMod.MOD_ID);
    public static final DeferredRegister<MenuType<?>> MENUS=DeferredRegister.create(ForgeRegistries.MENU_TYPES,ExodusMod.MOD_ID);
    public static final DeferredRegister<CreativeModeTab> TABS=DeferredRegister.create(Registries.CREATIVE_MODE_TAB,ExodusMod.MOD_ID);
    private static final EnumMap<TeleporterComponent,RegistryObject<Item>> COMPONENTS=new EnumMap<>(TeleporterComponent.class);
    static {for(TeleporterComponent component:TeleporterComponent.values())COMPONENTS.put(component,ITEMS.register(id(component),()->new TeleporterComponentItem(component)));}
    public static final RegistryObject<Block> TELEPORTER=BLOCKS.register("exodus_teleporter",()->new ExodusTeleporterBlock(BlockBehaviour.Properties.of().strength(-1,3_600_000).sound(SoundType.METAL).lightLevel(s->s.getValue(ExodusTeleporterBlock.ACTIVE)?10:2)));
    public static final RegistryObject<Item> TELEPORTER_ITEM=ITEMS.register("exodus_teleporter",()->new BlockItem(TELEPORTER.get(),new Item.Properties()));
    public static final RegistryObject<BlockEntityType<ExodusTeleporterBlockEntity>> TELEPORTER_ENTITY=BLOCK_ENTITIES.register("exodus_teleporter",()->BlockEntityType.Builder.of(ExodusTeleporterBlockEntity::new,TELEPORTER.get()).build(null));
    public static final RegistryObject<MenuType<ExodusTeleporterMenu>> TELEPORTER_MENU=MENUS.register("exodus_teleporter",()->IForgeMenuType.create(ExodusTeleporterMenu::client));
    public static final RegistryObject<CreativeModeTab> PROJECT_EXODUS=TABS.register("project_exodus",()->CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.exodus.project_exodus"))
            .icon(()->new ItemStack(COMPONENTS.get(TeleporterComponent.DIMENSIONAL_CORE).get()))
            .displayItems((parameters,output)->{
                output.accept(ExodusSupplyRegistry.BASIC_SUPPLY_RADIO_ITEM.get());output.accept(ExodusSupplyRegistry.SPECIAL_SUPPLY_RADIO_ITEM.get());
                output.accept(ExodusSupplyRegistry.DROP_BEACON_ITEM.get());output.accept(ExodusSupplyRegistry.LINKING_TOOL.get());output.accept(ExodusSupplyRegistry.SUPPLY_CRATE_ITEM.get());
                output.accept(TELEPORTER_ITEM.get());
                for(TeleporterComponent component:TeleporterComponent.values())output.accept(COMPONENTS.get(component).get());
            }).build());
    private ExodusTeleporterRegistry() {}
    public static RegistryObject<Item> item(TeleporterComponent component){return COMPONENTS.get(component);}
    public static void register(IEventBus bus){BLOCKS.register(bus);ITEMS.register(bus);BLOCK_ENTITIES.register(bus);MENUS.register(bus);TABS.register(bus);}
    private static String id(TeleporterComponent component){return component.name().toLowerCase(Locale.ROOT);}
}
