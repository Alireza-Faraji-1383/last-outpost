package dev.exodus.supply;

import dev.exodus.ExodusMod;
import dev.exodus.supply.block.DropBeaconBlock;
import dev.exodus.supply.block.SupplyCrateBlock;
import dev.exodus.supply.block.SupplyRadioBlock;
import dev.exodus.supply.domain.RadioType;
import dev.exodus.supply.entity.SupplyDropEntity;
import dev.exodus.supply.item.LinkingToolItem;
import dev.exodus.supply.blockentity.DropBeaconBlockEntity;
import dev.exodus.supply.blockentity.SupplyRadioBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import dev.exodus.supply.menu.SupplyRadioMenu;

public final class ExodusSupplyRegistry {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ExodusMod.MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ExodusMod.MOD_ID);
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, ExodusMod.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ExodusMod.MOD_ID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, ExodusMod.MOD_ID);

    private static BlockBehaviour.Properties protectedMetal() {
        return BlockBehaviour.Properties.of().strength(-1.0F, 3_600_000.0F).sound(SoundType.METAL);
    }

    public static final RegistryObject<Block> BASIC_SUPPLY_RADIO = BLOCKS.register("basic_supply_radio",
            () -> new SupplyRadioBlock(protectedMetal(), RadioType.BASIC));
    public static final RegistryObject<Block> SPECIAL_SUPPLY_RADIO = BLOCKS.register("special_supply_radio",
            () -> new SupplyRadioBlock(protectedMetal(), RadioType.SPECIAL));
    public static final RegistryObject<Block> DROP_BEACON = BLOCKS.register("drop_beacon",
            () -> new DropBeaconBlock(protectedMetal().lightLevel(state -> 7)));
    public static final RegistryObject<Block> SUPPLY_CRATE = BLOCKS.register("supply_crate",
            () -> new SupplyCrateBlock(protectedMetal()));

    public static final RegistryObject<Item> BASIC_SUPPLY_RADIO_ITEM = blockItem("basic_supply_radio", BASIC_SUPPLY_RADIO);
    public static final RegistryObject<Item> SPECIAL_SUPPLY_RADIO_ITEM = blockItem("special_supply_radio", SPECIAL_SUPPLY_RADIO);
    public static final RegistryObject<Item> DROP_BEACON_ITEM = blockItem("drop_beacon", DROP_BEACON);
    public static final RegistryObject<Item> SUPPLY_CRATE_ITEM = blockItem("supply_crate", SUPPLY_CRATE);
    public static final RegistryObject<Item> LINKING_TOOL = ITEMS.register("linking_tool",
            () -> new LinkingToolItem(new Item.Properties().stacksTo(16)));

    public static final RegistryObject<EntityType<SupplyDropEntity>> SUPPLY_DROP = ENTITIES.register("supply_drop",
            () -> EntityType.Builder.<SupplyDropEntity>of(SupplyDropEntity::new, MobCategory.MISC)
                    .sized(1.0F, 2.5F).clientTrackingRange(10).updateInterval(1).build("supply_drop"));
    public static final RegistryObject<BlockEntityType<SupplyRadioBlockEntity>> SUPPLY_RADIO_ENTITY = BLOCK_ENTITIES.register("supply_radio",
            () -> BlockEntityType.Builder.of(SupplyRadioBlockEntity::new, BASIC_SUPPLY_RADIO.get(), SPECIAL_SUPPLY_RADIO.get()).build(null));
    public static final RegistryObject<BlockEntityType<DropBeaconBlockEntity>> DROP_BEACON_ENTITY = BLOCK_ENTITIES.register("drop_beacon",
            () -> BlockEntityType.Builder.of(DropBeaconBlockEntity::new, DROP_BEACON.get()).build(null));
    public static final RegistryObject<MenuType<SupplyRadioMenu>> SUPPLY_RADIO_MENU = MENUS.register("supply_radio",
            () -> IForgeMenuType.create(SupplyRadioMenu::client));

    private ExodusSupplyRegistry() {}

    private static RegistryObject<Item> blockItem(String name, RegistryObject<Block> block) {
        return ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        ENTITIES.register(bus);
        BLOCK_ENTITIES.register(bus);
        MENUS.register(bus);
    }
}
