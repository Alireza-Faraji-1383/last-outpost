package dev.exodus.supply.item;

import net.minecraft.world.item.Item;
import dev.exodus.supply.link.LinkingService;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;

public class LinkingToolItem extends Item {
    public LinkingToolItem(Properties properties) { super(properties); }
    @Override public InteractionResult useOn(UseOnContext context){return LinkingService.use(context);}
}
