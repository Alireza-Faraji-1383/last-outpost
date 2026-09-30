package dev.exodus.wasteland.loot;

import dev.exodus.ExodusConfig;
import dev.exodus.ExodusMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** SEM supplies the weapon-specific AmmoId; run after its NORMAL-priority handler. */
@Mod.EventBusSubscriber(modid=ExodusMod.MOD_ID)
public final class SimpleEnemyDrops {
    private SimpleEnemyDrops() {}

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void onDrops(LivingDropsEvent event){
        var unit=event.getEntity();
        if(unit.level().isClientSide||!BuiltInRegistries.ENTITY_TYPE.getKey(unit.getType()).getNamespace().equals("simpleenemymod"))return;
        // Recruited PMC weapons are player property; SEM intentionally returns those intact.
        if(unit.getClass().getSimpleName().equals("PmcUnitEntity")){
            try{if(unit.getClass().getMethod("getOwnerUUID").invoke(unit)!=null)return;}
            catch(ReflectiveOperationException e){return;}
        }
        int first=ExodusConfig.ENEMY_AMMO_MIN.get(),second=ExodusConfig.ENEMY_AMMO_MAX.get();
        int offset=unit.getRandom().nextInt(Math.abs(second-first)+1);
        ItemEntity ammoDrop=null;
        var iterator=event.getDrops().iterator();
        while(iterator.hasNext()){
            var drop=iterator.next();
            if(BuiltInRegistries.ITEM.getKey(drop.getItem().getItem()).toString().equals("tacz:ammo")){
                if(ammoDrop==null){ammoDrop=drop;drop.getItem().setCount(EnemyDropPolicy.amount(first,second,offset));}
                else iterator.remove();
            }
        }
        if(EnemyDropPolicy.roll(unit.getRandom().nextDouble(),ExodusConfig.ENEMY_EMERALD_CHANCE.get())){
            first=ExodusConfig.ENEMY_EMERALD_MIN.get();second=ExodusConfig.ENEMY_EMERALD_MAX.get();
            ItemStack emeralds=new ItemStack(Items.EMERALD,EnemyDropPolicy.amount(first,second,unit.getRandom().nextInt(Math.abs(second-first)+1)));
            event.getDrops().add(new ItemEntity(unit.level(),unit.getX(),unit.getY(),unit.getZ(),emeralds));
        }
    }
}
