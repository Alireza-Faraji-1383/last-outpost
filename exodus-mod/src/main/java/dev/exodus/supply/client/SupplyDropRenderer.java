package dev.exodus.supply.client;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.exodus.supply.ExodusSupplyRegistry;
import dev.exodus.supply.entity.SupplyDropEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.Blocks;
public class SupplyDropRenderer extends EntityRenderer<SupplyDropEntity>{public SupplyDropRenderer(EntityRendererProvider.Context c){super(c);shadowRadius=.5f;}@Override public void render(SupplyDropEntity e,float y,float p,PoseStack s,MultiBufferSource b,int l){s.pushPose();s.translate(-.5,0,-.5);Minecraft.getInstance().getBlockRenderer().renderSingleBlock(ExodusSupplyRegistry.SUPPLY_CRATE.get().defaultBlockState(),s,b,l,OverlayTexture.NO_OVERLAY);s.popPose();s.pushPose();s.translate(0,2,0);s.scale(2.5f,.15f,2.5f);s.translate(-.5,0,-.5);Minecraft.getInstance().getBlockRenderer().renderSingleBlock(Blocks.WHITE_WOOL.defaultBlockState(),s,b,l,OverlayTexture.NO_OVERLAY);s.popPose();super.render(e,y,p,s,b,l);}@Override public ResourceLocation getTextureLocation(SupplyDropEntity e){return InventoryMenu.BLOCK_ATLAS;}}
