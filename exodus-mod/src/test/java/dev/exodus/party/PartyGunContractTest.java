package dev.exodus.party;

import org.junit.jupiter.api.Test;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.LogicalSide;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.tuple.Pair;
import java.net.URLClassLoader;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

class PartyGunContractTest {
 @Test void installedTaczPreEventHasVerifiedCancelableBridgeContract()throws Exception{
  Path jar=Path.of("..","mods","tacz-1.20.1-1.1.8-hotfix.jar");
  assertTrue(java.nio.file.Files.isRegularFile(jar),"Pinned installed TacZ artifact is required for the compatibility contract");
  try(var loader=new URLClassLoader(new java.net.URL[]{jar.toUri().toURL()},getClass().getClassLoader())){
   var type=Class.forName("com.tacz.guns.api.event.common.EntityHurtByGunEvent$Pre",true,loader).asSubclass(Event.class);
   assertEquals(Entity.class,type.getMethod("getHurtEntity").getReturnType());assertEquals(LivingEntity.class,type.getMethod("getAttacker").getReturnType());
   assertNotNull(type.getConstructor(Entity.class,Entity.class,LivingEntity.class,ResourceLocation.class,ResourceLocation.class,float.class,Pair.class,boolean.class,float.class,LogicalSide.class));
   // The real constructor posts a KubeJS hook and requires a launched Forge ModList.
   // GameTests exercise runtime cancellation; JUnit pins the installed ABI and annotation.
   assertTrue(type.isAnnotationPresent(net.minecraftforge.eventbus.api.Cancelable.class));
  }
 }
}
