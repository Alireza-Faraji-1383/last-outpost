package dev.exodus.party;

import dev.exodus.ExodusMod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=ExodusMod.MOD_ID)
public final class PartyCombatEvents {
 private PartyCombatEvents(){}
 private static ServerPlayer attacker(Entity entity){if(entity instanceof ServerPlayer p)return p;if(entity instanceof Projectile projectile&&projectile.getOwner() instanceof ServerPlayer p)return p;return null;}
 public static boolean blocks(Entity victim,DamageSource source){
  if(!(victim instanceof ServerPlayer target))return false;
  ServerPlayer attacker=attacker(source.getEntity());if(attacker==null)attacker=attacker(source.getDirectEntity());
  return attacker!=null&&target.server==attacker.server&&PartyService.sameParty(target.server,target.getUUID(),attacker.getUUID());
 }
 @SubscribeEvent(priority=EventPriority.HIGHEST) public static void attack(LivingAttackEvent e){if(blocks(e.getEntity(),e.getSource()))e.setCanceled(true);}
 @SubscribeEvent(priority=EventPriority.HIGHEST) public static void hurt(LivingHurtEvent e){if(blocks(e.getEntity(),e.getSource()))e.setCanceled(true);}
 @SubscribeEvent(priority=EventPriority.HIGHEST) public static void damage(LivingDamageEvent e){if(blocks(e.getEntity(),e.getSource()))e.setCanceled(true);}
}
