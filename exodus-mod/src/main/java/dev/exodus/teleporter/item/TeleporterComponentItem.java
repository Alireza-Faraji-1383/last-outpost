package dev.exodus.teleporter.item;

import dev.exodus.teleporter.domain.TeleporterComponent;
import net.minecraft.world.item.Item;

public final class TeleporterComponentItem extends Item {
    private final TeleporterComponent component;
    public TeleporterComponentItem(TeleporterComponent component){this(component,new Item.Properties().stacksTo(64));}
    public TeleporterComponentItem(TeleporterComponent component,Properties properties){super(properties);this.component=component;}
    public TeleporterComponent component(){return component;}
}
